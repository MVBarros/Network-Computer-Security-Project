package tig.grpc.server.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import tig.grpc.contract.CustomProtocolTigServiceGrpc;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigBackupServiceGrpc;
import tig.grpc.contract.TigKeyServiceGrpc;
import tig.grpc.server.dao.FileDAO;
import tig.grpc.server.session.CustomUserToken;
import tig.grpc.server.session.SessionAuthenticator;
import tig.grpc.server.throttle.Throttler;
import tig.utils.StringGenerator;
import tig.utils.encryption.EncryptionUtils;
import tig.utils.encryption.HashUtils;
import tig.utils.serialization.ObjectSerializer;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class CustomProtocolTigServiceImpl extends CustomProtocolTigServiceGrpc.CustomProtocolTigServiceImplBase {

    public static PrivateKey privateKey;
    public static PublicKey publicKey;

    public static TigKeyServiceGrpc.TigKeyServiceBlockingStub keyStub;
    public static TigBackupServiceGrpc.TigBackupServiceBlockingStub backupStub;

    @Override
    public void register(Tig.CustomProtocolMessage request, StreamObserver<Empty> responseObserver) {
        byte[] message = request.getMessage().toByteArray();
        Tig.Signature signature = request.getSignature();
        Tig.CustomLoginRequest registerRequest = (Tig.CustomLoginRequest)ObjectSerializer.Deserialize(message);

        //Get client pub key
        byte[] pubKey = registerRequest.getClientPubKey().toByteArray();
        PublicKey clientPubKey = EncryptionUtils.getPubRSAKey(pubKey);

        //get SessionKey
        byte[] sessionKey = registerRequest.getEncryptionKey().toByteArray();
        //decipher with server key
        sessionKey = EncryptionUtils.decryptbytesRSAPriv(sessionKey, privateKey);
        SecretKeySpec key = EncryptionUtils.getAesKey(sessionKey);


        //decipher message with session key
        message = registerRequest.getMessage().toByteArray();
        message = EncryptionUtils.decryptbytesAES(message, key);
        byte[] hash = signature.getValue().toByteArray();
        hash = EncryptionUtils.decryptbytesRSAPub(hash, clientPubKey);
        if (!HashUtils.verifyMessageSignature(message, hash)) {
            throw new IllegalArgumentException("Invalid Signature");
        }
        Tig.AccountRequest req = (Tig.AccountRequest) ObjectSerializer.Deserialize(message);
        Empty reply;
        try {
            Throttler.throttle(req.getUsername());
            reply = keyStub.registerTigKey(req);
            Throttler.success(req.getUsername());
        } catch(StatusRuntimeException e) {
            Throttler.failure(req.getUsername());
            throw new IllegalArgumentException(e.getMessage());
        }

        //client
        try {
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }catch (StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }


    @Override
    public void login(Tig.CustomProtocolMessage request, StreamObserver<Tig.CustomProtocolMessage> responseObserver) {
        byte[] message = request.getMessage().toByteArray();
        Tig.Signature signature = request.getSignature();
        Tig.CustomLoginRequest loginRequest = (Tig.CustomLoginRequest)ObjectSerializer.Deserialize(message);

        //Get client pub key
        byte[] pubKey = loginRequest.getClientPubKey().toByteArray();
        PublicKey clientPubKey = EncryptionUtils.getPubRSAKey(pubKey);

        //get SessionKey
        byte[] sessionKey = loginRequest.getEncryptionKey().toByteArray();
        //decipher with server key
        sessionKey = EncryptionUtils.decryptbytesRSAPriv(sessionKey, privateKey);
        SecretKeySpec key = EncryptionUtils.getAesKey(sessionKey);

        //decipher message with session key
        message = loginRequest.getMessage().toByteArray();
        message = EncryptionUtils.decryptbytesAES(message, key);
        byte[] hash = signature.getValue().toByteArray();
        hash = EncryptionUtils.decryptbytesRSAPub(hash, clientPubKey);

        if (!HashUtils.verifyMessageSignature(message, hash)) {
            throw new IllegalArgumentException("Invalid Signature");
        }

        Tig.AccountRequest req = (Tig.AccountRequest) ObjectSerializer.Deserialize(message);
        Tig.LoginReply reply;
        try {
            Throttler.throttle(req.getUsername());
            reply = keyStub.loginTigKey(req);
            Throttler.success(req.getUsername());
        } catch(StatusRuntimeException e) {
            Throttler.failure(req.getUsername());
            throw new IllegalArgumentException(e.getMessage());
        }
        //insert session key into memory
        String signerId = StringGenerator.randomString(256);
        SessionAuthenticator.insertCustomSession(signerId, key, clientPubKey);

        message = ObjectSerializer.Serialize(reply);
        String nonce = StringGenerator.randomString(256);

        Tig.Content content = Tig.Content.newBuilder()
                .setRequest(ByteString.copyFrom(message))
                .setNonce(nonce).build();

        message = ObjectSerializer.Serialize(content);

        //hash
        byte[] sign = HashUtils.hashBytes(message);

        //encrypt hash
        sign = EncryptionUtils.encryptBytesRSAPriv(sign, privateKey);
        message = EncryptionUtils.encryptBytesAES(message, key);

        signature = Tig.Signature.newBuilder()
                .setValue(ByteString.copyFrom(sign))
                .setSignerId(signerId)
                .build();

        //client
        responseObserver.onNext(
                Tig.CustomProtocolMessage.newBuilder()
                        .setMessage(ByteString.copyFrom(message))
                        .setSignature(signature)
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void logout(Tig.CustomProtocolMessage request, StreamObserver<Empty> responseObserver) {
        byte[] hash = request.getSignature().getValue().toByteArray();
        String signerId = request.getSignature().getSignerId();

        CustomUserToken sessionToken = SessionAuthenticator.authenticateSession(signerId);
        Key sessionKey = sessionToken.getSessionKey();
        PublicKey clientPubKey = sessionToken.getPublicKey();

        byte[] message = EncryptionUtils.decryptbytesAES(request.getMessage().toByteArray(), (SecretKeySpec) sessionKey);
        hash = EncryptionUtils.decryptbytesRSAPub(hash, clientPubKey);

        if (!HashUtils.verifyMessageSignature(message, hash)) {
            throw new IllegalArgumentException("Invalid Signature");
        }

        Tig.Content content = (Tig.Content) ObjectSerializer.Deserialize(message);

        sessionToken.authenticateNonce(content.getNonce());

        Tig.SessionRequest sessionRequest = (Tig.SessionRequest) ObjectSerializer.Deserialize(content.getRequest().toByteArray());
        Tig.ListFilesReply files;

        try {
            keyStub.logoutTigKey(sessionRequest);
        } catch (StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        SessionAuthenticator.clearSession(signerId);
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();

    }

    @Override
    public void deleteFile (Tig.CustomProtocolMessage request, StreamObserver<Empty> responseObserver) {
        byte[] hash = request.getSignature().getValue().toByteArray();
        String signerId = request.getSignature().getSignerId();

        CustomUserToken sessionToken = SessionAuthenticator.authenticateSession(signerId);
        Key sessionKey = sessionToken.getSessionKey();
        PublicKey clientPubKey = sessionToken.getPublicKey();


        byte[] message = EncryptionUtils.decryptbytesAES(request.getMessage().toByteArray(), (SecretKeySpec) sessionKey);
        hash = EncryptionUtils.decryptbytesRSAPub(hash, clientPubKey);
        if (!HashUtils.verifyMessageSignature(message, hash)) {
            throw new IllegalArgumentException("Invalid Signature");
        }
        Tig.Content content = (Tig.Content) ObjectSerializer.Deserialize(message);

        sessionToken.authenticateNonce(content.getNonce());

        Tig.DeleteFileRequest deleteRequest = (Tig.DeleteFileRequest) ObjectSerializer.Deserialize(content.getRequest().toByteArray());
        try {
            Tig.DeleteFileReply reply = keyStub.deleteFileTigKey(deleteRequest);
            FileDAO.deleteFile(reply.getFileId());
            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        }catch (StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

    }


    @Override
    public void setAccessControl (Tig.CustomProtocolMessage request, StreamObserver<Empty> responseObserver) {
        byte[] hash = request.getSignature().getValue().toByteArray();
        String signerId = request.getSignature().getSignerId();

        CustomUserToken sessionToken = SessionAuthenticator.authenticateSession(signerId);
        Key sessionKey = sessionToken.getSessionKey();
        PublicKey clientPubKey = sessionToken.getPublicKey();

        byte[] message = EncryptionUtils.decryptbytesAES(request.getMessage().toByteArray(), (SecretKeySpec) sessionKey);
        hash = EncryptionUtils.decryptbytesRSAPub(hash, clientPubKey);

        if (!HashUtils.verifyMessageSignature(message, hash)) {
            throw new IllegalArgumentException("Invalid Signature");
        }

        Tig.Content content = (Tig.Content) ObjectSerializer.Deserialize(message);

        sessionToken.authenticateNonce(content.getNonce());

        Tig.AccessControlRequest accessRequest = (Tig.AccessControlRequest) ObjectSerializer.Deserialize(content.getRequest().toByteArray());
        try {
            responseObserver.onNext(keyStub.accessControlFileTigKey(accessRequest));
            responseObserver.onCompleted();
        }catch (StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public void listFiles (Tig.CustomProtocolMessage request, StreamObserver<Tig.CustomProtocolMessage> responseObserver) {
        byte[] hash = request.getSignature().getValue().toByteArray();
        String signerId = request.getSignature().getSignerId();


        CustomUserToken sessionToken = SessionAuthenticator.authenticateSession(signerId);
        Key sessionKey = sessionToken.getSessionKey();
        PublicKey clientPubKey = sessionToken.getPublicKey();


        byte[] message = EncryptionUtils.decryptbytesAES(request.getMessage().toByteArray(), (SecretKeySpec) sessionKey);
        hash = EncryptionUtils.decryptbytesRSAPub(hash, clientPubKey);

        if (!HashUtils.verifyMessageSignature(message, hash)) {
            throw new IllegalArgumentException("Invalid Signature");
        }

        Tig.Content content = (Tig.Content) ObjectSerializer.Deserialize(message);

        sessionToken.authenticateNonce(content.getNonce());

        Tig.SessionRequest sessionRequest = (Tig.SessionRequest) ObjectSerializer.Deserialize(content.getRequest().toByteArray());
        Tig.ListFilesReply files;

        try {
            files = keyStub.listFileTigKey(Tig.TigKeySessionIdMessage.newBuilder(
                    Tig.TigKeySessionIdMessage.newBuilder().setSessionId(sessionRequest.getSessionId()).build()).build());

        } catch (StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        message = ObjectSerializer.Serialize(files);

        String nonce = StringGenerator.randomString(256);

        content = Tig.Content.newBuilder()
                .setRequest(ByteString.copyFrom(message))
                .setNonce(nonce).build();

        message = ObjectSerializer.Serialize(content);

        //hash
        byte[] signature = HashUtils.hashBytes(message);

        //encrypt hash
        signature = EncryptionUtils.encryptBytesRSAPriv(signature, privateKey);
        message = EncryptionUtils.encryptBytesAES(message, (SecretKeySpec) sessionKey);

        Tig.Signature sign = Tig.Signature.newBuilder()
                .setValue(ByteString.copyFrom(signature)).build();

        //client
        responseObserver.onNext(
                Tig.CustomProtocolMessage.newBuilder()
                        .setMessage(ByteString.copyFrom(message))
                        .setSignature(sign)
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public void listBackupFiles (Tig.CustomProtocolMessage request, StreamObserver<Tig.CustomProtocolMessage> responseObserver) {
        byte[] hash = request.getSignature().getValue().toByteArray();
        String signerId = request.getSignature().getSignerId();

        CustomUserToken sessionToken = SessionAuthenticator.authenticateSession(signerId);
        Key sessionKey = sessionToken.getSessionKey();
        PublicKey clientPubKey = sessionToken.getPublicKey();

        byte[] message = EncryptionUtils.decryptbytesAES(request.getMessage().toByteArray(), (SecretKeySpec) sessionKey);
        hash = EncryptionUtils.decryptbytesRSAPub(hash, clientPubKey);

        if (!HashUtils.verifyMessageSignature(message, hash)) {
            throw new IllegalArgumentException("Invalid Signature");
        }

        Tig.Content content = (Tig.Content) ObjectSerializer.Deserialize(message);

        sessionToken.authenticateNonce(content.getNonce());

        Tig.SessionRequest sessionRequest = (Tig.SessionRequest) ObjectSerializer.Deserialize(content.getRequest().toByteArray());
        Tig.ListFilesReply files;

        try {
            files = backupStub.listBackupFiles(Tig.ListBackupFilesRequest.newBuilder(Tig.ListBackupFilesRequest.newBuilder().setSessionId(sessionRequest.getSessionId()).build()).build());
        } catch (StatusRuntimeException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        message = ObjectSerializer.Serialize(files);

        String nonce = StringGenerator.randomString(256);

        content = Tig.Content.newBuilder()
                .setRequest(ByteString.copyFrom(message))
                .setNonce(nonce).build();

        message = ObjectSerializer.Serialize(content);

        //hash
        byte[] signature = HashUtils.hashBytes(message);

        //encrypt hash
        signature = EncryptionUtils.encryptBytesRSAPriv(signature, privateKey);
        message = EncryptionUtils.encryptBytesAES(message, (SecretKeySpec) sessionKey);

        Tig.Signature sign = Tig.Signature.newBuilder()
                .setValue(ByteString.copyFrom(signature)).build();

        //client
        responseObserver.onNext(
                Tig.CustomProtocolMessage.newBuilder()
                        .setMessage(ByteString.copyFrom(message))
                        .setSignature(sign)
                        .build()
        );
        responseObserver.onCompleted();
    }

}

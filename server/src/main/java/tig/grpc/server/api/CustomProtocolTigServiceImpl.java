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
import tig.grpc.server.session.SessionAuthenticator;
import tig.grpc.server.throttle.Throttler;
import tig.utils.StringGenerator;
import tig.utils.encryption.EncryptionUtils;
import tig.utils.encryption.HashUtils;
import tig.utils.keys.KeyGen;
import tig.utils.serialization.ObjectSerializer;

import javax.crypto.SecretKey;
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

        //get SessionKey
        byte[] sessionKey = registerRequest.getEncryptionKey().toByteArray();
        //decipher with server key
        sessionKey = EncryptionUtils.decryptbytesRSAPriv(sessionKey, privateKey);
        SecretKeySpec key = EncryptionUtils.getAesKey(sessionKey);

        //decipher message with session key
        message = registerRequest.getMessage().toByteArray();
        message = EncryptionUtils.decryptbytesAES(message, key);
        byte[] hash = signature.getValue().toByteArray();
        hash = EncryptionUtils.decryptbytesRSAPriv(hash, privateKey);
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

        //get SessionKey
        byte[] sessionKey = loginRequest.getEncryptionKey().toByteArray();
        //decipher with server key
        sessionKey = EncryptionUtils.decryptbytesRSAPriv(sessionKey, privateKey);
        SecretKeySpec key = EncryptionUtils.getAesKey(sessionKey);

        //decipher message with session key
        message = loginRequest.getMessage().toByteArray();
        message = EncryptionUtils.decryptbytesAES(message, key);
        byte[] hash = signature.getValue().toByteArray();
        hash = EncryptionUtils.decryptbytesRSAPriv(hash, privateKey);

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
                .setSignerId(StringGenerator.randomString(256))
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
    public void logout(Tig.CustomProtocolMessage request, StreamObserver<Empty> reply) {
        //validate message
        /*byte[] encryptedMessage = request.getMessage().toByteArray();

        byte[] serializedSignature = request.getSignature().toByteArray();
        Tig.Signature sign = (Tig.Signature)ObjectSerializer.Deserialize(serializedSignature);
        byte[] signature = EncryptionUtils.decryptbytesRSAPriv(sign.getValue().toByteArray(), privateKey);

        //Get User Session Key
        String signerId = sign.getSignerId();
        CustomUserToken token = (CustomUserToken)SessionAuthenticator.authenticateSession(signerId);
        Key sessionKey = token.getSessionKey();
        byte[] message = EncryptionUtils.decryptbytesAES(encryptedMessage, (SecretKeySpec)sessionKey);

        if (!HashUtils.verifyMessageSignature(message, signature)) {
            throw new IllegalArgumentException("Invalid Signature");
        }
        Tig.CustomProtocolLogoutRequest logoutRequest = (Tig.CustomProtocolLogoutRequest)ObjectSerializer.Deserialize(message);
        //FIXME check nonce

        SessionAuthenticator.clearSession(signerId);
        //generate response
        Tig.CustomProtocolLogoutReply logoutReply = Tig.CustomProtocolLogoutReply.newBuilder()
                .build();

        byte[] replyMessage = ObjectSerializer.Serialize(logoutReply);
        byte[] replySignature = HashUtils.hashBytes(replyMessage);
        byte[] replyIv = EncryptionUtils.generateIv();

        replyMessage = EncryptionUtils.encryptBytesAES(replyMessage, (SecretKeySpec)sessionKey);
        replySignature = EncryptionUtils.encryptBytesRSAPriv(replySignature, privateKey);
        Tig.CustomProtocolMessage actualReply = Tig.CustomProtocolMessage.newBuilder()
                .setSignature(ByteString.copyFrom(replySignature))
                .setMessage(ByteString.copyFrom(replyMessage))
                .build();

        reply.onNext(actualReply);
        reply.onCompleted();*/


    }

    @Override
    public void deleteFile (Tig.CustomProtocolMessage request, StreamObserver<Empty> responseObserver) {
        byte[] hash = request.getSignature().getValue().toByteArray();
        String signerId = request.getSignature().getSignerId();
        Key sessionKey = SessionAuthenticator.authenticateSession(signerId).getSessionKey();
        byte[] message = EncryptionUtils.decryptbytesAES(request.getMessage().toByteArray(), (SecretKeySpec) sessionKey);
        hash = EncryptionUtils.decryptbytesRSAPriv(hash, privateKey);
        if (!HashUtils.verifyMessageSignature(message, hash)) {
            throw new IllegalArgumentException("Invalid Signature");
        }
        Tig.Content content = (Tig.Content) ObjectSerializer.Deserialize(message);
        //TODO verify nonces
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

        Key sessionKey = SessionAuthenticator.authenticateSession(signerId).getSessionKey();

        byte[] message = EncryptionUtils.decryptbytesAES(request.getMessage().toByteArray(), (SecretKeySpec) sessionKey);
        hash = EncryptionUtils.decryptbytesRSAPriv(hash, privateKey);

        if (!HashUtils.verifyMessageSignature(message, hash)) {
            throw new IllegalArgumentException("Invalid Signature");
        }

        Tig.Content content = (Tig.Content) ObjectSerializer.Deserialize(message);

        //TODO verify nonces
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

        Key sessionKey = SessionAuthenticator.authenticateSession(signerId).getSessionKey();

        byte[] message = EncryptionUtils.decryptbytesAES(request.getMessage().toByteArray(), (SecretKeySpec) sessionKey);
        hash = EncryptionUtils.decryptbytesRSAPriv(hash, privateKey);

        if (!HashUtils.verifyMessageSignature(message, hash)) {
            throw new IllegalArgumentException("Invalid Signature");
        }

        Tig.Content content = (Tig.Content) ObjectSerializer.Deserialize(message);

        //TODO verify nonces
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

}

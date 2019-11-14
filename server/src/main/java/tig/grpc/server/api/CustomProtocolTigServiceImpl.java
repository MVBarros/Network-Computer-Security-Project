package tig.grpc.server.api;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import tig.grpc.contract.CustomProtocolTigServiceGrpc;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigBackupServiceGrpc;
import tig.grpc.contract.TigKeyServiceGrpc;
import tig.grpc.server.session.SessionAuthenticator;
import tig.grpc.server.throttle.Throttler;
import tig.utils.encryption.EncryptionUtils;
import tig.utils.encryption.HashUtils;
import tig.utils.keys.KeyGen;
import tig.utils.serialization.ObjectSerializer;

import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class CustomProtocolTigServiceImpl extends CustomProtocolTigServiceGrpc.CustomProtocolTigServiceImplBase {

    public static PrivateKey privateKey;
    public static PublicKey publicKey;


    public static TigKeyServiceGrpc.TigKeyServiceBlockingStub keyStub;
    public static TigBackupServiceGrpc.TigBackupServiceBlockingStub backupStub;


    @Override
    public void login(Tig.CustomProtocolMessage request, StreamObserver<Tig.CustomProtocolMessage> responseObserver) {
    }

    @Override
    public void logout(Tig.CustomProtocolMessage request, StreamObserver<Tig.CustomProtocolMessage> reply) {
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
        reply.onCompleted();

         */
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

}

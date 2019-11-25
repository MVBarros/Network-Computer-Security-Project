package tig.grpc.server.api;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import tig.grpc.contract.CustomProtocolTigServiceGrpc;
import tig.grpc.contract.Tig;
import tig.grpc.server.data.dao.UsersDAO;
import tig.grpc.server.session.CustomUserToken;
import tig.grpc.server.session.SessionAuthenticator;
import tig.utils.encryption.EncryptionUtils;
import tig.utils.encryption.HashUtils;
import tig.utils.keys.KeyGen;
import tig.utils.serialization.ObjectSerializer;

import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class CustomProtocolTigServiceImpl extends CustomProtocolTigServiceGrpc.CustomProtocolTigServiceImplBase {

    public static PrivateKey privateKey;
    public static PublicKey publicKey;


    @Override
    public void login(Tig.CustomProtocolMessage request, StreamObserver<Tig.CustomProtocolMessage> reply) {
        try {
            Tig.CustomLoginRequest loginRequest = (Tig.CustomLoginRequest)ObjectSerializer.Deserialize(request.getMessage().toByteArray());
            //Get Message Key
            byte[] encryptionKey = loginRequest.getEncryptionKey().toByteArray();
            encryptionKey = EncryptionUtils.decryptbytesRSAPriv(encryptionKey, privateKey);
            SecretKeySpec secretKey = new SecretKeySpec(encryptionKey, "AES");

            //Verify signature
            byte[] message = request.getMessage().toByteArray();
            byte[] encryptedSignature = request.getSignature().toByteArray();
            byte[] signature = EncryptionUtils.decryptbytesRSAPriv(encryptedSignature, privateKey);
            if (!HashUtils.verifyMessageSignature(message, signature)) {
                throw new IllegalArgumentException("Invalid Signature");
            }

            byte[] accountMessage = EncryptionUtils.decryptbytesAES(loginRequest.getMessage().toByteArray(), secretKey);
            Tig.AccountRequest accountRequest = (Tig.AccountRequest) ObjectSerializer.Deserialize(accountMessage);

            //login user
            UsersDAO.authenticateUser(accountRequest.getUsername(), accountRequest.getPassword());

            //generate session Key
            Key sessionKey = KeyGen.generateSessionKey();
            String sessionId = SessionAuthenticator.createCustomSession(accountRequest.getUsername(), sessionKey);

            //generate response
            Tig.CustomProtocolLoginReply loginReply = Tig.CustomProtocolLoginReply.newBuilder()
                    .setSecretKey(ByteString.copyFrom(sessionKey.getEncoded()))
                    .setSessionId(sessionId)
                    .build();
            byte[] replyMessage = ObjectSerializer.Serialize(loginReply);
            byte[] replySignature = HashUtils.hashBytes(replyMessage);
            replyMessage = EncryptionUtils.encryptBytesAES(replyMessage, secretKey);

            //sign with client public key
            byte[] clientKey = loginRequest.getClientPubKey().toByteArray();
            PublicKey clientPubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(clientKey));
            replySignature = EncryptionUtils.encryptBytesRSAPub(replySignature, clientPubKey);

            Tig.CustomProtocolMessage actualReply = Tig.CustomProtocolMessage.newBuilder()
                    .setSignature(ByteString.copyFrom(replySignature))
                    .setMessage(ByteString.copyFrom(replyMessage))
                    .build();


            reply.onNext(actualReply);
            reply.onCompleted();
        }catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            //Should never happen
            throw new RuntimeException();
        }
    }

    @Override
    public void logout(Tig.CustomProtocolMessage request, StreamObserver<Tig.CustomProtocolMessage> reply) {
        //validate message
        /*byte[] encryptedMessage = request.getMessage().toByteArray();
        byte[] encryptedSignature = request.getSignature().toByteArray();
        byte[] iv = request.getIv().toByteArray();

        byte[] serializedSignature = EncryptionUtils.decryptbytesAES(encryptedSignature, new SecretKeySpec(privateKey.getEncoded(), "RSA"), iv);
        Tig.Signature signature = (Tig.Signature)ObjectSerializer.Deserialize(serializedSignature);
        String signerId = signature.getSignerId();
        CustomUserToken token = (CustomUserToken)SessionAuthenticator.authenticateSession(signerId);
        Key sessionKey = token.getSessionKey();
        byte[] message = EncryptionUtils.decryptbytesAES(encryptedMessage, new SecretKeySpec(sessionKey.getEncoded(), "RSA"), iv);

        if (!HashUtils.verifyMessageSignature(message, signature.getValue().toByteArray())) {
            throw new IllegalArgumentException("Invalid Signature");
        }
        Tig.CustomProtocolLogoutRequest logoutRequest = (Tig.CustomProtocolLogoutRequest)ObjectSerializer.Deserialize(message);
        //check nonce
        SessionAuthenticator.clearSession(signerId);
        //generate response
        Tig.CustomProtocolLogoutReply logoutReply = Tig.CustomProtocolLogoutReply.newBuilder()
                .build();

        byte[] replyMessage = ObjectSerializer.Serialize(logoutReply);
        byte[] replySignature = HashUtils.hashBytes(replyMessage);
        byte[] replyIv = EncryptionUtils.generateIv();

        replyMessage = EncryptionUtils.encryptBytesAES(replyMessage, new SecretKeySpec(sessionKey.getEncoded(), "RSA"), replyIv);
        replySignature = EncryptionUtils.encryptBytesAES(replySignature, new SecretKeySpec(privateKey.getEncoded(), "RSA"), replyIv);
        Tig.CustomProtocolMessage actualReply = Tig.CustomProtocolMessage.newBuilder()
                .setIv(ByteString.copyFrom(replyIv))
                .setSignature(ByteString.copyFrom(replySignature))
                .setMessage(ByteString.copyFrom(replyMessage))
                .build();

        reply.onNext(actualReply);
        reply.onCompleted();*/
    }

}

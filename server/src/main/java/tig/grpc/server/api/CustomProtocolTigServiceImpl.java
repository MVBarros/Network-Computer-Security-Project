package tig.grpc.server.api;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import tig.grpc.contract.CustomProtocolTigServiceGrpc;
import tig.grpc.contract.Tig;
import tig.grpc.server.data.dao.UsersDAO;
import tig.grpc.server.session.SessionAuthenticator;
import tig.utils.encryption.EncryptionUtils;
import tig.utils.encryption.HashUtils;
import tig.utils.keys.KeyGen;
import tig.utils.serialization.ObjectSerializer;

import javax.crypto.spec.SecretKeySpec;
import java.security.*;

public class CustomProtocolTigServiceImpl extends CustomProtocolTigServiceGrpc.CustomProtocolTigServiceImplBase {

    public static PrivateKey privateKey;
    public static PublicKey publicKey;


    @Override
    public void login(Tig.CustomProtocolMessage request, StreamObserver<Tig.CustomProtocolMessage> reply) {
        //validate message
        byte[] encryptedMessage = request.getMessage().toByteArray();
        byte[] encryptedSignature = request.getSignature().toByteArray();
        byte[] iv = request.getIv().toByteArray();
        byte[] message = EncryptionUtils.decryptbytes(encryptedMessage, new SecretKeySpec(privateKey.getEncoded(), "RSA"), iv);
        Tig.CustomProtocolLoginRequest loginRequest = (Tig.CustomProtocolLoginRequest) ObjectSerializer.Deserialize(message);
        byte[] clientPubKey = loginRequest.getClientPubKey().toByteArray();
        byte[] signature = EncryptionUtils.decryptbytes(encryptedSignature, new SecretKeySpec(clientPubKey, "RSA"), iv);
        if (!HashUtils.verifyMessageSignature(message, signature)) {
            throw new IllegalArgumentException("Invalid Signature");
        }

        //login user
        UsersDAO.authenticateUser(loginRequest.getUsename(), loginRequest.getPassword());

        Key sessionKey = KeyGen.generateSessionKey();
        String sessionId = SessionAuthenticator.createCustomSession(loginRequest.getUsename(), sessionKey);

        //generate response
        Tig.CustomProtocolLoginReply loginReply = Tig.CustomProtocolLoginReply.newBuilder()
                .setSecretKey(ByteString.copyFrom(sessionKey.getEncoded()))
                .build();

        byte[] replyMessage = ObjectSerializer.Serialize(loginReply);
        byte[] replySignature = HashUtils.hashBytes(replyMessage);
        byte[] replyIv = EncryptionUtils.generateIv();

        replyMessage = EncryptionUtils.encryptBytes(replyMessage, new SecretKeySpec(clientPubKey, "RSA"), replyIv);
        replySignature = EncryptionUtils.encryptBytes(replySignature, new SecretKeySpec(privateKey.getEncoded(), "RSA"), replyIv);
        Tig.CustomProtocolMessage actualReply = Tig.CustomProtocolMessage.newBuilder()
                .setIv(ByteString.copyFrom(replyIv))
                .setSignature(ByteString.copyFrom(replySignature))
                .setMessage(ByteString.copyFrom(replyMessage))
                .build();

        reply.onNext(actualReply);
        reply.onCompleted();
    }
}

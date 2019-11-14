package tig.grpc.client.operations;

import com.google.protobuf.ByteString;
import io.grpc.StatusRuntimeException;
import tig.grpc.client.Client;
import tig.grpc.contract.Tig;
import tig.utils.StringGenerator;
import tig.utils.encryption.EncryptionUtils;
import tig.utils.encryption.HashUtils;
import tig.utils.serialization.ObjectSerializer;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CustomProtocolOperations {

    /*public static void registerClient(Client client) {
        try {
            System.out.println(String.format("Register Client %s", client.getUsername()));
            client.getStub().register(Tig.AccountRequest.newBuilder()
                    .setUsername(client.getUsername())
                    .setPassword(client.getPassword()).build());
            System.out.println(String.format("User %s Successfully registered", client.getUsername()));
        } catch (StatusRuntimeException e) {
            System.out.print("Error registering user: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }*/

    public static void loginClient(Client client) {
        /*try {
            System.out.println(String.format("Login Client Custom Protocol %s", client.getUsername()
            ));

            //Create and Serialize AccountRequest
            Tig.AccountRequest.Builder builder = Tig.AccountRequest.newBuilder();
            builder.setUsername(client.getUsername());
            builder.setPassword(client.getPassword());
            byte[] message = ObjectSerializer.Serialize(builder.build());

            //Create Login Request by encripting the request with a AES Key
            SecretKeySpec secretKey = (SecretKeySpec) EncryptionUtils.generateAESKey();
            message = EncryptionUtils.encryptBytesAES(message, secretKey);

            //Encrypt the key with the server key so only the server can decipher
            byte[] encryptedKey = EncryptionUtils.encryptBytesRSAPub(secretKey.getEncoded(), client.getServerKey());
            Tig.CustomLoginRequest loginRequest = Tig.CustomLoginRequest.newBuilder()
                    .setMessage(ByteString.copyFrom(message))
                    .setEncryptionKey(ByteString.copyFrom(encryptedKey))
                    .setClientPubKey(ByteString.copyFrom(client.getPubKey().getEncoded()))
                    .build();

            //create final message and signature
            message = ObjectSerializer.Serialize(loginRequest);
            byte[] signature = HashUtils.hashBytes(message);
            signature = EncryptionUtils.encryptBytesRSAPub(signature, client.getServerKey());

            //login user
            Tig.CustomProtocolMessage response = client.getCustomProtocolStub().login(
                    Tig.CustomProtocolMessage.newBuilder()
                            .setMessage(ByteString.copyFrom(message))
                            .setSignature(ByteString.copyFrom(signature))
                            .build());

            //unravel response
            byte[] responseBytes = response.getMessage().toByteArray();
            byte[] responseSignature = response.getSignature().toByteArray();

            //decrypt reply
            responseBytes = EncryptionUtils.decryptbytesAES(responseBytes, (SecretKeySpec) secretKey);
            Tig.CustomProtocolLoginReply loginReply = (Tig.CustomProtocolLoginReply) ObjectSerializer.Deserialize(responseBytes);

            //decrypt signature and verify
            responseSignature = EncryptionUtils.decryptbytesRSAPriv(responseSignature, client.getPrivKey());
            HashUtils.verifyMessageSignature(responseBytes, responseSignature);

            client.setSessionKey(new SecretKeySpec(loginReply.getSecretKey().toByteArray(), "AES"));
            client.setSessionId(loginReply.getSessionId());

            System.out.println(String.format("User %s Successfully logged in", client.getUsername()));

        } catch (StatusRuntimeException e) {
            System.out.print("Error logging in: ");
            System.out.println(e.getStatus().getDescription());
            System.out.println(e);
            System.exit(1);
        }*/
    }

    public static void logoutClient(Client client) {
        /*try {
            System.out.println(String.format("Login Client Custom Protocol %s", client.getUsername()
            ));

            //Create Message
            Tig.CustomProtocolLogoutRequest logoutRequest = Tig.CustomProtocolLogoutRequest.newBuilder().build();
            byte[] message = ObjectSerializer.Serialize(logoutRequest);

            //Create signature and encrypt it
            byte[] signature = HashUtils.hashBytes(message);
            signature = EncryptionUtils.encryptBytesRSAPub(signature, client.getServerKey());
            Tig.Signature sign = Tig.Signature.newBuilder().setSignerId(client.getSessionId()).setValue(ByteString.copyFrom(signature)).build();
            signature = ObjectSerializer.Serialize(sign);

            //Encrypt message
            message = EncryptionUtils.encryptBytesAES(message, (SecretKeySpec) client.getSessionKey());


            //logout user
            Tig.CustomProtocolMessage response = client.getCustomProtocolStub().logout(
                    Tig.CustomProtocolMessage.newBuilder()
                            .setMessage(ByteString.copyFrom(message))
                            .setSignature(ByteString.copyFrom(signature))
                            .build());

            //unravel response
            byte[] responseBytes = response.getMessage().toByteArray();
            byte[] responseSignature = response.getSignature().toByteArray();

            //decrypt reply
            responseBytes = EncryptionUtils.decryptbytesAES(responseBytes, (SecretKeySpec) client.getSessionKey());
            Tig.CustomProtocolLogoutReply loginReply = (Tig.CustomProtocolLogoutReply) ObjectSerializer.Deserialize(responseBytes);

            //decrypt signature and verify
            responseSignature = EncryptionUtils.decryptbytesRSAPub(responseSignature, client.getServerKey());
            HashUtils.verifyMessageSignature(responseBytes, responseSignature);

            //FIXME Verify nonce
            System.out.println(String.format("User %s Successfully logged out", client.getUsername()));

        } catch (StatusRuntimeException e) {
            System.out.print("Error logging in: ");
            System.out.println(e.getStatus().getDescription());
            System.out.println(e);
            System.exit(1);
        }*/
    }



    /*public static void deleteFile(Client client, String filename) {
        try {
            System.out.println(String.format("Delete File %s ", filename));
            client.getStub().deleteFile(Tig.DeleteFileRequest.newBuilder()
                    .setSessionId(client.getSessionId())
                    .setFilename(filename).build());
            System.out.println(String.format("File %s Successfully deleted", filename));
        } catch (StatusRuntimeException e) {
            System.out.print("Error deleting file: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }*/

    public static void setAccessControl(Client client, String filename, String target, Tig.PermissionEnum permission) {
        try {
            System.out.println(String.format("Set access control File: %s to user: %s set permission to  %s ", filename, target, permission.toString()));

            //request
            Tig.AccessControlRequest request = Tig.AccessControlRequest.newBuilder()
                    .setFileName(filename)
                    .setSessionId(client.getSessionId())
                    .setTarget(target)
                    .setPermission(permission).build();

            //serialize request
            byte[] message = ObjectSerializer.Serialize(request);

            String nonce = StringGenerator.randomString(256);

            Tig.Content content = Tig.Content.newBuilder()
                    .setRequest(ByteString.copyFrom(message))
                    .setNonce(nonce).build();

            message = ObjectSerializer.Serialize(content);

            //hash
            byte[] signature = HashUtils.hashBytes(message);

            //encrypt hash
            signature = EncryptionUtils.encryptBytesRSAPub(signature, client.getServerKey());
            message = EncryptionUtils.encryptBytesAES(message, (SecretKeySpec) client.getSessionKey());

            Tig.Signature sign = Tig.Signature.newBuilder()
                    .setValue(ByteString.copyFrom(signature)).build();

            //server
            client.getCustomProtocolStub().setAccessControl(
                    Tig.CustomProtocolMessage.newBuilder()
                    .setMessage(ByteString.copyFrom(message))
                    .setSignature(sign)
                    .build()
            );

        } catch (StatusRuntimeException e) {
            System.out.print("Error setting access control: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }

        System.out.println("Access control set successfully");
    }

    public static void listFiles(Client client) {
        try {
            System.out.println("List all Files");

            //request
            Tig.SessionRequest request = Tig.SessionRequest.newBuilder()
                    .setSessionId(client.getSessionId())
                    .build();

            //serialize request
            byte[] message = ObjectSerializer.Serialize(request);

            String nonce = StringGenerator.randomString(256);

            Tig.Content content = Tig.Content.newBuilder()
                    .setRequest(ByteString.copyFrom(message))
                    .setNonce(nonce).build();

            message = ObjectSerializer.Serialize(content);

            //hash
            byte[] signature = HashUtils.hashBytes(message);

            //encrypt hash
            signature = EncryptionUtils.encryptBytesRSAPub(signature, client.getServerKey());
            message = EncryptionUtils.encryptBytesAES(message, (SecretKeySpec) client.getSessionKey());

            Tig.Signature sign = Tig.Signature.newBuilder()
                    .setValue(ByteString.copyFrom(signature)).build();

            //server
            Tig.CustomProtocolMessage response = client.getCustomProtocolStub().listFiles(
                    Tig.CustomProtocolMessage.newBuilder()
                            .setMessage(ByteString.copyFrom(message))
                            .setSignature(sign)
                            .build()
            );

            //Decrypt response
            message = EncryptionUtils.decryptbytesAES(response.getMessage().toByteArray(), (SecretKeySpec) client.getSessionKey());

            byte[] hash = response.getSignature().getValue().toByteArray();
            hash = EncryptionUtils.decryptbytesRSAPub(hash, client.getServerKey());

            if (!HashUtils.verifyMessageSignature(message, hash)) {
                throw new IllegalArgumentException("Invalid Signature");
            }

            content = (Tig.Content) ObjectSerializer.Deserialize(message);

            //TODO verify nonces
            Tig.ListFilesReply reply = (Tig.ListFilesReply) ObjectSerializer.Deserialize(content.getRequest().toByteArray());

            Object[] names = reply.getFileInfoList().toArray();
            for (Object name : names) {
                System.out.println(name.toString() + "\n");
            }

        } catch (StatusRuntimeException e) {
            System.out.print("Error listing files: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
    }

}


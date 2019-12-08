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
        try {
            System.out.println(String.format("Login Client Custom Protocol %s", client.getUsername()
            ));

            //Create and Serialize AccountRequest
            Tig.AccountRequest.Builder builder = Tig.AccountRequest.newBuilder();
            builder.setUsername(client.getUsername());
            builder.setPassword(client.getPassword());
            byte[] message = ObjectSerializer.Serialize(builder.build());

            //Create Login Request by encripting the request with an AES Key
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

            Tig.Signature sign = Tig.Signature.newBuilder().setValue(ByteString.copyFrom(signature)).build();

            //login user
            Tig.CustomProtocolMessage response = client.getCustomProtocolStub().login(
                    Tig.CustomProtocolMessage.newBuilder()
                            .setMessage(ByteString.copyFrom(message))
                            .setSignature(sign)
                            .build());

            //unravel response
            message = response.getMessage().toByteArray();
            message = EncryptionUtils.decryptbytesAES(response.getMessage().toByteArray(), secretKey);

            byte[] hash = response.getSignature().getValue().toByteArray();
            hash = EncryptionUtils.decryptbytesRSAPub(hash, client.getServerKey());

            if (!HashUtils.verifyMessageSignature(message, hash)) {
                throw new IllegalArgumentException("Invalid Signature");
            }

            Tig.Content content = (Tig.Content) ObjectSerializer.Deserialize(message);

            //TODO verify nonces

            Tig.LoginReply reply = (Tig.LoginReply) ObjectSerializer.Deserialize(content.getRequest().toByteArray());

            System.out.println(String.format("User %s Successfully logged in", client.getUsername()));

        } catch (StatusRuntimeException e) {
            System.out.print("Error logging in: ");
            System.out.println(e.getStatus().getDescription());
            System.out.println(e);
            System.exit(1);
        }
    }

    public static void logoutClient(Client client) {
        try {
            System.out.println(String.format("Logout Client Custom Protocol %s", client.getUsername()
            ));

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

            //TODO MANDAR SIGNERID
            Tig.Signature sign = Tig.Signature.newBuilder()
                    .setValue(ByteString.copyFrom(signature)).build();

            //server
            client.getCustomProtocolStub().logout(
                    Tig.CustomProtocolMessage.newBuilder()
                            .setMessage(ByteString.copyFrom(message))
                            .setSignature(sign)
                            .build()
            );

        } catch (StatusRuntimeException e) {
            System.out.print("Error logging in: ");
            System.out.println(e.getStatus().getDescription());
            System.out.println(e);
            System.exit(1);
        }
    }

    public static void deleteFile(Client client, String filename) {
        try {
            System.out.println(String.format("Delete File %s ", filename));

            //request
            Tig.DeleteFileRequest request = Tig.DeleteFileRequest.newBuilder()
                    .setFilename(filename)
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

            //TODO SIGNER ID
            Tig.Signature sign = Tig.Signature.newBuilder()
                    .setValue(ByteString.copyFrom(signature)).build();

            //server
            client.getCustomProtocolStub().deleteFile(
                    Tig.CustomProtocolMessage.newBuilder()
                            .setMessage(ByteString.copyFrom(message))
                            .setSignature(sign)
                            .build()
            );

        } catch (StatusRuntimeException e) {
            System.out.print("Error deleting file: ");
            System.out.println(e.getStatus().getDescription());
            System.exit(1);
        }
        System.out.println(String.format("File %s Successfully deleted", filename));

    }

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

            //TODO SIGNER ID
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


            //TODO MANDAR SIGNERID
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


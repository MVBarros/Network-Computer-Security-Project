package tig.grpc.client;

import tig.grpc.contract.CustomProtocolTigServiceGrpc;
import tig.grpc.contract.TigServiceGrpc;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;

public class Client {

    private String username;
    private String password;
    private TigServiceGrpc.TigServiceBlockingStub stub;
    private TigServiceGrpc.TigServiceStub asyncStub;
    private CustomProtocolTigServiceGrpc.CustomProtocolTigServiceBlockingStub customProtocolStub;
    private String sessionId;
    private PublicKey pubKey;
    private PrivateKey privKey;
    private PublicKey serverKey;
    private SecretKey sessionKey;
    private String signerId;

    public TigServiceGrpc.TigServiceStub getAsyncStub() {
        return asyncStub;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() { return password; }

    public TigServiceGrpc.TigServiceBlockingStub getStub() {
        return stub;
    }

    public PublicKey getPubKey() {
        return this.pubKey;
    }

    public PrivateKey getPrivKey() {
        return this.privKey;
    }

    public void setPubKey(PublicKey pubKey) { this.pubKey = pubKey; }

    public void setPrivKey(PrivateKey privKey) {
        this.privKey = privKey;
    }

    public PublicKey getServerKey() {
        return serverKey;
    }

    public void setServerKey(PublicKey serverKey) {
        this.serverKey = serverKey;
    }

    public SecretKey getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(SecretKey sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getSignerId() { return signerId; }

    public void setSignerId(String signerId) { this.signerId = signerId; }


    public CustomProtocolTigServiceGrpc.CustomProtocolTigServiceBlockingStub getCustomProtocolStub() {
        return customProtocolStub;
    }


    public Client(TigServiceGrpc.TigServiceBlockingStub stub, TigServiceGrpc.TigServiceStub asyncStub,
                  CustomProtocolTigServiceGrpc.CustomProtocolTigServiceBlockingStub customProtocolStub,
                  String username, String password) {
        this.stub = stub;
        this.customProtocolStub = customProtocolStub;
        this.username = username;
        this.password = password;
        this.asyncStub = asyncStub;
    }
}

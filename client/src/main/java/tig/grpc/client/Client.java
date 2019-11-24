package tig.grpc.client;

import tig.grpc.contract.TigServiceGrpc;

import java.security.Key;

public class Client {

    private String username;
    private String password;
    private TigServiceGrpc.TigServiceBlockingStub stub;
    private TigServiceGrpc.TigServiceStub asyncStub;
    private String sessionId;
    private Key pubKey;
    private Key privKey;

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

    public String getPassword() {
        return password;
    }

    public TigServiceGrpc.TigServiceBlockingStub getStub() {
        return stub;
    }

    public Key getPubKey() { return this.pubKey; }

    public Key getPrivKey() { return this.privKey; }

    public void setPubKey(Key pubKey) { this.pubKey = pubKey; }

    public void setPrivKey(Key privKey) { this.privKey = privKey; }

    public Client(TigServiceGrpc.TigServiceBlockingStub stub, TigServiceGrpc.TigServiceStub asyncStub, String username, String password) {
        this.stub = stub;
        this.username = username;
        this.password = password;
        this.asyncStub = asyncStub;
    }
}

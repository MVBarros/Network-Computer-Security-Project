package tig.grpc.client;

import tig.grpc.contract.CustomProtocolTigServiceGrpc;
import tig.grpc.contract.TigServiceGrpc;

import java.security.Key;

public class Client {

    private String username;
    private String password;
    private TigServiceGrpc.TigServiceBlockingStub stub;
    private TigServiceGrpc.TigServiceStub asyncStub;
    private CustomProtocolTigServiceGrpc.CustomProtocolTigServiceBlockingStub customProtocolStub;
    private String sessionId;
    private Key pubKey;
    private Key privKey;
    private Key serverKey;
    private Key sessionKey;

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

    public Key getPubKey() {
        return this.pubKey;
    }

    public Key getPrivKey() {
        return this.privKey;
    }

    public void setPubKey(Key pubKey) {
        this.pubKey = pubKey;
    }

    public void setPrivKey(Key privKey) {
        this.privKey = privKey;
    }

    public Key getServerKey() {
        return serverKey;
    }

    public void setServerKey(Key serverKey) {
        this.serverKey = serverKey;
    }

    public Key getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(Key sessionKey) {
        this.sessionKey = sessionKey;
    }

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

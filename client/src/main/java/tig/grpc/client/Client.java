package tig.grpc.client;

import tig.grpc.contract.TigServiceGrpc;

public class Client {

    private String username;
    private String password;
    private TigServiceGrpc.TigServiceBlockingStub stub;
    private String sessionId;

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

    public Client(TigServiceGrpc.TigServiceBlockingStub stub, String username, String password) {
        this.stub = stub;
        this.username = username;
        this.password = password;
    }

}

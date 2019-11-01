package tig.grpc.client;

import io.grpc.StatusRuntimeException;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Arrays;

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

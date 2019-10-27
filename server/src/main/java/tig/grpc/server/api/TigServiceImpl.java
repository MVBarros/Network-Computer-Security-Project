package tig.grpc.server.api;


import com.google.protobuf.Empty;
import tig.grpc.contract.Tig.*;
import tig.grpc.contract.TigServiceGrpc;

import io.grpc.stub.StreamObserver;

public class TigServiceImpl extends TigServiceGrpc.TigServiceImplBase {

    @Override
    public void login(LoginRequest request, StreamObserver<LoginReply> responseObserver) {

    }

    @Override
    public void logout(SessionRequest request, StreamObserver<Empty> responseObserver) {

    }

    @Override
    public void deleteFile(FileRequest request, StreamObserver<StatusReply> responseObserver) {

    }

    @Override
    public void accessControlFile(OperationRequest request, StreamObserver<StatusReply> responseObserver) {

    }

    @Override
    public void listFiles(SessionRequest request, StreamObserver<ListFilesReply> responseObserver) {

    }


    @Override
    public StreamObserver<FileChunk> uploadFile(StreamObserver<StatusReply> responseObserver) {
        return null;
    }

    @Override
    public StreamObserver<FileChunk> editFile(StreamObserver<StatusReply> responseObserver) {
        return null;
    }

}

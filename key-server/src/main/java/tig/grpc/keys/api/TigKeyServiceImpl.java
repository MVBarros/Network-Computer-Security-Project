package tig.grpc.keys.api;

import io.grpc.stub.StreamObserver;
import tig.grpc.contract.Tig;
import tig.grpc.contract.TigKeyServiceGrpc;

public class TigKeyServiceImpl extends TigKeyServiceGrpc.TigKeyServiceImplBase {

    @Override
    public void helloTigKey(Tig.HelloTigKeyRequest request, StreamObserver<Tig.HelloTigKeyReply> reply) {
        System.out.println(String.format("Hello %s", request.getRequest()));
        Tig.HelloTigKeyReply keyReply = Tig.HelloTigKeyReply.newBuilder().setRequest("Hello from Tig Key").build();
        reply.onNext(keyReply);
        reply.onCompleted();
    }
}

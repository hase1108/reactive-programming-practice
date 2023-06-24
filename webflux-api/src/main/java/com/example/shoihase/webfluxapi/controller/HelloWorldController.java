package com.example.shoihase.webfluxapi.controller;


import com.example.tutorial.protos.ReactorHelloWorldSimpleServiceGrpc;
import com.google.rpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcService;
import reactor.core.publisher.Mono;

@Slf4j
@GRpcService
public class HelloWorldController extends ReactorHelloWorldSimpleServiceGrpc.HelloWorldSimpleServiceImplBase {

    @Override
    public reactor.core.publisher.Mono<com.google.rpc.Status> saySimpleHello(reactor.core.publisher.Mono<com.google.protobuf.Empty> request) {
        return request.log().thenReturn(Status.newBuilder().setCode(0).setMessage("test").build());
    }
}

package org.javaboy.grpc.client;

import com.google.protobuf.StringValue;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.stub.StreamObserver;
import org.javaboy.grpc.api.HelloServiceGrpc;
import org.javaboy.grpc.api.LoginBody;
import org.javaboy.grpc.api.LoginResponse;
import org.javaboy.grpc.api.LoginServiceGrpc;

import javax.net.ssl.SSLException;
import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author baize
 * @date 2023/2/17
 * @site www.qfedu.com
 */
public class LoginClient {
    public static void main(String[] args) throws InterruptedException, SSLException {

        File certFile = Paths.get( "certs", "ca.crt").toFile();
        SslContext sslContext = GrpcSslContexts.forClient().trustManager(certFile).build();

        ManagedChannel channel = NettyChannelBuilder.forAddress("local.javaboy.org", 50051)
                .useTransportSecurity()
                .sslContext(sslContext)
                .build();

        LoginServiceGrpc.LoginServiceStub stub = LoginServiceGrpc.newStub(channel).withDeadline(Deadline.after(3, TimeUnit.SECONDS));
        sayHello(channel);
    }

    private static void sayHello(ManagedChannel channel) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        HelloServiceGrpc.HelloServiceStub helloServiceStub = HelloServiceGrpc.newStub(channel);
        helloServiceStub
                .withCallCredentials(new HttpBasicCredential("javaboy", "123"))
                .sayHello(StringValue.newBuilder().setValue("wangwu").build(), new StreamObserver<StringValue>() {
            @Override
            public void onNext(StringValue stringValue) {
                System.out.println("stringValue.getValue() = " + stringValue.getValue());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("throwable.getMessage() = " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
    }
}

package com.chainML.service;

import com.google.protobuf.ByteString;
import com.chainML.pb.*;
import io.grpc.ManagedChannel;
import com.chainML.pb.chainMLServiceGrpc.chainMLServiceBlockingStub;

import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.String;


public class chainMLClient {
    private static final Logger logger = Logger.getLogger(chainServer.class.getName());

    private final ManagedChannel channel;
    private final chainMLServiceBlockingStub blockingStub;
    private final chainMLServiceGrpc.chainMLServiceStub asyncStub;

    public chainMLClient(String host, int port){
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = chainMLServiceGrpc.newBlockingStub(channel);
        asyncStub = chainMLServiceGrpc.newStub(channel);
    }
    public void defineOrder(String name) {
        OrderRequest request = OrderRequest.newBuilder().setName(name).build();
        OrderReply response;
        try {
            response = blockingStub.defineOrder(request);
            logger.info(response.getMessage());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }
    //
    //Send upload time to the controller
    public void sendUploadTime(double execTime, String device) {
        TimeRequest request = TimeRequest.newBuilder().setTime(execTime).setDevice(device).build();
        TimeReply response;
        try {
            response = blockingStub.sendUploadTime(request);
            logger.info(response.getName());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }
    //
    //Send execution time to the controller
    public void sendExecTime(double execTime, String device) {
        TimeRequest request = TimeRequest.newBuilder().setTime(execTime).setDevice(device).build();
        TimeReply response;
        try {
            response = blockingStub.sendExecTime(request);
            logger.info(response.getName());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(10, TimeUnit.SECONDS);
    }
    //
    //Function to upload file to the server, arg: file path, type of file sent
    public void uploadFile(String imagePath, String type) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<UploadFileRequest> requestObserver = asyncStub.withDeadlineAfter(20,TimeUnit.SECONDS)
                .uploadFile(new StreamObserver<UploadFileResponse>() {
                    @Override
                    public void onNext(UploadFileResponse response) {
                        logger.info("receive response: " + response);

                    }

                    @Override
                    public void onError(Throwable t) {
                        logger.log(Level.SEVERE, "upload failed: " + t);
                        finishLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("file uploaded");
                        finishLatch.countDown();
                    }
                });

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(imagePath);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "cannot read file " + e.getMessage());
        }

        String fileType = imagePath.substring(imagePath.lastIndexOf("."));
        FileInfo info = FileInfo.newBuilder().setImageType(fileType).build();
        TypeFile typeFile = TypeFile.newBuilder().setTypefile(type).build();
        UploadFileRequest request = UploadFileRequest.newBuilder().setInfo(info).setTypeFile(typeFile).build();

        try {
            requestObserver.onNext(request);
            logger.info("sent file info" + info);

            byte[] buffer = new byte[1024];
            while (true) {
                int n = fileInputStream.read(buffer);
                if (n <= 0) {
                    break;
                }
                if (finishLatch.getCount() == 0) {
                    return;
                }
                request = UploadFileRequest.newBuilder()
                        .setChunkData(ByteString.copyFrom(buffer, 0, n))
                        .build();
                requestObserver.onNext(request);
                logger.info("sent file chunk with size: " + n);
            }
        }catch (Exception e){
            logger.log(Level.SEVERE, "unexcepted error: " + e.getMessage());
            requestObserver.onError(e);
            return;
        }

        requestObserver.onCompleted();

        if (!finishLatch.await(1, TimeUnit.MINUTES)){
            logger.warning("request cannot finish within 1 minute");
        }
    }

}

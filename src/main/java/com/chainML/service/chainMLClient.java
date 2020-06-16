package com.chainML.service;

import com.google.protobuf.ByteString;
import com.chainML.pb.*;
import io.grpc.ManagedChannel;
import com.chainML.pb.chainMLServiceGrpc.chainMLServiceBlockingStub;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.conscrypt.io.IoUtils;
import org.junit.Assert;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    //Function to upload file to the server, arg: file path
    public void uploadFile(String imagePath, String type) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);

        StreamObserver<UploadFileRequest> requestObserver = asyncStub.withDeadlineAfter(5,TimeUnit.SECONDS)
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
                        logger.info("image uploaded");
                        finishLatch.countDown();
                    }
                });

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(imagePath);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "cannot read image file " + e.getMessage());
        }

        String imageType = imagePath.substring(imagePath.lastIndexOf("."));
        ImageInfo info = ImageInfo.newBuilder().setImageType(imageType).build();
        TypeFile typeFile = TypeFile.newBuilder().setTypefile(type).build();
        UploadFileRequest request = UploadFileRequest.newBuilder().setInfo(info).setTypeFile(typeFile).build();

        try {
            requestObserver.onNext(request);
            logger.info("sent image info" + info);

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
                logger.info("sent image chunk with size: " + n);
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

    public static void main(String[] args) throws InterruptedException {

        String Android = "192.168.1.77";
        String Linux = "0.0.0.0";
        String RPI = "192.168.1.78";
        chainMLClient client = new chainMLClient(Linux, 50051);

        try {
            client.uploadFile("tmp/mobilenet.tflite", "model");
            //client.uploadFile("tmp/label.txt", "label");
            //client.uploadFile("tmp/index.jpeg", "image");
            // client.downloadFile("laptop.jpg");
        } finally {
            client.shutdown();
        }
    }

}

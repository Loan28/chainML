package com.chainML.service;


import com.chainML.pb.*;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.io.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class chainMLService extends chainMLServiceGrpc.chainMLServiceImplBase {

    private static final Logger logger = Logger.getLogger(chainMLService.class.getName());
    private FileStore fileStore;
    String nextDevice;

    public chainMLService(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    @Override
    public void defineOrder(OrderRequest request, StreamObserver<OrderReply> responseObserver) {

        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory();
        int numberOfProcessors = runtime.availableProcessors();
        String OS = System.getProperty("os.name").toLowerCase();
        OrderReply reply = OrderReply.newBuilder().setMessage("Device : " + OS + "\n" + "Number of processors: " + numberOfProcessors + "\n" + "Memory available: " + memory + " bytes \n").build();
        nextDevice = request.getName();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void getSpecs(OrderRequest request, StreamObserver<OrderReply> responseObserver) {
        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        int numberOfProcessors = runtime.availableProcessors();
        String OS = System.getProperty("os.name").toLowerCase();
        OrderReply reply = OrderReply.newBuilder().setMessage("Device : " + OS + "\n" + "Number of processors: " + numberOfProcessors + "\n" + "Memory available: " + memory + " bytes \n").build();
        nextDevice = request.getName();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<UploadFileRequest> uploadFile(StreamObserver<UploadFileResponse> responseObserver) {
        return new StreamObserver<UploadFileRequest>() {
            private String fileType;
            private ByteArrayOutputStream fileData;
            private TypeFile type_file;
            private FileName file_name;

            @Override
            public void onNext(UploadFileRequest request) {
                if(request.getDataCase() == UploadFileRequest.DataCase.INFO) {
                    FileInfo info = request.getInfo();
                    type_file = request.getTypeFile();
                    fileType = info.getImageType();
                    fileData = new ByteArrayOutputStream();
                    file_name = request.getFileName();
                    return;

                }
                ByteString chunkData = request.getChunkData();
                try {
                    chunkData.writeTo(fileData);
                } catch (IOException e) {
                    responseObserver.onError(
                            Status.INTERNAL
                                    .withDescription("cannot write chunk data: " + e.getMessage())
                                    .asRuntimeException()
                    );
                    return;
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.warning(t.getMessage());
            }

            @Override
            public void onCompleted() {
                String fileID = "";
                int imageSize = fileData.size();

                try {
                    if(type_file.getTypefile().equals("image")) {
                        fileID = fileStore.Save(fileType, fileData, file_name.getFilename());
                        Processing process = new Processing();
                        process.ProcessImage(fileID, fileType, nextDevice);
                    } else if (type_file.getTypefile().equals("model")) {
                        fileID = fileStore.Save(fileType, fileData, file_name.getFilename());
                    }else if (type_file.getTypefile().equals("video")){
                        fileID = fileStore.Save(fileType, fileData, file_name.getFilename());
                        Processing process = new Processing();
                        process.ProcessVideo(fileID + fileType, nextDevice);
                    }
                    else
                    {
                        fileID = fileStore.Save(fileType, fileData, "label");
                    }
                    logger.info("receive " + type_file.getTypefile());

                } catch (IOException | InterruptedException | ExecutionException e) {
                    responseObserver.onError(
                            Status.INTERNAL
                                    .withDescription("cannot save the " + type_file.getTypefile() + " to the store: " + e.getMessage())
                                    .asRuntimeException()
                    );
                }

                UploadFileResponse response = UploadFileResponse.newBuilder()
                        .setId(fileID)
                        .setSize(imageSize)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();

            }

        };
    }
}

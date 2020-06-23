package com.chainML.service;


import com.chainML.pb.*;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.io.*;
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
        OrderReply reply = OrderReply.newBuilder().setMessage("Hello " + request.getName()).build();
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

            @Override
            public void onNext(UploadFileRequest request) {
                if(request.getDataCase() == UploadFileRequest.DataCase.INFO) {
                    FileInfo info = request.getInfo();
                    type_file = request.getTypeFile();
                    logger.info("receive " + type_file.getTypefile() + " info" + info);
                    fileType = info.getImageType();
                    fileData = new ByteArrayOutputStream();
                    return;

                }
                ByteString chunkData = request.getChunkData();
                logger.info("receive " + type_file.getTypefile() + " chunk with size: " + chunkData.size());
                if (fileData == null) {
                    logger.info( type_file.getTypefile()+ " info was not sent before");
                    responseObserver.onError(
                            Status.INVALID_ARGUMENT
                                    .withDescription(type_file.getTypefile() + "info was not sent before")
                                    .asRuntimeException()
                    );
                    return;
                }
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
                        fileID = fileStore.Save(fileType, fileData, "image");
                        Processing process = new Processing();
                        process.ProcessImage(fileID, fileType, nextDevice);
                    } else if (type_file.getTypefile().equals("model")) {
                        fileID = fileStore.Save(fileType, fileData, "model");
                    }
                    else
                    {
                        fileID = fileStore.Save(fileType, fileData, "label");
                    }

                } catch (IOException | InterruptedException e) {
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

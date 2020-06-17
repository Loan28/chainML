package com.chainML.service;


import com.chainML.pb.*;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.io.*;
import java.util.logging.Logger;

public class chainMLService extends chainMLServiceGrpc.chainMLServiceImplBase {

    private static final Logger logger = Logger.getLogger(chainMLService.class.getName());
    private ImageStore imageStore;

    public chainMLService(ImageStore imageStore) {
        this.imageStore = imageStore;
    }

    @Override
    public StreamObserver<UploadFileRequest> uploadFile(StreamObserver<UploadFileResponse> responseObserver) {
        return new StreamObserver<UploadFileRequest>() {
            private String laptopID;
            private String imageType;
            private ByteArrayOutputStream imageData;
            private TypeFile type_file;

            @Override
            public void onNext(UploadFileRequest request) {
                if(request.getDataCase() == UploadFileRequest.DataCase.INFO) {
                    ImageInfo info = request.getInfo();
                    type_file = request.getTypeFile();
                    logger.info("receive " + type_file.getTypefile() + " info" + info);
                    imageType = info.getImageType();
                    imageData = new ByteArrayOutputStream();
                    return;

                }
                ByteString chunkData = request.getChunkData();
                //logger.info("receive " + type_file.getTypefile() + " chunk with size: " + chunkData.size());
                if (imageData == null) {
                    logger.info( type_file.getTypefile()+ " info was not sent before");
                    responseObserver.onError(
                            Status.INVALID_ARGUMENT
                                    .withDescription(type_file.getTypefile() + "info was not sent before")
                                    .asRuntimeException()
                    );
                    return;
                }
                try {
                    chunkData.writeTo(imageData);
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
                String imageID = "";
                int imageSize = imageData.size();

                try {
                    if(type_file.getTypefile().equals("image")) {
                        imageID = imageStore.Save(imageType, imageData, "image");
                        Processing process = new Processing();
                        process.ProcessImage(imageID);
                    } else if (type_file.getTypefile().equals("model")) {
                        imageID = imageStore.Save(imageType, imageData, "model");
                    }
                    else
                    {
                        imageID = imageStore.Save(imageType, imageData, "label");
                    }

                } catch (IOException | InterruptedException e) {
                    responseObserver.onError(
                            Status.INTERNAL
                                    .withDescription("cannot save the " + type_file.getTypefile() + " to the store: " + e.getMessage())
                                    .asRuntimeException()
                    );
                }

                UploadFileResponse response = UploadFileResponse.newBuilder()
                        .setId(imageID)
                        .setSize(imageSize)
                        .build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();

            }

        };
    }
}

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
    String ipController;
    String model;
    String label;
    String modelType;
    String condition;
    String condition2;
    String action;
    String action2;
    String applicationType;
    String location;
    int portController;

    public chainMLService(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    //
    //Receives :
    // - All the information needed from a device to run a model
    // - Action that should be taken once this model has been run
    @Override
    public void defineModelLabel(DefineModelLabelRequest request, StreamObserver<DefineModelLabelReply> responseObserver) {
        model = request.getModel();
        label = request.getLabel();
        condition = request.getCondition();
        condition2 = request.getCondition2();
        action = request.getAction();
        action2 = request.getAction2();
        modelType = request.getType();
        applicationType = request.getApplicationType();
        location = request.getLocation();
        ipController =request.getIpConroller();
        portController = request.getPortController();
        DefineModelLabelReply reply = DefineModelLabelReply.newBuilder().setMessage("Received model and label info").build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    //
    //Receives from the controller it's IP and port
    @Override
    public void defineController(DefineControllerRequest request, StreamObserver<DefineControllerReply> responseObserver) {
        ipController = request.getIpController();
        portController = request.getPortController();
        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory();
        int numberOfProcessors = runtime.availableProcessors();
        String OS = System.getProperty("os.name").toLowerCase();
        DefineControllerReply reply = DefineControllerReply.newBuilder().setMessage("Device :" + OS + "\n" + "Number of processors: " +
                                + numberOfProcessors + "\n" + "Memory available: \"\n" +
                                + memory + " bytes \nModels available on the device: \ndeeplabv3_257.tflite\nresnet_model_v2\nefficientnet\ninception_v1\nface_detection.pb\nposenet_mobilenet_100_257_257.tflite").build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    //
    //Receive which device is next in line
    @Override
    public void defineOrder(OrderRequest request, StreamObserver<OrderReply> responseObserver) {
        OrderReply reply = OrderReply.newBuilder().setMessage("Order defined").build();
        nextDevice = request.getName();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    //
    //Sends the spec of the device to the controller
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

    //
    //Function that stores file receive from a client
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
                        process.ProcessImage(fileID, fileType, nextDevice, ipController, portController, model, label, condition, condition2, action, action2, modelType, applicationType, location);
                    }else {
                        fileID = fileStore.Save(fileType, fileData, file_name.getFilename());
                        Processing process = new Processing();
                        process.ProcessVideo(fileID + fileType, nextDevice, ipController, portController, model, label, condition, condition2, action, action2, modelType, applicationType, location);
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

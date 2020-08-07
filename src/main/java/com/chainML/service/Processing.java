package com.chainML.service;

import com.google.common.io.Resources;
import com.google.protobuf.StringValue;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Processing {
    int totalExecutionTime = 0;
    //
    //function that take care of processing a frame/image,
    //Define the script to use first, then process it and finally take care of where the processed frame needs to go
    public void ProcessImage(String fileID, String fileType, String nextDevice, String ipController, int portController, String modelName, String labelName, String condition, String condition2, String action, String action2, String modelType, String applicationType, String location) throws IOException, InterruptedException {
        List<String> pythonOutputList;
        String pythonScript = "";
        String file_path = "files/" + fileID + fileType;
        pythonScript = definePythonScript(modelName, modelType);
        pythonOutputList = executePython(modelName, labelName, fileID, pythonScript, fileType, ipController, portController, location);
        sendFrame(file_path, nextDevice, condition, condition2, action, action2, pythonOutputList, ipController, portController, applicationType, location);

    }

    //
    //Take care of the frame that has been proceed. Can send it to the next device in line or the controller or drop it.
    public void sendFrame(String file_path, String nextDevice, String condition, String condition2, String action,String action2, List<String> pythonOutputList, String ipController, int portController, String applicationType, String location) throws InterruptedException {
        chainMLClient controller = new chainMLClient(ipController, portController);
        chainMLClient client = new chainMLClient(nextDevice, 50051);
        if (applicationType.equals("pipeline")) {
            boolean isInFrame = false;
            for (int j = 0; j < pythonOutputList.size(); j++) {
                if (condition.equals(pythonOutputList.get(j))) {
                    isInFrame = true;
                }
            }
            if (isInFrame || condition.equals("none")) {
                System.out.println("In the frame");
                if (action.equals("send_controller")) {
                    long startTimeFileTransfer = System.nanoTime();
                    controller.uploadFile(file_path, "image");
                    long endTimeFileTransfer = System.nanoTime();
                    long timeElapsedFileTransfer = endTimeFileTransfer - startTimeFileTransfer;
                    controller.sendUploadTime(timeElapsedFileTransfer, location);
                }else if(!action.equals("drop")){
                    chainMLClient client1 = new chainMLClient(action, 50051);
                    long startTimeFileTransfer = System.nanoTime();
                    client1.uploadFile(file_path, "image");
                    long endTimeFileTransfer = System.nanoTime();
                    long timeElapsedFileTransfer = endTimeFileTransfer - startTimeFileTransfer;
                    controller.sendUploadTime(timeElapsedFileTransfer, location);
                    client1.shutdown();
                }
            } else {
                client.shutdown();
                controller.shutdown();
            }
        } else {
            boolean isCondition1 = false;
            boolean isCondition2 = false;
            for (int j = 0; j < pythonOutputList.size(); j++) {
                if (condition.equals(pythonOutputList.get(j))) {
                    isCondition1 = true;
                }
                if (condition2.equals(pythonOutputList.get(j))) {
                    isCondition2 = true;
                }
            }
            if (isCondition1) {
                System.out.println(condition + " in the frame");
                if (!action.equals("drop")) {
                    chainMLClient client1 = new chainMLClient(action, 50051);
                    long startTimeFileTransfer = System.nanoTime();
                    client1.uploadFile(file_path, "image");
                    long endTimeFileTransfer = System.nanoTime();
                    long timeElapsedFileTransfer = endTimeFileTransfer - startTimeFileTransfer;
                    client1.shutdown();
                    controller.sendUploadTime((int) timeElapsedFileTransfer, location);
                }
            }
            if (isCondition2) {
                System.out.println(condition2 + " in the frame");
                if (!action2.equals("drop")) {
                    chainMLClient client2 = new chainMLClient(action2, 50051);
                    long startTimeFileTransfer = System.nanoTime();
                    client2.uploadFile(file_path, "image");
                    long endTimeFileTransfer = System.nanoTime();
                    long timeElapsedFileTransfer = endTimeFileTransfer - startTimeFileTransfer;
                    client2.shutdown();
                    controller.sendUploadTime((int) timeElapsedFileTransfer, location);
                }
            }
            client.shutdown();
            controller.shutdown();
        }

    }
    //
    // Define which python script to use depending on the model sent
    public String definePythonScript(String modelName, String modelType){
        String pythonScript = "";
        if(modelType.equals("tflite")){
            if(modelName.equals("deeplabv3_257.tflite")){
                pythonScript =  "python/tflite_segmentation.py";
            }else if(modelName.equals("posenet_mobilenet_100_257_257.tflite")){
                pythonScript =  "python/tflite_pos_classification.py";
            }
            else{
                pythonScript =  "python/tflite_classification.py";
            }
        }else if(modelType.equals("server")){
            pythonScript =  "python/all_models.py";
        }
        else {
            if(modelName.equals("face_detection.pb")){
                pythonScript =  "python/face_detection.py";
            }else{
                pythonScript =  "python/regular_classification.py";
            }
        }
        return pythonScript;
    }

    //
    //Function that execute the python script matching the model deployed
    public List<String> executePython(String modelName,  String labelName, String file, String pythonScript, String fileType, String ipController, int portController, String location) throws IOException, InterruptedException {
        List<String> pythonOutputList = new ArrayList<String>();
        String file_path = "files/" +  file + fileType;
        String model_path = "models/" + modelName; //change
        String label_path = "labels/" + labelName; //change
        System.out.println(file_path);
        System.out.println(model_path);
        System.out.println(label_path);

        //
        //Get the execution time
        long startTime = System.nanoTime();
        Process p = Runtime.getRuntime().exec("python3 "+ pythonScript + " " + file_path + " " + label_path + " " + model_path);
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;

        //
        //Get the output from the python script
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        int i = 0;
        String output = "";
        while ((output = in.readLine()) != null) {
            pythonOutputList.add(output);
            System.out.println(output);
            System.out.println(pythonOutputList.get(i));
            i++;
        }

        String OS = System.getProperty("os.name").toLowerCase();
        chainMLClient controller = new chainMLClient(ipController, portController);
        controller.defineOrder("Feedback from :" + OS +" \nExecution time in microseconde : " + timeElapsed / 1000 + " \n" + " Bytes"  ); //Send basic feedback to controller
        totalExecutionTime += timeElapsed;
        System.out.println(i);
        controller.sendExecTime(Double.parseDouble(pythonOutputList.get(i-1))/1048576, location+"m");   //Send memory consumption, "m" indicate that it's memory consumption time to controller
        controller.sendExecTime(timeElapsed, location);

        controller.shutdown();
        return pythonOutputList  ;
    }

    //
    //Split video into frames and send it to the processImage function
    public void ProcessVideo(String filePath, String nextDevice, String ipController, int portController, String modelName, String labelName, String condition, String condition2, String action, String action2, String modelType, String applicationType, String location) throws IOException, InterruptedException, ExecutionException {
        Runtime runtime = Runtime.getRuntime();

        FFmpegFrameGrabber g = new FFmpegFrameGrabber( "files/" + filePath);
        Java2DFrameConverter c = new Java2DFrameConverter();
        g.start();
        Frame frame;
        long memory = runtime.totalMemory() - runtime.freeMemory();
        String imageName = "";
        while ((frame = g.grabImage()) != null) {
            imageName = "image"+System.nanoTime();
            ImageIO.write(c.convert(frame), "png", new File(
                    "files/" + imageName +".png"));
                ProcessImage(imageName,".png",nextDevice, ipController, portController, modelName, labelName, condition, condition2, action, action2, modelType, applicationType, location);
        }
        g.stop();
        chainMLClient controller = new chainMLClient(ipController, portController);

        controller.defineOrder("\nExecution time in milliseconds : \n Memory available : " + memory + " Bytes");
        controller.shutdown();

    }

    //
    //Used to do tests
    public static void main(String[] args) throws InterruptedException, IOException, ExecutionException {
        Processing process = new Processing();
       //process.ProcessVideo("video_test.mp4", "end","192.168.1.70",50052, "resnet_model", "label.txt", "regular");
        process.ProcessImage("index",".jpeg","end", "192.168.0.143",50052,"deeplabv3_257.tflite","labeltest.txt", "dog", "null","drop","drop","server", "pipeline", "");
    }
}



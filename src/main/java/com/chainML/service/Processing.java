package com.chainML.service;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Processing {
    public void ProcessImage(String fileID, String fileType, String nextDevice) throws IOException, InterruptedException {
        String s = null;
        Process p = Runtime.getRuntime().exec("python3 python/tflite.py img/"+ fileID + fileType + " img/label.txt img/model.tflite");
        //Process p = Runtime.getRuntime().exec("python3 python/tflite.py img/image.jpg img/label.txt img/model.tflite");

        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((s = in.readLine()) != null) {
            System.out.println(s);
        }

        if(nextDevice.equals("Linux")){
            chainMLClient client = new chainMLClient("192.168.1.73", 50051);
            client.uploadFile("img/"+ fileID+fileType, "image");
            client.shutdown();
        } else if(nextDevice.equals("RPI"))
        {
            chainMLClient client = new chainMLClient("192.168.1.75", 50051);
            client.uploadFile("img/"+ fileID +fileType, "image");
            client.shutdown();
        }
        else if(nextDevice.equals("Android"))
        {
            chainMLClient client = new chainMLClient("192.168.1.67", 50051);
            client.uploadFile("img/"+ fileID+fileType, "image");
            client.shutdown();
        }
        else if(nextDevice.equals("end"))
        {
            System.out.println("end");
        }
        else{
            System.out.println("unknown device");
        }


    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Processing process = new Processing();
        process.ProcessImage("image",".PNG", "end");


    }
}



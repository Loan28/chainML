package com.chainML.service;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Processing {
    public void ProcessImage(String fileID, String fileType, String nextDevice) throws IOException, InterruptedException {
        Runtime runtime = Runtime.getRuntime();

        String s = null;
        chainMLClient controller = new chainMLClient("192.168.1.70", 50052);
        long startTime = System.nanoTime();
        long memory = runtime.totalMemory();
        Process p = Runtime.getRuntime().exec("python3 python/tflite.py img/"+ fileID + fileType + " img/label.txt img/mobilenet.tflite");
        //Process p = Runtime.getRuntime().exec("python3 python/tflite.py img/image.jpg img/label.txt img/model.tflite");
        long memoryLeft = memory - runtime.freeMemory();
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        controller.defineOrder("\nExecution time in milliseconds : " + timeElapsed / 1000000 + "\n" + "Memory left : " + memoryLeft + " Bytes");
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((s = in.readLine()) != null) {
            System.out.println(s);
        }
        if(!"end".equals(nextDevice))
        {
            chainMLClient client = new chainMLClient(nextDevice, 50051);
            client.uploadFile("img/"+ fileID+fileType, "image");
            client.shutdown();
        }
    }

    public void ProcessVideo(String filePath, String nextDevice) throws FrameGrabber.Exception, IOException, InterruptedException, ExecutionException {

        FFmpegFrameGrabber g = new FFmpegFrameGrabber("img/"+filePath);
        Java2DFrameConverter c = new Java2DFrameConverter();
        g.start();
        Frame frame;
        while ((frame = g.grabImage()) != null) {
            if(frame.keyFrame){
            ImageIO.write(c.convert(frame), "png", new File("img/image.png"));
                ProcessImage("image",".png","end");
            }
        }
        g.stop();
    }


    public static void main(String[] args) throws InterruptedException, IOException, FrameGrabber.Exception, ExecutionException {
        Processing process = new Processing();
        process.ProcessVideo("video-test-objects.mp4", "end");
    }
}



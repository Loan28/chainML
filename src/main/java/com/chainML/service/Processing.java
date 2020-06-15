package com.chainML.service;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Processing {
    public void ProcessImage(String imageID) throws IOException {
        String s = null;
        Process p = Runtime.getRuntime().exec("python3 /home/loan/IdeaProjects/chainML/python/tflite.py /home/loan/IdeaProjects/chainML/img/"+ imageID +".jpeg /home/loan/IdeaProjects/chainML/img/label.txt /home/loan/IdeaProjects/chainML/img/model.tflite");
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((s = in.readLine()) != null) {
            System.out.println(s);
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        Processing process = new Processing();
        process.ProcessImage("image");


    }
}



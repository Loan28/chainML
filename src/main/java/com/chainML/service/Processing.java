package com.chainML.service;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Processing {
    public void ProcessImage(String imageID) throws IOException {
        String s = null;
        Process p = Runtime.getRuntime().exec("python3 python/tflite.py img/"+ imageID +".jpeg img/label.txt img/model.tflite");
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



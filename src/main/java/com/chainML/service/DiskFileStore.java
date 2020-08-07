package com.chainML.service;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DiskFileStore implements FileStore {
    private String fileFolder;
    private ConcurrentMap<String, FileMetada> data;

    public DiskFileStore(String fileFolder) {
        this.fileFolder = fileFolder;
        this.data = new ConcurrentHashMap<>(0);
    }
    //
    //Function to save file on a folder specified in the args
    @Override
    public String Save(String fileType, ByteArrayOutputStream fileData, String fileID) throws IOException {
        String filePath = String.format("%s/%s%s", fileFolder, fileID, fileType);
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        fileData.writeTo(fileOutputStream);
        fileOutputStream.close();

        FileMetada metadata = new FileMetada(fileType, filePath);
        data.put(fileID, metadata);
        return fileID;
    }
}

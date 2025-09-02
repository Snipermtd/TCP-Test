package com.example.tcptest;

import  android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

//Class that will receive a file and store it in private storage
public class FileServerThread implements Runnable {
    private final Context context;
    private MainActivity mainActivity;
    public FileServerThread(Context context, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.context = context;
    }


    @Override
    public void run() {
        final int port = 9090;
        //Creates a server socket on port 9090 and listens for requests
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            mainActivity.updateStatus("Server is listening on port " + port);

            while (!Thread.currentThread().isInterrupted()) {
                //Blocks until a client connects - once connected, a socket representing the client is returned
                //Try-with-resources will automatically close all connections at the appropriate time
                try (Socket clientSocket = serverSocket.accept()) {
                    //Get input from the client - form is byte stream
                    InputStream input = clientSocket.getInputStream();
                    //BufferedReader handles buffering of the input stream for efficient reading - convention to wrap a reader with BufferedReader
                    //Only the header is being buffered
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    //Files are expected to have a header in the form of fileName:fileSize
                    String header = reader.readLine();
                    if (header == null || !header.contains(":"))
                        System.out.println("Invalid header.");
                    //Separate the header
                    String[] parts = header.split(":");
                    String fileName = parts[0];
                    int fileSize = Integer.parseInt(parts[1]);

                    //Create a File object - Path is private storage path + filename
                    File file = new File(context.getFilesDir(), fileName);
                    //FileOutputStream writes data into a File object
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        //4096 byte buffer means we read 4096 bytes at a time
                        byte[] buffer = new byte[4096];
                        int totalRead = 0;
                        //This loop keeps track of how many bytes have been read and writes the data into the File in 4096 byte chunks
                        while (totalRead < fileSize) {
                            int bytes_read = input.read(buffer);
                            if (bytes_read == -1) break;
                            fos.write(buffer, 0, bytes_read);
                            totalRead += bytes_read;
                        }
                        mainActivity.updateStatus("File received: " + file.getAbsolutePath());
                    } catch (Exception e) {
                        mainActivity.updateStatus(e.getMessage());
                    }
                } catch (IOException e) {
                    mainActivity.updateStatus(e.getMessage());
                }
            }
        } catch (IOException e) {
            mainActivity.updateStatus(e.getMessage());
        }
    }
}

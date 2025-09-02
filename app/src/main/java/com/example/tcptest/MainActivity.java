package com.example.tcptest;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private TextView viewFiles;
    private TextView serverStatusTextView; // TextView for logging status
    private ServerSocket serverSocket;
    private Thread serverThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        serverStatusTextView = findViewById(R.id.server_status);
        Button getStringButton = findViewById(R.id.send_button);
        Button viewPrivateStorageButton = findViewById(R.id.private_storage_button);
        viewFiles = findViewById(R.id.viewFiles);
        updateStatus("App started. Click button to start server.");

        viewPrivateStorageButton.setOnClickListener(v -> {
            File[] fileList = getFilesDir().listFiles();  // use the Activity context

            if (fileList == null || fileList.length == 0) {
                viewFiles.setText("No files found in private storage.");
                return;
            }

            StringBuilder sb = new StringBuilder("Files in private storage:\n");
            for (File f : fileList) {
                sb.append(f.getName().trim()).append("\n");
            }
            for (File f : fileList) {
                Log.d("PrivateFiles", "File path: " + f.getAbsolutePath());
            }
            runOnUiThread(() -> viewFiles.setText(sb.toString())); // update TextView
        });



        getStringButton.setOnClickListener(v -> {
            if (serverThread != null && serverThread.isAlive()) {
                Toast.makeText(this, "Server is already running.", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Starting server...", Toast.LENGTH_SHORT).show();
            //this.serverThread = new Thread(new ServerThread());
            //this.serverThread.start();
            this.serverThread = new Thread(new FileServerThread(getApplicationContext(), this));
            this.serverThread.start();
        });
    }

    // Helper method to update the status TextView and log to console
    public void updateStatus(final String message) {
        runOnUiThread(() -> {
            // Append new status to the existing text instead of replacing it
            String currentStatus = serverStatusTextView.getText().toString();
            serverStatusTextView.setText(currentStatus + "\n> " + message);
            System.out.println(message);
        });
    }
        class ServerThread implements Runnable {
            @Override
            public void run() {
                final int port = 9090;
                try {
                    serverSocket = new ServerSocket(port);
                    updateStatus("Server is listening on port " + port);

                    while (!Thread.currentThread().isInterrupted()) {
                        Socket clientSocket = null;
                        try {
                            clientSocket = serverSocket.accept();
                            updateStatus("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            final String receivedString = input.readLine();

                            if (receivedString != null && !receivedString.isEmpty()) {
                                updateStatus("Read successful. String: '" + receivedString + "'");

                            } else {
                                updateStatus("Warning: Received an empty or null string.");
                            }

                        } catch (IOException e) {
                            updateStatus("Error during connection: " + e.getMessage());
                        } finally {
                            if (clientSocket != null) {
                                try {
                                    clientSocket.close();
                                    updateStatus("Client socket closed. Listening again...");
                                } catch (IOException e) {
                                    updateStatus("Error closing client socket: " + e.getMessage());
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    updateStatus("Server Error (could not bind to port?): " + e.getMessage());
                } finally {
                    updateStatus("Server thread finished.");
                }
            }
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
                if (serverThread != null) {
                    serverThread.interrupt();
                }
            } catch (IOException e) {
                updateStatus(e.getMessage());
            }
        }
    }
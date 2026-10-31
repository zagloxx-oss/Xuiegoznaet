package com.minimilitary3d;

import com.badlogic.gdx.Gdx;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkManager {
    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    public volatile String lastReceivedData = "";
    public volatile boolean isConnected = false;
    public volatile String errorMsg = "";

    public void startServer() {
        new Thread(() -> {
            try {
                Gdx.app.log("NET", "Создаем сервер на порту 7777...");
                serverSocket = new ServerSocket(7777);
                Gdx.app.log("NET", "Сервер создан. Ждем клиента...");
                socket = serverSocket.accept(); 
                socket.setTcpNoDelay(true); // ⚡ ОТКЛЮЧАЕМ БУФЕРИЗАЦИЮ
                Gdx.app.log("NET", "Клиент подключился!");
                setupStreams();
            } catch (Exception e) {
                errorMsg = "Server Error: " + e.getMessage();
                Gdx.app.error("NET", errorMsg);
            }
        }).start();
    }

    public void connectToServer(String ip) {
        new Thread(() -> {
            try {
                Gdx.app.log("NET", "Подключаемся к " + ip + ":7777...");
                socket = new Socket(ip, 7777);
                socket.setTcpNoDelay(true); // ⚡ ОТКЛЮЧАЕМ БУФЕРИЗАЦИЮ
                Gdx.app.log("NET", "Успешно подключились!");
                setupStreams();
            } catch (Exception e) {
                errorMsg = "Connect Error: " + e.getMessage();
                Gdx.app.error("NET", errorMsg);
            }
        }).start();
    }

    private void setupStreams() throws Exception {
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        isConnected = true;
        
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    lastReceivedData = line;
                }
            } catch (Exception e) {
                isConnected = false;
                errorMsg = "Read Error: " + e.getMessage();
                Gdx.app.error("NET", errorMsg);
            }
        }).start();
    }

    public void sendData(String data) {
        if (out != null && isConnected) {
            out.println(data);
        }
    }

    public void dispose() {
        isConnected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
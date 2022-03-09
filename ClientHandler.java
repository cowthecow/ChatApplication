package com.laserinfinite.java;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clients = new ArrayList<>();
    private Socket socket;

    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = bufferedReader.readLine();

            clients.add(this);
            broadcastMessage("SERVER:" + username + " joined the chat!");
        } catch (IOException e) {
            broadcastMessage("SERVER:" + username + " got kicked out of the chat!");
            closeAll(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();

                broadcastMessage(messageFromClient);
            } catch (IOException e) {
                closeAll(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    public String getUsername() {
        return this.username;
    }

    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clients) {
            try {
                if (!clientHandler.getUsername().equals(username)) {
                    clientHandler.bufferedWriter.write(message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException | NullPointerException e) {
                closeAll(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler() {
        clients.remove(this);
        broadcastMessage("SERVER:" + username + " has left the chat!");
    }

    public void closeAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

//Commands:
//    /kick user (kicks someone out of group chat)
//    /mute user (mutes someone by disabling their textfield)
//    /

package com.laserinfinite.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Application {

    private double startAngle = 0;
    private double arcAngle = 0;
    private boolean angleIncreasing = true;

    public void startServer(ServerSocket serverSocket) {
        System.out.println("Server started");
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                try {
                    while (!serverSocket.isClosed()) {
                        Socket socket = serverSocket.accept();
                        System.out.println("A new client has connected!");
                        ClientHandler clientHandler = new ClientHandler(socket);
                        new Thread(clientHandler).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("server.fxml"));
        primaryStage.setTitle("Server GUI");
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.show();
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            Server server = new Server();
            server.startServer(serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        launch(args);

    }
}

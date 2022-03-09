package com.laserinfinite.java;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Client extends Application implements Initializable {

    private ArrayList<Message> displayedMessages = new ArrayList<>();
    private static ArrayList<HBox> children = new ArrayList<>();

    private static String messageEntered = null;

    private static ArrayList<String> unreadMessages = new ArrayList<>();

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private int formatBackwards = 0;
    private static String clientUsername;

    private static Object susLock = new Object();

    @FXML
    ScrollPane scrollPane;

    @FXML
    VBox messages = new VBox();

    @FXML
    TextField messageField;

    @FXML
    Label displayedUsername;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        displayedUsername.setText(clientUsername);

        messages.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                scrollPane.setVvalue((Double) newValue);
            }
        });

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(children.size() > 0) messages.getChildren().add(children.get(0));
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(;;) {
                    try {
                        Robot sus = new Robot();
                        sus.keyRelease(java.awt.event.KeyEvent.VK_E);
                        Thread.sleep(200);
                    }catch (AWTException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public synchronized void startClient(Socket socket) {
        Scanner sus = new Scanner(System.in);
        clientUsername = sus.nextLine();

        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            closeAll(socket, bufferedReader, bufferedWriter);
        }
    }

    @FXML
    public void onMouseMoved() {
        System.out.println("updating..");
        if(unreadMessages.size() > 0) {
            while(unreadMessages.size() > 0) {
                receiveMessageGUI(unreadMessages.get(0));
                unreadMessages.remove(0);
            }
        }
    }

    @FXML
    public void onEnterClicked() {
        pushMessageGUI(messageField.getText());
        notifyMessenger();
    }

    @FXML
    public void onKeyPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            pushMessageGUI(messageField.getText());
            notifyMessenger();
        }
    }

    public void notifyMessenger() {
        synchronized (susLock) {
            susLock.notifyAll();
        }
    }

    @FXML
    public void sendMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    bufferedWriter.write(clientUsername);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                    while (socket.isConnected()) {

                        synchronized (susLock) {
                            susLock.wait();
                        }

                        bufferedWriter.write(clientUsername + ":" + messageEntered);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();

                        Calendar cal = Calendar.getInstance();
                        displayedMessages.add(new Message("You", messageEntered, cal.getTime().getHours(), cal.getTime().getMinutes()));

                        messageEntered = null;
                    }
                } catch (IOException | InterruptedException e) {
                    closeAll(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    @FXML
    public synchronized void listenForMessages() {
        new Thread(new Runnable() {
            @Override
            public synchronized void run() {
                String msgFromGroupChat;

                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        unreadMessages.add(msgFromGroupChat);
                    } catch (IOException e) {
                        closeAll(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void pushMessageGUI(String message) {
        HBox textBox = new HBox(5);
        textBox.setAlignment(Pos.CENTER_RIGHT);
        textBox.setPadding(new Insets(5, 5, 5, 10));

        Text username = new Text(clientUsername);
        Text text = new Text(message);
        text.setWrappingWidth(200);

        TextFlow userFlow = new TextFlow(username);
        userFlow.setStyle("-fx-color: rgb(255,255,255);" +
                "-fx-background-color: rgb(101, 154, 142);" +
                "-fx-background-radius: 5px");
        userFlow.setPadding(new Insets(5, 10, 5, 10));

        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-color: rgb(255,255,255);" +
                "-fx-background-color: rgb(200, 215, 189);" +
                "-fx-background-radius: 5px");
        textFlow.setPadding(new Insets(5, 10, 5, 10));
        textFlow.setMaxWidth(250);

        username.setFill(Color.color(0, 0, 0));
        text.setFill(Color.color(0, 0, 0));

        textBox.getChildren().add(textFlow);
        textBox.getChildren().add(userFlow);

        messages.getChildren().add(textBox);

        messageEntered = message;

        messageField.clear();
    }

    public void receiveMessageGUI(String received) {
        int firstColon = received.indexOf(':');
        String user = received.substring(0, firstColon);
        String message = received.substring(firstColon+1);

        HBox textBox = new HBox(5);
        textBox.setAlignment(Pos.CENTER_LEFT);
        textBox.setPadding(new Insets(5, 5, 5, 10));

        Text username = new Text(user);
        Text text = new Text(message);
        text.setWrappingWidth(200);

        TextFlow userFlow = new TextFlow(username);
        userFlow.setStyle("-fx-color: rgb(255,255,255);" +
                "-fx-background-color: rgb(101, 154, 142);" +
                "-fx-background-radius: 5px");
        userFlow.setPadding(new Insets(5, 10, 5, 10));

        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-color: rgb(255,255,255);" +
                "-fx-background-color: rgb(200, 215, 189);" +
                "-fx-background-radius: 5px");
        textFlow.setPadding(new Insets(5, 10, 5, 10));
        textFlow.setMaxWidth(250);

        username.setFill(Color.color(0, 0, 0));
        text.setFill(Color.color(0, 0, 0));

        textBox.getChildren().add(userFlow);
        textBox.getChildren().add(textFlow);

        System.out.println("added a textobx");
        messages.getChildren().add(textBox);

        children.add(textBox);
    }

    public void closeAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) bufferedReader.close();
            if (bufferedWriter != null) bufferedWriter.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("client.fxml"));
        primaryStage.setTitle("Client GUI");
        primaryStage.setScene(new Scene(root, 400, 575));
        primaryStage.show();
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(InetAddress.getLocalHost().getHostAddress(), 1234);

        Client client = new Client();

        client.startClient(socket);
        client.listenForMessages();
        client.sendMessages();

        System.out.println("launching bitches");
        launch(args);
    }


}


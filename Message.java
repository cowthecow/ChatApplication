package com.laserinfinite.java;

public class Message {

    private final String sender;
    private String message;

    private final int hours;
    private final int minutes;

    public Message(String sender, String message, int hours, int minutes) {
        this.sender = sender;
        this.message = message;
        this.hours = hours;
        this.minutes = minutes;
    }

    public void appendToMessage(String appended) {
        this.message += "\n";
        this.message += appended;
        System.out.println(this.message);
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public boolean msgEquals(Message anotherMessage) {
        return this.minutes == anotherMessage.getMinutes() && this.hours == anotherMessage.getHours() && this.getSender().equals(anotherMessage.getSender());
    }
}

package com.qunar.qchat.consumeevent;

public interface MessageHandler {

    void handle(String key, String msg);
}

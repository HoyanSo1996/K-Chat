package com.pccw.client.service;

import com.pccw.common.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Class ClientConnectServerThread
 *
 * @author KennySo
 * @version 1.0
 */
public class ClientConnectServerThread extends Thread {

    // 该线程需要持有socket
    private final Socket socket;

    public ClientConnectServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        // 客户端线程 等待读取从服务器端回复的消息
        while (true) {
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) ois.readObject();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}

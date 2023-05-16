package com.pccw.server.service;

import com.pccw.common.Message;

import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Class ServerConnectClientThread
 *
 * @author KennySu
 * @date 2023/5/16
 */
public class ServerConnectClientThread extends Thread {

    private final String userId;  // 连接到服务端的用户id

    private final Socket socket;

    public ServerConnectClientThread(String userId, Socket socket) {
        this.userId = userId;
        this.socket = socket;
    }


    @Override
    public void run() {
        System.out.println("log: { 服务器端与用户 " + userId + " 成功建立连接, 等待读取信息. }");
        while (true) {
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) ois.readObject();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

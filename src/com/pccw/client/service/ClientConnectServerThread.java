package com.pccw.client.service;

import com.pccw.common.CommonUtils;
import com.pccw.common.Message;
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

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        // 客户端线程 等待读取从服务器端回复的消息
        try {
            while (true) {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) ois.readObject();

                // (1)判断消息类型是否是 获取在线用户
                if (message.getMsgType().equals(CommonUtils.MSG.RET_ONLINE_USERS)) {
                    String onlineUsers = message.getContent();
                    String[] users = onlineUsers.split(" ");
                    System.out.println("\n=========== 在线用户列表 ===========");
                    for (String user : users) {
                        System.out.println("用户: " + user);
                    }
                } else {
                    // TODO 其他业务消息
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}

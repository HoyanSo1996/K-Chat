package com.pccw.server.service;

import com.pccw.common.CommonUtils;
import com.pccw.common.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

                // (1)判断消息类型是否是 获取在线用户
                if (message.getMsgType().equals(CommonUtils.MSG.GET_ONLINE_USERS)) {
                    System.out.println("log: { " + userId + " 请求在线用户列表.}");

                    Message responseMsg = new Message();
                    responseMsg.setReceiver(message.getSender());
                    responseMsg.setContent(ServerThreadManagerService.getAllOnlineUsers());
                    oos.writeObject(responseMsg);

                } else {
                    // TODO 其他业务消息
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

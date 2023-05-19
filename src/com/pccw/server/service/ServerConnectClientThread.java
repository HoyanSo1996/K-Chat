package com.pccw.server.service;

import com.pccw.common.CommonUtils;
import com.pccw.common.Message;
import com.pccw.utils.DateUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

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

        try {
            while (true) {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) ois.readObject();

                // (1)判断消息类型是否是 获取在线用户
                if (message.getMsgType().equals(CommonUtils.MSG.GET_ONLINE_USERS)) {
                    System.out.println("log: { " + userId + " 请求在线用户列表.}");

                    Message responseMsg = new Message();
                    responseMsg.setReceiver(message.getSender());
                    responseMsg.setMsgType(CommonUtils.MSG.RET_ONLINE_USERS);
                    responseMsg.setContent(ServerThreadManagerService.getAllOnlineUsers());

                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(responseMsg);

                // (2)判断消息类型是否是 退出登录
                } else if (message.getMsgType().equals(CommonUtils.MSG.LOGOUT)) {
                    Message responseMsg = new Message();
                    responseMsg.setReceiver(message.getSender());
                    responseMsg.setMsgType(CommonUtils.MSG.LOGOUT_SUCCEEDED);

                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(responseMsg);
                    System.out.println("log: { " + userId + " 退出登录.}");

                    // 关闭socket
                    socket.close();
                    // 从线程管理器中移除对应的线程
                    ServerThreadManagerService.removeThread(userId);
                    // 跳出 while 循环, 结束线程
                    break;

                // (3)判断消息类型是否是 普通消息
                } else if (message.getMsgType().equals(CommonUtils.MSG.TO_ONE_MESSAGE)) {
                    ServerConnectClientThread serverThread = ServerThreadManagerService.getThread(message.getReceiver());

                    // 如果线程不存在, 代表用户不在线, 发送一条消息表示用户未上线
                    if (serverThread == null) {
                        System.out.println("log: { " + "[" + message.getTime() + "] " +
                                message.getSender() + " 对 " + message.getReceiver() + " 发送消息: " + "\" "+ message.getContent() + "\" "+ "失败, " +
                                message.getReceiver() + " 不在线/不存在.}");

                        Message responseMsg = new Message();
                        responseMsg.setReceiver(message.getReceiver());
                        responseMsg.setContent(message.getContent());
                        responseMsg.setTime(DateUtils.getDataTime());
                        responseMsg.setMsgType(CommonUtils.MSG.TO_ONE_MESSAGE_FAILED);

                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(responseMsg);

                    // 线程存在, 向线程所在socket转发消息
                    } else {
                        ObjectOutputStream oos = new ObjectOutputStream(serverThread.socket.getOutputStream());
                        oos.writeObject(message);   // 如果用户不存在, 可以保存到数据库, 这样就可以实现离线留言

                        System.out.println("log: { " + "[" + message.getTime() + "] " +
                                message.getSender() + " 对 " + message.getReceiver() + " 发送消息: " + "\"" + message.getContent() + "\" }");
                    }

                } else {
                    // TODO 其他业务消息
                }
            }

        } catch (SocketException e) {
            // 如果客户端暴力关闭, 则客户端这边也要关闭socket, 线程管理器要删除线程
            System.out.println("log: { " + userId + " 异常退出登录.}");

            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            ServerThreadManagerService.removeThread(userId);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

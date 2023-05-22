package com.pccw.server.service;

import com.pccw.common.CommonUtils;
import com.pccw.common.Message;
import com.pccw.utils.DateUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;

import static com.pccw.common.CommonUtils.*;

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
                if (message.getMsgType().equals(MSG.GET_ONLINE_USERS)) {
                    System.out.println("log: { " + userId + " 请求在线用户列表.}");

                    Message responseMsg = new Message();
                    responseMsg.setReceiver(message.getSender());
                    responseMsg.setMsgType(MSG.RET_ONLINE_USERS);
                    responseMsg.setContent(ServerThreadManagerService.getAllOnlineUsers());

                    // 转发消息给原客户端
                    sendMessageToClient(socket.getOutputStream(), responseMsg);

                // (2)判断消息类型是否是 退出登录
                } else if (message.getMsgType().equals(MSG.LOGOUT)) {
                    Message responseMsg = new Message();
                    responseMsg.setReceiver(message.getSender());
                    responseMsg.setMsgType(MSG.LOGOUT_SUCCEEDED);

                    // 转发消息给原客户端
                    sendMessageToClient(socket.getOutputStream(), responseMsg);
                    System.out.println("log: { " + userId + " 退出登录.}");

                    // 关闭socket
                    socket.close();
                    // 从线程管理器中移除对应的线程
                    ServerThreadManagerService.removeThread(userId);
                    // 跳出 while 循环, 结束线程
                    break;

                // (3)判断消息类型是否是 私聊消息
                } else if (message.getMsgType().equals(MSG.TO_ONE_MESSAGE)) {
                    // 获取目标客户端的socket
                    ServerConnectClientThread serverThread = ServerThreadManagerService.getThread(message.getReceiver());

                    // 如果线程不存在, 代表用户不在线, 发送一条消息表示用户未上线
                    // todo: 如果用户不存在, 可以保存到数据库, 这样就可以实现离线留言
                    if (serverThread == null) {
                        System.out.println("log: { " + "[" + message.getTime() + "] " +
                                message.getSender() + " 对 " + message.getReceiver() + " 发送消息: " + "\" " + message.getContent() + "\" " + "失败, " +
                                message.getReceiver() + " 不在线/不存在.}");

                        Message responseMsg = new Message();
                        responseMsg.setReceiver(message.getReceiver());
                        responseMsg.setContent(message.getContent());
                        responseMsg.setTime(DateUtils.getDataTime());
                        responseMsg.setMsgType(MSG.TO_ONE_MESSAGE_FAILED);

                        // 转发消息给原客户端
                        sendMessageToClient(socket.getOutputStream(), responseMsg);

                        // 线程存在, 向线程所在socket转发消息
                    } else {
                        /*
                            Tips：这一层 oos 的 new ObjectOutputStream(xxx) 与上面不同, 不能直接直接
                                  oos = new ObjectOutputStream(serverThread.socket.getOutputStream(), 否则会出现异常.
                         */
                        // 转发消息给指定客户端.
                        message.setMsgType(MSG.TO_ONE_MESSAGE_SUCCEEDED);
                        sendMessageToClient(serverThread.socket.getOutputStream(), message);

                        System.out.println("log: { " + "[" + message.getTime() + "] " +
                                message.getSender() + " 对 " + message.getReceiver() + " 发送消息: " + "\"" + message.getContent() + "\" }");
                    }

                // (4)判断消息类型是否是 群发消息.
                } else if(message.getMsgType().equals(MSG.TO_ALL_MESSAGE)) {
                    // 在线程管理器中获取所有在线用户(排除自己), 然后遍历发送群发消息
                    Map<String, ServerConnectClientThread> threadManager = ServerThreadManagerService.getThreadManager();
                    for (String onlineUserId : threadManager.keySet()) {
                        if (!onlineUserId.equals(message.getSender())) {
                            sendMessageToClient(threadManager.get(onlineUserId).socket.getOutputStream(), message);
                        }
                    }
                    System.out.println("log: { " + "[" + message.getTime() + "] " +
                            message.getSender() + " 群发了消息: " + "\"" + message.getContent() + "\" }");

                // (5)判断消息类型是否是 发送文件消息.
                } else if(message.getMsgType().equals(MSG.TO_ONE_FILE_MESSAGE)) {
                    // 获取目标客户端的socket
                    ServerConnectClientThread serverThread = ServerThreadManagerService.getThread(message.getReceiver());

                    // 如果线程不存在, 代表用户不在线, 发送一条消息表示用户未上线
                    if (serverThread == null) {
                        System.out.println("log: { " + "[" + message.getTime() + "] " +
                                message.getSender() + " 对 " + message.getReceiver() + " 发送文件: " + "\" " + message.getFileName() + "\" " + "失败, " +
                                message.getReceiver() + " 不在线/不存在.}");

                        Message responseMsg = new Message(
                                null,
                                message.getReceiver(),
                                message.getContent(),
                                DateUtils.getDataTime(),
                                MSG.TO_ONE_FILE_MESSAGE_FAILED);

                        // 转发消息给原客户端
                        sendMessageToClient(socket.getOutputStream(), responseMsg);

                        // 线程存在, 向线程所在socket转发消息
                    } else {
                        // 转发消息给指定客户端.
                        message.setMsgType(MSG.TO_ONE_FILE_MESSAGE_SUCCEEDED);
                        sendMessageToClient(serverThread.socket.getOutputStream(), message);

                        System.out.println("log: { " + "[" + message.getTime() + "] " +
                                message.getSender() + " 对 " + message.getReceiver() + " 发送文件: " + "\"" + message.getFileName() + "\" }");
                    }

                } else {
                    // TODO 拓展其他业务消息
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


    /**
     * 转发消息给客户端
     * @param outputStream 根据具体转发对象设置的输入流
     * @param responseMsg 根据具体业务设置的消息
     */
    public void sendMessageToClient(OutputStream outputStream, Message responseMsg) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(responseMsg);
    }
}

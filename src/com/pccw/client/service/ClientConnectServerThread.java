package com.pccw.client.service;

import com.pccw.common.Message;
import com.pccw.utils.DateUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;

import static com.pccw.common.CommonUtils.MSG;

/**
 * Class ClientConnectServerThread
 *
 * @author KennySo
 * @version 1.0
 */
public class ClientConnectServerThread extends Thread {

    // 该线程需要持有socket
    private final Socket socket;

    private final String userId;

    public ClientConnectServerThread(String userId, Socket socket) {
        this.userId = userId;
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        // 客户端线程 等待读取从服务器端回复的消息
        while (true) {
            try {
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) ois.readObject();

                // (1)判断消息类型是否是 获取在线用户
                if (message.getMsgType().equals(MSG.RET_ONLINE_USERS)) {
                    String onlineUsers = message.getContent();
                    String[] users = onlineUsers.split(" ");
                    System.out.println("\n=========== 在线用户列表 ===========");
                    for (String user : users) {
                        System.out.println("用户: " + user);
                    }

                // (2)判断消息类型是否是 用户正常登出
                } else if (message.getMsgType().equals(MSG.LOGOUT_SUCCEEDED)) {
                    // 关闭socket
                    socket.close();
                    // 移除线程管理器中的对应的线程
                    ClientThreadManagerService.removeThread(userId);
                    break;

                // (3.1)判断消息类型是否是 私聊消息成功
                } else if (message.getMsgType().equals(MSG.TO_ONE_MESSAGE_SUCCEEDED)) {
                    System.out.println("[" + message.getTime() + "] " +
                            "收到 " + message.getSender() + " 的消息: " + "\"" + message.getContent() + "\"");

                // (3.2)判断消息类型是否是 私聊消息失败
                } else if (message.getMsgType().equals(MSG.TO_ONE_MESSAGE_FAILED)) {
                    System.out.println("[" + message.getTime() + "] " +
                            "发送消息: " + "\"" + message.getContent() + "\"" + " 失败, " + message.getReceiver() + " 不在线/不存在.");

                // (4)判断消息类型是否是 私聊消息成功
                } else if (message.getMsgType().equals(MSG.TO_ALL_MESSAGE)) {
                    System.out.println("[" + message.getTime() + "] " +
                            "收到 " + message.getSender() + " 群发的消息: " + "\"" + message.getContent() + "\"");

                // (5.1)判断消息类型是否是 发送文件消息成功
                } else if (message.getMsgType().equals(MSG.TO_ONE_FILE_MESSAGE_SUCCEEDED)) {
                    // 设置文件保存路径
                    String filePath = "C:\\WorkSpace\\test\\" + message.getFileName();

                    try(FileOutputStream fos = new FileOutputStream(filePath)) {
                        fos.write(message.getFileBytes(), 0, message.getFileLen());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println("[" + message.getTime() + "] " +
                            "收到 " + message.getSender() + " 的文件: " + "\"" + message.getFileName() + "\"");

                // (5.1)判断消息类型是否是 发送文件消息失败
                } else if (message.getMsgType().equals(MSG.TO_ONE_FILE_MESSAGE_FAILED)) {
                    System.out.println("[" + message.getTime() + "] " +
                            "发送文件: " + "\"" + message.getFileName() + "\"" + " 失败, " + message.getReceiver() + " 不在线/不存在.");

                } else {
                    // TODO 其他业务消息
                }

            } catch (SocketException e) {
                // 如果服务器挂机, 由于循环中的 ois.readObject() 正在阻塞状态, 那么读取会报错, 直接跳到这一步.
                System.out.println("[" + DateUtils.getDataTime() + "] " + "服务器异常, 断开连接... ");

                try {
                    // 关闭socket
                    socket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                // 移除线程管理器中的对应的线程
                ClientThreadManagerService.removeThread(userId);

                // 退出循环
                break;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

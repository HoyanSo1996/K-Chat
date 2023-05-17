package com.pccw.server.service;

import com.pccw.common.CommonUtils;
import com.pccw.common.Message;
import com.pccw.common.User;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class server
 *
 * @author KennySu
 * @date 2023/5/16
 */
public class ServerService {

    private ServerSocket serverSocket;

    private final ServerThreadManagerService serverThreadManagerService = new ServerThreadManagerService();

    // 创建一个集合, 充当临时DataBase, 存储用户名和密码
    // 这里使用ConcurrentHashMap,处理并发的集合,没有线程安全问题
    private static ConcurrentHashMap<String, User> userData = new ConcurrentHashMap();

    static {
        userData.put("100", new User("100", "123456"));
        userData.put("Kenny", new User("Kenny", "123456"));
        userData.put("Cody", new User("Cody", "123456"));
        userData.put("John", new User("John", "123456"));
    }


    public ServerService() {
        try {
            System.out.println("服务器端在9999端口监听...");
            serverSocket = new ServerSocket(9999);

            // 当和某个客户端建立连接后,会开启一个线程,然后循环等待下一个客户端的连接...
            while (true) {
                Socket socket = serverSocket.accept();
                // 1.接收客户端登录时发送的user消息
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                User user = (User) ois.readObject();

                // 2.创建返回给客户端的信息
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                Message message = new Message();

                // 3.1 登录失败
                if (!ValidatedUserInfo(user.getUserId(), user.getPassword())) {
                    System.out.println("log: { 用户 " + user.getUserId() + " 登录失败. }");
                    message.setMsgType(CommonUtils.MSG.LOGIN_FAILED);
                    oos.writeObject(message);
                    socket.close();  // 关闭socket
                    continue;
                }

                // 3.2 登录成功
                // 4.创建一条线程与客户端保持联系
                ServerConnectClientThread serverConnectClientThread = new ServerConnectClientThread(user.getUserId(), socket);
                serverConnectClientThread.start();

                // 5.将线程放入一个集合进行管理
                serverThreadManagerService.addThread(user.getUserId(), serverConnectClientThread);

                // 6.发送成功连接成功信息
                message.setMsgType(CommonUtils.MSG.LOGIN_SUCCEEDED);
                oos.writeObject(message);
            }

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            // 如果服务器退出while, 说明服务器不在监听, 因此关闭serverSocket
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 验证用户id和密码
     * @param userId
     * @param password
     * @return
     */
    public boolean ValidatedUserInfo(String userId, String password) {
        User user = userData.get(userId);
        if (user == null) {
            return false;
        }
        if (!user.getPassword().equals(password)) {
            return false;
        }
        return true;
    }
}

package com.pccw.client.service;

import com.pccw.common.CommonUtils;
import com.pccw.common.Message;
import com.pccw.common.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author KennySo
 * @version 1.0
 * 该类完成 用户登录验证 和 用户注册 功能
 */
public class ClientService {

    // 因为可能在其他地方用到user信息，所以把它提取出来做成员属性
    private final User user = new User();
    // 将socket提取出来方便管理
    private Socket socket;

    private final ThreadManagerService threadManagerService = new ThreadManagerService();


    /**
     * 用户登录验证功能
     * @implNote 将userId和password封装成一个User类发送给服务器进行验证
     * @implNote 如果登录成功,就在客户端创建一条线程与服务端进行通信,并把这条线程使用集合管理起来
     *
     * @param userId 用户id
     * @param password
     * @return
     */
    public boolean login(String userId, String password) {
        user.setUserId(userId);
        user.setPassword(password);

        try {
            // 1.连接服务器
            socket = new Socket(InetAddress.getLocalHost(), 9999);
            // 2.发送用户信息到服务进行验证
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(user);

            // 3.从服务器获取登录成功或失败的回复消息
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Message message = (Message) ois.readObject();

            // 如果登录失败
            if (message.getMsgType().equals(CommonUtils.LOGIN_FAILED)) {
                socket.close();
                return false;
            }

            // 如果user登录成功
            // 4.创建一条线程和服务器保持联系
            ClientConnectServerThread clientConnectServerThread = new ClientConnectServerThread(socket);
            clientConnectServerThread.start();

            // 5.将线程放入一个集合进行管理(为了后续客户端的拓展)
            threadManagerService.addThread(userId, clientConnectServerThread);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * 用户注册功能
     */
}

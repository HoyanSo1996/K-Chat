package com.pccw.client.service;

import com.pccw.common.CommonUtils;
import com.pccw.common.Message;
import com.pccw.common.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Class ClientService
 *
 * @author KennySo
 * @version 1.0
 * 该类完成 用户登录验证 和 用户注册 功能
 */
public class ClientService {

    private final User user = new User(); // 因为可能在其他功能中用到user信息，所以把它提取出来做成员属性

    private Socket socket; // 将socket提取出来方便管理

    private final ClientThreadManagerService clientThreadManagerService = new ClientThreadManagerService();


    /**
     * 用户登录验证功能
     * @implNote 将userId和password封装成一个User类发送给服务器进行验证
     * @implNote 如果登录成功,就在客户端创建一条线程与服务端进行通信,并把这条线程使用集合管理起来
     *
     * @param userId 用户id
     * @param password 用户密码
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

            // 4.1 如果登录失败
            if (message.getMsgType().equals(CommonUtils.MSG_LOGIN_FAILED)) {
                socket.close();
                return false;
            }

            // 4.2 如果user登录成功
            // 5.创建一条线程和服务器保持联系
            ClientConnectServerThread clientConnectServerThread = new ClientConnectServerThread(socket);
            clientConnectServerThread.start();

            // 6.将线程放入一个集合进行管理(为了后续客户端的拓展)
            clientThreadManagerService.addThread(userId, clientConnectServerThread);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }



}

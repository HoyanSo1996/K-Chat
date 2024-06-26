package com.pccw.client.service;

import com.pccw.common.CommonUtils;
import com.pccw.common.Message;
import com.pccw.common.User;
import com.pccw.utils.DateUtils;
import com.pccw.utils.Utility;

import java.io.*;
import java.net.ConnectException;
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

    private Socket socket; // 将socket抽取出来方便管理


    /**
     * 用户登录验证功能
     *
     * @param userId   用户id
     * @param password 用户密码
     * @return 是否成功登录的标志
     * @implNote 将userId和password封装成一个User类发送给服务器进行验证
     * @implNote 如果登录成功, 就在客户端创建一条线程与服务端进行通信, 并把这条线程使用集合管理起来
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
            if (message.getMsgType().equals(CommonUtils.MSG.LOGIN_FAILED)) {
                socket.close();
                return false;
            }

            // 4.2 如果user登录成功
            // 5.创建一条线程和服务器保持联系
            ClientConnectServerThread clientConnectServerThread = new ClientConnectServerThread(user.getUserId(), socket);
            clientConnectServerThread.start();

            // 6.将线程放入一个集合进行管理(为了后续客户端的拓展)
            ClientThreadManagerService.addThread(userId, clientConnectServerThread);

            // 7.返回登录成功标记
            return true;

        } catch (ConnectException e) {
            // 连接不上服务器, 登录失败
            System.out.println("=========== info: 连接服务器失败 ===========");
            return false;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    /**
     * 用户端断开和服务器的连接
     */
    public void logout() {
        Message message = new Message();
        message.setSender(user.getUserId());
        message.setMsgType(CommonUtils.MSG.LOGOUT);

        try {
            SendMessageToServer(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取在线用户功能
     */
    public void getOnlineUserList() {
        Message message = new Message();
        message.setSender(user.getUserId());
        message.setMsgType(CommonUtils.MSG.GET_ONLINE_USERS);

        try {
            SendMessageToServer(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 私聊功能
     */
    public void sendToOne() {
        System.out.println("请输入想聊天的用户号(在线): ");
        String receiverId = Utility.readString(20);
        System.out.println("请输入想说的话: ");
        String content = Utility.readString(100);

        // 设置 发送者, 接受者, 内容, 日期, 消息类型
        Message message = new Message(
                user.getUserId(),
                receiverId,
                content,
                DateUtils.getDataTime(),
                CommonUtils.MSG.TO_ONE_MESSAGE);
        System.out.println("[" + message.getTime() + "] " +
                "对 " + message.getReceiver() + " 发送消息: " + "\"" + message.getContent() + "\"");

        try {
            SendMessageToServer(message);
        } catch (IOException e) {
            System.out.println("[" + message.getTime() + "] " +
                    message.getSender() + " 对 " + message.getReceiver() + " 发送消息: " + "\" "+ message.getContent() + "\" "+ "失败, " +
                    message.getReceiver() + " 不在线/不存在.");
            e.printStackTrace();
        }
    }


    /**
     * 群发消息功能
     */
    public void SendToAll() {
        System.out.println("请输入想群发的话: ");
        String content = Utility.readString(100);

        Message message = new Message(
                user.getUserId(),
                null,
                content,
                DateUtils.getDataTime(),
                CommonUtils.MSG.TO_ALL_MESSAGE);
        System.out.println("[" + message.getTime() + "] " +
                "向 所有人 发送消息: " + "\"" + message.getContent() + "\"");

        try {
            SendMessageToServer(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 发送文件功能
     *
     * @Tips 暂时只支持int最大值 2^31 Byte, 即 2GB 大小文件的传输. 超过指定大小会损坏文件
     *       如需传输更大的文件, 需要设置多个byte数组 或者 对文件边读边写...
     */
    public void sendFileToOne() {
        System.out.println("请输入想发送文件的用户号(在线): ");
        String receiverId = Utility.readString(20);
        System.out.println("请输入发送的文件完整路径(形式 d:\\xxx.jpg): ");
        String filePath = Utility.readString(100);

        // 1.从磁盘中读取文件
        File file = new File(filePath);
        int fileLen = (int) file.length();
        byte[] fileBytes = new byte[fileLen];
        String[] split = filePath.split("\\\\");
        String fileName = split[split.length - 1];

        try(FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2.构建文件信息
        Message message = new Message(
                user.getUserId(),
                receiverId,
                null,
                DateUtils.getDataTime(),
                CommonUtils.MSG.TO_ONE_FILE_MESSAGE);
        message.setFileName(fileName);
        message.setFileBytes(fileBytes);
        message.setFileLen(fileLen);

        // 3.发送消息到服务器
        try {
            SendMessageToServer(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("对 " + receiverId + " 发送了文件: \"" + fileName + "\" .");
    }


    /**
     * 向服务端发送信息
     * @param message 根据具体业务设置的消息
     * @throws IOException
     */
    public void SendMessageToServer(Message message) throws IOException {
        /*
            1. 先从先从管理器中通过userId获取线程对象
            2. 再通过线程对象获取socket
            3. 通过socket获取对应的OutputStream
            TODO (暂且不知道为什么不能用本类成员属性中的socket, 可能是后期一个用户有多个socket用来同步发消息和发文件, 方便拓展or面对对象编程)
         */
        ObjectOutputStream oos = new ObjectOutputStream(
                ClientThreadManagerService.getThread(user.getUserId()).getSocket().getOutputStream()
        );
        oos.writeObject(message);
    }
}

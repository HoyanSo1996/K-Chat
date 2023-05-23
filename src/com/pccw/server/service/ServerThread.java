package com.pccw.server.service;

import com.pccw.common.CommonUtils;
import com.pccw.common.Message;
import com.pccw.utils.DateUtils;
import com.pccw.utils.Utility;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Class ServerThread
 *
 * @author KennySu
 * @date 2023/5/23
 */
public class ServerThread extends Thread {


    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("请输入服务器要推送的新闻/消息[按exit退出]: ");
                String content = Utility.readString(200);
                if (content.equals("exit")) {
                    break;
                }

                // 构建消息
                Message message = new Message(
                        "服务器",
                        "所有在线用户",
                        content,
                        DateUtils.getDataTime(),
                        CommonUtils.MSG.TO_ALL_MESSAGE
                );

                // 获取所有在线用户并发送消息
                Map<String, ServerConnectClientThread> threadManager = ServerThreadManagerService.getThreadManager();
                for (String userId : threadManager.keySet()) {
                    ServerConnectClientThread serverConnectClientThread = threadManager.get(userId);
                    sendMessageToAll(message, serverConnectClientThread.getSocket().getOutputStream());

                }

                System.out.println("log: { " + "[" + message.getTime() + "] " +
                        message.getSender() + " 对 " + message.getReceiver() + " 发送消息: " + "\"" + message.getContent() + "\" .");

            }

            System.out.println("log: { " + "[" + DateUtils.getDataTime() + "] " + "服务器推送消息功能关闭.");

        } catch (IOException e) {
            System.out.println("log: { " + "[" + DateUtils.getDataTime() + "] " + "服务器推送消息失败.");
            e.printStackTrace();
        }
    }


    /**
     * 发送消息给客户端
     *
     * @param message
     * @param outputStream
     */
    public void sendMessageToAll(Message message, OutputStream outputStream) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(message);
    }
}

package com.pccw.server.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Class ServerThreadManagerService
 *
 * @author KennySu
 * @date 2023/5/16
 */
public class ServerThreadManagerService {

    private final static Map<String, ServerConnectClientThread> threadManager = new HashMap();


    /**
     * 将线程放入线程管理器
     * @param userId
     * @param serverConnectServerThread
     */
    public void addThread(String userId, ServerConnectClientThread serverConnectServerThread) {
        threadManager.put(userId, serverConnectServerThread);
    }

    /**
     * 根据userId从线程管理器获取线程
     * @param userId
     * @return
     */
    public ServerConnectClientThread getThread(String userId) {
        return threadManager.get(userId);
    }

    /**
     * 获取所有在线用户
     * @return
     */
    public static String getAllOnlineUsers() {
        Iterator<String> iterator = threadManager.keySet().iterator();
        String onlineUserList = "";

        while (iterator.hasNext()) {
            onlineUserList += (iterator.next() + " ");
        }
        return onlineUserList;
    }
}

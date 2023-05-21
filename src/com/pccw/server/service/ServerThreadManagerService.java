package com.pccw.server.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class ServerThreadManagerService
 *
 * @author KennySu
 * @date 2023/5/16
 */
public class ServerThreadManagerService {

    private final static Map<String, ServerConnectClientThread> threadManager = new HashMap();

    public static Map<String, ServerConnectClientThread> getThreadManager() {
        return threadManager;
    }

    /**
     * 将线程放入集合
     * @param userId
     * @param serverConnectServerThread
     */
    public static void addThread(String userId, ServerConnectClientThread serverConnectServerThread) {
        threadManager.put(userId, serverConnectServerThread);
    }


    /**
     * 将线程从集合中删除
     * @param userId
     */
    public static void removeThread(String userId) {
        threadManager.remove(userId);
    }


    /**
     * 根据userId从集合获取线程
     * @param userId
     * @return
     */
    public static ServerConnectClientThread getThread(String userId) {
        return threadManager.get(userId);
    }


    /**
     * 获取集合中所有在线用户
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

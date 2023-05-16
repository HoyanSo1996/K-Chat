package com.pccw.server.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Class ServerThreadManagerService
 *
 * @author KennySu
 * @date 2023/5/16
 */
public class ServerThreadManagerService {

    private final Map<String, ServerConnectClientThread> threadManager = new HashMap();


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
}

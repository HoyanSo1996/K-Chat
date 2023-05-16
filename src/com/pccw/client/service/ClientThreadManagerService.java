package com.pccw.client.service;

import java.util.HashMap;

/**
 * Class ClientThreadManagerService
 *
 * @author KennySo
 * @version 1.0
 * 该服务持有一个集合用来管理客户端和服务端之间通信的线程，Key是userId，value是线程
 */
public class ClientThreadManagerService {

    private HashMap<String, ClientConnectServerThread> threadManager = new HashMap();

    /**
     * 将某个线程加入集合
     * @param userId
     * @param clientConnectServerThread
     */
    public void addThread(String userId, ClientConnectServerThread clientConnectServerThread) {
        threadManager.put(userId, clientConnectServerThread);
    }

    /**
     * 通过userId获取对应的线程
     * @param userId
     * @return
     */
    public ClientConnectServerThread getThread(String userId) {
        return threadManager.get(userId);
    }
}

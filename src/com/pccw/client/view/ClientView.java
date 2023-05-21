package com.pccw.client.view;

import com.pccw.client.service.ClientService;
import com.pccw.utils.Utility;

/**
 * Class ClientView
 *
 * @author KennySu
 * @date 2023/5/10
 */
public class ClientView {

    private boolean loop1 = true;  // 控制一级菜单
    private boolean loop2;         // 控制二级菜单
    private String key = "";       // 接收用户的键盘输入

    private final ClientService clientService = new ClientService();

    public void mainMenu() {
        while (loop1) {
            System.out.println("=========== 欢迎登录网络通信系统 ===========");
            System.out.println("\t\t\t 1 登录系统");
            System.out.println("\t\t\t 9 退出系统");
            System.out.println("请输入你的选择：");
            key = Utility.readString(1);

            switch (key) {
                case "1":
                    System.out.println("请输入账号：");
                    String userId = Utility.readString(20);
                    System.out.println("请输入密码：");
                    String password = Utility.readString(20);

                    // 登录验证
                    if (clientService.login(userId, password)) {
                        System.out.println("=========== 用户 " + userId + " 登录成功 ===========");
                        loop2 = true;

                        // 进入二级菜单
                        while (loop2) {
                            System.out.println("=========== 网络通信系统二级菜单 ===========");
                            System.out.println("\t\t\t 1 显示在线用户列表");
                            System.out.println("\t\t\t 2 群  发  消  息");
                            System.out.println("\t\t\t 3 私  聊  消  息");
                            System.out.println("\t\t\t 4 发  送  文  件");
                            System.out.println("\t\t\t 9 用  户  登  出");
                            System.out.println("请输入你的选择：");
                            key = Utility.readString(1);

                            try {
                                switch (key) {
                                    case "1":
                                        clientService.getOnlineUserList();
                                        Thread.sleep(50);
                                        break;
                                    case "2":
                                        clientService.SendToAll();
                                        Thread.sleep(50);
                                        break;
                                    case "3":
                                        clientService.privateChat();
                                        Thread.sleep(50);
                                        break;
                                    case "4":
                                        // todo 发送文件
                                        Thread.sleep(50);
                                        break;
                                    case "9":
                                        clientService.logout();
                                        loop2 = false;
                                        System.out.println("=========== 用户 " + userId + " 退出登录 ===========");
                                        break;
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("=========== 用户 " + userId + " 登录失败 ===========");
                    }
                    break;

                case "9":
                    System.out.println("===========  退  出  系  统  ===========");
                    loop1 = false;
                    break;
            }
        }
    }
}

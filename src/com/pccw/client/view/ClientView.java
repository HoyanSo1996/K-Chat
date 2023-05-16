package com.pccw.client.view;

import com.pccw.client.service.ClientService;
import com.pccw.common.User;
import com.pccw.utils.Utility;

/**
 * Class ClientView
 *
 * @author KennySu
 * @date 2023/5/10
 */
public class ClientView {

    private boolean loop = true;  // 控制菜单首页
    private String key = "";       // 接收用户的键盘输入

    private ClientService clientService = new ClientService();

    public void mainMenu() {
        while (loop) {
            System.out.println("===========欢迎登录网络通信系统===========");
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
                        // 进入二级菜单
                        while (loop) {
                            System.out.println("=========== 网络通信系统二级菜单(用户: " + userId + ") ===========");
                            System.out.println("\t\t 1 显示在线用户列表");
                            System.out.println("\t\t 2 群  发  消  息");
                            System.out.println("\t\t 3 私  聊  消  息");
                            System.out.println("\t\t 4 发  送  文  件");
                            System.out.println("\t\t 9 用  户  登  出");
                            System.out.println("请输入你的选择：");
                            key = Utility.readString(1);

                            switch (key) {
                                case "1":
                                    System.out.println("显示在线用户列表");
                                    break;
                                case "2" :
                                    System.out.println("群发消息");
                                    break;
                                case "3" :
                                    System.out.println("私聊消息");
                                    break;
                                case "4" :
                                    System.out.println("发送文件");
                                    break;
                                case "9" :
                                    System.out.println("用户" + userId + "登出...");
                                    break;
                            }
                        }
                    } else {
                        System.out.println("=========== 用户 " + userId + " 登录失败 ===========");
                    }
                    break;

                case "9":
                    System.out.println("===========  退  出  系  统  ===========");
                    loop = false;
                    break;
            }
        }
    }
}

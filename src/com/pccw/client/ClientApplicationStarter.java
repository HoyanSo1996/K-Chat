package com.pccw.client;

import com.pccw.client.view.ClientView;

/**
 * Class ClientStarter
 *
 * @author KennySu
 * @date 2023/5/16
 */
public class ClientApplicationStarter {

    public static void main(String[] args) {
        ClientView cv = new ClientView();
        cv.mainMenu();
    }
}

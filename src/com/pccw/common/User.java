package com.pccw.common;

import java.io.Serializable;

/**
 * Class User
 *
 * @author KennySu
 * @date 2023/5/10
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private String userId;
    private String password;

    public User(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

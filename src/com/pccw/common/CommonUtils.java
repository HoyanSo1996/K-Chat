package com.pccw.common;

/**
 * Class CommonUtil
 *
 * @author KennySu
 * @date 2023/5/10
 */
public class CommonUtils {

    public enum MSG {
        LOGIN_SUCCEEDED,  // 登录成功消息
        LOGIN_FAILED,     // 登录失败消息
        LOGOUT,
        LOGOUT_SUCCEEDED,

        GET_ONLINE_USERS,
        RET_ONLINE_USERS,

        TO_ONE_MESSAGE,
        TO_ONE_MESSAGE_SUCCEEDED,
        TO_ONE_MESSAGE_FAILED,
        TO_ALL_MESSAGE

    }
}

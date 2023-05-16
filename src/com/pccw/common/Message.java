package com.pccw.common;

import java.io.Serializable;

/**
 * Class Message
 *
 * @author KennySu
 * @date 2023/5/10
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;
    private String sender;
    private String receiver;
    private String content;
    private String time;
    private String msgType;  // 消息类型(可以在接口中定义消息类型)

    public Message() {
    }

    public Message(String sender, String receiver, String content, String time) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.time = time;
    }


    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
}

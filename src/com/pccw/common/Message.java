package com.pccw.common;

import java.io.Serializable;

import static com.pccw.common.CommonUtils.*;

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
    private MSG msgType;  // 消息类型(可以在接口中定义消息类型)

    // 文件传输相关
    private String fileName;
    private byte[] fileBytes;
    private int fileLen;

    public Message() {
    }

    public Message(String sender, String receiver, String content, String time, MSG msgType) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.time = time;
        this.msgType = msgType;
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

    public MSG getMsgType() {
        return msgType;
    }

    public void setMsgType(MSG msgType) {
        this.msgType = msgType;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }

    public int getFileLen() {
        return fileLen;
    }

    public void setFileLen(int fileLen) {
        this.fileLen = fileLen;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}

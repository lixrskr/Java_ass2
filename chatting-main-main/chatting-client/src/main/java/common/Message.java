package common;

import java.util.List;
public class Message implements java.io.Serializable{
    String mesType;
    String sender;
    String getter;
    String content;
    String sendTime;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getGetter() {
        return getter;
    }

    public void setGetter(String getter) {
        this.getter = getter;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getMesType() {
        return mesType;
    }

    public void setMesType(String mesType) {
        this.mesType = mesType;
    }

    private List<String> groupMembers;
    public List<String> getGroupMembers() {
        return groupMembers;
    }
    public void setGroupMembers(List<String> groupMembers) {
        this.groupMembers = groupMembers;
    }

}

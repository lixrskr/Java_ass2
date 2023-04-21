package cn.edu.sustech.cs209.chatting.client;

import java.util.HashMap;

public class GroupchatwindowManage {
  public static HashMap hm = new HashMap<String, Groupchatwindow>();
  public static void addgroupchat(String uid, Groupchatwindow gcc) {
    hm.put(uid, gcc);
  }
  public static Groupchatwindow getgroupchat(String uid) {
    return (Groupchatwindow) hm.get(uid);
  }
}
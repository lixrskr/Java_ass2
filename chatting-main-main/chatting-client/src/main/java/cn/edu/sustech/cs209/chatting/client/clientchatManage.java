package cn.edu.sustech.cs209.chatting.client;


import java.util.HashMap;

public class clientchatManage {
  public static HashMap hm = new HashMap<String, ChatWindow>();

  public static void addclientchat(String uidandfriendid, ChatWindow cc) {
    hm.put(uidandfriendid, cc);

  }

  public static ChatWindow getclientchat(String uidandfriendid) {
    return (ChatWindow) hm.get(uidandfriendid);

  }


  public boolean equals(){
    return false;
  }

}

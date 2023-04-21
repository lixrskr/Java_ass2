package cn.edu.sustech.cs209.chatting.client;


import java.util.HashMap;

public class clientfriendlistManage {
  public static HashMap hm = new HashMap<String, ClientFriendList>();

  public static void addclientconnectThread_receive(String uid, ClientFriendList cfl) {
    hm.put(uid, cfl);

  }

  public static ClientFriendList getclientfriendlist(String uid) {
    return (ClientFriendList) hm.get(uid);
  }

}

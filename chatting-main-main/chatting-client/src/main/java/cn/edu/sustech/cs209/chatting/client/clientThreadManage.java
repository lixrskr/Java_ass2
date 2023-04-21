package cn.edu.sustech.cs209.chatting.client;

import java.util.HashMap;

public class clientThreadManage {

  public static HashMap hm = new HashMap<String, clientconnectThread_receive>();

  public static void addclientconnectThread_receive(String uid, clientconnectThread_receive cctr) {
    hm.put(uid, cctr);

  }

  public static clientconnectThread_receive getclientconnectThread_receive(String uid) {
    return (clientconnectThread_receive) hm.get(uid);

  }

}

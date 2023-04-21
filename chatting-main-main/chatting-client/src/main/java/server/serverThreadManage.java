package server;

import java.util.HashMap;
import java.util.Iterator;

public class serverThreadManage {

  public static HashMap hm = new HashMap<String, serverconnectThread>();

  //add serverconnectionThread to the hm,using uid to identify
  public static void addclientThread(String uid, serverconnectThread act) {
    hm.put(uid, act);
  }

  public static void deleteclientThread(String uid) {
    if (hm.containsKey(uid)) {
      hm.remove(uid);
    }
  }

  //we can judge if someone is online or not in this part
  public static serverconnectThread getclientThread(String uid) {
    return (serverconnectThread) hm.get(uid);
  }

  //return online userid
  public static String getOnlineUserid() {
    Iterator it = hm.keySet().iterator();
    String res = "";
    while (it.hasNext()) {
      res += it.next().toString() + ",";
    }
    return res;
  }


}

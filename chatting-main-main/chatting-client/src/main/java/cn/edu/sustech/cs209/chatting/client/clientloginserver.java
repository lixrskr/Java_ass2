package cn.edu.sustech.cs209.chatting.client;

import common.Message;
import common.MessageType;
import common.User;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class clientloginserver {

  public Socket s;


  public boolean login(Object o) {
    boolean b = false;
    try {

      System.out.println("send Userloginmessage start");
      s = new Socket("127.0.0.1", 9999);
      ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
      oos.writeObject(o);
      System.out.println("send Userloginmessage successfully");

      ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
      System.out.println("get Userloginmessage start");
      Message msg = (Message) ois.readObject();
      System.out.println("get Userloginmessage successfully");

      if (msg.getMesType().equals(MessageType.message_succeed)) {
        //if successfully login, then create a receive_thread to connect this user to the server
        clientconnectThread_receive clientconnectThread_receive = new clientconnectThread_receive(
            s);
        //add this receive_thread to hm
        clientThreadManage.addclientconnectThread_receive(((User) o).getUserid(),
            clientconnectThread_receive);
        //start this receive_thread
        clientconnectThread_receive.start();
        b = true;
      }


    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return b;
  }


}

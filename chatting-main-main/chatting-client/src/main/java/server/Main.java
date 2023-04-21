package server;

import common.Message;
import common.MessageType;
import common.User;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    ServerSocket ss = new ServerSocket(9999);
    System.out.println("server listening port: " + ss.getLocalPort());
    Runtime.getRuntime()
        .addShutdownHook(new Thread(() -> serverconnectThread.notifyAllClientsOfServerShutdown()));
    try {
      while (true) {
        Socket s = ss.accept();
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        User u = (User) ois.readObject();
        System.out.println(
            "get message from client id:" + u.getUserid() + " client password:" + u.getPasswd());
        Message msg = new Message();
        ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());

        if (u.getPasswd().equals(MessageType.message_succeed)) {
          System.out.println("client login passed");
          msg.setMesType(MessageType.message_succeed);
          oos.writeObject(msg);
          System.out.println("create a thread for: " + u.getUserid());
          serverconnectThread serverconnectThread = new serverconnectThread(s);
          serverThreadManage.addclientThread(u.getUserid(), serverconnectThread);
          System.out.println("add " + u.getUserid() + "'s thread to hm");
          serverconnectThread.start();
          //send clientlogin message to all online client
          serverconnectThread.notifyother();
        } else {
          System.out.println("client login failed");
          msg.setMesType(MessageType.message_failed);
          oos.writeObject(msg);
          s.close();
        }
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}

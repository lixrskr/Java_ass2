package server;


import common.Message;
import common.MessageType;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class serverconnectThread extends Thread {
  Socket s;
  public serverconnectThread(Socket socket) {
    //connect server with client ,using this socket s
    this.s = socket;
  }
  public static void notifyother() {
    String Onlineid = serverThreadManage.getOnlineUserid();
    HashMap hm = serverThreadManage.hm;
    Iterator it = hm.keySet().iterator();
    Message message = new Message();
    while (it.hasNext()) {
      String res = it.next().toString();
      message.setContent(Onlineid);
      message.setMesType(MessageType.message_returnOnlineFriend);
      try {
        ObjectOutputStream oos = new ObjectOutputStream(
            serverThreadManage.getclientThread(res).s.getOutputStream());
        message.setGetter(res);
        oos.writeObject(message);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    System.out.println(message.getContent());
  }
  public static void notifyAllClientsOfServerShutdown() {
    String Onlineid = serverThreadManage.getOnlineUserid();
    HashMap hm = serverThreadManage.hm;
    Iterator it = hm.keySet().iterator();
    Message message = new Message();
    while (it.hasNext()) {
      String res = it.next().toString();
      message.setContent(Onlineid);
      message.setMesType(MessageType.message_servershutdown);
      try {
        ObjectOutputStream oos = new ObjectOutputStream(serverThreadManage.getclientThread(res).s.getOutputStream());
        message.setGetter(res);
        oos.writeObject(message);
        serverThreadManage.getclientThread(res).s.close();
        System.out.println("send server shut down to:"+message.getContent());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void run() {

    while (true) {
      //this thread can connect with this client through this socket s,receive message from this client
      try {
        ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
        Message message = (Message) ois.readObject();
        System.out.println(
            message.getSender() + " send " + message.getContent() + " to: " + message.getGetter()
                + " at " + message.getSendTime());
        //judge message type and deal with it
        if (message.getMesType().equals(MessageType.message_common)) {
          //get getter's thread
          serverconnectThread serverconnectThread = serverThreadManage.getclientThread(
              message.getGetter());
          ObjectOutputStream oos = new ObjectOutputStream(serverconnectThread.s.getOutputStream());
          oos.writeObject(message);
        } else if (message.getMesType().equals(MessageType.message_getOnlineFriend)) {
          String res = serverThreadManage.getOnlineUserid();
          Message message1 = new Message();
          message1.setMesType(MessageType.message_returnOnlineFriend);
          message1.setContent(res);
          message1.setGetter(message.getSender());
          ObjectOutputStream objectOutputStream = new ObjectOutputStream(s.getOutputStream());
          objectOutputStream.writeObject(message1);
          System.out.println("return online clientlist:" + res+" to "+message1.getGetter());
        }else if (message.getMesType().equals(MessageType.message_deleteThreadandnotifyother)){
          serverThreadManage.deleteclientThread(message.getSender());
          String res = serverThreadManage.getOnlineUserid();
          Message message1 = new Message();
          message1.setMesType(MessageType.message_deleteThreadandnotifyother);
          message1.setContent(res);
          message1.setGetter(res);
          System.out.println("return online clientlist:" + res+",MessageType:"+MessageType.message_deleteThreadandnotifyother+",to:"+message1.getGetter());
          ObjectOutputStream objectOutputStream = new ObjectOutputStream(s.getOutputStream());
          objectOutputStream.writeObject(message1);
          serverconnectThread.notifyother();
        }
        else if (message.getMesType().equals(MessageType.message_file)) {
          System.out.println("server receive message_file from: "+message.getSender());
          String receiverUsername = message.getGetter();
          String[] contentParts = message.getContent().split(":", 2);
          String fileName = contentParts[0];
          String fileContentBase64 = contentParts[1];
          Message forwardMessage = new Message();
          forwardMessage.setMesType(MessageType.message_file);
          forwardMessage.setSender(message.getSender());
          forwardMessage.setGetter(receiverUsername);
          forwardMessage.setContent(fileName + ":" + fileContentBase64);
          forwardMessage.setSendTime(message.getSendTime());
          serverconnectThread serverconnectThread = serverThreadManage.getclientThread(
              message.getGetter());
          ObjectOutputStream oos = new ObjectOutputStream(serverconnectThread.s.getOutputStream());
          oos.writeObject(forwardMessage);
          System.out.println("server send message_file to: "+forwardMessage.getGetter());
        }
        else if (message.getMesType().equals(MessageType.message_createGroupChat)) {
          HashMap hm = serverThreadManage.hm;
          String[] joinedFriendNames = message.getGroupMembers().toArray(new String[message.getGroupMembers().size()]);
          String content = Arrays.toString(joinedFriendNames)
              .replaceAll("\\[|\\]|\\s", "");
          System.out.println("server get creategroup request from:"+message.getSender()+",the group contains:"+ content);
          for (String friendName : joinedFriendNames) {
            if (hm.containsKey(friendName)) {
              try {
                ObjectOutputStream oos = new ObjectOutputStream(
                    serverThreadManage.getclientThread(friendName).s.getOutputStream());
                Message groupChatNotification = new Message();
                groupChatNotification.setMesType(MessageType.message_createGroupChat);
                groupChatNotification.setSender(message.getSender());
                groupChatNotification.setGetter(friendName);
                groupChatNotification.setContent(content);
                oos.writeObject(groupChatNotification);
                oos.flush();
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        }else if (message.getMesType().equals(MessageType.message_groupChat)){
          System.out.println("server recieve group chat meaasge: "+message.getContent()+" from: "+message.getSender()+" to: "+message.getGetter());
          HashMap hm = serverThreadManage.hm;
          String[] joinedFriendNames = message.getGetter().split(",");
          for (String friendName : joinedFriendNames) {
            if (hm.containsKey(friendName)) {
              try {
                ObjectOutputStream oos = new ObjectOutputStream(
                    serverThreadManage.getclientThread(friendName).s.getOutputStream());
                Message groupChatNotification = new Message();
                groupChatNotification.setMesType(MessageType.message_groupChat);
                groupChatNotification.setSender(message.getSender());
                groupChatNotification.setGetter(friendName);
                groupChatNotification.setContent(message.getContent());
                oos.writeObject(groupChatNotification);
                oos.flush();
                System.out.println("successfully send group chat meaasge to: "+message.getGetter());
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
      }
      }catch (IOException e) {
        throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }


    }
  }
}

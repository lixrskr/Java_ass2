package cn.edu.sustech.cs209.chatting.client;

import common.Message;
import common.MessageType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class clientconnectThread_receive extends Thread {

  public Socket socket;

  public clientconnectThread_receive(Socket socket) {
    this.socket = socket;

  }

  public void run() {
    while (true) {
      try {
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        Message m = (Message) ois.readObject();

        if (m.getMesType().equals(MessageType.message_common)) {
          Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("client" + m.getGetter() + "的新消息通知");
            alert.setHeaderText(null);
            alert.setContentText("您收到了一个来自client" + m.getSender() + "的新消息！");
            alert.showAndWait();
          });
          System.out.println(
              "receive message from the server : " + m.getSender() + " send: " + m.getContent()
                  + " to " + m.getGetter() + " at " + m.getSendTime());
          ChatWindow cw = clientchatManage.getclientchat(m.getGetter() + " " + m.getSender());
          cw.showmessage(m);
        } else if (m.getMesType().equals(MessageType.message_returnOnlineFriend)) {
          String getter = m.getGetter();
          ClientFriendList clientFriendList = clientfriendlistManage.getclientfriendlist(getter);
          System.out.println("get returnOnlineFriend from server:" + m.getContent());
          if (clientFriendList != null) {
            System.out.println("update friendlist:" + m.getContent());
            clientFriendList.updateFriendList(m);
          }
        } else if (m.getMesType().equals(MessageType.message_deleteThreadandnotifyother)) {
          String getter = m.getGetter();
          ClientFriendList clientFriendList = clientfriendlistManage.getclientfriendlist(getter);
          System.out.println("get deleteThreadandnotifyother from server:" + m.getContent());
          if (clientFriendList != null) {
            System.out.println("update friendlist:" + m.getContent());
            clientFriendList.updateFriendList(m);
          }
        } else if (m.getMesType().equals(MessageType.message_servershutdown)) {
          String getter = m.getGetter();
          System.out.println("receive server shut down from:" + getter);
          m.setContent(null);
          ClientFriendList clientFriendList = clientfriendlistManage.getclientfriendlist(getter);
          System.out.println("update friendlist:" + m.getContent());
          clientFriendList.updateFriendList(m);
          Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("服务器关闭");
            alert.setHeaderText(null);
            alert.setContentText("服务器已关闭");
            alert.showAndWait();
          });
        } else if (m.getMesType().equals(MessageType.message_file)) {
          System.out.println(m.getGetter()+" receive message_file from: "+m.getSender());
          String[] contentParts = m.getContent().split(":", 2);
          String fileName = contentParts[0];
          String fileContentBase64 = contentParts[1];
          byte[] fileContentBytes = Base64.getDecoder().decode(fileContentBase64);
          // 将文件保存到客户端的本地目录中
          String savePath = "D:\\Program\\Project_Java\\chatingass\\chatting-main-main\\chatting-main-main\\chatting-client\\src\\main\\resources\\receive\\";
          File receivedFile = new File(savePath + fileName);
          try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
            fos.write(fileContentBytes);
            fos.flush();
          } catch (IOException e) {
            e.printStackTrace();
            // Handle error
          }
          System.out.println("resources from: "+m.getSender()+" named: "+fileName+"successfully received");

        } else if (m.getMesType().equals(MessageType.message_createGroupChat)) {
          System.out.println("get message_createGroupChat from server: " + m.getSender()+" create a group ");
          List<Friend> selectedFriends = Arrays.stream(m.getContent().split(","))
              .map(Friend::new)
              .collect(Collectors.toList());

          Groupchatwindow groupchatwindow = new Groupchatwindow();

          GroupchatwindowManage.addgroupchat(m.getGetter(),groupchatwindow);
          System.out.println("GroupchatwindowManage addgroupchat: "+m.getGetter()+" to hm");


          Platform.runLater(() -> {
            groupchatwindow.showGroupChatWindow(selectedFriends, m,m.getGetter());
          });
        } else if (m.getMesType().equals(MessageType.message_groupChat)){
          System.out.println("get message_createGroupChat:"+m.getContent()+" from server:" + m.getSender());
          Groupchatwindow groupchatwindow = GroupchatwindowManage.getgroupchat(m.getGetter());
          System.out.println("groupchatwindow showmessage for: "+m.getGetter());
          groupchatwindow.showmessage(m);

        }
      }catch (IOException e) {
        throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

  }
}

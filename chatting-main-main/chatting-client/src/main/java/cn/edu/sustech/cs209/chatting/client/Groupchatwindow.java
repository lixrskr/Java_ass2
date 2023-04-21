package cn.edu.sustech.cs209.chatting.client;

import common.Message;
import common.MessageType;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Groupchatwindow {

  String username;
  String joinedFriendNames;
  TextArea messageArea = new TextArea();
  TextField inputField = new TextField();

  List<Friend> selectedFriends;
  List<String> joinedFriendNameslist;

  public void showCreateGroupChatWindow(Stage parentStage, ObservableList<Friend> friends,
      String username) {
    this.username = username;
    Stage groupChatStage = new Stage();
    groupChatStage.initModality(Modality.APPLICATION_MODAL);
    groupChatStage.initOwner(parentStage);
    ListView<CheckBox> groupChatFriendListView = new ListView<>();
    for (Friend friend : friends) {
      CheckBox checkBox = new CheckBox("好友" + friend.getFriendname());
      checkBox.selectedProperty().bindBidirectional(friend.selectedProperty());
      groupChatFriendListView.getItems().add(checkBox);
    }
    Button createGroupChatButton = new Button("创建群聊");
    createGroupChatButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectedFriends = friends.stream()
            .filter(Friend::isSelected)
            .collect(Collectors.toList());
        sendCreateGroupChatRequest(selectedFriends, username);
        groupChatStage.close();
      }
    });
    VBox vbox = new VBox(groupChatFriendListView, createGroupChatButton);
    vbox.setSpacing(10);
    vbox.setPadding(new Insets(10));

    groupChatStage.setTitle("创建群聊");
    groupChatStage.setScene(new Scene(vbox, 300, 400));
    groupChatStage.show();
  }

  public void showGroupChatWindow(List<Friend> selectedFriends, Message message, String username) {
    String Username = username;
    Stage groupChatWindowStage = new Stage();
    ListView<Friend> userListView = new ListView<>(
        FXCollections.observableArrayList(selectedFriends));
    System.out.println("selectedFriends are: " + selectedFriends.stream().map(Friend::getFriendname)
        .collect(Collectors.joining(", ")));
    userListView.setCellFactory(new Callback<ListView<Friend>, ListCell<Friend>>() {
      @Override
      public ListCell<Friend> call(ListView<Friend> param) {
        return new ListCell<Friend>() {
          public void updateItem(Friend item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
              setText(null);
              setGraphic(null);
            } else {
              HBox hbox = new HBox();
              Label idLabel = new Label("好友 " + item.getFriendname());
              hbox.getChildren().addAll(idLabel);
              hbox.setSpacing(20);
              setGraphic(hbox);
            }
          }
        };
      }
    });
    // 添加一个TextArea用于展示聊天内容
    messageArea = new TextArea();
    inputField = new TextField();
    messageArea.setPrefWidth(400);
    messageArea.setWrapText(true);
    messageArea.setEditable(false);
    inputField.setPromptText("输入你的消息...");
    Button sendButton = new Button("发送");
    sendButton.setOnAction(event -> {
      sendmessage(selectedFriends, messageArea, Username);
      System.out.println("showGroupChatWindow username:" + Username);
    });
    HBox inputArea = new HBox(inputField, sendButton);
    inputArea.setSpacing(10);
    inputArea.setAlignment(Pos.CENTER);
    VBox chatArea = new VBox(messageArea, inputArea);
    chatArea.setSpacing(10);
    chatArea.setPadding(new Insets(10));
    HBox hbox = new HBox(chatArea, userListView);
    hbox.setSpacing(10);
    groupChatWindowStage.setTitle("群聊");
    groupChatWindowStage.setScene(new Scene(hbox, 600, 220));
    groupChatWindowStage.show();
  }

  private void sendCreateGroupChatRequest(List<Friend> selectedFriends, String username) {
    this.username = username;
    joinedFriendNameslist = selectedFriends.stream()
        .map(Friend::getFriendname)
        .collect(Collectors.toList());
    Message createGroupChatMessage = new Message();
    createGroupChatMessage.setMesType(MessageType.message_createGroupChat);
    createGroupChatMessage.setSender(username);
    createGroupChatMessage.setGroupMembers(joinedFriendNameslist);
    createGroupChatMessage.setSendTime(new java.util.Date().toString());
    try {
      ObjectOutputStream oos = new ObjectOutputStream(
          clientThreadManage.getclientconnectThread_receive(username).socket.getOutputStream());
      oos.writeObject(createGroupChatMessage);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String info0 =
        createGroupChatMessage.getSender() + " send to " + createGroupChatMessage.getGetter()
            + " at " + createGroupChatMessage.getSendTime() + " : "
            + " with joinedFriendNames:" + createGroupChatMessage.getGroupMembers();
    System.out.println("send create group message to server:" + info0);
  }


  private void sendmessage(List<Friend> selectedFriends, TextArea messageArea, String username) {
    this.username = username;
    joinedFriendNames = selectedFriends.stream()
        .map(Friend::getFriendname)
        .collect(Collectors.joining(","));

    List<String> joinedFriendNameslist = selectedFriends.stream()
        .map(Friend::getFriendname)
        .collect(Collectors.toList());
    Message message = new Message();
    message.setSender(username);
    message.setMesType(MessageType.message_groupChat);
    message.setGetter(joinedFriendNames);
    message.setContent(inputField.getText());
    message.setSendTime(new java.util.Date().toString());
    message.setGroupMembers(joinedFriendNameslist);
    try {
      System.out.println("getclientconnectThread_receive(username):" + username);
      ObjectOutputStream oos = new ObjectOutputStream(
          clientThreadManage.getclientconnectThread_receive(username).socket.getOutputStream());
      oos.writeObject(message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    inputField.clear();
    System.out.println(
        message.getSender() + " send: " + message.getContent() + " to group members: "
            + message.getGetter());
  }


  public void showmessage(Message m) {
    System.out.println("receive message: " + m.getContent() + " from sender: " + m.getSender());
    m.setSendTime(new java.util.Date().toString());

    String info1 =
        m.getSender() + " send " + " at " + m.getSendTime() + " : " + "\r\n" + m.getContent()
            + "\r\n";

    messageArea.appendText(info1);
  }


  public void typealter() {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle("输入格式错误");
    alert.setHeaderText(null);
    alert.setContentText("输入的格式，请重新输入。");
    alert.showAndWait();
  }
}

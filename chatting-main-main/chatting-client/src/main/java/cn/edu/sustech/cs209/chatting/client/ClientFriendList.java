package cn.edu.sustech.cs209.chatting.client;

import common.Message;
import common.MessageType;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;


class Friend {

  public Friend(String friendname) {
    this.friendname = friendname;
  }

  private int id;
  private boolean online;
  String friendname;
  private BooleanProperty selected;

  Friend(int id, boolean online) {
    this.id = id;
    this.online = online;
    this.friendname = String.valueOf(id);
    this.selected = new SimpleBooleanProperty(false);
  }

  int getId() {
    return id;
  }

  String getFriendname() {
    return friendname;
  }

  boolean isOnline() {
    return online;
  }

  void setOnline(boolean online) {
    this.online = online;
  }

  public BooleanProperty selectedProperty() {
    return selected;
  }

  public boolean isSelected() {
    return selected.get();
  }

  public void setSelected(boolean selected) {
    this.selected.set(selected);
  }
}

public class ClientFriendList extends Application {

  Stage friendliststage;
  String username;
  VBox vbox;
  ListView<Friend> listView;
  ListView<Friend> groupChatFriendListView;


  private final Map<String, Stage> chatWindows = new HashMap<>();

  public ClientFriendList(String username, Stage Friendliststage) {
    this.friendliststage = Friendliststage;
    this.username = username;
    listView = new ListView<>(createFriendList(username));
    Button createGroupChatButton = new Button("创建群聊");
    createGroupChatButton.setOnAction(event -> {
      Groupchatwindow groupChatWindow = new Groupchatwindow();
      groupChatWindow.showCreateGroupChatWindow(Friendliststage, listView.getItems(), username);
    });
    vbox = new VBox(listView, createGroupChatButton);
    vbox.setSpacing(10);
    vbox.setPadding(new Insets(10));
    listView.setCellFactory(new Callback<ListView<Friend>, ListCell<Friend>>() {
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
              Label onlineLabel = new Label(item.isOnline() ? "在线" : "离线");
              onlineLabel.setTextFill(item.isOnline() ? Color.GREEN : Color.RED);
              hbox.getChildren().addAll(idLabel, onlineLabel);
              hbox.setSpacing(20);
              setGraphic(hbox);
            }
          }
        };
      }
    });

    listView.setOnMouseClicked(event -> {
      Friend selectedItem = listView.getSelectionModel().getSelectedItem();
      if (event.getClickCount() == 2) {
        String friendName = selectedItem.getFriendname();
        openChatWindow(friendName);
      }
    });
    friendliststage.setOnCloseRequest(event -> {
      ObjectOutputStream oos = null;
      try {
        oos = new ObjectOutputStream(
            clientThreadManage.getclientconnectThread_receive(
                username).socket.getOutputStream());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      Message m = new Message();
      m.setMesType(MessageType.message_deleteThreadandnotifyother);
      m.setSender(username);
      try {
        oos.writeObject(m);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      System.exit(0);
    });
    friendliststage.setTitle(username + "的好友列表：");
    friendliststage.setScene(new Scene(vbox, 400, 400));
    friendliststage.show();

  }

  public ObservableList<Friend> createFriendList(String username) {
    ObservableList<Friend> friends = FXCollections.observableArrayList();

    for (int i = 1; i <= 10; i++) { // 更改此处以仅创建10个好友
      boolean online = (Integer.parseInt(username) == i);
      friends.add(new Friend(i, online));
    }
    return friends;
  }


  public void updateFriendList(Message m) {
    ObservableList<Friend> updatedFriends = listView.getItems();
    String content = m.getContent();
    if (content == null || content.equals("")) {
      System.out.println("server shut down, online is not valid");
      for (Friend friend : updatedFriends) {
        friend.setOnline(false);
      }
    } else {
      System.out.println(Arrays.toString(content.toCharArray()));
      String[] onlinefriend = content.split(",");
      for (Friend friend : updatedFriends) {
        boolean found = false;
        for (String friendId : onlinefriend) {
          if (friendId.equals(String.valueOf(friend.getFriendname()))) {
            friend.setOnline(true);
            found = true;
            break;
          }
        }
        if (!found) {
          friend.setOnline(false);
        }
      }
    }
    listView.setItems(updatedFriends);
    listView.refresh();
  }


  private void openChatWindow(String friendName) {
    if (chatWindows.containsKey(friendName)) {
      chatWindows.get(friendName).setIconified(false);
      chatWindows.get(friendName).isAlwaysOnTop();
      chatWindows.get(friendName).requestFocus();
    } else {
      createChatWindow(friendName);
    }
  }

  private void createChatWindow(String friendName) {
    String uidandfriendid = username + " " + friendName;
    System.out.println("client: " + username + " want to chat with client: "
        + friendName);
    ChatWindow cw = new ChatWindow(username,
        friendName, chatWindows);
    chatWindows.put(friendName, cw);
    //add this chat uniquely to hm
    clientchatManage.addclientchat(uidandfriendid, cw);
    System.out.println("this chat is added to hm");

  }

  //update online friend

  @Override
  public void start(Stage stage) throws Exception {

  }
}











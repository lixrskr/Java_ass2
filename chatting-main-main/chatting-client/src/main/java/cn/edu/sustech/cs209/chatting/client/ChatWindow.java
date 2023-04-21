package cn.edu.sustech.cs209.chatting.client;

import common.Message;
import common.MessageType;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ChatWindow extends Stage {

  String username;
  String friendName;
  TextArea messageArea = new TextArea();
  TextArea messageField = new TextArea();


  public ChatWindow(String username, String friendName, Map<String, Stage> chatWindows) {
    this.username = username;
    this.friendName = friendName;
    VBox root = new VBox(10);
    root.setPadding(new Insets(10));
    messageArea.setEditable(false);
    messageArea.setWrapText(true);
    ScrollPane scrollPane = new ScrollPane(messageArea);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);

    messageField.setWrapText(true);
    messageField.setPrefRowCount(2);
    messageField.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER && event.isShiftDown()) {
        messageField.appendText("\n");
      } else if ((event.getCode() == KeyCode.ENTER)) {
        if (messageField.getText().trim().equals("")) {
          typealter();
        } else {
          sendmessage(messageField);
        }
      }
    });
    messageField.setPromptText("输入消息...");
    HBox buttonBox = new HBox(5);
    buttonBox.setAlignment(Pos.CENTER);
    Button emojiButton = new Button("emoji");
    Button fileButton = new Button("up/down load .docx/.md");
    Button sendButton = new Button("Send");
    sendButton.setOnAction(event -> {
      // 发送消息
      if (messageField.getText().trim().equals("")) {
        typealter();
      } else {
        sendmessage(messageField);
      }
    });
    buttonBox.getChildren().addAll(emojiButton, fileButton, sendButton);
    root.getChildren().addAll(scrollPane, messageField, buttonBox);
    Scene scene = new Scene(root, 400, 300);
    setScene((scene));
    Stage stage = new Stage();
    stage.setTitle("client: " + username + " chatting with client: " + friendName);
    stage.setOnCloseRequest(event -> {
      chatWindows.remove(friendName);
      System.out.println("this chat is deleted from hm");
    });
    emojiButton.setOnAction(event -> {
      EmojiPicker picker = new EmojiPicker();
      picker.showAndWait();
      String selectedEmoji = picker.getResult();
      if (selectedEmoji != null) {
        messageField.setText(messageField.getText() + selectedEmoji);
      }
    });

    fileButton.setOnAction(event -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("选择文件");
      fileChooser.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("Supported Files", "*.md", "*.doc", "*.docx")
      );
      File selectedFile = fileChooser.showOpenDialog(stage);
      if (selectedFile != null) {
        try {
          byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
          String fileContentBase64 = Base64.getEncoder().encodeToString(fileContent);
          Message message = new Message();
          message.setMesType(MessageType.message_file);
          message.setSender(username);
          message.setGetter(friendName);
          message.setContent(selectedFile.getName() + ":" + fileContentBase64);
          message.setSendTime(
              LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
          try {
            ObjectOutputStream oos = new ObjectOutputStream(
                clientThreadManage.getclientconnectThread_receive(
                    username).socket.getOutputStream());
            oos.writeObject(message);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        messageArea.appendText("已发送文件: " + selectedFile.getName() + "\n");
      }
    });

    stage.setScene(getScene());
    stage.setIconified(false);
    stage.toFront();
    stage.requestFocus();
    stage.show();

  }


  private void sendmessage(TextArea messageField) {
    Message message = new Message();
    message.setSender(this.username);
    message.setMesType("3");
    message.setGetter(this.friendName);
    message.setContent(messageField.getText());
    message.setSendTime(new java.util.Date().toString());
    try {
      ObjectOutputStream oos = new ObjectOutputStream(
          clientThreadManage.getclientconnectThread_receive(username).socket.getOutputStream());
      oos.writeObject(message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String info0 =
        message.getSender() + " send to " + message.getGetter()
            + " at " + message.getSendTime() + " : " + "\r\n" + message.getContent() + "\r\n";
    messageArea.appendText(info0);
    messageField.clear();
  }

  public void showmessage(Message m) {
    String info1 = m.getSender() + " send to " + m.getGetter()
        + " at " + m.getSendTime() + " : " + "\r\n" + m.getContent() + "\r\n";
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










class EmojiPicker extends Stage {

  private ObjectProperty<String> resultProperty;

  public EmojiPicker() {
    setTitle("选择 Emoji");
    initModality(Modality.APPLICATION_MODAL);
    resultProperty = new SimpleObjectProperty<>();

    FlowPane root = new FlowPane(5, 5);
    root.setPadding(new Insets(10));
    root.setPrefWrapLength(200);
    String[] emojis = {"😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍",
        "🥰", "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🥸", "😏"};

    for (String emoji : emojis) {
      Button emojiButton = new Button(emoji);
      emojiButton.setStyle("-fx-font-size: 24px;");
      emojiButton.setOnAction(event -> {
        setResult(emoji);
        close();
      });
      root.getChildren().add(emojiButton);
    }

    Scene scene = new Scene(root);
    setScene(scene);
  }

  public final ObjectProperty<String> resultProperty() {
    return this.resultProperty;
  }

  public final String getResult() {
    return this.resultProperty.get();
  }

  public final void setResult(final String result) {
    this.resultProperty.set(result);
  }
}




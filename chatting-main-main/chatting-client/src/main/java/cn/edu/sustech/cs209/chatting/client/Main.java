package cn.edu.sustech.cs209.chatting.client;

import common.Message;
import common.MessageType;
import common.User;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Main extends Application {

  //client_login_Thread
  public Stage stage;
  String username;
  String password;

  Stage primaryStage;


  public static void main(String[] args) {
    launch();
  }


  @Override
  public void start(Stage primaryStage) throws IOException {
    primaryStage.setTitle("登录");
    GridPane grid = new GridPane();
    grid.setPadding(new Insets(20, 20, 20, 20));
    grid.setVgap(10);
    grid.setHgap(10);
    Label usernameLabel = new Label("用户名：");
    GridPane.setConstraints(usernameLabel, 0, 0);
    TextField usernameInput = new TextField();
    GridPane.setConstraints(usernameInput, 1, 0);
    Label passwordLabel = new Label("密码：");
    GridPane.setConstraints(passwordLabel, 0, 1);
    PasswordField passwordInput = new PasswordField();
    GridPane.setConstraints(passwordInput, 1, 1);
    Button loginButton = new Button("登录");
    GridPane.setConstraints(loginButton, 0, 2);
    Button registerButton = new Button("注册");
    GridPane.setConstraints(registerButton, 1, 2);
    grid.getChildren()
        .addAll(usernameLabel, usernameInput, passwordLabel, passwordInput, loginButton,
            registerButton);
    //login button

    loginButton.setOnAction(
        e -> handleLoginButton(primaryStage, loginButton, usernameInput, passwordInput));
    registerButton.setOnAction(e -> handleRegisterButton());
    Scene scene = new Scene(grid, 300, 150);
    primaryStage.setScene(scene);
    primaryStage.show();
  }


  public void handleLoginButton(Stage primaryStage, Button loginButton, TextField usernameInput,
      PasswordField passwordInput) {
    username = usernameInput.getText().trim();
    password = passwordInput.getText();
    System.out.println("client:"+username + " clicked login.");
    // 判断用户名和密码是否为空
    if (!username.isEmpty() && !password.isEmpty()) {
      // 创建用户类并初始化
      clientlogic clientlogic = new clientlogic();
      User u = new User();
      u.setUserid(username);
      u.setPasswd(password);
      // 检查用户名和密码是否正确
      if (clientlogic.checkUser(u)) {
        // 在此处添加登录成功后的逻辑


        try {
          // 登录成功，创建新窗口
          Stage friendliststage = new Stage();
          ClientFriendList clientFriendList = new ClientFriendList(username, friendliststage);
          clientfriendlistManage.addclientconnectThread_receive(username,clientFriendList);
          //once passed login then there are several messages to send and receive
          //Thread start
          //send a request to ask the server to send back the online friend

          ObjectOutputStream oos = new ObjectOutputStream(
              clientThreadManage.getclientconnectThread_receive(
                  u.getUserid()).socket.getOutputStream());
          Message m = new Message();
          m.setMesType(MessageType.message_getOnlineFriend);
          m.setSender(u.getUserid());
          oos.writeObject(m);

          try {
            clientFriendList.start(stage);
            primaryStage.close(); // 关闭登录窗口
          } catch (Exception ex) {
            ex.printStackTrace();
          }

        } catch (IOException ex) {
          throw new RuntimeException(ex);
        }
      } else {
        // 登录失败，弹出错误窗口
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("登录失败");
        alert.setHeaderText(null);
        alert.setContentText("输入的用户名或密码错误，请重新输入。");
        alert.showAndWait();
      }
    } else {
      // 如果用户名或密码为空，则弹出一个对话框
      Alert alert = new Alert(AlertType.WARNING);
      alert.setTitle("错误");
      alert.setHeaderText(null);
      alert.setContentText("请输入正确的用户名和密码！");
      alert.showAndWait();
    }
  }


  public void handleRegisterButton() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(25, 25, 25, 25));
    Label nameLabel = new Label("用户名:");
    TextField nameTextField = new TextField();
    nameTextField.setPromptText("请输入用户名");
    Label passwordLabel = new Label("密码:");
    PasswordField passwordTextField = new PasswordField();
    passwordTextField.setPromptText("请输入密码");
    grid.add(nameLabel, 0, 0);
    grid.add(nameTextField, 1, 0);
    grid.add(passwordLabel, 0, 1);
    grid.add(passwordTextField, 1, 1);
    Button okButton = new Button("确定");
    okButton.setOnAction(e -> {
      Stage dialog = new Stage();
      dialog.initOwner(stage);
      dialog.initModality(Modality.APPLICATION_MODAL);
      VBox vbox = new VBox();
      vbox.setAlignment(Pos.CENTER);
      vbox.setPadding(new Insets(10));
      vbox.setSpacing(10);
      Label label = new Label("注册成功！");
      Button closeButton = new Button("关闭");
      closeButton.setOnAction(event -> dialog.close());
      vbox.getChildren().addAll(label, closeButton);
      Scene scene = new Scene(vbox);
      dialog.setScene(scene);
      dialog.show();
    });
    grid.add(okButton, 1, 2);
    Scene scene = new Scene(grid);
    Stage dialog = new Stage();
    dialog.initOwner(stage);
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setScene(scene);
    dialog.show();
  }


}

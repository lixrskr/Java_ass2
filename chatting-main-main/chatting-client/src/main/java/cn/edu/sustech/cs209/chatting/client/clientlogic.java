package cn.edu.sustech.cs209.chatting.client;


import common.User;

//connect client with server
public class clientlogic {

  public static boolean checkUser(User u) {
    return new clientloginserver().login(u);
  }


}

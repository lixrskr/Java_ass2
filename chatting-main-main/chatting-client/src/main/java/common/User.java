package common;

public class User implements java.io.Serializable {

  private String userid;
  private String passwd;

  public User(String username, String password) {
    this.userid = username;
    this.passwd = password;
  }

  public User() {
  }

  public String getUserid() {
    return userid;
  }

  public void setUserid(String userid) {
    this.userid = userid;
  }

  public String getPasswd() {
    return passwd;
  }

  public void setPasswd(String passwd) {
    this.passwd = passwd;
  }

}

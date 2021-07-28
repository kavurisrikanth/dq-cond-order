package classes;

import models.User;

public class LoginResult {
  public String failureMessage;
  public boolean success;
  public String token;
  public User userObject;

  public LoginResult() {}

  public LoginResult(String failureMessage, boolean success, String token, User userObject) {
    this.failureMessage = failureMessage;
    this.success = success;
    this.token = token;
    this.userObject = userObject;
  }
}

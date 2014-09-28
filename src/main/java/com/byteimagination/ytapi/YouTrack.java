package com.byteimagination.ytapi;

public interface YouTrack {

  void signIn(String login, String password);

  boolean signedIn();

}

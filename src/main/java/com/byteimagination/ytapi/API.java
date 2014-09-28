package com.byteimagination.ytapi;

public abstract class API implements YouTrack {

  protected String url;

  protected API(String url) {
    this.url = url;
  }

}

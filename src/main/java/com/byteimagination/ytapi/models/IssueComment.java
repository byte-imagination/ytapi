package com.byteimagination.ytapi.models;

import java.util.Date;

public class IssueComment implements Model {

  public String id;
  public String author;
  public String issueId;
  public Boolean deleted;
  public String text;
  public Boolean shownForIssueAuthor;
  public Date created;

}

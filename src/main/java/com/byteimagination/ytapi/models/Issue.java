package com.byteimagination.ytapi.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class Issue implements Model {

  public String id;
  public String jiraId;
  public String projectShortName;
  public Long numberInProject;
  public String summary;
  public String description;
  public Date created;
  public Date updated;
  public String updaterName;
  public Date resolved;
  public String reporterName;
  public Long commentsCount;
  public Long votes;
  public String permittedGroup;
  public Collection<IssueComment> comments = new ArrayList<IssueComment>();
  public Collection<IssueField> fields = new ArrayList<IssueField>();

}

package com.byteimagination.ytapi.models;

import java.util.ArrayList;
import java.util.Collection;

public class IssueField implements Model {

  public String name;
  public Collection<IssueFieldValue> values = new ArrayList<IssueFieldValue>();

}

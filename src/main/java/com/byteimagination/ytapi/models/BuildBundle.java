package com.byteimagination.ytapi.models;

import java.util.ArrayList;
import java.util.Collection;

public class BuildBundle implements Bundle {

  public String name;
  public Collection<Build> builds = new ArrayList<Build>();

}

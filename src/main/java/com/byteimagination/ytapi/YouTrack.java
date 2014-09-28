package com.byteimagination.ytapi;

import com.byteimagination.ytapi.models.Project;
import com.byteimagination.ytapi.models.ProjectReference;

import java.util.Collection;

public interface YouTrack {

  void signIn(String login, String password);

  boolean signedIn();

  void putProject(String projectId, String projectName, Integer startingNumber, String projectLeadLogin, String description);

  Collection<ProjectReference> getProjects();

  Project getProject(String id);

  void deleteProject(String id);

}

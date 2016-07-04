package com.byteimagination.ytapi;

import com.byteimagination.ytapi.models.*;

import java.util.Collection;
import java.util.Date;

public interface YouTrack {

  void signIn(String login, String password);

  boolean signedIn();

  void putProject(String projectId, String projectName, Integer startingNumber, String projectLeadLogin, String description);

  Collection<ProjectReference> getProjects();

  Project getProject(String id);

  void deleteProject(String id);

  void puBuildBundle(String bundleName);

  BuildBundle getBuildBundle(String bundleName);

  void deleteBuildBundle(String bundleName);

  void putBuild(String bundleName, String buildName, String description, Integer colorIndex, Date assembleDate);

  Build getBuild(String bundleName, String buildName);

  void deleteBuild(String bundleName, String buildName);

  String putIssue(String project, String summary, String description, String permittedGroup);

  Issue getIssue(String id, Boolean wikifyDescription);

}

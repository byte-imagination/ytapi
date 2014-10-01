package acceptance.com.byteimagination.ytapi;

import com.byteimagination.ytapi.CurrentAPI;
import com.byteimagination.ytapi.YouTrack;
import com.byteimagination.ytapi.exceptions.InvalidCredentials;
import com.byteimagination.ytapi.models.*;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

public class CurrentAPITest {

  private static Properties properties;
  private static String url;
  private YouTrack youTrack;

  @BeforeClass
  public static void prepareData() throws IOException {
    properties = loadProperties();
    url = youTrackUrl();
  }

  private static Properties loadProperties() throws IOException {
    Properties properties = new Properties();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream in = classLoader.getResourceAsStream("youtrack_data.personal.properties");
    properties.load(in);
    in.close();
    return properties;
  }

  private static String youTrackUrl() {
    return properties.getProperty("ytHost");
  }

  @Before
  public void prepareYouTrackCurrentAPIAndSignIn() {
    youTrack = createCurrentAPI();
    youTrack.signIn(properties.getProperty("ytLogin"), properties.getProperty("ytPassword"));
  }

  @Test
  public void signsIn() {
    YouTrack youTrack = new CurrentAPI(url);
    assert !youTrack.signedIn();
    youTrack.signIn(properties.getProperty("ytLogin"), properties.getProperty("ytPassword"));
    assert youTrack.signedIn();
  }

  @Test(expected = InvalidCredentials.class)
  public void doesNotSignInWhenCredentialsAreInvalid() {
    createCurrentAPI().signIn("invalidLogin", "invalidPassword");
  }

  private CurrentAPI createCurrentAPI() {
    return new CurrentAPI(url);
  }

  @Test(expected = FailingHttpStatusCodeException.class)
  public void doesNotGetProjectThatDoesNotExist() {
    Project projectFound = youTrack.getProject("invalidProjectNameProjectDoesNotExist");
    assert projectFound == null;
  }

  @Test
  public void getsCreatesAndDeletesProjects() {
    Project project = new Project();
    project.id = properties.getProperty("ytAPITestProject");
    project.name = properties.getProperty("ytAPITestProject") + " Name";
    project.lead = properties.getProperty("ytAPITestProjectAdminLogin");
    project.description = "project description goes here";
    Collection<ProjectReference> projects = youTrack.getProjects();
    assert projects != null;
    youTrack.putProject(project.id, project.name, 666, project.lead, project.description);
    projects = youTrack.getProjects();
    boolean found = false;
    for (ProjectReference projectReference : projects)
      if (projectReference.id.equals(project.id))
        found = true;
    assert found;
    Project projectFound = youTrack.getProject(project.id);
    assert projectFound != null;
    assert projectFound.id.equals(project.id);
    assert projectFound.name.equals(project.name);
    assert projectFound.lead.equals(project.lead);
    assert projectFound.description == null;
    assert projectFound.assigneesUrl != null;
    assert projectFound.subsystemsUrls != null;
    assert projectFound.buildsUrl != null;
    assert projectFound.versionsUrl != null;
    youTrack.deleteProject(project.id);
    projects = youTrack.getProjects();
    found = false;
    for (ProjectReference projectReference : projects)
      if (projectReference.id.equals(project.id))
        found = true;
    assert !found;
  }

  @Test
  public void createsGetsAndDeletesBuildBundle() {
    String bundleName = properties.getProperty("ytAPITestProject") + " " + randomString() + " " + randomString();
    String buildName = "1.12";
    Date assembleDate = new Date();
    youTrack.puBuildBundle(bundleName);
    youTrack.putBuild(bundleName, buildName, "Build description goes here.", 6, assembleDate);
    BuildBundle bundle = youTrack.getBuildBundle(bundleName);
    assert bundle.name.equals(bundleName);
    boolean found = false;
    for (Build build : bundle.builds)
      if (build.name.equals(buildName))
        found = true;
    assert found;
    Build buildFound = youTrack.getBuild(bundleName, buildName);
    assert buildFound.name.equals(buildName);
    assert buildFound.assembleDate.equals("" + assembleDate.getTime());
    youTrack.deleteBuild(bundleName, buildName);
    try {
      youTrack.getBuild(bundleName, buildName);
      assert false;
    } catch (Exception ignored) {
    }
    youTrack.deleteBuildBundle(bundleName);
    try {
      youTrack.getBuildBundle(bundleName);
      assert false;
    } catch (Exception ignored) {
    }
  }

  @Test
  public void putsAndGetsIssue() throws URISyntaxException {
    Date now = new Date();
    String project = properties.getProperty("ytAPITestProject") + randomString();
    youTrack.putProject(project, project, 6, properties.getProperty("ytAPITestProjectAdminLogin"), "description");
    String summary = "Issue summary";
    String description = "The issue '''description'''";
    String permittedGroup = "All Users";
    String issueId = youTrack.putIssue(project, summary, description, permittedGroup);
    Issue issue = youTrack.getIssue(issueId, false);
    Issue wikifiedIssue = youTrack.getIssue(issueId, true);
    assert issue.id.equals(issueId);
    assert issue.projectShortName.equals(project);
    assert issue.numberInProject.equals(6L);
    assert issue.summary.equals(summary);
    assert issue.description.equals(description);
    assert issue.created.before(now);
    assert issue.updated.before(now);
    assert issue.updaterName.equals(properties.getProperty("ytAPITestProjectAdminLogin"));
    assert issue.resolved == null;
    assert issue.reporterName.equals(properties.getProperty("ytAPITestProjectAdminLogin"));
    assert issue.commentsCount.equals(0L);
    assert issue.votes.equals(0L);
    assert issue.comments.isEmpty();
    assert issue.fields.size() > 0;
    String wikifiedDescription = "<div class=\"wiki text\">" + description.replaceFirst("'''", "<strong>").replaceFirst("'''", "</strong>") + "</div>";
    assert wikifiedIssue.description.equals(wikifiedDescription);
    youTrack.deleteProject(project);
  }

  private static String randomString() {
    return new BigInteger(10, new SecureRandom()).toString(32);
  }

}

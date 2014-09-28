package acceptance.com.byteimagination.ytapi;

import com.byteimagination.ytapi.CurrentAPI;
import com.byteimagination.ytapi.YouTrack;
import com.byteimagination.ytapi.exceptions.InvalidCredentials;
import com.byteimagination.ytapi.models.Project;
import com.byteimagination.ytapi.models.ProjectReference;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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

  @Test(expected = FailingHttpStatusCodeException.class)
  public void doesNotGetProjectThatDoesNotExist() {
    Project projectFound = youTrack.getProject("invalidProjectNameProjectDoesNotExist");
    assert projectFound == null;
  }

  @Test
  public void getsCreatesAndDeletesProjects() {
    Project project = new Project();
    project.id = properties.getProperty("ytAPITestProject");
    project.name = properties.getProperty("ytAPITestProject") + "Name";
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
    assert projectFound.startingNumber.equals(666);
    youTrack.deleteProject(project.id);
    projects = youTrack.getProjects();
    found = false;
    for (ProjectReference projectReference : projects)
      if (projectReference.id.equals(project.id))
        found = true;
    assert !found;
  }

  private CurrentAPI createCurrentAPI() {
    return new CurrentAPI(url);
  }

}

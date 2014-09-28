package acceptance.com.byteimagination.ytapi;

import com.byteimagination.ytapi.CurrentAPI;
import com.byteimagination.ytapi.YouTrack;
import com.byteimagination.ytapi.exceptions.InvalidCredentials;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CurrentAPITest {

  private Properties properties;
  private String url;
  private YouTrack youTrack;

  @Before
  public void prepareYouTrackCurrentAPI() throws IOException {
    properties = loadProperties();
    url = youTrackUrl();
    youTrack = createCurrentAPI();
  }

  private Properties loadProperties() throws IOException {
    Properties properties = new Properties();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream in = classLoader.getResourceAsStream("youtrack_data.personal.properties");
    properties.load(in);
    in.close();
    return properties;
  }

  @Test
  public void signsIn() {
    assert !youTrack.signedIn();
    youTrack.signIn(properties.getProperty("ytLogin"), properties.getProperty("ytPassword"));
    assert youTrack.signedIn();
  }

  @Test(expected = InvalidCredentials.class)
  public void doesNotSignInWhenCredentialsAreInvalid() {
    createCurrentAPI().signIn("invalidLogin", "invalidPassword");
  }

  private String youTrackUrl() {
    return properties.getProperty("ytHost");
  }

  private CurrentAPI createCurrentAPI() {
    return new CurrentAPI(url);
  }

}

package com.byteimagination.ytapi;

import com.byteimagination.ytapi.exceptions.InvalidCredentials;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrentAPI extends API {

  private final WebClient client;
  private boolean signedIn;

  public CurrentAPI(String url) {
    super(url);
    client = new WebClient();
  }

  @Override
  public void signIn(String login, String password) {
    try {
      XmlPage page = (XmlPage) post("/rest/user/login",
        new NameValuePair("login", login),
        new NameValuePair("password", password));
      assertSignedIn(page);
      signedIn = true;
    } catch (FailingHttpStatusCodeException e) {
      throw new InvalidCredentials();
    }
    signedIn = true;
  }

  private void assertSignedIn(XmlPage page) {
    Document document = Jsoup.parse(page.asXml());
    if (!"ok".equalsIgnoreCase(document.select("login").text()))
      throw new InvalidCredentials();
  }

  @Override
  public boolean signedIn() {
    return signedIn;
  }

  private Page post(String path, NameValuePair... parameters) {
    try {
      WebRequest request = new WebRequest(new URL(url + path));
      request.setHttpMethod(HttpMethod.POST);
      List<NameValuePair> requestParameters = new ArrayList<NameValuePair>();
      Collections.addAll(requestParameters, parameters);
      request.setRequestParameters(requestParameters);
      return client.getPage(request);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}

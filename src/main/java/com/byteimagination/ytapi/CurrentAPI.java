package com.byteimagination.ytapi;

import com.byteimagination.ytapi.exceptions.InvalidCredentials;
import com.byteimagination.ytapi.models.Project;
import com.byteimagination.ytapi.models.ProjectReference;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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

  public void putProject(String projectId, String projectName, Integer startingNumber, String projectLeadLogin,
                         String description) {
    put("/rest/admin/project/" + projectId,
      new NameValuePair("projectName", projectName),
      new NameValuePair("startingNumber", startingNumber.toString()),
      new NameValuePair("projectLeadLogin", projectLeadLogin),
      new NameValuePair("description", description));
  }

  public Collection<ProjectReference> getProjects() {
    XmlPage page = (XmlPage) get("/rest/admin/project");
    Document document = Jsoup.parse(page.asXml());
    Collection<ProjectReference> projectReferences = new ArrayList<ProjectReference>();
    for (Element projectElement : document.select("project")) {
      ProjectReference projectReference = new ProjectReference();
      projectReference.id = projectElement.attr("id");
      projectReference.url = projectElement.attr("url");
      projectReferences.add(projectReference);
    }
    return projectReferences;
  }

  public Project getProject(String id) {
    XmlPage page = (XmlPage) get("/rest/admin/project/" + id);
    Document document = Jsoup.parse(page.asXml());
    Project project = new Project();
    Element projectElement = document.select("project").first();
    project.name = projectElement.attr("name");
    project.id = projectElement.attr("id");
    project.lead = projectElement.attr("lead");
    project.assigneesUrl = projectElement.attr("assignessUrl");
    project.subsystemsUrls = projectElement.attr("subsystemsUrl");
    project.buildsUrl = projectElement.attr("buildsUrl");
    project.versionsUrl = projectElement.attr("versionsUrl");
    String startingNumber = projectElement.attr("startingNumber");
    if (startingNumber != null && !startingNumber.isEmpty())
      project.startingNumber = Integer.valueOf(startingNumber);
    return project;
  }

  public void deleteProject(String id) {
    delete("/rest/admin/project/" + id);
  }

  private Page get(String path, NameValuePair... parameters) {
    return performRequest(HttpMethod.GET, path, parameters);
  }

  private Page post(String path, NameValuePair... parameters) {
    return performRequest(HttpMethod.POST, path, parameters);
  }

  private Page put(String path, NameValuePair... parameters) {
    return performRequest(HttpMethod.PUT, path, true, parameters);
  }

  private Page delete(String path, NameValuePair... parameters) {
    return performRequest(HttpMethod.DELETE, path, parameters);
  }

  private Page performRequest(HttpMethod method, String path, NameValuePair... parameters) {
    return performRequest(method, path, false, parameters);
  }

  private Page performRequest(HttpMethod method, String path,
                              boolean appendParametersToUrl, NameValuePair... parameters) {
    try {
      URL url = prepareUrl(path, appendParametersToUrl, parameters);
      WebRequest request = new WebRequest(url);
      request.setHttpMethod(method);
      setParameters(appendParametersToUrl, request, parameters);
      return client.getPage(request);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private URL prepareUrl(String path, boolean appendParametersToUrl,
                         NameValuePair... parameters) throws MalformedURLException {
    StringBuilder urlStringBuilder = new StringBuilder();
    urlStringBuilder.append(url).append(path);
    if (appendParametersToUrl)
      urlStringBuilder.append(prepareParametersString(parameters));
    return new URL(urlStringBuilder.toString());
  }

  private String prepareParametersString(NameValuePair... parameters) {
    StringBuilder parametersString = new StringBuilder();
    for (NameValuePair parameter : parameters) {
      appendParameterSeparator(parametersString);
      parametersString.append(parameter.getName()).append("=").append(parameter.getValue());
    }
    return parametersString.toString();
  }

  private void appendParameterSeparator(StringBuilder parametersString) {
    if (parametersString.length() == 0)
      parametersString.append("?");
    else
      parametersString.append("&");
  }

  private void setParameters(boolean appendParametersToUrl, WebRequest request, NameValuePair[] parameters) {
    if (appendParametersToUrl)
      return;
    List<NameValuePair> requestParameters = new ArrayList<NameValuePair>();
    Collections.addAll(requestParameters, parameters);
    request.setRequestParameters(requestParameters);
  }

}

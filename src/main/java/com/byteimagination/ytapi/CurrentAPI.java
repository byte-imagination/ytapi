package com.byteimagination.ytapi;

import com.byteimagination.ytapi.exceptions.InvalidCredentials;
import com.byteimagination.ytapi.models.*;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.gargoylesoftware.htmlunit.xml.XmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

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
    return project;
  }

  public void deleteProject(String id) {
    delete("/rest/admin/project/" + id);
  }

  @Override
  public void puBuildBundle(String bundleName) {
    String body = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
      "<buildBundle name=\"" + bundleName + "\"></buildBundle>";
    put("/rest/admin/customfield/buildBundle", body);
  }

  @Override
  public BuildBundle getBuildBundle(String bundleName) {
    XmlPage page = (XmlPage) get("/rest/admin/customfield/buildBundle/" + bundleName);
    Document document = Jsoup.parse(page.asXml());
    Element buildBundleElement = document.select("buildBundle").first();
    BuildBundle buildBundle = new BuildBundle();
    buildBundle.name = buildBundleElement.attr("name");
    Elements buildElements = buildBundleElement.select("build");
    for (Element buildElement : buildElements) {
      Build build = parseBuildElement(buildElement);
      buildBundle.builds.add(build);
    }
    return buildBundle;
  }

  @Override
  public void deleteBuildBundle(String bundleName) {
    delete("/rest/admin/customfield/buildBundle/" + bundleName);
  }

  @Override
  public void putBuild(String bundleName, String buildName, String description, Integer colorIndex, Date assembleDate) {
    put("/rest/admin/customfield/buildBundle/" + bundleName + "/" + buildName,
      new NameValuePair("description", description),
      new NameValuePair("colorIndex", colorIndex.toString()),
      new NameValuePair("assembleDate", "" + assembleDate.getTime()));
  }

  @Override
  public Build getBuild(String bundleName, String buildName) {
    XmlPage page = (XmlPage) get("/rest/admin/customfield/buildBundle/" + bundleName + "/" + buildName);
    Document document = Jsoup.parse(page.asXml());
    Element buildElement = document.select("build").first();
    return parseBuildElement(buildElement);
  }

  @Override
  public void deleteBuild(String bundleName, String buildName) {
    delete("/rest/admin/customfield/buildBundle/" + bundleName + "/" + buildName);
  }

  @Override
  public String putIssue(String project, String summary, String description, String permittedGroup) {
    Page page = put("/rest/issue",
      new NameValuePair("project", project),
      new NameValuePair("summary", summary),
      new NameValuePair("description", description),
      new NameValuePair("permittedGroup", permittedGroup));
    String location = page.getWebResponse().getResponseHeaderValue("Location");
    return location.substring(location.lastIndexOf("/") + 1, location.length());
  }

  @Override
  public Issue getIssue(String id, Boolean wikifyDescription) {
    XmlPage page = (XmlPage) get("/rest/issue/" + id,
      new NameValuePair("wikifyDescription", wikifyDescription.toString()));
    Document document = Jsoup.parse(page.asXml());
    Element issueElement = document.select("issue").first();
    Issue issue = new Issue();
    issue.id = issueElement.attr("id");
    issue.projectShortName = issueElement.select("field[name=projectShortName]").text();
    issue.numberInProject = Long.valueOf(issueElement.select("field[name=numberInProject]").text());
    issue.description = issueElement.select("field[name=description]").text();
    issue.created = new Date(Long.valueOf(issueElement.select("field[name=created]").text()));
    issue.updated = new Date(Long.valueOf(issueElement.select("field[name=updated]").text()));
    issue.updaterName = issueElement.select("field[name=updaterName]").text();
    if (!issueElement.select("field[name=resolved]").isEmpty())
      issue.resolved = new Date(Long.valueOf(issueElement.select("field[name=resolved]").text()));
    issue.reporterName = issueElement.select("field[name=reporterName]").text();
    issue.summary = issueElement.select("field[name=summary]").text();
    issue.commentsCount = Long.valueOf(issueElement.select("field[name=commentsCount]").text());
    issue.votes = Long.valueOf(issueElement.select("field[name=votes]").text());
    issue.permittedGroup = issueElement.select("field[name=permittedGroup]").text();
    parseComments(issue, issueElement);
    parseFields(issue, issueElement);
    return issue;
  }

  private void parseComments(Issue issue, Element issueElement) {
    Elements commentElements = issueElement.select("comments");
    for (Element commentElement : commentElements) {
      IssueComment comment = new IssueComment();
      comment.id = commentElement.attr("id");
      comment.author = commentElement.attr("author");
      comment.issueId = commentElement.attr("issueId");
      comment.deleted = Boolean.valueOf(commentElement.attr("deleted"));
      comment.text = commentElement.attr("text");
      comment.shownForIssueAuthor = Boolean.valueOf(commentElement.attr("shownForIssueAuthor"));
      comment.created = new Date(Long.valueOf(commentElement.attr("created")));
      issue.comments.add(comment);
    }
  }

  private void parseFields(Issue issue, Element issueElement) {
    Elements fieldElements = issueElement.select("field");
    for (Element fieldElement : fieldElements) {
      IssueField field = new IssueField();
      field.name = fieldElement.attr("name");
      Elements valueElements = fieldElement.select("value");
      for (Element valueElement : valueElements) {
        IssueFieldValue value = new IssueFieldValue();
        value.role = valueElement.attr("role");
        value.type = valueElement.attr("type");
        value.value = valueElement.text();
        field.values.add(value);
      }
      issue.fields.add(field);
    }
  }

  private Build parseBuildElement(Element buildElement) {
    Build build = new Build();
    build.name = buildElement.text();
    build.assembleDate = buildElement.attr("assembleDate");
    return build;
  }

  private Page get(String path, NameValuePair... parameters) {
    return performRequest(HttpMethod.GET, path, parameters);
  }

  private Page post(String path, NameValuePair... parameters) {
    return performRequest(HttpMethod.POST, path, parameters);
  }

  private Page put(String path, NameValuePair... parameters) {
    return put(path, null, parameters);
  }

  private Page put(String path, String body, NameValuePair... parameters) {
    return performRequest(HttpMethod.PUT, path, true, body, parameters);
  }

  private Page delete(String path, NameValuePair... parameters) {
    return performRequest(HttpMethod.DELETE, path, parameters);
  }

  private Page performRequest(HttpMethod method, String path, NameValuePair... parameters) {
    return performRequest(method, path, false, null, parameters);
  }

  private Page performRequest(HttpMethod method, String path,
                              boolean appendParametersToUrl, String body, NameValuePair... parameters) {
    try {
      URL url = prepareUrl(path, appendParametersToUrl, parameters);
      WebRequest request = new WebRequest(url);
      request.setHttpMethod(method);
      if (body != null) {
        request.getAdditionalHeaders().put("Content-Type", "application/xml");
        request.setRequestBody(body);
      }
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

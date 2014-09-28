# ytapi - YouTrack API

Simple library to simplify communication between your tools and YouTrack.

## Table of Contents

* [Testing](#testing)
* [Usage](#usage)

## Testing

In the src/test/resources directory create a file named youtrack_data.personal.properties based on the sample located in this directory.

# Usage

First, you need to sign in:

```java
YouTrack youtrack = new CurrentApi(YOU_TRACK_HOST_URL);
youtrack.signIn(YOU_TRACK_LOGIN, YOU_TRACK_PASSWORD);
```
Then you can:

- create new project

```java
youtrack.putProject("projectIdentifier", "Project Name", 1, "root", "My first project created with ytapi.");
```

- get list of existing projects

```java
youtrack.getProjects();
```

- get details of selected project

```java
youtrack.getProject("projectIdentifier");
```

- remove existing project

youtrack.deleteProject("projectIdentifier");
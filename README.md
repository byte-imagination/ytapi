# ytapi - YouTrack API

Simple library to simplify communication between your tools and YouTrack.

## Table of Contents

* [Testing](#testing)
* [Usage](#usage)

## Testing

In the src/test/resources directory create a file named youtrack_data.personal.properties based on the sample located in this directory.

# Usage

```java
YouTrack youtrack = new CurrentApi(YOU_TRACK_HOST_URL);
youtrack.signIn(YOU_TRACK_LOGIN, YOU_TRACK_PASSWORD);
```
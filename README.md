Google App Engine Auth0 Callback
================================

Background
----------

Our Google App Engine (GAE) site made in Java needed a authorization with social media and with registration. Auth0 solved all problems but the Java sample project couldn't be fully used by GAE since it used Resty which produced to following exception message:

No CookieHandler. Running on GAE? Fine. No cookie support for you!

Caused by

    java.security.AccessControlException: access denied ("java.net.NetPermission" "setDefaultAuthenticator")
    at java.security.AccessControlContext.checkPermission(AccessControlContext.java:375)

Setup
-----

Follow the guidelines at Auth0.com

[https://auth0.com/docs/quickstart/webapp/java](https://auth0.com/docs/quickstart/webapp/java)

Suggested implementation of (above) webapp to GAE

1. Add to maven

```
    &lt;dependency&gt;
        &lt;groupId&gt;com.auth0&lt;/groupId&gt;
        &lt;artifactId&gt;auth0-servlet&lt;/artifactId&gt;
        &lt;version&gt;2.0&lt;/version&gt;
    &lt;/dependency&gt;
```
    
2. Add GAEAuth0Callback files to your source

3. Instead of using Auth0Callback, use this setup in web.xml

```
    &lt;!-- Auth0 servlets --&gt;
    &lt;servlet&gt;
        &lt;servlet-name&gt;RedirectCallback&lt;/servlet-name&gt;
        &lt;servlet-class&gt;com.auth0.gae.GAEAuth0Callback&lt;/servlet-class&gt;
    &lt;init-param&gt;
        &lt;param-name&gt;auth0.redirect_on_success&lt;/param-name&gt;
        &lt;param-value&gt;/auth0/&lt;/param-value&gt;
    &lt;/init-param&gt;
    &lt;init-param&gt;
        &lt;param-name&gt;auth0.redirect_on_error&lt;/param-name&gt;
        &lt;param-value&gt;/signin/&lt;/param-value&gt;
    &lt;/init-param&gt;
    &lt;init-param&gt;
        &lt;param-name&gt;gae.auth0.token_attribute_key&lt;/param-name&gt;
        &lt;param-value&gt;auth0tokens&lt;/param-value&gt;
    &lt;/init-param&gt;
    &lt;init-param&gt;
        &lt;param-name&gt;gae.auth0.user_attribute_key&lt;/param-name&gt;
        &lt;param-value&gt;user&lt;/param-value&gt;
    &lt;/init-param&gt;
    &lt;/servlet&gt;
    &lt;servlet-mapping&gt;
        &lt;servlet-name&gt;RedirectCallback&lt;/servlet-name&gt;
        &lt;url-pattern&gt;/auth0-callback/&lt;/url-pattern&gt;
    &lt;/servlet-mapping&gt;
```

  
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
    <dependency>
        <groupId>com.auth0</groupId>
        <artifactId>auth0-servlet</artifactId>
        <version>2.0</version>
    </dependency>
```
    
2. Add GAEAuth0Callback files to your source

3. Instead of using Auth0Callback, use this setup in web.xml

```
    <!-- Auth0 servlets -->
    <servlet>
        <servlet-name>RedirectCallback</servlet-name>
        <servlet-class>com.auth0.gae.GAEAuth0Callback</servlet-class>
    <init-param>
        <param-name>auth0.redirect_on_success</param-name>
        <param-value>/auth0/</param-value>
    </init-param>
    <init-param>
        <param-name>auth0.redirect_on_error</param-name>
        <param-value>/signin/</param-value>
    </init-param>
    <init-param>
        <param-name>gae.auth0.token_attribute_key</param-name>
        <param-value>auth0tokens</param-value>
    </init-param>
    <init-param>
        <param-name>gae.auth0.user_attribute_key</param-name>
        <param-value>user</param-value>
    </init-param>
    </servlet>
    <servlet-mapping>
        <servlet-name>RedirectCallback</servlet-name>
        <url-pattern>/auth0-callback/</url-pattern>
    </servlet-mapping>
```

  
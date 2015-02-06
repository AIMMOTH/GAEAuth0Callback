package com.auth0.gae;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.auth0.Auth0ServletCallback;
import com.auth0.Tokens;
import com.google.gson.JsonObject;

/**
 * <p>Super class Auth0ServletCallback uses Resty which on Google App Engine (GAE) throws the following sarcast exception message:
 * <p>"No CookieHandler. Running on GAE? Fine. No cookie support for you!"
 * <p>This rewrite excludes the lazy usage of Resty and uses simple HttpURLConnection instead. Also, session stored results are made serialized and called GAEAuth0User and GAEAuth0Tokens.
 * <p>Additional settings can be made to set session attribute key for GAEAuth0User and GAEAuth0Tokens. Full example of web.xml entry:
 *
 * <pre>
 &lt;servlet&gt;
    &lt;servlet-name&gt;RedirectCallback&lt;/servlet-name&gt;
    &lt;servlet-class&gt;se.ce.workaround.GAEAuth0Callback&lt;/servlet-class&gt;
    &lt;init-param&gt;
      &lt;param-name&gt;auth0.redirect_on_success&lt;/param-name&gt;
      &lt;param-value&gt;/auth0-success/&lt;/param-value&gt;
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
 </pre>
 *
 * @see com.auth0.Auth0ServletCallback
 * @see java.net.HttpURLConnection
 * @see com.auth0.gae.GAEAuth0User
 * @see com.auth0.gae.GAETokens
 */
public class GAEAuth0Callback extends Auth0ServletCallback {

	/**
	 *
	 */
	private static final long serialVersionUID = -7912869527734069604L;

	private static final Logger log = Logger.getLogger(GAEAuth0Callback.class.getName());

	private Properties properties = new Properties();
	private String redirectOnSuccess;
	private String redirectOnFail;
	private String tokenAttributeKey;
	private String userAttributeKey;

	protected void store(final GAETokens tokens, final GAEAuth0User user, final HttpServletRequest req) {
		final HttpSession session = req.getSession();

		// Save tokens on a persistent session
		session.setAttribute(tokenAttributeKey, tokens);
		session.setAttribute(userAttributeKey, user);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException,
			ServletException {
		if (isValidRequest(request, response)) {
			try {
				final GAETokens tokens = fetchTokens(request, response);
				final GAEAuth0User user = fetchUser(tokens.toTokens());
				store(tokens, user, request);
				onSuccess(request, response);
			} catch (final IllegalArgumentException | IllegalStateException | JSONException ex) {
				onFailure(request, response, ex);
			}
		}
	}

	private GAETokens fetchTokens(final HttpServletRequest request, final HttpServletResponse response) throws IOException, JSONException {

		final URL tokenUrl = new URL(getUri("/oauth/token"));

		final JsonObject postJson = new JsonObject();

		/*
		 * state is read as parameter from Auth0 callback.
		 * redirect_uri should point back here to read the access token and identity id
		 */
		postJson.addProperty("code", request.getParameter("code"));
		postJson.addProperty("client_id", properties.getProperty("auth0.client_id"));
		postJson.addProperty("client_secret", properties.getProperty("auth0.client_secret"));
		postJson.addProperty("grant_type", "authorization_code");
		postJson.addProperty("redirect_uri", request.getRequestURL().toString());

		// Post JSON and read returned JSON
		final JSONObject returnedJson = post(tokenUrl, postJson);

		try {
			if (returnedJson.has("access_token") && returnedJson.has("id_token")) {
				return new GAETokens(returnedJson);
			} else {
				throw new IllegalStateException("Cannot get Token from Auth0");
			}
		} catch (final JSONException e1) {
			throw e1;
		}
	}

	private JSONObject post(final URL url, final JsonObject postData) throws IOException, JSONException {
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

		final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		writer.write(postData.toString());
		writer.close();

		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return new JSONObject(readConnection(connection.getInputStream()));
		} else {
			return new JSONObject("{errorCode:" + connection.getResponseCode() + ",message:\""
					+ connection.getResponseMessage() + "\"}");
		}
	}

	private String get(final URL url) throws IOException, JSONException {
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return readConnection(connection.getInputStream());
		} else {
			return "{errorCode:" + connection.getResponseCode() + ",message:\""
					+ connection.getResponseMessage() + "\"}";
		}
	}

	private String readConnection(final InputStream stream) throws IOException {
		try {
			final long streamLength = stream.available();
			final byte[] bytes = new byte[(int) streamLength];

			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length && (numRead = stream.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}
			return new String(bytes);
		} finally {
			stream.close();
		}
	}

	/*
	 * Use access token to read user info from
	 */
	private GAEAuth0User fetchUser(final Tokens tokens) {
		final String userInfoUri = getUri("/userinfo?access_token=" + tokens.getAccessToken());
		try {
			final String json = get(new URL(userInfoUri));
			return new GAEAuth0User(json);
		} catch (final Exception ex) {
			throw new IllegalStateException("Cannot get User from Auth0", ex);
		}
	}

	private String getUri(final String path) {
		return String.format("https://%s%s", properties.get("auth0.domain"), path);
	}

	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);

		redirectOnSuccess= readParameter("auth0.redirect_on_success", config);
		redirectOnFail   = readParameter("auth0.redirect_on_error", config);
		tokenAttributeKey= readParameter("gae.auth0.token_attribute_key", config);
		userAttributeKey = readParameter("gae.auth0.user_attribute_key", config);

		log.fine("Reading config ...");
		log.fine("auth0.redirect_on_succes=" + redirectOnSuccess);
		log.fine("auth0.redirect_on_error=" + redirectOnFail);
		log.fine("gae.auth0.token_attribute_key=" + tokenAttributeKey);
		log.fine("gae.auth0.user_attribute_key=" + userAttributeKey);

		for (final String param : asList("auth0.client_id", "auth0.client_secret", "auth0.domain")) {
			final String value = readParameter(param, config);
			log.fine(param + "=" + value);
			properties.put(param, value);
		}
	}

	static String readParameter(final String parameter, final ServletConfig config) {
		final String first = config.getInitParameter(parameter);
		if (hasValue(first)) {
			return first;
		}
		final String second = config.getServletContext().getInitParameter(parameter);
		if (hasValue(second)) {
			return second;
		}
		throw new IllegalArgumentException(parameter + " needs to be defined");
	}

	private static boolean hasValue(final String value) {
		return value != null && value.trim().length() > 0;
	}

	private boolean isValidRequest(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException {
		if (hasError(request) || !isValidState(request)) {
			return false;
		}
		return true;
	}

	private boolean isValidState(final HttpServletRequest req) {
		return req.getParameter("state") != null && req.getParameter("state").equals(getNonceStorage(req).getState());
	}

	private static boolean hasError(final HttpServletRequest req) {
		return req.getParameter("error") != null;
	}
}
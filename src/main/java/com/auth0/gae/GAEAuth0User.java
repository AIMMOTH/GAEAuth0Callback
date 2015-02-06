package com.auth0.gae;

import java.io.Serializable;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

/**
 * A copy of Auth0User but added serialization. Cannot inherit since Auth0User doesn't have an empty constructor.
 *
 * @see com.auth0.Auth0User
 * @author Carl
 *
 */
public class GAEAuth0User implements Principal, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -2291573291265602673L;

	private String json;

	public GAEAuth0User() {

	}

	public GAEAuth0User(final String json) {
		this.json = json;
	}

	public static GAEAuth0User get(final HttpServletRequest req) {
		return (GAEAuth0User) req.getSession().getAttribute("user");
	}

	public String getProperty(final String prop) {
		return get(prop, String.class);
	}

	@SuppressWarnings("unchecked")
	private <T> T get(final String prop, final Class<T> clazz) {
		try {
			final JSONObject o = new JSONObject(json);
			return (T) o.get(prop);
		} catch(final JSONException ex) {
			throw new IllegalStateException("Cannot get property " + prop + " from Auth0user", ex);
		}
	}

	@Override
	public String getName() {
		return getProperty("name");
	}

	public String getEmail() {
		return getProperty("email");
	}

	public String getUserId() {
		return getProperty("user_id");
	}

	public String getNickname() {
		return getProperty("nickname");
	}

	public String getPicture() {
		return getProperty("picture");
	}

	public JSONArray getIdentities() {
		return get("identities", JSONArray.class);
	}

	@Override
	public String toString() {
		return json;
	}
}

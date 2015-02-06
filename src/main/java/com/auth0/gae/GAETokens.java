package com.auth0.gae;

import java.io.Serializable;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

import com.auth0.Tokens;

/**
 * A copy of Tokens but added serialization. Cannot inherit since Tokens doesn't have an empty constructor.
 *
 * @see com.auth0.Tokens
 * @author Carl
 *
 */
public class GAETokens implements Serializable {

    /**
	   *
	   */
	  private static final long serialVersionUID = -584195882646185695L;
	  private String idToken;
    private String accessToken;

    @Override
    public String toString() {
    	return "{idToken:\"" + idToken + "\",accessToken:\"" + accessToken + "\n}";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final GAETokens tokens = (GAETokens) o;

        if (accessToken != null ? !accessToken.equals(tokens.accessToken) : tokens.accessToken != null) return false;
        if (idToken != null ? !idToken.equals(tokens.idToken) : tokens.idToken != null) return false;

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = idToken != null ? idToken.hashCode() : 0;
        result = 31 * result + (accessToken != null ? accessToken.hashCode() : 0);
        return result;
    }

    public GAETokens() {

    }

    public GAETokens(final String idToken, final String accessToken) {
        this.idToken = idToken;
        this.accessToken = accessToken;
    }

    public GAETokens(final JSONObject json) throws JSONException {
    	this((String) json.get("id_token"), (String) json.get("access_token"));
    }

	public String getAccessToken() {
        return accessToken;
    }

	public String getIdToken() {
        return idToken;
    }

	public boolean exist() {
		return getIdToken() != null && getAccessToken() != null;
	}

	public Tokens toTokens() {
		return new Tokens(idToken, accessToken);
	}
}

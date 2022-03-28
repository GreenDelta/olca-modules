package org.openlca.io.openepd;

import java.io.File;
import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;
import org.slf4j.LoggerFactory;

public class Ec3Credentials {

	private String ec3Url;
	private String epdUrl;
	private String user;
	private String token;
	private String password;

	public String ec3Url() {
		return ec3Url;
	}

	public Ec3Credentials ec3Url(String url) {
		this.ec3Url = url;
		return this;
	}

	public String epdUrl() {
		return epdUrl;
	}

	public Ec3Credentials epdUrl(String url) {
		this.epdUrl = url;
		return this;
	}

	public String user() {
		return user;
	}

	public Ec3Credentials user(String user) {
		this.user = user;
		return this;
	}

	public String password() {
		return password;
	}

	public Ec3Credentials password(String password) {
		this.password = password;
		return this;
	}

	public String token() {
		return token;
	}

	public Ec3Credentials token(String token) {
		this.token = token;
		return this;
	}

	public static Ec3Credentials getDefault(File file) {
		var c = new Ec3Credentials();
		c.ec3Url = "https://buildingtransparency.org/api";
		c.epdUrl = "https://openepd.buildingtransparency.org/api";
		if (!file.exists())
			return c;
		try {
			var json = Json.readObject(file).orElse(null);
			if (json == null)
				return c;
			c.ec3Url = Objects.requireNonNullElse(
				Json.getString(json, "ec3Url"), c.ec3Url);
			c.epdUrl = Objects.requireNonNullElse(
				Json.getString(json, "epdUrl"), c.epdUrl);
			c.user = Json.getString(json, "user");
			c.token = Json.getString(json, "token");
			return c;
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Ec3Credentials.class);
			log.error("failed to read EC3 credentials from " + file, e);
			return c;
		}
	}

	public void save(File file) {
		var json = new JsonObject();
		json.addProperty("ec3Url", ec3Url);
		json.addProperty("epdUrl", epdUrl);
		json.addProperty("user", user);
		json.addProperty("token", token);
		try {
			Json.write(json, file);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Ec3Credentials.class);
			log.error("failed to write EC3 credentials to " + file, e);
		}
	}

}

package org.openlca.cloud.util;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.openlca.cloud.api.RepositoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class WebRequests {

	private static final Logger log = LoggerFactory.getLogger(WebRequests.class);

	static {
	}

	public static ClientResponse call(Type type, String url, String sessionId) throws WebRequestException {
		return call(type, url, sessionId, null);
	}

	public static ClientResponse call(Type type, String url, String sessionId, Object data) throws WebRequestException {
		log.info(type.name() + " " + url);
		Builder request = builder(url, sessionId, data);
		try {
			ClientResponse response = call(type, request);
			if (response.getStatus() >= 400 && response.getStatus() <= 599)
				throw new WebRequestException(url, response);
			if (response.getStatusInfo().getFamily() == Family.REDIRECTION)
				return call(type, response.getLocation().toString(), sessionId, data);
			return response;
		} catch (Exception e) {
			if (e instanceof WebRequestException)
				throw e;
			throw new WebRequestException(url, e);
		}
	}

	private static ClientResponse call(Type type, Builder builder) {
		switch (type) {
		case GET:
			return builder.get(ClientResponse.class);
		case POST:
			return builder.post(ClientResponse.class);
		case PUT:
			return builder.put(ClientResponse.class);
		case DELETE:
			return builder.delete(ClientResponse.class);
		default:
			return null;
		}
	}

	private static Builder builder(String url, String sessionId, Object data) {
		WebResource resource = createClient().resource(url);
		Builder builder = resource.accept(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_PLAIN_TYPE,
				MediaType.APPLICATION_OCTET_STREAM_TYPE);
		builder.header("lca-cs-client-api-version", RepositoryClient.API_VERSION);
		if (sessionId != null)
			builder.cookie(new Cookie("JSESSIONID", sessionId));
		if (data instanceof InputStream)
			builder.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		else if (data != null)
			builder.entity(new Gson().toJson(data), MediaType.APPLICATION_JSON_TYPE);
		return builder;
	}

	private static Client createClient() {
		ClientConfig config = new DefaultClientConfig();
		SSLContext context = Ssl.createContext();
		if (context != null) {
			config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
					new HTTPSProperties(HttpsURLConnection.getDefaultHostnameVerifier(), context));
		}
		config.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);
		Client client = Client.create(config);
		client.setChunkedEncodingSize(1024 * 100); // 100kb
		return client;
	}

	public static enum Type {
		GET, POST, PUT, DELETE;
	}

	public static class WebRequestException extends Exception {

		private static final long serialVersionUID = 1423557937866180113L;
		private int errorCode;
		private String host;
		private int port;

		private WebRequestException(String url, ClientResponse response) {
			super(toMessage(response));
			setHostAndPort(url);
			this.errorCode = response.getStatus();
		}

		private void setHostAndPort(String url) {
			if (url.startsWith("https://")) {
				url = url.substring(8);
				port = 443;
			} else if (url.startsWith("http://")) {
				url = url.substring(7);
				port = 80;
			}
			host = url.substring(0, url.indexOf("/"));
			if (host.contains(":")) {
				port = Integer.parseInt(host.substring(host.indexOf(":") + 1));
				host = host.substring(0, host.indexOf(":"));
			}
		}

		private WebRequestException(String url, Exception e) {
			super(e);
			setHostAndPort(url);
			this.errorCode = 500;
		}

		public int getErrorCode() {
			return errorCode;
		}

		@Override
		public String getMessage() {
			if (isConnectException())
				return "Server " + host + " on port " + port + " unavailable";
			if (isUnauthorized() && Strings.isNullOrEmpty(super.getMessage()))
				return "Invalid credentials";
			return super.getMessage();
		}

		public String getOriginalMessage() {
			return super.getMessage();
		}

		public boolean isConnectException() {
			if (getCause() instanceof ConnectException)
				return true;
			if (getCause() instanceof SocketException && getCause().getCause() instanceof ClientHandlerException)
				return true;
			if (!(getCause() instanceof ClientHandlerException))
				return false;
			if (getCause().getCause() instanceof ConnectException)
				return true;
			return false;
		}

		public boolean isSslCertificateException() {
			if ((getCause() instanceof ClientHandlerException))
				if (getCause().getCause() instanceof SSLHandshakeException)
					return true;
			return false;
		}

		private static String toMessage(ClientResponse response) {
			return response.getEntity(String.class);
		}

		public boolean isUnauthorized() {
			return errorCode == Status.UNAUTHORIZED.getStatusCode();
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}

	}

}

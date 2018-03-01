package org.openlca.cloud.util;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
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

	public static ClientResponse call(Type type, String url, String sessionId, Object data)
			throws WebRequestException {
		log.info(type.name() + " " + url);
		Builder request = builder(url, sessionId, data);
		try {
			ClientResponse response = call(type, request);
			if (response.getStatus() >= 400 && response.getStatus() <= 599)
				throw new WebRequestException(response);
			return response;
		} catch (Exception e) {
			if (e instanceof WebRequestException)
				throw e;
			throw new WebRequestException(e);
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
		Builder builder = resource.accept(MediaType.APPLICATION_JSON_TYPE,
				MediaType.TEXT_PLAIN_TYPE, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		if (sessionId != null)
			builder.cookie(new Cookie("JSESSIONID", sessionId));
		if (data == null)
			return builder;
		if (data instanceof InputStream)
			builder.entity(data, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		else
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

		private WebRequestException(ClientResponse response) {
			super(response.getEntity(String.class));
			this.errorCode = response.getStatus();
		}

		private WebRequestException(Exception e) {
			super(e);
			this.errorCode = 500;
		}

		public int getErrorCode() {
			return errorCode;
		}
		
		@Override
		public String getMessage() {
			if (isConnectException()) 
				return "Server unavailable";
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

	}

}

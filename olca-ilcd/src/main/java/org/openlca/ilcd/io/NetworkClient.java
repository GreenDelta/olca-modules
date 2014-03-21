package org.openlca.ilcd.io;

import java.io.IOException;
import java.util.Iterator;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.openlca.ilcd.descriptors.DescriptorList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

public class NetworkClient implements DataStore {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String baseUri;
	private String user;
	private String password;
	private Client client;
	private boolean isConnected = false;
	private XmlBinder binder = new XmlBinder();

	public NetworkClient(String baseUri) {
		this(baseUri, null, null);
	}

	public NetworkClient(String baseUrl, String user, String password) {
		this.baseUri = baseUrl;
		this.user = user;
		this.password = password;
	}

	public void connect() throws DataStoreException {
		log.info("Create ILCD network connection {}", baseUri);
		DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
		config.getProperties().put(
				DefaultApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);
		client = ApacheHttpClient.create(config);
		if (user != null || password != null) {
			authenticate();
		}
		isConnected = true;
	}

	private void authenticate() throws DataStoreException {
		log.info("Authenticate user: {}", user);
		ClientResponse response = client.resource(baseUri).path("authenticate")
				.path("login").queryParam("userName", user)
				.queryParam("password", password).get(ClientResponse.class);
		eval(response);
		log.trace("Server response: {}", response.getEntity(String.class));
	}

	public Authentication getAuthentication() throws DataStoreException {
		checkConnection();
		log.trace("Get authentication information.");
		ClientResponse response = client.resource(baseUri).path("authenticate")
				.path("status").get(ClientResponse.class);
		eval(response);
		Authentication authentication = response
				.getEntity(Authentication.class);
		return authentication;
	}

	@Override
	public <T> T get(Class<T> type, String id) throws DataStoreException {
		checkConnection();
		WebResource resource = resource(type, id);
		log.info("Get resource: {}", resource.getURI());
		ClientResponse response = resource.get(ClientResponse.class);
		eval(response);
		try {
			return binder.fromStream(type, response.getEntityInputStream());
		} catch (Exception e) {
			throw new DataStoreException("Failed to load resource " + id
					+ " of type " + type, e);
		}
	}

	@Override
	public void put(Object obj, String id) throws DataStoreException {
		checkConnection();
		WebResource resource = client.resource(baseUri).path(
				Path.forClass(obj.getClass()));
		log.info("Publish resource: {}/{}", resource.getURI(), id);
		try {
			byte[] bytes = binder.toByteArray(obj);
			ClientResponse response = resource.type(MediaType.APPLICATION_XML)
					.post(ClientResponse.class, bytes);
			eval(response);
			log.trace("Server response: {}", fetchMessage(response));
		} catch (Exception e) {
			throw new DataStoreException("Failed to upload resource " + obj
					+ " with id " + id, e);
		}
	}

	@Override
	public <T> void delete(Class<T> type, String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> Iterator<T> iterator(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> boolean contains(Class<T> type, String id)
			throws DataStoreException {
		checkConnection();
		WebResource resource = resource(type, id);
		log.trace("Contains resource {} ?", resource.getURI());
		ClientResponse response = resource.head();
		log.trace("Server response: {}", response);
		return response.getStatus() == Status.OK.getStatusCode();
	}

	public DescriptorList search(Class<?> type, String name)
			throws DataStoreException {
		checkConnection();
		String term = null;
		if (name == null)
			term = "";
		else
			term = name.trim();
		WebResource resource = client.resource(baseUri)
				.path(Path.forClass(type)).queryParam("search", "true")
				.queryParam("name", term);
		log.trace("Search resources: {}", resource.getURI());
		DescriptorList list = resource.get(DescriptorList.class);
		return list;
	}

	private void checkConnection() throws DataStoreException {
		if (!isConnected) {
			connect();
		}
	}

	private <T> WebResource resource(Class<T> type, String id) {
		WebResource resource = client.resource(baseUri)
				.path(Path.forClass(type)).path(id).queryParam("format", "xml");
		return resource;
	}

	private void eval(ClientResponse response) throws DataStoreException {
		if (response == null)
			throw new DataStoreException("Client response is NULL.");
		Status status = Status.fromStatusCode(response.getStatus());
		Family family = status.getFamily();
		if (family == Family.CLIENT_ERROR || family == Family.SERVER_ERROR) {
			String message = status.getReasonPhrase() + ": "
					+ fetchMessage(response);
			throw new DataStoreException(message);
		}
	}

	private String fetchMessage(ClientResponse response) {
		if (response.hasEntity())
			return response.getEntity(String.class);
		return "";
	}

	@Override
	public void close() throws IOException {
		client.destroy();
	}

}

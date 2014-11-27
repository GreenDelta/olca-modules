package org.openlca.ilcd.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.openlca.ilcd.descriptors.DataStock;
import org.openlca.ilcd.descriptors.DataStockList;
import org.openlca.ilcd.descriptors.DescriptorList;
import org.openlca.ilcd.sources.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.StreamDataBodyPart;

public class NetworkClient implements DataStore {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String baseUri;
	private String user;
	private String password;
	private Client client;
	private boolean isConnected = false;
	private DataStock dataStock;
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

	public DataStockList getDataStockList() throws DataStoreException {
		checkConnection();
		log.trace("get data stocks");
		WebResource resource = client.resource(baseUri)
				.path("datastocks");
		return resource.get(DataStockList.class);
	}

	public void setDataStock(DataStock dataStock) {
		this.dataStock = dataStock;
	}

	@Override
	public <T> T get(Class<T> type, String id) throws DataStoreException {
		checkConnection();
		WebResource resource = initGetRequest(type).path(id).queryParam(
				"format", "xml");
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
			Builder builder = resource.type(MediaType.APPLICATION_XML);
			if (dataStock != null) {
				log.trace("post to data stock {}", dataStock.getUuid());
				builder = builder.header("stock", dataStock.getUuid());
			}
			ClientResponse response = builder.post(ClientResponse.class, bytes);
			eval(response);
			log.trace("Server response: {}", fetchMessage(response));
		} catch (Exception e) {
			throw new DataStoreException("Failed to upload resource " + obj
					+ " with id " + id, e);
		}
	}

	@Override
	public void put(Source source, String id, File file)
			throws DataStoreException {
		checkConnection();
		WebResource resource = client.resource(baseUri).path(
				Path.forClass(source.getClass()));
		log.info("Publish source with file: {} + {}", id, file);
		try {
			MultiPart multiPart = new MultiPart();
			multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
			Builder builder = resource.type(MediaType.MULTIPART_FORM_DATA_TYPE);
			if (dataStock != null) {
				log.trace("post to data stock {}", dataStock.getUuid());
				builder = builder.header("stock", dataStock.getUuid());
			}
			byte[] bytes = binder.toByteArray(source);
			ByteArrayInputStream xmlStream = new ByteArrayInputStream(bytes);
			multiPart.bodyPart(new StreamDataBodyPart("file", xmlStream));
			FileInputStream fileStream = new FileInputStream(file);
			multiPart.bodyPart(new StreamDataBodyPart(file.getName(),
					fileStream));
			ClientResponse resp = builder.post(ClientResponse.class, multiPart);
			eval(resp);
			log.trace("Server response: {}", fetchMessage(resp));
		} catch (Exception e) {
			throw new DataStoreException("Failed to upload source " + id
					+ " with file " + file, e);
		}

	}

	@Override
	public <T> boolean delete(Class<T> type, String id) {
		// TODO Auto-generated method stub
		return false;
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
		WebResource resource = initGetRequest(type).path(id).queryParam(
				"format", "xml");
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
		WebResource resource = initGetRequest(type)
				.queryParam("search", "true")
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

	private WebResource initGetRequest(Class<?> type) {
		if (dataStock == null)
			return client.resource(baseUri).path(Path.forClass(type));
		else
			return client.resource(baseUri).path("datastocks")
					.path(dataStock.getUuid()).path(Path.forClass(type));
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

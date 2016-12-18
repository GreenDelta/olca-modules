package org.openlca.ilcd.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

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
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * A client interface of a Soda4LCA service end-point.
 */
public class SodaClient implements DataStore {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private final SodaConnection con;

	private Client client;
	private boolean isConnected = false;
	private XmlBinder binder = new XmlBinder();

	public SodaClient(SodaConnection con) {
		this.con = con;
	}

	public void connect() throws DataStoreException {
		log.info("Create ILCD network connection {}", con);
		DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
		config.getProperties().put(
				DefaultApacheHttpClientConfig.PROPERTY_HANDLE_COOKIES, true);
		client = ApacheHttpClient.create(config);
		if (con.user != null || con.password != null) {
			authenticate();
		}
		isConnected = true;
	}

	private void authenticate() throws DataStoreException {
		log.info("Authenticate user: {}", con.user);
		ClientResponse response = client.resource(con.url).path("authenticate")
				.path("login").queryParam("userName", con.user)
				.queryParam("password", con.password).get(ClientResponse.class);
		eval(response);
		log.trace("Server response: {}", response.getEntity(String.class));
	}

	public AuthInfo getAuthentication() throws DataStoreException {
		checkConnection();
		log.trace("Get authentication information.");
		ClientResponse response = client.resource(con.url).path("authenticate")
				.path("status").get(ClientResponse.class);
		eval(response);
		AuthInfo authentication = response
				.getEntity(AuthInfo.class);
		return authentication;
	}

	public DataStockList getDataStockList() throws DataStoreException {
		checkConnection();
		log.trace("get data stocks");
		WebResource resource = client.resource(con.url)
				.path("datastocks");
		return resource.get(DataStockList.class);
	}

	@Override
	public <T> T get(Class<T> type, String id) throws DataStoreException {
		checkConnection();
		WebResource resource = client.resource(con.url)
				.path(Dir.get(type)).path(id).queryParam("format", "xml");
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
		WebResource resource = client.resource(con.url).path(
				Dir.get(obj.getClass()));
		log.info("Publish resource: {}/{}", resource.getURI(), id);
		try {
			byte[] bytes = binder.toByteArray(obj);
			Builder builder = resource.type(MediaType.APPLICATION_XML);
			if (con.dataStockId != null) {
				log.trace("post to data stock {}", con.dataStockId);
				builder = builder.header("stock", con.dataStockId);
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
		WebResource resource = client.resource(con.url).path(
				"sources/withBinaries");
		log.info("Publish source with file: {} + {}", id, file);
		try {
			FormDataMultiPart multiPart = new FormDataMultiPart();
			if (con.dataStockId != null) {
				log.trace("post to data stock {}", con.dataStockId);
				multiPart.field("stock", con.dataStockId);
			}
			byte[] bytes = binder.toByteArray(source);
			ByteArrayInputStream xmlStream = new ByteArrayInputStream(bytes);
			FormDataBodyPart xmlPart = new FormDataBodyPart("file", xmlStream,
					MediaType.MULTIPART_FORM_DATA_TYPE);
			multiPart.bodyPart(xmlPart);
			FileInputStream fileStream = new FileInputStream(file);
			FormDataBodyPart filePart = new FormDataBodyPart(file.getName(),
					fileStream, MediaType.MULTIPART_FORM_DATA_TYPE);
			multiPart.bodyPart(filePart);
			ClientResponse resp = resource.type(
					MediaType.MULTIPART_FORM_DATA_TYPE)
					.post(ClientResponse.class, multiPart);
			eval(resp);
			log.trace("Server response: {}", fetchMessage(resp));
		} catch (Exception e) {
			throw new DataStoreException("Failed to upload source " + id
					+ " with file " + file, e);
		}
	}

	public InputStream getExternalDocument(String sourceId, String fileName)
			throws DataStoreException {
		checkConnection();
		WebResource resource = client.resource(con.url).path(
				"sources").path(sourceId).path(fileName);
		log.info("Get external document {} for source {}", fileName, sourceId);
		try {
			return resource.type(MediaType.APPLICATION_OCTET_STREAM).get(
					InputStream.class);
		} catch (Exception e) {
			throw new DataStoreException("Failed to get file " + fileName +
					"for source " + sourceId, e);
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
		WebResource resource = client.resource(con.url)
				.path(Dir.get(type)).path(id).queryParam("format", "xml");
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
		WebResource resource = initSearchRequest(type)
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

	private WebResource initSearchRequest(Class<?> type) {
		if (con.dataStockId == null)
			return client.resource(con.url).path(Dir.get(type));
		else
			return client.resource(con.url).path("datastocks")
					.path(con.dataStockId).path(Dir.get(type));
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

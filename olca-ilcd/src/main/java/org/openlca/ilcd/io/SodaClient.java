package org.openlca.ilcd.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response.Status.Family;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
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
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;

/**
 * A client interface of a Soda4LCA service end-point.
 */
public class SodaClient implements DataStore {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private final SodaConnection con;

	private Client client;
	private List<Cookie> cookies = new ArrayList<>();
	private boolean isConnected = false;
	private XmlBinder binder = new XmlBinder();

	public SodaClient(SodaConnection con) {
		this.con = con;
	}

	public void connect() throws DataStoreException {
		log.info("Create ILCD network connection {}", con);
		client = Client.create();
		authenticate();
		isConnected = true;
	}

	private void authenticate() throws DataStoreException {
		if (con.user == null || con.user.trim().isEmpty()
				|| con.password == null || con.password.trim().isEmpty()) {
			log.info("no user or password -> anonymous access");
			return;
		}
		log.info("Authenticate user: {}", con.user);
		ClientResponse response = client.resource(con.url).path("authenticate")
				.path("login").queryParam("userName", con.user)
				.queryParam("password", con.password).get(ClientResponse.class);
		eval(response);
		log.trace("Server response: {}", response.getEntity(String.class));
		for (NewCookie c : response.getCookies()) {
			cookies.add(c.toCookie());
		}
	}

	public AuthInfo getAuthentication() throws DataStoreException {
		checkConnection();
		log.trace("Get authentication information.");
		WebResource r = resource("authenticate", "status");
		ClientResponse response = cookies(r).get(ClientResponse.class);
		eval(response);
		AuthInfo authInfo = response.getEntity(AuthInfo.class);
		return authInfo;
	}

	public DataStockList getDataStockList() throws DataStoreException {
		checkConnection();
		log.trace("get data stocks");
		WebResource r = resource("datastocks");
		return cookies(r).get(DataStockList.class);
	}

	@Override
	public <T> T get(Class<T> type, String id) throws DataStoreException {
		checkConnection();
		WebResource r = resource(Dir.get(type), id).queryParam("format", "xml");
		log.info("Get resource: {}", r.getURI());
		ClientResponse response = cookies(r).get(ClientResponse.class);
		eval(response);
		try {
			return binder.fromStream(type, response.getEntityInputStream());
		} catch (Exception e) {
			throw new DataStoreException("Failed to load resource " + id
					+ " of type " + type, e);
		}
	}

	@Override
	public void put(IDataSet ds) throws DataStoreException {
		checkConnection();
		WebResource r = resource(Dir.get(ds.getClass()));
		log.info("Publish resource: {}/{}", r.getURI(), ds.getUUID());
		try {
			byte[] bytes = binder.toByteArray(ds);
			Builder builder = cookies(r).type(MediaType.APPLICATION_XML);
			if (con.dataStockId != null) {
				log.trace("post to data stock {}", con.dataStockId);
				builder = builder.header("stock", con.dataStockId);
			}
			ClientResponse response = builder.post(ClientResponse.class, bytes);
			eval(response);
			log.trace("Server response: {}", fetchMessage(response));
		} catch (Exception e) {
			throw new DataStoreException("Failed to upload data set + " + ds +
					":  " + e.getMessage(), e);
		}
	}

	@Override
	public void put(Source source, File[] files) throws DataStoreException {
		checkConnection();
		log.info("Publish source with files {}", source);
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
			addFiles(files, multiPart);
			WebResource r = resource("sources/withBinaries");
			ClientResponse resp = cookies(r).type(
					MediaType.MULTIPART_FORM_DATA_TYPE)
					.post(ClientResponse.class, multiPart);
			eval(resp);
			log.trace("Server response: {}", fetchMessage(resp));
		} catch (Exception e) {
			throw new DataStoreException("Failed to upload source with file: "
					+ e.getMessage(), e);
		}
	}

	private void addFiles(File[] files, FormDataMultiPart multiPart)
			throws Exception {
		if (files == null)
			return;
		for (File file : files) {
			if (file == null)
				continue;
			FileInputStream is = new FileInputStream(file);
			FormDataBodyPart part = new FormDataBodyPart(file.getName(),
					is, MediaType.MULTIPART_FORM_DATA_TYPE);
			multiPart.bodyPart(part);
		}
	}

	public InputStream getExternalDocument(String sourceId, String fileName)
			throws DataStoreException {
		checkConnection();
		WebResource r = resource("sources", sourceId, fileName);
		log.info("Get external document {} for source {}", fileName, sourceId);
		try {
			return cookies(r).type(MediaType.APPLICATION_OCTET_STREAM).get(
					InputStream.class);
		} catch (Exception e) {
			throw new DataStoreException("Failed to get file " + fileName +
					"for source " + sourceId + ": " + e.getMessage(), e);
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
		WebResource r = resource(Dir.get(type), id)
				.queryParam("format", "xml");
		log.trace("Contains resource {} ?", r.getURI());
		ClientResponse response = cookies(r).head();
		log.trace("Server response: {}", response);
		return response.getStatus() == Status.OK.getStatusCode();
	}

	/** Includes also the version in the check. */
	public boolean contains(Ref ref) throws DataStoreException {
		if (ref == null || ref.type == null || ref.uuid == null)
			return false;
		checkConnection();
		WebResource r = resource(Dir.get(ref.getDataSetClass()), ref.uuid)
				.queryParam("format", "xml");
		if (ref.version != null)
			r = r.queryParam("version", ref.version);
		ClientResponse response = cookies(r).head();
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
		WebResource r = initSearchRequest(type)
				.queryParam("search", "true")
				.queryParam("name", term);
		log.trace("Search resources: {}", r.getURI());
		DescriptorList list = cookies(r).get(DescriptorList.class);
		return list;
	}

	private WebResource resource(String... path) {
		WebResource r = client.resource(con.url);
		for (String p : path) {
			r = r.path(p);
		}
		return r;
	}

	private Builder cookies(WebResource r) {
		Builder b = r.getRequestBuilder();
		for (Cookie c : cookies)
			b.cookie(c);
		return b;
	}

	private void checkConnection() throws DataStoreException {
		if (!isConnected) {
			connect();
		}
	}

	private WebResource initSearchRequest(Class<?> type) {
		if (con.dataStockId == null)
			return resource(Dir.get(type));
		else
			return resource("datastocks", con.dataStockId, Dir.get(type));
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

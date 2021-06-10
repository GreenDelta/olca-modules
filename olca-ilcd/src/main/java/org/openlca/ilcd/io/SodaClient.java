package org.openlca.ilcd.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.descriptors.CategorySystemList;
import org.openlca.ilcd.descriptors.DataStockList;
import org.openlca.ilcd.descriptors.Descriptor;
import org.openlca.ilcd.descriptors.DescriptorList;
import org.openlca.ilcd.lists.CategorySystem;
import org.openlca.ilcd.sources.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.Response.Status.Family;


/**
 * A client interface of a Soda4LCA service end-point.
 */
public class SodaClient implements DataStore {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final SodaConnection con;
	private final List<Cookie> cookies = new ArrayList<>();
	private final XmlBinder binder = new XmlBinder();

	private Client client;
	private boolean isConnected = false;

	public SodaClient(SodaConnection con) {
		this.con = con;
	}

	public void connect() {
		log.info("Create ILCD network connection {}", con);
		client = ClientBuilder.newClient()
			.register(MultiPartFeature.class);
		authenticate();
		isConnected = true;
	}

	private void authenticate() {
		if (con.user == null || con.user.trim().isEmpty()
			|| con.password == null || con.password.trim().isEmpty()) {
			log.info("no user or password -> anonymous access");
			return;
		}
		log.info("Authenticate user: {}", con.user);
		var response = client.target(con.url)
			.path("authenticate")
			.path("login")
			.queryParam("userName", con.user)
			.queryParam("password", con.password)
			.request()
			.get();
		eval(response);
		response.getCookies().forEach((key, value) -> cookies.add(value.toCookie()));
	}

	public AuthInfo getAuthInfo() {
		checkConnection();
		log.trace("Get authentication information.");
		var r = resource("authenticate", "status");
		var response = cookies(r).get();
		eval(response);
		return response.readEntity(AuthInfo.class);
	}

	public DataStockList getDataStockList() {
		checkConnection();
		log.trace("get data stock list: /datastocks");
		var r = resource("datastocks");
		return cookies(r).get(DataStockList.class);
	}

	public CategorySystemList getCategorySystemList() {
		checkConnection();
		log.trace("get category system list: /categorySystems");
		var r = resource("categorySystems");
		return cookies(r).get(CategorySystemList.class);
	}

	public CategorySystem getCategorySystem(String name) {
		checkConnection();
		log.trace("get category system list: /categorySystems/{}", name);
		var r = resource("categorySystems", name);
		return cookies(r).get(CategorySystem.class);
	}

	@Override
	public <T extends IDataSet> T get(Class<T> type, String id) {
		checkConnection();
		var r = resource(Dir.get(type), id).queryParam("format", "xml");
		log.info("Get resource: {}", r.getUri());
		var response = cookies(r).get();
		eval(response);
		try {
			return binder.fromStream(type, response.readEntity(InputStream.class));
		} catch (Exception e) {
			throw new RuntimeException("Failed to load resource " + id
				+ " of type " + type, e);
		}
	}

	@Override
	public void put(IDataSet ds) {
		checkConnection();
		var r = resource(Dir.get(ds.getClass()));
		log.info("Publish resource: {}/{}", r.getUri(), ds.getUUID());
		try {
			byte[] bytes = binder.toByteArray(ds);
			var builder = cookies(r).accept(MediaType.APPLICATION_XML);
			if (con.dataStockId != null) {
				log.trace("post to data stock {}", con.dataStockId);
				builder = builder.header("stock", con.dataStockId);
			}
			var response = builder.post(Entity.xml(bytes));
			eval(response);
			log.trace("Server response: {}", fetchMessage(response));
		} catch (Exception e) {
			throw new RuntimeException("Failed to upload data set + " + ds, e);
		}
	}

	@Override
	public void put(Source source, File[] files) {
		checkConnection();
		log.info("Publish source with files {}", source);
		try {
			var formData = new FormDataMultiPart();
			if (con.dataStockId != null) {
				log.trace("post to data stock {}", con.dataStockId);
				formData.field("stock", con.dataStockId);
			}

			// add the XML as `file` parameter
			byte[] bytes = binder.toByteArray(source);
			var xmlStream = new ByteArrayInputStream(bytes);
			var xmlPart = new FormDataBodyPart("file", xmlStream,
				MediaType.MULTIPART_FORM_DATA_TYPE);
			formData.bodyPart(xmlPart);

			// add the other files
			addFiles(files, formData);


			var r = resource("sources/withBinaries");
			var resp = cookies(r)
				.post(Entity.entity(formData, MediaType.MULTIPART_FORM_DATA_TYPE));
			eval(resp);
			log.trace("Server response: {}", fetchMessage(resp));
		} catch (Exception e) {
			throw new RuntimeException("Failed to upload source with file", e);
		}
	}

	private void addFiles(File[] files, FormDataMultiPart multiPart)
		throws Exception {
		if (files == null)
			return;
		for (File file : files) {
			if (file == null)
				continue;
			var is = new FileInputStream(file);
			var part = new FormDataBodyPart(file.getName(),
				is, MediaType.MULTIPART_FORM_DATA_TYPE);
			multiPart.bodyPart(part);
		}
	}

	@Override
	public InputStream getExternalDocument(String sourceId, String fileName) {
		checkConnection();
		var r = resource("sources", sourceId, fileName);
		log.info("Get external document {} for source {}", fileName, sourceId);
		var response = cookies(r)
			.accept(MediaType.APPLICATION_OCTET_STREAM)
			.get();
		eval(response);
		return response.readEntity(InputStream.class);
	}

	@Override
	public <T extends IDataSet> boolean delete(Class<T> type, String id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T extends IDataSet> Iterator<T> iterator(Class<T> type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends IDataSet> boolean contains(Class<T> type, String id) {
		checkConnection();
		var r = resource(Dir.get(type), id)
			.queryParam("format", "xml");
		log.trace("Contains resource {} ?", r.getUri());
		var response = cookies(r).head();
		log.trace("Server response: {}", response);
		return response.getStatus() == Status.OK.getStatusCode();
	}

	/**
	 * Includes also the version in the check.
	 */
	public boolean contains(Ref ref) {
		if (ref == null || ref.type == null || ref.uuid == null)
			return false;
		checkConnection();
		var r = resource(Dir.get(ref.getDataSetClass()), ref.uuid)
			.queryParam("format", "xml");
		if (ref.version != null)
			r = r.queryParam("version", ref.version);
		var response = cookies(r).head();
		return response.getStatus() == Status.OK.getStatusCode();
	}

	public DescriptorList search(Class<?> type, String name) {
		try {
			checkConnection();
			String term = name == null ? "" : name.trim();
			var r = con.dataStockId == null
				? resource(Dir.get(type))
				: resource("datastocks", con.dataStockId, Dir.get(type));
			r = r.queryParam("search", "true")
				.queryParam("name", term);
			log.trace("Search resources: {}", r.getUri());
			return cookies(r).get(DescriptorList.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<Descriptor> getDescriptors(Class<?> type) {
		log.debug("get descriptors for {}", type);
		try {
			checkConnection();
			var r = con.dataStockId == null
				? resource(Dir.get(type))
				: resource("datastocks", con.dataStockId, Dir.get(type));
			r = r.queryParam("pageSize", "1000");
			List<Descriptor> list = new ArrayList<>();
			int total;
			int idx = 0;
			do {
				log.debug("get descriptors for {} @startIndex={}", type, idx);
				r = r.queryParam("startIndex", Integer.toString(idx));
				DescriptorList data = cookies(r).get(DescriptorList.class);
				total = data.totalSize;
				int fetched = data.descriptors.size();
				if (fetched == 0)
					break;
				list.addAll(data.descriptors);
				idx += fetched;
			} while (list.size() < total);
			return list;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private WebTarget resource(String... path) {
		var target = client.target(con.url);
		for (String p : path) {
			target = target.path(p);
		}
		return target;
	}

	private Invocation.Builder cookies(WebTarget target) {
		var builder = target.request();
		for (Cookie c : cookies) {
			builder.cookie(c);
		}
		return builder;
	}

	private void checkConnection() {
		if (!isConnected) {
			connect();
		}
	}

	private void eval(Response resp) {
		if (resp == null)
			throw new IllegalArgumentException("Client response is NULL.");
		Status status = Status.fromStatusCode(resp.getStatus());
		Family family = status.getFamily();
		if (family == Family.CLIENT_ERROR || family == Family.SERVER_ERROR) {
			String message = status.getReasonPhrase()
				+ ": " + fetchMessage(resp);
			throw new RuntimeException(message);
		}
	}

	private String fetchMessage(Response response) {
		if (response.hasEntity())
			return response.readEntity(String.class);
		return "";
	}

	@Override
	public void close() {
		client.close();
	}
}

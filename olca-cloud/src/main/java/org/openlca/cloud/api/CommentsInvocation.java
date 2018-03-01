package org.openlca.cloud.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.openlca.cloud.model.Comment;
import org.openlca.cloud.util.Valid;
import org.openlca.cloud.util.WebRequests;
import org.openlca.cloud.util.WebRequests.Type;
import org.openlca.cloud.util.WebRequests.WebRequestException;
import org.openlca.core.model.ModelType;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Invokes a web service call to retrieve comments of a dataset from a
 * repository
 */
class CommentsInvocation {

	private static final String PATH = "/comment/";
	String baseUrl;
	String sessionId;
	String repositoryId;
	ModelType type;
	String refId;

	List<Comment> execute() throws WebRequestException {
		Valid.checkNotEmpty(baseUrl, "base url");
		Valid.checkNotEmpty(repositoryId, "repository id");
		String url = baseUrl + PATH + repositoryId;
		boolean forDataset = type != null && refId != null;
		if (forDataset) {
			url += "/" + type.name() + "/" + refId;
		} else {
			url += "?includeReplies=true";
		}
		try {
			ClientResponse response = WebRequests.call(Type.GET, url, sessionId);
			Map<String, Object> data = new Gson().fromJson(
					response.getEntity(String.class),
					new TypeToken<Map<String, Object>>() {
					}.getType());
			String field = forDataset ? "comments" : "data";
			return parseComments(data.get(field));
		} catch (WebRequestException e) {
			if (e.getErrorCode() == Status.SERVICE_UNAVAILABLE.getStatusCode()) 
				return new ArrayList<>();
			throw e;
		}
	}

	private List<Comment> parseComments(Object value) {
		if (value == null)
			return new ArrayList<>();
		if (!(value instanceof Collection))
			return new ArrayList<>();
		List<Comment> comments = new ArrayList<>();
		for (Object o : (Collection<?>) value) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) o;
			Comment comment = new Comment();
			comment.id = toLong(map, "id");
			comment.date = toDate(map, "date");
			comment.replyTo = toLong(map, "replyTo");
			comment.text = toString(map, "text");
			comment.user = toString(toMap(map, "user"), "name");
			comment.released = is(map, "released");
			comment.approved = is(map, "approved");
			Map<String, Object> fieldMap = toMap(map, "field");
			comment.type = toType(fieldMap, "modelType");
			comment.refId = toString(fieldMap, "refId");
			comment.path = toString(fieldMap, "path");
			comments.add(comment);
		}
		return comments;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> toMap(Map<String, Object> data, String property) {
		if (!data.containsKey(property))
			return null;
		Object value = data.get(property);
		if (value == null)
			return null;
		if (!(value instanceof Map))
			return null;
		return (Map<String, Object>) value;
	}

	private String toString(Map<String, Object> data, String property) {
		if (data == null || !data.containsKey(property))
			return null;
		Object value = data.get(property);
		if (value == null)
			return null;
		return value.toString();
	}

	private ModelType toType(Map<String, Object> data, String property) {
		String type = toString(data, property);
		if (type == null)
			return null;
		return ModelType.valueOf(type);
	}

	private long toLong(Map<String, Object> data, String property) {
		if (data == null || !data.containsKey(property))
			return 0;
		Object value = data.get(property);
		if (value == null)
			return 0;
		if (value instanceof Long)
			return (Long) value;
		if (value instanceof Integer)
			return ((Integer) value).longValue();
		if (value instanceof Double)
			return ((Double) value).longValue();
		if (value instanceof Float)
			return ((Float) value).longValue();
		if (value instanceof String)
			return Long.parseLong(value.toString());
		return 0;
	}

	private Date toDate(Map<String, Object> data, String property) {
		long time = toLong(data, property);
		if (time == 0)
			return null;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar.getTime();
	}

	private boolean is(Map<String, Object> data, String property) {
		if (data == null || !data.containsKey(property))
			return false;
		Object value = data.get(property);
		if (value == null)
			return false;
		if (value instanceof Boolean)
			return (Boolean) value;
		if (value instanceof String)
			return Boolean.parseBoolean(value.toString());
		return false;
	}

}

package org.openlca.cloud.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Comments {

	private final Map<String, List<CommentDescriptor>> byRefId = new HashMap<>();
	private final Map<String, List<CommentDescriptor>> byPath = new HashMap<>();

	public Comments(List<CommentDescriptor> comments) {
		initialize(comments);
	}

	public List<CommentDescriptor> getForRefId(String refId) {
		return get(byRefId, refId);
	}

	public List<CommentDescriptor> getForPath(String path) {
		return get(byPath, PathMap.get(path));
	}

	private List<CommentDescriptor> get(Map<String, List<CommentDescriptor>> map, String key) {
		List<CommentDescriptor> comments = map.get(key);
		if (comments == null)
			return new ArrayList<>();
		sort(comments);
		return comments;
	}

	public boolean hasRefId(String refId) {
		return has(byRefId, refId);
	}

	public boolean hasPath(String path) {
		return has(byPath, PathMap.get(path));
	}

	private boolean has(Map<String, List<CommentDescriptor>> map, String key) {
		List<CommentDescriptor> comments = map.get(key);
		return comments != null && !comments.isEmpty();
	}

	public boolean hasAnyPath(String path) {
		for (String key : byPath.keySet()) {
			String nKey = key;
			if (!nKey.contains("["))
				continue;
			while (nKey.contains("[")) {
				nKey = nKey.substring(0, nKey.indexOf("[")) + nKey.substring(nKey.indexOf("]") + 1);
			}
			if (nKey.equals(PathMap.get(path)) && !byPath.get(key).isEmpty())
				return true;
		}
		return false;
	}

	private void initialize(List<CommentDescriptor> comments) {
		for (CommentDescriptor comment : comments) {
			put(byRefId, comment.refId, comment);
			put(byPath, comment.path, comment);
		}
	}

	private void put(Map<String, List<CommentDescriptor>> map, String key, CommentDescriptor value) {
		List<CommentDescriptor> list = map.get(key);
		if (list == null) {
			map.put(key, list = new ArrayList<>());
		}
		list.add(value);
	}

	private void sort(List<CommentDescriptor> list) {
		List<CommentDescriptor> sorted = new ArrayList<>();
		Set<Long> added = new HashSet<>();
		Collections.sort(list, (a, b) -> b.date.compareTo(a.date));
		for (CommentDescriptor comment : list) {
			if (added.contains(comment.id))
				continue;
			if (comment.replyTo != 0)
				continue;
			sorted.add(comment);
			added.add(comment.id);
			List<CommentDescriptor> replies = new ArrayList<>();
			for (CommentDescriptor c : list) {
				if (c.replyTo != comment.id)
					continue;
				replies.add(c);
				added.add(c.id);
			}
			Collections.sort(replies, (a, b) -> a.date.compareTo(b.date));
			sorted.addAll(replies);
		}
		list.clear();
		list.addAll(sorted);
	}

}

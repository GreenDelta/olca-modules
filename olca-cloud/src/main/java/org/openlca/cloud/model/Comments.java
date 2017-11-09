package org.openlca.cloud.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Comments {

	private final Map<String, List<CommentDescriptor>> comments = new HashMap<>();

	public Comments(List<CommentDescriptor> comments) {
		initialize(comments);
	}

	public List<CommentDescriptor> get(String path) {
		List<CommentDescriptor> comments = this.comments.get(PathMap.get(path));
		if (comments == null)
			return new ArrayList<>();
		sort(comments);
		return comments;
	}

	public boolean has(String path) {
		List<CommentDescriptor> comments = this.comments.get(PathMap.get(path));
		return comments != null && !comments.isEmpty();
	}

	private void initialize(List<CommentDescriptor> comments) {
		for (CommentDescriptor comment : comments) {
			List<CommentDescriptor> forPath = this.comments.get(comment.path);
			if (forPath == null) {
				this.comments.put(comment.path, forPath = new ArrayList<>());
			}
			forPath.add(comment);
		}
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

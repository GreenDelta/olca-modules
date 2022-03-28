package org.openlca.io.openepd;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import org.openlca.jsonld.Json;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

public class Ec3CategoryTree {

	private final Ec3Category root;
	private final Map<String, Ec3Category> index = new HashMap<>();
	private final Map<String, String> paths = new HashMap<>();

	private Ec3CategoryTree(Ec3Category root) {
		this.root = root;
	}

	public static Ec3CategoryTree empty() {
		return new Ec3CategoryTree(null);
	}

	public boolean isEmpty() {
		return root == null;
	}

	public Ec3Category root() {
		return root;
	}

	public static Ec3CategoryTree of(Ec3Category category) {
		var idx = new Ec3CategoryTree(category);
		if (category == null)
			return idx;
		var queue = new ArrayDeque<Ec3Category>();
		queue.add(category);
		while (!queue.isEmpty()) {
			var next = queue.poll();
			idx.index.put(next.id, next);
			for (var sub : next.subCategories) {
				if (!sub.parents.contains(next.id)) {
					sub.parents.add(next.id);
				}
				queue.add(sub);
			}
		}
		return idx;
	}

	public static Ec3CategoryTree fromFile(File file) {
		if (file == null || !file.exists())
			return Ec3CategoryTree.empty();
		var json = Json.readObject(file);
		if (json.isEmpty())
			return Ec3CategoryTree.empty();
		var root = Ec3Category.fromJson(json.get());
		return root.isEmpty()
			? Ec3CategoryTree.empty()
			: Ec3CategoryTree.of(root.get());
	}

	/**
	 * Tries to save this category tree to the given file. If this tree is empty
	 * and the given file exists, it tries to delete the given file.
	 */
	public void save(File file) {
		try {
			if (root != null) {
				Json.write(root.toJson(), file);
				return;
			}
			if (file.exists()) {
				Files.delete(file.toPath());
			}
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(getClass());
			log.error("failed to delete file: " + file, e);
		}
	}

	public String pathOf(Ec3Category category) {
		if (category == null)
			return null;
		var path = pathOf(category.id);
		return Strings.nullOrEmpty(path)
			? category.name
			: path;
	}

	public String pathOf(String id) {
		if (id == null)
			return null;
		var path = paths.get(id);
		if (path != null)
			return path;
		var cat = index.get(id);
		if (cat == null)
			return null;
		var parent = cat.parents.isEmpty()
			? null
			: pathOf(cat.parents.get(0));
		path = parent == null
			? cat.name
			: parent + "/" + cat.name;
		paths.put(id, path);
		return path;
	}
}

package org.openlca.io.olca;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

class CategoryTransfer {

	private final Config conf;

	CategoryTransfer(Config conf) {
		this.conf = Objects.requireNonNull(conf);
	}

	static void syncAll(Config conf) {
		var dao = new CategoryDao(conf.source());
		for (var root : dao.getRootCategories()) {
			for (var path : Path.allFromTree(root)) {
				path.sync(conf);
			}
		}
	}

	Category sync(Category category) {
		if (category == null)
			return null;
		var targetId = conf.seq().get(ModelType.CATEGORY, category.id);
		if (targetId > 0) {
			var target = conf.target().get(Category.class, targetId);
			if (target != null)
				return target;
		}
		return Path.of(category).sync(conf);
	}

	private record Path(Category category, int length, Path parent) {

		static Path root(Category category) {
			return new Path(category, 1, null);
		}

		static Path of(Category category) {
			var stack = new Stack<Category>();
			var c = category;
			while (c != null) {
				stack.push(c);
				c = c.category;
			}

			Path path = null;
			int len = 0;
			while (!stack.isEmpty()) {
				len++;
				path = new Path(stack.pop(), len, path);
			}
			return path;
		}

		static List<Path> allFromTree(Category category) {
			if (category == null)
				return List.of();
			var queue = new ArrayDeque<Path>();
			queue.add(Path.root(category));
			var paths = new ArrayList<Path>();
			while (!queue.isEmpty()) {
				var path = queue.poll();
				var cs = path.category.childCategories;
				if (cs.isEmpty()) {
					paths.add(path);
					continue;
				}
				for (var ci : cs) {
					queue.add(path.append(ci));
				}
			}
			return paths;
		}

		private Path append(Category category) {
			return new Path(category, length + 1, this);
		}

		private String[] toStringArray() {
			var array = new String[length];
			var node = this;
			for (int i = length - 1; i >= 0; i--) {
				if (node == null) break;
				array[i] = node.category.name;
				node = node.parent;
			}
			return array;
		}

		Category sync(Config conf) {
			var target = CategoryDao.sync(
				conf.target(), category().modelType, toStringArray());
			var ti = target;
			var pi = this;
			while (ti != null && pi != null) {
				conf.seq().put(ModelType.CATEGORY, pi.category.id, ti.id);
				ti = ti.category;
				pi = pi.parent;
			}
			return target;
		}
	}

}

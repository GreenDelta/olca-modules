package org.openlca.io.olca;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public final class CategoryTransfer implements EntityTransfer<Category> {

	private final TransferConfig conf;

	public CategoryTransfer(TransferConfig conf) {
		this.conf = Objects.requireNonNull(conf);
	}

	@Override
	public void syncAll() {
		var dao = new CategoryDao(conf.source());
		for (var root : dao.getRootCategories()) {
			for (var path : Path.allFromTree(root)) {
				path.sync(conf);
			}
		}
	}

	@Override
	public Category sync(Category category) {
		if (category == null) return null;
		var mapped = conf.getMapped(category);
		return mapped == null
			? Path.of(category).sync(conf)
			: mapped;
	}

	private record Path(Category category, int length, Path parent) {

		static Path root(Category category) {
			return new Path(category, 1, null);
		}

		static Path of(Category category) {
			var stack = new ArrayDeque<Category>();
			var c = category;
			var visited = new HashSet<Long>(); // check for cycles
			while (c != null) {
				if (visited.contains(c.id)) {
					break;
				}
				visited.add(c.id);
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
			var visited = new HashSet<Long>(); // check for cycles

			while (!queue.isEmpty()) {
				var path = queue.poll();
				if (visited.contains(path.category.id)) {
					continue;
				}
				visited.add(path.category.id);

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

		private String[] segments() {
			var array = new String[length];
			var node = this;
			for (int i = length - 1; i >= 0; i--) {
				if (node == null) break;
				array[i] = node.category.name;
				node = node.parent;
			}
			return array;
		}

		Category sync(TransferConfig conf) {
			var target = CategoryDao.sync(
				conf.target(), category().modelType, segments());
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

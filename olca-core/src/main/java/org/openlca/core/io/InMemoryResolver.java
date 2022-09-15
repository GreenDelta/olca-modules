package org.openlca.core.io;

import org.openlca.core.model.Category;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.store.InMemoryStore;
import org.openlca.util.Strings;

public record InMemoryResolver(InMemoryStore store) implements EntityResolver {

	@Override
	public <T extends RootEntity> T get(Class<T> type, String refId) {
		return store.get(type, refId);
	}

	@Override
	public Category getCategory(ModelType type, String path) {
		if (Strings.nullOrEmpty(path))
			return null;
		var parts = path.split("/");
		var root = store.getAll(Category.class)
				.stream()
				.filter(c -> c.modelType == type
						&& Strings.nullOrEqual(c.name, parts[0]))
				.findAny()
				.orElse(null);
		if (root == null)
			return null;

		var category = root;
		for (int i = 1; i < parts.length; i++) {
			var name = parts[i];
			var next = category.childCategories.stream()
					.filter(c -> Strings.nullOrEqual(c.name, name))
					.findAny()
					.orElse(null);
			if (next == null)
				return null;
			category = next;
		}
		return category;
	}

	@Override
	public void resolveProvider(String providerId, Exchange exchange) {
		var process = store.get(Process.class, providerId);
		if (process != null) {
			exchange.defaultProviderId = process.id;
		}
	}
}

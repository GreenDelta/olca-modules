package org.openlca.git.iterator;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.git.Config;
import org.openlca.util.Categories;
import org.openlca.util.Categories.PathBuilder;

public class DatabaseIterator extends EntryIterator {

	private final Config config;
	private final PathBuilder categoryPaths;

	public DatabaseIterator(Config config) {
		this(config, init(config));
	}

	private DatabaseIterator(Config config, List<TreeEntry> entries) {
		super(entries);
		this.config = config;
		this.categoryPaths = Categories.pathsOf(config.database);
	}

	private DatabaseIterator(DatabaseIterator parent, Config config, List<TreeEntry> entries) {
		super(parent, entries);
		this.config = config;
		this.categoryPaths = Categories.pathsOf(config.database);
	}

	private static List<TreeEntry> init(Config config) {
		return Arrays.stream(ModelType.categorized()).filter(type -> {
			var dao = new CategoryDao(config.database);
			if (type == ModelType.CATEGORY)
				return false;
			if (!dao.getRootCategories(type).isEmpty())
				return true;
			return !Daos.categorized(config.database, type).getDescriptors(Optional.empty()).isEmpty();
		}).map(TreeEntry::new)
				.toList();
	}

	private static List<TreeEntry> init(Config config, ModelType type) {
		var entries = new CategoryDao(config.database).getRootCategories(type)
				.stream().map(TreeEntry::new)
				.collect(Collectors.toList());
		entries.addAll(Daos.categorized(config.database, type).getDescriptors(Optional.empty())
				.stream().map(d -> new TreeEntry(d))
				.toList());
		return entries;
	}

	private static List<TreeEntry> init(Config config, Category category) {
		var entries = category.childCategories
				.stream().map(TreeEntry::new)
				.collect(Collectors.toList());
		entries.addAll(Daos.categorized(config.database, category.modelType).getDescriptors(Optional.of(category))
				.stream().map(d -> new TreeEntry(d))
				.toList());
		return entries;
	}

	@Override
	public boolean hasId() {
		var e = getEntry();
		if (e.data instanceof ModelType)
			return config.store.has((ModelType) e.data);
		if (e.data instanceof Category)
			return config.store.has((Category) e.data);
		return config.store.has(categoryPaths, (CategorizedDescriptor) e.data);
	}

	@Override
	public byte[] idBuffer() {
		var e = getEntry();
		if (e.data instanceof ModelType)
			return config.store.getRaw((ModelType) e.data);
		if (e.data instanceof Category)
			return config.store.getRaw((Category) e.data);
		return config.store.getRaw(categoryPaths, (CategorizedDescriptor) e.data);
	}

	@Override
	public int idOffset() {
		return 0;
	}

	@Override
	public AbstractTreeIterator createSubtreeIterator(ObjectReader reader) {
		var entry = getEntry();
		if (entry.data instanceof ModelType type)
			return new DatabaseIterator(this, config, init(config, type));
		if (entry.data instanceof Category category)
			return new DatabaseIterator(this, config, init(config, category));
		return null;
	}

}

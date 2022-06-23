package org.openlca.git.iterator;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.ObjectIdStore;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Categories;
import org.openlca.util.Categories.PathBuilder;

public class DatabaseIterator extends EntryIterator {

	private final PathBuilder categoryPaths;
	private final IDatabase database;
	private final ObjectIdStore idStore;

	public DatabaseIterator(IDatabase database, ObjectIdStore idStore) {
		this(database, idStore, init(database));
	}

	private DatabaseIterator(IDatabase database, ObjectIdStore idStore, List<TreeEntry> entries) {
		super(entries);
		this.database = database;
		this.idStore = idStore;
		this.categoryPaths = Categories.pathsOf(database);
	}

	private DatabaseIterator(DatabaseIterator parent, IDatabase database, ObjectIdStore idStore,
			List<TreeEntry> entries) {
		super(parent, entries);
		this.database = database;
		this.idStore = idStore;
		this.categoryPaths = Categories.pathsOf(database);
	}

	private static List<TreeEntry> init(IDatabase database) {
		return Arrays.stream(ModelType.rootTypes()).filter(type -> {
			if (type == ModelType.CATEGORY)
				return false;
			var dao = new CategoryDao(database);
			if (!dao.getRootCategories(type).isEmpty())
				return true;
			return !Daos.root(database, type).getDescriptors(Optional.empty()).isEmpty();
		}).map(TreeEntry::new)
				.toList();
	}

	private static List<TreeEntry> init(IDatabase database, ModelType type) {
		var entries = new CategoryDao(database).getRootCategories(type).stream()
				.filter(c -> !c.isFromLibrary())
				.map(TreeEntry::new)
				.collect(Collectors.toList());
		entries.addAll(Daos.root(database, type).getDescriptors(Optional.empty()).stream()
				.filter(d -> !d.isFromLibrary())
				.map(TreeEntry::new)
				.toList());
		return entries;
	}

	private static List<TreeEntry> init(IDatabase database, Category category) {
		var entries = category.childCategories.stream()
				.filter(c -> !c.isFromLibrary())
				.map(TreeEntry::new)
				.collect(Collectors.toList());
		entries.addAll(Daos.root(database, category.modelType).getDescriptors(Optional.of(category)).stream()
				.filter(d -> !d.isFromLibrary())
				.map(TreeEntry::new)
				.toList());
		return entries;
	}

	@Override
	public boolean hasId() {
		if (idStore == null)
			return false;
		var data = getEntryData();
		if (data == null)
			return false;
		if (data instanceof ModelType)
			return idStore.has((ModelType) data);
		if (data instanceof Category)
			return idStore.has((Category) data);
		return idStore.has(categoryPaths, (RootDescriptor) data);
	}

	@Override
	public byte[] idBuffer() {
		if (idStore == null)
			return GitUtil.getBytes(ObjectId.zeroId());
		var data = getEntryData();
		if (data == null)
			return GitUtil.getBytes(ObjectId.zeroId());
		if (data instanceof ModelType)
			return idStore.getRaw((ModelType) data);
		if (data instanceof Category)
			return idStore.getRaw((Category) data);
		return idStore.getRaw(categoryPaths, (RootDescriptor) data);
	}

	@Override
	public int idOffset() {
		return 0;
	}

	@Override
	public DatabaseIterator createSubtreeIterator(ObjectReader reader) {
		var data = getEntryData();
		if (data instanceof ModelType type)
			return new DatabaseIterator(this, database, idStore, init(database, type));
		if (data instanceof Category category)
			return new DatabaseIterator(this, database, idStore, init(database, category));
		return null;
	}

}

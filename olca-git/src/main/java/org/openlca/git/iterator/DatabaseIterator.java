package org.openlca.git.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.GitIndex;
import org.openlca.git.util.Descriptors;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;

public class DatabaseIterator extends EntryIterator {

	private final GitIndex gitIndex;
	private final Descriptors descriptors;

	public DatabaseIterator(GitIndex gitIndex, Descriptors descriptors) {
		this(gitIndex, descriptors, init(descriptors));
	}

	public DatabaseIterator(GitIndex gitIndex, Descriptors descriptors, String path) {
		this(gitIndex, descriptors, init(descriptors, path));
	}

	private DatabaseIterator(GitIndex gitIndex, Descriptors descriptors, List<TreeEntry> entries) {
		super(entries);
		this.gitIndex = gitIndex;
		this.descriptors = descriptors;
	}

	private DatabaseIterator(DatabaseIterator parent, List<TreeEntry> entries) {
		super(parent, entries);
		this.gitIndex = parent.gitIndex;
		this.descriptors = parent.descriptors;
	}

	private static List<TreeEntry> init(Descriptors descriptors) {
		return Arrays.stream(ModelType.values()).filter(type -> {
			if (type == ModelType.CATEGORY)
				return false;
			if (!descriptors.getCategories(type).isEmpty())
				return true;
			return !descriptors.get(type).isEmpty();
		}).map(TreeEntry::new)
				.toList();
	}

	private static List<TreeEntry> init(Descriptors descriptors, String path) {
		if (Strings.nullOrEmpty(path))
			return init(descriptors);
		if (!path.contains("/")) {
			var modelType = ModelType.parse(path);
			if (modelType == null)
				return new ArrayList<>();
			return init(descriptors, modelType);
		}
		var category = descriptors.getCategory(path);
		if (category == null)
			return new ArrayList<>();
		return init(descriptors, category);
	}

	private static List<TreeEntry> init(Descriptors descriptors, ModelType type) {
		var entries = descriptors.getCategories(type).stream()
				.map(TreeEntry::new)
				.collect(Collectors.toList());
		entries.addAll(descriptors.get(type).stream()
				.filter(d -> !d.isFromLibrary())
				.map(TreeEntry::new)
				.toList());
		return entries;
	}

	private static List<TreeEntry> init(Descriptors descriptors, Category category) {
		var categories = category.childCategories;
		var entries = categories.stream()
				.filter(c -> !isFromLibrary(descriptors, c))
				.map(TreeEntry::new)
				.collect(Collectors.toList());
		entries.addAll(descriptors.get(category).stream()
				.filter(d -> !d.isFromLibrary())
				.map(TreeEntry::new)
				.toList());
		if (entries.isEmpty() && !isFromLibrary(descriptors, category)) {
			entries.add(TreeEntry.empty());
		}
		return entries;
	}

	private static boolean isFromLibrary(Descriptors descriptors, Category category) {
		var hasLibrariesElements = false;
		for (var model : descriptors.get(category)) {
			if (!model.isFromLibrary())
				return false;
			hasLibrariesElements = true;
		}
		for (var child : category.childCategories) {
			if (!isFromLibrary(descriptors, child))
				return false;
			hasLibrariesElements = true;
		}
		return hasLibrariesElements;
	}

	@Override
	public boolean hasId() {
		if (gitIndex == null)
			return false;
		var data = getEntryData();
		if (data == null)
			return false;
		if (data instanceof ModelType)
			return gitIndex.has((ModelType) data);
		if (data instanceof Category)
			return gitIndex.has((Category) data);
		var d = (RootDescriptor) data;
		if (!gitIndex.has(descriptors.categoryPaths, d))
			return false;
		var entry = gitIndex.get(descriptors.categoryPaths, d);
		return entry.version() == d.version && entry.lastChange() == d.lastChange;
	}

	@Override
	public byte[] idBuffer() {
		if (gitIndex == null)
			return GitUtil.getBytes(ObjectId.zeroId());
		var data = getEntryData();
		if (data == null)
			return GitUtil.getBytes(ObjectId.zeroId());
		if (data instanceof ModelType)
			return gitIndex.get((ModelType) data).rawObjectId();
		if (data instanceof Category)
			return gitIndex.get((Category) data).rawObjectId();
		var d = (RootDescriptor) data;
		var entry = gitIndex.get(descriptors.categoryPaths, d);
		if (entry.version() == d.version && entry.lastChange() == d.lastChange)
			return entry.rawObjectId();
		return GitUtil.getBytes(ObjectId.zeroId());
	}

	@Override
	public int idOffset() {
		return 0;
	}

	@Override
	public DatabaseIterator createSubtreeIterator(ObjectReader reader) {
		var data = getEntryData();
		if (data instanceof ModelType type)
			return new DatabaseIterator(this, init(descriptors, type));
		if (data instanceof Category category)
			return new DatabaseIterator(this, init(descriptors, category));
		return null;
	}

}

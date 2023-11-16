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
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.GitUtil;
import org.openlca.git.util.Path;
import org.openlca.util.Strings;

public class DatabaseIterator extends EntryIterator {

	private final ClientRepository repo;

	public DatabaseIterator(ClientRepository repo, String path) {
		this(repo, init(repo, path));
	}

	private DatabaseIterator(ClientRepository repo, List<TreeEntry> entries) {
		super(entries);
		this.repo = repo;
	}

	private DatabaseIterator(DatabaseIterator parent, List<TreeEntry> entries) {
		super(parent, entries);
		this.repo = parent.repo;;
	}

	private static List<TreeEntry> init(ClientRepository repo) {
		return Arrays.stream(ModelType.values()).filter(type -> {
			if (type == ModelType.CATEGORY)
				return false;
			if (!repo.descriptors.getCategories(type).isEmpty())
				return true;
			return !repo.descriptors.get(type).isEmpty();
		}).map(TreeEntry::new)
				.toList();
	}

	private static List<TreeEntry> init(ClientRepository repo, String path) {
		if (Strings.nullOrEmpty(path))
			return init(repo);
		if (!path.contains("/")) {
			var modelType = ModelType.parse(path);
			if (modelType == null)
				return new ArrayList<>();
			return init(repo, modelType);
		}
		path = GitUtil.decode(path);
		var category = repo.descriptors.getCategory(path);
		if (category == null)
			return new ArrayList<>();
		return init(repo, category);
	}

	private static List<TreeEntry> init(ClientRepository repo, ModelType type) {
		var entries = repo.descriptors.getCategories(type).stream()
				.map(TreeEntry::new)
				.collect(Collectors.toList());
		entries.addAll(repo.descriptors.get(type).stream()
				.filter(d -> !d.isFromLibrary())
				.map(TreeEntry::new)
				.toList());
		return entries;
	}

	private static List<TreeEntry> init(ClientRepository repo, Category category) {
		var categories = category.childCategories;
		var entries = categories.stream()
				.filter(c -> !isFromLibrary(repo, c))
				.map(TreeEntry::new)
				.collect(Collectors.toList());
		entries.addAll(repo.descriptors.get(category).stream()
				.filter(d -> !d.isFromLibrary())
				.map(TreeEntry::new)
				.toList());
		if (entries.isEmpty() && !isFromLibrary(repo, category)) {
			entries.add(TreeEntry.empty());
		}
		return entries;
	}

	private static boolean isFromLibrary(ClientRepository repo, Category category) {
		var hasLibrariesElements = false;
		for (var model : repo.descriptors.get(category)) {
			if (!model.isFromLibrary())
				return false;
			hasLibrariesElements = true;
		}
		for (var child : category.childCategories) {
			if (!isFromLibrary(repo, child))
				return false;
			hasLibrariesElements = true;
		}
		return hasLibrariesElements;
	}

	@Override
	public boolean hasId() {
		var path = getPath();
		if (path == null)
			return false;
		if (getEntryData() instanceof RootDescriptor d) {
			if (!repo.index.contains(path))
				return false;
			return repo.index.isSameVersion(path, d);
		}
		return repo.index.contains(path);
	}

	@Override
	public byte[] idBuffer() {
		var path = getPath();
		if (path == null)
			return GitUtil.getBytes(ObjectId.zeroId());
		if (!repo.index.contains(path))
			return GitUtil.getBytes(ObjectId.zeroId());
		if (!(getEntryData() instanceof RootDescriptor d) || repo.index.isSameVersion(path, d))
			return GitUtil.getBytes(repo.index.getObjectId(path));
		return GitUtil.getBytes(ObjectId.zeroId());
	}

	private String getPath() {
		var data = getEntryData();
		if (data == null)
			return null;
		if (data instanceof ModelType t)
			return Path.of(t);
		if (data instanceof Category c)
			return Path.of(c);
		if (data instanceof RootDescriptor d)
			return Path.of(repo.descriptors.categoryPaths, d);
		return null;
	}

	@Override
	public int idOffset() {
		return 0;
	}

	@Override
	public DatabaseIterator createSubtreeIterator(ObjectReader reader) {
		var data = getEntryData();
		if (data instanceof ModelType type)
			return new DatabaseIterator(this, init(repo, type));
		if (data instanceof Category category)
			return new DatabaseIterator(this, init(repo, category));
		return null;
	}

}

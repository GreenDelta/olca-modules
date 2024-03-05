package org.openlca.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Derby;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.git.model.Change;
import org.openlca.git.model.Change.ChangeType;
import org.openlca.git.model.Commit;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.GitUtil;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.util.Dirs;
import org.openlca.util.Strings;

public abstract class AbstractRepositoryTests {

	protected ClientRepository repo;
	protected PersonIdent committer;
	private File dir;

	@Before
	public void createRepo() throws IOException {
		dir = Files.createTempDirectory("olca-git-test").toFile();
		repo = new ClientRepository(new File(dir, "repo"), Derby.createInMemory());
		repo.create(true);
		committer = new PersonIdent("user", "user@example.com");
	}

	@After
	public void closeRepo() throws IOException {
		repo.close();
		try {
			Dirs.delete(dir);
		} catch (Exception e) {
			// fail silent
		}
	}

	protected String commit(List<Change> changes) throws IOException {
		return commit(null, changes);
	}

	protected String commit(Commit reference, List<Change> changes) throws IOException {
		// create and delete models in database
		var categoryDao = new CategoryDao(repo.database);
		changes.forEach(change -> {
			if (change.changeType == ChangeType.ADD) {
				create(change.path);
			} else if (change.changeType == ChangeType.MODIFY) {
				var model = repo.database.get(change.type.getModelClass(), change.refId);
				var version = new Version(model.version);
				version.incUpdate();
				model.version = version.getValue();
				repo.database.update(model);
			} else if (change.changeType == ChangeType.DELETE) {
				if (change.isCategory) {
					repo.database.delete(categoryDao.getForPath(change.type, change.getCategoryPath()));
				} else {
					repo.database.delete(repo.database.get(change.type.getModelClass(), change.refId));
				}
			}
		});
		repo.descriptors.reload();
		// write commit to repo
		var writer = new DbCommitWriter(repo, getBinaryResolver());
		if (reference != null) {
			writer.parent(repo.parseCommit(ObjectId.fromString(reference.id)));
		}
		return writer.write("commit", changes);
	}

	protected BinaryResolver getBinaryResolver() {
		return new StaticBinaryResolver(new HashMap<>());
	}

	protected List<String> create(String... paths) {
		for (var path : paths) {
			create(path);
		}
		repo.descriptors.reload();
		return Arrays.asList(paths);
	}

	private void create(String path) {
		try {
			var isCategory = !path.endsWith(GitUtil.DATASET_SUFFIX);
			var type = ModelType.valueOf(path.substring(0, path.indexOf("/")));
			Category category = null;
			if (isCategory && path.contains("/")) {
				var categoryPath = path.substring(path.indexOf("/") + 1);
				category = CategoryDao.sync(repo.database, type, categoryPath.split("/"));
			} else if (!isCategory && path.indexOf("/") != path.lastIndexOf("/")) {
				var categoryPath = path.substring(path.indexOf("/") + 1, path.lastIndexOf("/"));
				category = CategoryDao.sync(repo.database, type, categoryPath.split("/"));
			}
			if (isCategory)
				return;
			var entity = type.getModelClass().getDeclaredConstructor().newInstance();
			entity.refId = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(GitUtil.DATASET_SUFFIX));
			entity.category = category;
			repo.database.insert(entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void delete(String... paths) {
		for (var path : paths) {
			delete(path);
		}
		repo.descriptors.reload();
	}

	private void delete(String path) {
		var isCategory = !path.endsWith(GitUtil.DATASET_SUFFIX);
		var type = ModelType.valueOf(path.substring(0, path.indexOf("/")));
		if (isCategory) {
			var categoryPath = path.substring(path.indexOf("/") + 1);
			var categoryDao = new CategoryDao(repo.database);
			var category = categoryDao.getForPath(type, categoryPath);
			if (category.category != null) {
				category.category.childCategories.remove(category);
				categoryDao.update(category.category);
			}
			categoryDao.delete(category);
		} else {
			var refId = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(GitUtil.DATASET_SUFFIX));
			repo.database.delete(repo.database.get(Actor.class, refId));
		}
	}

	protected void move(String path, String categoryPath) {
		if (!path.endsWith(GitUtil.DATASET_SUFFIX))
			throw new IllegalArgumentException("Moving categories not supported");
		var prevCategoryPath = path.indexOf("/") != path.lastIndexOf("/")
				? path.substring(path.indexOf("/") + 1, path.lastIndexOf("/"))
				: "";
		if (prevCategoryPath.equals(categoryPath))
			return;
		var type = ModelType.valueOf(path.substring(0, path.indexOf("/")));
		var refId = path.substring(path.lastIndexOf("/") + 1, path.indexOf(".json"));
		var model = repo.database.get(type.getModelClass(), refId);
		if (model == null)
			throw new IllegalArgumentException("Could not find " + path);
		model.category = CategoryDao.sync(repo.database, type, categoryPath.split("/"));
		repo.database.update(model);
		repo.descriptors.reload();
	}

	protected long count(ModelType type) {
		return repo.database.getDescriptors(type.getModelClass()).size();
	}

	protected AbstractTreeIterator createIterator() throws IOException {
		return createIterator(null);
	}

	protected AbstractTreeIterator createIterator(String commitId) throws IOException {
		return createIterator(null, null);
	}

	protected AbstractTreeIterator createIterator(String commitId, String path) throws IOException {
		var commit = commitId != null
				? repo.parseCommit(ObjectId.fromString(commitId))
				: repo.getHeadCommit();
		if (commit == null)
			return new EmptyTreeIterator();
		var treeId = Strings.nullOrEmpty(path)
				? commit.getTree().getId()
				: repo.getSubTreeId(commit.getTree().getId(), path);
		if (ObjectId.zeroId().equals(treeId))
			return new EmptyTreeIterator();
		var it = new CanonicalTreeParser();
		it.reset(repo.newObjectReader(), treeId);
		return it;
	}

	protected void assertEqualRecursive(AbstractTreeIterator iterator, String... entries) {
		var stack = new LinkedList<>(Arrays.asList(entries));
		try (var walk = new TreeWalk(repo)) {
			walk.addTree(iterator);
			walk.setRecursive(true);
			while (walk.next()) {
				var actual = GitUtil.decode(walk.getPathString());
				var expected = stack.pop();
				Assert.assertEquals(expected, actual);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void assertEqual(AbstractTreeIterator iterator, String... entries) {
		var stack = new LinkedList<>(Arrays.asList(entries));
		try (var walk = new TreeWalk(repo)) {
			walk.addTree(iterator);
			walk.setRecursive(false);
			while (walk.next()) {
				var actual = GitUtil.decode(walk.getPathString());
				var expected = stack.pop();
				Assert.assertEquals(expected, actual);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class StaticBinaryResolver implements BinaryResolver {

		private final Map<String, List<String>> paths;

		public StaticBinaryResolver(Map<String, List<String>> paths) {
			this.paths = paths;
		}

		@Override
		public List<String> list(Change change, String relativePath) {
			if (!paths.containsKey(change.path))
				return new ArrayList<>();
			return new ArrayList<>(paths.get(change.path));
		}

		@Override
		public boolean isDirectory(Change change, String relativePath) {
			return false;
		}

		@Override
		public byte[] resolve(Change change, String relativePath) throws IOException {
			return getContent(relativePath).getBytes();
		}

		public static String getContent(String relativePath) {
			return "Content of " + relativePath;
		}

	}
}

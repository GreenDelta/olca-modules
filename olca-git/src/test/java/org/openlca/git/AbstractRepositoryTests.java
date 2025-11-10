package org.openlca.git;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.jgit.api.errors.GitAPIException;
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
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.git.actions.GitCommit;
import org.openlca.git.actions.GitInit;
import org.openlca.git.actions.GitStashCreate;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.DiffType;
import org.openlca.git.model.ModelRef;
import org.openlca.git.model.Reference;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.GitUtil;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.util.Dirs;
import org.openlca.commons.Strings;

import com.google.common.base.Objects;

public abstract class AbstractRepositoryTests {

	protected TestRepository repo;
	protected PersonIdent committer;

	@Before
	public void createRepo() throws IOException, GitAPIException, URISyntaxException {
		repo = new TestRepository(getRemotePath());
		committer = new PersonIdent("user", "user@example.com");
	}

	protected String getRemotePath() {
		return null;
	}

	protected BinaryResolver getBinaryResolver() {
		return new StaticBinaryResolver(new HashMap<>());
	}

	@After
	public void closeRepo() throws IOException {
		repo.close();
	}

	protected class TestRepository extends ClientRepository {

		private final String name;

		public TestRepository(String remotePath) throws GitAPIException, IOException, URISyntaxException {
			super(init(remotePath), Derby.createInMemory());
			var id = UUID.randomUUID().toString();
			this.name = "repo-" + id.substring(0, id.indexOf("-"));
		}

		private static File init(String remotePath) throws GitAPIException, IOException, URISyntaxException {
			var dir = Files.createTempDirectory("olca-git-test").toFile();
			var init = GitInit.in(dir);
			if (remotePath != null) {
				init = init.remoteUrl("file://" + remotePath);
			}
			init.run();
			return dir;
		}

		public void stashWorkspace() throws IOException, GitAPIException {
			var diffs = this.diffs.find().withDatabase();
			var commitId = GitStashCreate.on(this)
					.changes(diffs)
					.as(committer)
					.run();
			var stashCommit = commits.stash();
			Assert.assertNotNull(stashCommit);
			Assert.assertEquals(commitId, stashCommit.id);
		}

		public String commitWorkspace() throws IOException {
			var diffs = this.diffs.find().withDatabase();
			Assert.assertFalse(this.diffs.find().withDatabase().isEmpty());
			var commitId = GitCommit.on(this)
					.as(committer)
					.changes(diffs)
					.withMessage(getCommitMessage())
					.run();
			Assert.assertTrue(this.diffs.find().withDatabase().isEmpty());
			return commitId;
		}

		private String getCommitMessage() {
			var commitCount = commits.find().all().size();
			return "commit " + ++commitCount + " from " + name;
		}

		public String commit(List<Diff> changes, String... libraries) throws IOException {
			return commit(null, changes, libraries);
		}

		public String commit(Commit reference, List<Diff> changes, String... libraries) throws IOException {
			// create, modify and delete models in database
			changes.forEach(change -> {
				if (change.diffType == DiffType.ADDED) {
					create(change.path);
				} else if (change.diffType == DiffType.MODIFIED) {
					modify(change.path);
				} else if (change.diffType == DiffType.MOVED) {
					move(change.oldRef.path, change.newRef.category);
				} else if (change.diffType == DiffType.DELETED) {
					delete(change.path);
				}
			});
			var commitChanges = new ArrayList<>(changes);
			if (libraries != null && libraries.length > 0) {
				for (var library : libraries) {
					commitChanges
							.add(Diff.added(new Reference(RepositoryInfo.FILE_NAME + "/" + library)));
					database.addLibrary(library);
				}
			}
			descriptors.reload();
			// write commit to repo
			var writer = new DbCommitWriter(this, getBinaryResolver());
			if (reference != null) {
				writer.parent(parseCommit(ObjectId.fromString(reference.id)));
			}
			return writer.write(getCommitMessage(), commitChanges);
		}

		public void create(String... paths) {
			for (var path : paths) {
				create(path);
			}
			descriptors.reload();
		}

		private void create(String path) {
			var isCategory = !GitUtil.isDatasetPath(path);
			var type = ModelType.valueOf(path.substring(0, path.indexOf("/")));
			Category category = null;
			if (isCategory && path.contains("/")) {
				var categoryPath = path.substring(path.indexOf("/") + 1);
				category = CategoryDao.sync(database, type, categoryPath.split("/"));
			} else if (!isCategory && path.indexOf("/") != path.lastIndexOf("/")) {
				var categoryPath = path.substring(path.indexOf("/") + 1, path.lastIndexOf("/"));
				category = CategoryDao.sync(database, type, categoryPath.split("/"));
			}
			if (isCategory)
				return;
			var refId = GitUtil.getRefId(path);
			var entity = database.get(type.getModelClass(), refId);
			if (entity != null) {
				if (!Objects.equal(entity.category, category))
					throw new IllegalArgumentException("Entity with same ref id exists in different category");
			} else {
				try {
					entity = type.getModelClass().getDeclaredConstructor().newInstance();
					entity.refId = refId;
					entity.category = category;
					database.insert(entity);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void modify(String... paths) {
			for (var path : paths) {
				modify(path);
			}
			descriptors.reload();
		}

		private void modify(String path) {
			if (!GitUtil.isDatasetPath(path))
				throw new IllegalArgumentException("Can not modify categories");
			var type = ModelType.valueOf(path.substring(0, path.indexOf("/")));
			var refId = GitUtil.getRefId(path);
			var model = database.get(type.getModelClass(), refId);
			var version = new Version(model.version);
			version.incUpdate();
			model.version = version.getValue();
			model.lastChange = System.currentTimeMillis();
			database.update(model);
		}

		public void delete(String... paths) {
			for (var path : paths) {
				delete(path);
			}
			descriptors.reload();
		}

		private void delete(String path) {
			var isCategory = !GitUtil.isDatasetPath(path);
			var type = ModelType.valueOf(path.substring(0, path.indexOf("/")));
			if (isCategory) {
				var categoryPath = path.substring(path.indexOf("/") + 1);
				var categoryDao = new CategoryDao(database);
				var category = categoryDao.getForPath(type, categoryPath);
				if (category.category != null) {
					category.category.childCategories.remove(category);
					categoryDao.update(category.category);
				}
				categoryDao.delete(category);
			} else {
				var refId = GitUtil.getRefId(path);
				database.delete(database.get(type.getModelClass(), refId));
			}
		}

		public void move(String path, String categoryPath) {
			if (!GitUtil.isDatasetPath(path))
				throw new IllegalArgumentException("Moving categories not supported");
			var prevCategoryPath = path.indexOf("/") != path.lastIndexOf("/")
					? path.substring(path.indexOf("/") + 1, path.lastIndexOf("/"))
					: "";
			if (prevCategoryPath.equals(categoryPath))
				return;
			var type = ModelType.valueOf(path.substring(0, path.indexOf("/")));
			var refId = GitUtil.getRefId(path);
			var model = database.get(type.getModelClass(), refId);
			if (model == null)
				throw new IllegalArgumentException("Could not find " + path);
			model.category = Strings.isNotBlank(categoryPath)
					? CategoryDao.sync(database, type, categoryPath.split("/"))
					: null;
			database.update(model);
			descriptors.reload();
		}

		public long count(ModelType type) {
			return database.getDescriptors(type.getModelClass()).size();
		}

		public AbstractTreeIterator createIterator() throws IOException {
			return createIterator(null);
		}

		public AbstractTreeIterator createIterator(String commitId) throws IOException {
			return createIterator(null, null);
		}

		public AbstractTreeIterator createIterator(String commitId, String path) throws IOException {
			var commit = commitId != null
					? parseCommit(ObjectId.fromString(commitId))
					: getHeadCommit();
			if (commit == null)
				return new EmptyTreeIterator();
			var treeId = Strings.isBlank(path)
					? commit.getTree().getId()
					: getSubTreeId(commit.getTree().getId(), path);
			if (ObjectId.zeroId().equals(treeId))
				return new EmptyTreeIterator();
			var it = new CanonicalTreeParser();
			it.reset(newObjectReader(), treeId);
			return it;
		}

		public void assertEqualRecursive(AbstractTreeIterator iterator, String... entries) {
			var stack = new LinkedList<>(Arrays.asList(entries));
			try (var walk = new TreeWalk(this)) {
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

		public void assertEqual(AbstractTreeIterator iterator, String... entries) {
			var stack = new LinkedList<>(Arrays.asList(entries));
			try (var walk = new TreeWalk(this)) {
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

		public void assertDatabaseEquals(String... paths) {
			var counts = new EnumMap<ModelType, Integer>(ModelType.class);
			var categories = new HashSet<>();
			for (var path : paths) {
				var ref = new ModelRef(path);
				if (ref.isCategory) {
					var category = new CategoryDao(database).getForPath(ref.type, ref.getCategoryPath());
					Assert.assertNotNull(category);
					categories.add(ref.getCategoryPath());
				} else {
					var model = database.get(ref.type.getModelClass(), ref.refId);
					Assert.assertNotNull(model);
					var categoryPath = model.category != null ? model.category.toPath() : "";
					Assert.assertEquals(ref.getCategoryPath(), categoryPath);
					var count = counts.getOrDefault(ref.type, 0);
					counts.put(ref.type, count + 1);
					if (ref.getCategoryPath().isEmpty())
						continue;
					var category = new CategoryDao(database).getForPath(ref.type, ref.getCategoryPath());
					Assert.assertNotNull(category);
					categories.add(ref.getCategoryPath());
				}
			}
			for (var type : ModelType.values()) {
				var dbCount = database.getDescriptors(type.getModelClass()).size();
				var count = type == ModelType.CATEGORY
						? categories.size()
						: counts.getOrDefault(type, 0);
				Assert.assertEquals(dbCount, count);
			}
		}

		@Override
		public void close() {
			super.close();
			Dirs.delete(dir);
		}

	}

	public static class StaticBinaryResolver implements BinaryResolver {

		private final Map<String, List<String>> paths;

		public StaticBinaryResolver(Map<String, List<String>> paths) {
			this.paths = paths;
		}

		@Override
		public List<String> list(Diff change, String relativePath) {
			if (!paths.containsKey(change.path))
				return new ArrayList<>();
			return new ArrayList<>(paths.get(change.path));
		}

		@Override
		public boolean isDirectory(Diff change, String relativePath) {
			return false;
		}

		@Override
		public byte[] resolve(Diff change, String relativePath) throws IOException {
			return getContent(relativePath).getBytes();
		}

		public static String getContent(String relativePath) {
			return "Content of " + relativePath;
		}

	}
}

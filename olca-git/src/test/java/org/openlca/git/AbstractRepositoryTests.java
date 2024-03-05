package org.openlca.git;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.git.actions.GitCommit;
import org.openlca.git.actions.GitInit;
import org.openlca.git.actions.GitStashCreate;
import org.openlca.git.model.Change;
import org.openlca.git.model.Change.ChangeType;
import org.openlca.git.model.Commit;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.GitUtil;
import org.openlca.git.writer.DbCommitWriter;
import org.openlca.util.Dirs;
import org.openlca.util.Strings;

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

	protected IDatabase getDatabase() {
		return Derby.createInMemory();
	}

	@After
	public void closeRepo() throws IOException {
		repo.close();
	}

	protected class TestRepository extends ClientRepository {

		public TestRepository() throws GitAPIException, IOException, URISyntaxException {
			this(null);
		}

		public TestRepository(String remotePath) throws GitAPIException, IOException, URISyntaxException {
			super(init(remotePath), getDatabase());
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
			var diffs = repo.diffs.find().withDatabase();
			var commitId = GitStashCreate.on(repo)
					.changes(Change.of(diffs))
					.as(committer)
					.run();
			var stashCommit = repo.commits.stash();
			Assert.assertNotNull(stashCommit);
			Assert.assertEquals(commitId, stashCommit.id);
		}

		public void commitWorkspace() throws IOException {
			var diffs = repo.diffs.find().withDatabase();
			GitCommit.on(repo)
					.as(committer)
					.changes(Change.of(diffs))
					.withMessage("commit")
					.run();
			Assert.assertEquals(0, repo.diffs.find().withDatabase().size());
		}

		public String commit(List<Change> changes) throws IOException {
			return commit(null, changes);
		}

		public String commit(Commit reference, List<Change> changes) throws IOException {
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

		public List<String> create(String... paths) {
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
				var refId = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(GitUtil.DATASET_SUFFIX));
				var entity = repo.database.get(type.getModelClass(), refId);
				if (entity != null) {
					if (!Objects.equal(entity.category, category))
						throw new IllegalArgumentException("Entity with same ref id exists in different category");
				} else {
					entity = type.getModelClass().getDeclaredConstructor().newInstance();
					entity.refId = refId;
					entity.category = category;
					repo.database.insert(entity);
				}
			} catch (

			Exception e) {
				e.printStackTrace();
			}
		}

		public void delete(String... paths) {
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

		public void move(String path, String categoryPath) {
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

		public long count(ModelType type) {
			return repo.database.getDescriptors(type.getModelClass()).size();
		}

		public AbstractTreeIterator createIterator() throws IOException {
			return createIterator(null);
		}

		public AbstractTreeIterator createIterator(String commitId) throws IOException {
			return createIterator(null, null);
		}

		public AbstractTreeIterator createIterator(String commitId, String path) throws IOException {
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

		public void assertEqualRecursive(AbstractTreeIterator iterator, String... entries) {
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

		public void assertEqual(AbstractTreeIterator iterator, String... entries) {
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

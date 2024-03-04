package org.openlca.git;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.PersonIdent;
import org.junit.After;
import org.junit.Before;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Derby;
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
		writer.reference(reference);
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
				category = CategoryDao.sync(repo.database, type, path.substring(path.indexOf("/") + 1).split("/"));
			} else if (!isCategory && path.indexOf("/") != path.lastIndexOf("/")) {
				category = CategoryDao.sync(repo.database, type,
						path.substring(path.indexOf("/") + 1, path.lastIndexOf("/")).split("/"));
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

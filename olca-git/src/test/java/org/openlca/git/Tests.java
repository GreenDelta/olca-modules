package org.openlca.git;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;

import org.eclipse.jgit.lib.PersonIdent;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.git.repo.ClientRepository;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Dirs;

public class Tests {

	private static final IDatabase db = Derby.createInMemory();

	public record TmpConfig(ClientRepository repo, PersonIdent committer,
			File dir) implements AutoCloseable {

		public static TmpConfig create() {
			try {
				var dir = Files.createTempDirectory("olca-git-test").toFile();
				var repo = new ClientRepository(new File(dir, "repo"), db);
				repo.create(true);
				return new TmpConfig(repo, new PersonIdent("user", "user@example.com"), dir);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void close() {
			repo.close();
			try {
				Dirs.delete(dir);
			} catch (Exception e) {
				// fail silent
			}
		}

	}

	public static RootEntity create(IDatabase db, String path) {
		try {
			var isCategory = !path.endsWith(GitUtil.DATASET_SUFFIX);
			var type = ModelType.valueOf(path.substring(0, path.indexOf("/")));
			Category category = null;
			if (isCategory && path.contains("/")) {
				category = CategoryDao.sync(db, type, path.substring(path.indexOf("/") + 1).split("/"));
			} else if (!isCategory && path.indexOf("/") != path.lastIndexOf("/")) {
				category = CategoryDao.sync(db, type,
						path.substring(path.indexOf("/") + 1, path.lastIndexOf("/")).split("/"));
			}
			if (isCategory)
				return category;
			var entity = type.getModelClass().getDeclaredConstructor().newInstance();
			entity.refId = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf(GitUtil.DATASET_SUFFIX));
			entity.category = category;
			return db.insert(entity);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException |

				SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}

}

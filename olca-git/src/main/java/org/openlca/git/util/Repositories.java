package org.openlca.git.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.openlca.git.model.Commit;
import org.openlca.jsonld.PackageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public final class Repositories {

	private static final Logger log = LoggerFactory.getLogger(Repositories.class);

	private Repositories() {
	}

	public static Repository open(File dir) {
		return open(dir.toPath());
	}

	public static Repository open(Path dir) {
		try {
			var repo = new FileRepository(dir.toFile());
			if (!Files.exists(dir)) {
				// create a new bare repository
				repo.create(true);
			}
			return repo;
		} catch (IOException e) {
			throw new RuntimeException(
					"could not get repository from folder: " + dir, e);
		}
	}

	public static PackageInfo infoOf(Repository repo) {
		return infoOf(repo, null);
	}

	public static PackageInfo infoOf(Repository repo, Commit commit) {
		try (var walk = new TreeWalk(repo);
				var reader = repo.getObjectDatabase().newReader()) {
			var revCommit = commit != null
					? repo.parseCommit(ObjectId.fromString(commit.id))
					: headCommitOf(repo);
			if (revCommit == null)
				return null;
			walk.addTree(revCommit.getTree().getId());
			walk.setRecursive(false);
			walk.setFilter(PathFilter.create(PackageInfo.FILE_NAME));
			if (!walk.next())
				return null;
			var blobId = walk.getObjectId(0);
			var bytes = reader.open(blobId).getBytes();
			var json = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), JsonElement.class);
			return PackageInfo.of(json);
		} catch (IOException e) {
			log.error("failed to read schema version", e);
			return null;
		}
	}

	public static RevCommit headCommitOf(Repository repo) {
		try (var walk = new RevWalk(repo)) {
			var head = repo.resolve(Constants.LOCAL_BRANCH);
			if (head == null) {
				head = repo.resolve(Constants.LOCAL_REF);
			}
			if (head == null)
				return null;
			var commit = walk.parseCommit(head);
			if (commit == null)
				return null;
			return commit;
		} catch (IOException e) {
			log.error("Error getting head commit", e);
			return null;
		}
	}

}

package org.openlca.git.repo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.model.Commit;
import org.openlca.git.util.Constants;
import org.openlca.git.util.GitUtil;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class OlcaRepository extends FileRepository {

	private static final Logger log = LoggerFactory.getLogger(OlcaRepository.class);
	public final Commits commits;
	public final Datasets datasets;
	public final Entries entries;
	public final References references;
	public final Diffs diffs;
	public final History localHistory;
	public final History remoteHistory;
	public final File dir;

	public OlcaRepository(File gitDir) throws IOException {
		super(gitDir);
		this.commits = Commits.of(this);
		this.datasets = Datasets.of(this);
		this.entries = Entries.of(this);
		this.references = References.of(this);
		this.diffs = Diffs.of(this);
		this.localHistory = History.of(this, Constants.LOCAL_REF);
		this.remoteHistory = History.of(this, Constants.REMOTE_REF);
		this.dir = gitDir;
	}

	public RepositoryInfo getInfo() {
		return getInfo(null);
	}

	public RepositoryInfo getInfo(Commit commit) {
		try (var walk = new TreeWalk(this);
				var reader = getObjectDatabase().newReader()) {
			var revCommit = commit != null
					? parseCommit(ObjectId.fromString(commit.id))
					: getHeadCommit();
			if (revCommit == null)
				return null;
			walk.addTree(revCommit.getTree().getId());
			walk.setRecursive(false);
			walk.setFilter(PathFilter.create(RepositoryInfo.FILE_NAME));
			if (!walk.next())
				return null;
			var blobId = walk.getObjectId(0);
			var bytes = reader.open(blobId).getBytes();
			var json = new Gson().fromJson(new String(bytes, StandardCharsets.UTF_8), JsonElement.class);
			return RepositoryInfo.of(json);
		} catch (IOException e) {
			log.error("failed to read schema version", e);
			return null;
		}
	}

	public RevCommit getHeadCommit() {
		try (var walk = new RevWalk(this)) {
			var head = resolve(Constants.LOCAL_BRANCH);
			if (head == null) {
				head = resolve(Constants.LOCAL_REF);
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

	ObjectId getSubTreeId(ObjectId treeId, String path) {
		if (Strings.nullOrEmpty(path))
			return treeId;
		try (var walk = TreeWalk.forPath(this, GitUtil.encode(path), treeId)) {
			if (walk == null)
				return ObjectId.zeroId();
			if (walk.getFileMode() != FileMode.TREE)
				return ObjectId.zeroId();
			return walk.getObjectId(0);
		} catch (IOException e) {
			log.error("Error finding entry for " + path);
			return null;
		}
	}

	ObjectId getObjectId(RevCommit commit, String path) {
		var treeId = commit.getTree().getId();
		if (Strings.nullOrEmpty(path))
			return treeId;
		try (var walk = TreeWalk.forPath(this, GitUtil.encode(path), treeId)) {
			if (walk == null)
				return ObjectId.zeroId();
			return walk.getObjectId(0);
		} catch (IOException e) {
			log.error("Error finding entry for " + path);
			return null;
		}
	}

}

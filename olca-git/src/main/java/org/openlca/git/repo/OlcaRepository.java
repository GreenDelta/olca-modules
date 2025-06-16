package org.openlca.git.repo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.openlca.core.database.DataPackage;
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
	public final References references;
	public final Diffs diffs;
	public final History localHistory;
	public final File dir;

	public OlcaRepository(File gitDir) throws IOException {
		super(gitDir);
		this.commits = Commits.of(this);
		this.datasets = Datasets.of(this);
		this.references = References.of(this);
		this.diffs = Diffs.of(this);
		this.localHistory = History.of(this, Constants.LOCAL_REF);
		this.dir = gitDir;
	}

	public RepositoryInfo getInfo() {
		return getInfo((String) null);
	}

	public RepositoryInfo getInfo(String commitId) {
		try (var walk = new TreeWalk(this);
				var reader = getObjectDatabase().newReader()) {
			var revCommit = commitId != null
					? parseCommit(ObjectId.fromString(commitId))
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

	public DataPackage getDataPackage(String name) {
		return getDataPackage(name, null);
	}

	public DataPackage getDataPackage(String name, String commitId) {
		for (var dataPackage : getDataPackages(commitId))
			if (dataPackage.name().equals(name))
				return dataPackage;
		return null;
	}

	public Set<DataPackage> getDataPackages() {
		return getDataPackages((String) null);
	}

	public Set<DataPackage> getDataPackages(RevCommit commit) {
		if (commit == null)
			return new HashSet<>();
		return getDataPackages(commit.name());
	}

	public Set<DataPackage> getDataPackages(Commit commit) {
		if (commit == null)
			return new HashSet<>();
		return getDataPackages(commit.id);
	}

	public Set<DataPackage> getDataPackages(String commitId) {
		var info = getInfo(commitId);
		if (info == null)
			return new HashSet<>();
		return info.dataPackages();
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

	protected ObjectId getSubTreeId(ObjectId treeId, String path) {
		return getObjectId(treeId, path, FileMode.TREE);
	}

	protected ObjectId getObjectId(RevCommit commit, String path) {
		if (commit == null)
			return ObjectId.zeroId();
		return getObjectId(commit.getTree().getId(), path, null);
	}

	private ObjectId getObjectId(ObjectId treeId, String path, FileMode fileMode) {
		if (treeId == null)
			return ObjectId.zeroId();
		if (Strings.nullOrEmpty(path))
			return treeId;
		try (var walk = TreeWalk.forPath(this, GitUtil.encode(path), treeId)) {
			if (walk == null)
				return ObjectId.zeroId();
			if (fileMode != null && walk.getFileMode() != fileMode)
				return ObjectId.zeroId();
			return walk.getObjectId(0);
		} catch (IOException e) {
			log.error("Error finding entry for " + path);
			return null;
		}
	}

}

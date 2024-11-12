package org.openlca.git.repo;

import java.util.ArrayList;
import java.util.List;

import org.openlca.git.model.Commit;
import org.openlca.git.model.Diff;
import org.openlca.git.model.TriDiff;
import org.openlca.git.util.Constants;
import org.openlca.util.Strings;

import com.google.common.base.Predicates;

public class Conflicts {

	private final ClientRepository repo;
	private final String ref;
	private Commit commonParent;
	private List<Diff> remoteChanges;

	private Conflicts(ClientRepository repo, String ref) {
		this.repo = repo;
		this.ref = ref;
	}

	public static Conflicts of(ClientRepository repo, String ref) {
		return new Conflicts(repo, ref);
	}

	public List<TriDiff> withWorkspace() {
		var workspaceChanges = repo.diffs.find().excludeLibraries().withDatabase();
		if (workspaceChanges.isEmpty())
			return new ArrayList<>();
		var remoteCommit = repo.commits.find().refs(ref).latest();
		this.commonParent = repo.localHistory.commonParentOf(ref);
		remoteChanges = repo.diffs.find().excludeLibraries().commit(commonParent).with(remoteCommit);
		return between(workspaceChanges, remoteChanges);
	}

	public List<TriDiff> withLocal() {
		var localCommit = repo.commits.get(repo.commits.resolve(Constants.LOCAL_BRANCH));
		var commonParent = repo.localHistory.commonParentOf(ref);
		if (localCommit == null)
			return new ArrayList<>();
		if (commonParent != null && localCommit.id.equals(commonParent.id))
			return new ArrayList<>();
		var localChanges = repo.diffs.find().excludeLibraries().commit(commonParent).with(localCommit);
		if (remoteChanges == null || commonParent != null && !commonParent.equals(this.commonParent)) {
			var remoteCommit = repo.commits.find().refs(ref).latest();
			remoteChanges = repo.diffs.find().excludeLibraries().commit(commonParent).with(remoteCommit);
		}
		if (localChanges.isEmpty() || remoteChanges.isEmpty())
			return new ArrayList<>();
		return between(localChanges, remoteChanges);
	}

	private List<TriDiff> between(List<Diff> localChanges, List<Diff> remoteChanges) {
		var conflicts = new ArrayList<TriDiff>();
		new ArrayList<>(localChanges).forEach(local -> {
			var conflict = findConflict(local, remoteChanges);
			if (conflict != null) {
				localChanges.remove(local);
				conflicts.add(conflict);
			}
		});
		remoteChanges.stream()
				.map(remote -> findConflict(remote, localChanges))
				.filter(Predicates.notNull())
				.forEach(conflicts::add);
		return conflicts;
	}

	private TriDiff findConflict(Diff element, List<Diff> others) {
		return others.stream()
				.filter(e -> e.path.equals(element.path)
						|| (e.type == element.type && Strings.nullOrEqual(e.refId, element.refId)))
				.findFirst()
				.map(e -> new TriDiff(element, e))
				.orElse(null);
	}

}

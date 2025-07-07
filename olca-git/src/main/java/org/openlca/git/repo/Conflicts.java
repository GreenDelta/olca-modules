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
	private Commit remoteCommit;
	private List<Diff> remoteChanges;

	public final List<TriDiff> local = new ArrayList<>();
	public final List<TriDiff> workspace = new ArrayList<>();

	private Conflicts(ClientRepository repo, String ref) {
		this.repo = repo;
		this.ref = ref;
	}

	public static Conflicts of(ClientRepository repo, String ref) {
		return new Conflicts(repo, ref).init();
	}

	private Conflicts init() {
		findWithWorkspace();
		findWithLocal();
		return this;
	}

	private void findWithWorkspace() {
		var workspaceChanges = repo.diffs.find().excludeDataPackages().withDatabase();
		if (workspaceChanges.isEmpty())
			return;
		remoteCommit = repo.commits.find().refs(ref).latest();
		commonParent = repo.localHistory.commonParentOf(ref);
		remoteChanges = diffsOf(commonParent, remoteCommit);
		var conflicts = between(workspaceChanges, remoteChanges);
		workspace.addAll(conflicts);
	}

	private void findWithLocal() {
		var localCommit = repo.commits.get(repo.commits.resolve(Constants.LOCAL_BRANCH));
		if (localCommit == null)
			return;
		var commonParent = repo.localHistory.commonParentOf(ref);
		if (commonParent != null && localCommit.id.equals(commonParent.id))
			return;
		var localChanges = diffsOf(commonParent, localCommit);
		if (remoteChanges == null || commonParent != null && !commonParent.equals(this.commonParent)) {
			remoteChanges = diffsOf(commonParent, remoteCommit);
		}
		if (localChanges.isEmpty() || remoteChanges.isEmpty())
			return;
		var conflicts = between(localChanges, remoteChanges);
		local.addAll(conflicts);
	}

	private List<Diff> diffsOf(Commit left, Commit right) {
		return repo.diffs.find().excludeDataPackages().excludeCategories().commit(left).with(right);
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

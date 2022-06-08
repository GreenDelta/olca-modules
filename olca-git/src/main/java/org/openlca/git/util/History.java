package org.openlca.git.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.openlca.git.find.Commits;
import org.openlca.git.model.Commit;

public class History {

	private final Commits commits;
	private final String ref;

	public static History of(Repository repo, String ref) {
		return new History(repo, ref);
	}

	public static History localOf(Repository repo) {
		return of(repo, Constants.LOCAL_REF);
	}

	public static History remoteOf(Repository repo) {
		return of(repo, Constants.REMOTE_REF);
	}

	private History(Repository repo, String ref) {
		this.commits = Commits.of(repo);
		this.ref = ref;
	}

	public List<Commit> get() {
		return of(ref);
	}

	public boolean contains(Commit commit) {
		return get().contains(commit);
	}

	public boolean isAheadOf(Commit commit, String ref) {
		return getAheadOf(ref).contains(commit);
	}

	public List<Commit> getAheadOf(String ref) {
		return diffBetween(of(this.ref), of(ref));
	}

	public List<Commit> getBehindOf(String ref) {
		return diffBetween(of(ref), of(this.ref));
	}

	public Commit commonParentOf(String ref) {
		var local = commits.find().refs(this.ref).all();
		if (local.isEmpty())
			return null;
		var other = commits.find().refs(ref).all();
		if (other.isEmpty())
			return null;
		var commonHistory = other.stream()
				.filter(c -> local.contains(c))
				.toList();
		if (commonHistory.isEmpty())
			return null;
		return commonHistory.get(commonHistory.size() - 1);
	}

	private List<Commit> of(String ref) {
		return commits.find().refs(ref).all();
	}

	private List<Commit> diffBetween(List<Commit> left, List<Commit> right) {
		var diff = new ArrayList<Commit>();
		for (var i = left.size() - 1; i >= 0; i--) {
			if (right.contains(left.get(i)))
				return diff;
			diff.add(left.get(i));
		}
		return diff;
	}

}

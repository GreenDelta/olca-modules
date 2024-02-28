package org.openlca.git.repo;

import java.util.ArrayList;
import java.util.List;

import org.openlca.git.model.Commit;

public class History {

	private final OlcaRepository repo;
	private final String ref;

	static History of(OlcaRepository repo, String ref) {
		return new History(repo, ref);
	}

	private History(OlcaRepository repo, String ref) {
		this.repo = repo;
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
	
	public boolean isBehindOf(Commit commit, String ref) {
		return getBehindOf(ref).contains(commit);
	}


	public List<Commit> getBehindOf(String ref) {
		return diffBetween(of(ref), of(this.ref));
	}

	public Commit commonParentOf(String ref) {
		var local = repo.commits.find().refs(this.ref).all();
		if (local.isEmpty())
			return null;
		var other = repo.commits.find().refs(ref).all();
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
		return repo.commits.find().refs(ref).all();
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

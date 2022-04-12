package org.openlca.git.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.openlca.git.find.Commits;
import org.openlca.git.model.Commit;

public class History {

	private final Commits commits;

	public static History of(FileRepository repo) {
		return new History(repo);
	}

	private History(FileRepository repo) {
		this.commits = Commits.of(repo);
	}

	public List<Commit> of(String ref) {
		return commits.find().refs(ref).all();
	}

	public boolean isAhead(Commit commit) {
		return isAhead(commit, Constants.REMOTE_REF);
	}

	public boolean isAhead(Commit commit, String ref) {
		return getAhead(ref).contains(commit);
	}

	public List<Commit> getAhead() {
		return getAhead(Constants.REMOTE_REF);
	}

	public List<Commit> getAhead(String ref) {
		return getAhead(of(Constants.LOCAL_REF), of(ref));
	}

	public List<Commit> getBehind() {
		return getAhead(Constants.REMOTE_REF);
	}

	public List<Commit> getBehind(String ref) {
		return getAhead(of(ref), of(Constants.LOCAL_REF));
	}

	private static List<Commit> getAhead(List<Commit> left, List<Commit> right) {
		var diff = new ArrayList<Commit>();
		for (var i = left.size() - 1; i >= 0; i--) {
			if (right.contains(left.get(i)))
				return diff;
			diff.add(left.get(i));
		}
		return diff;
	}

}

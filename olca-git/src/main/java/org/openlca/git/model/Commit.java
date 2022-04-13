package org.openlca.git.model;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.revwalk.RevCommit;

public class Commit {

	public final String id;
	public final String message;
	public final String user;
	public final long timestamp;
	public final List<String> parentIds;

	public Commit(RevCommit rev) {
		this.id = rev.getId().getName();
		this.message = rev.getFullMessage();
		this.timestamp = rev.getCommitTime() * 1000l;
		this.user = rev.getAuthorIdent().getName();
		this.parentIds = Arrays.stream(rev.getParents())
				.map(p -> p.getId().name())
				.collect(Collectors.toList());
	}

	@Override
	public String toString() {
		var timestamp = Long.toString(this.timestamp);
		var length = id.length() + message.length() + user.length() + timestamp.length() + 3;
		var string = new StringBuilder(length);
		string.append(id);
		string.append(' ');
		string.append(timestamp);
		string.append(' ');
		string.append(user);
		string.append(' ');
		string.append(message);
		return string.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Commit))
			return false;
		var commit = (Commit) obj;
		return id.equals(commit.id);
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

}
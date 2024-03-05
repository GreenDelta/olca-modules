package org.openlca.git.repo;

import static org.openlca.git.repo.ExampleData.COMMIT_1;
import static org.openlca.git.repo.ExampleData.COMMIT_2;
import static org.openlca.git.repo.ExampleData.COMMIT_3;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.util.BinaryResolver;
import org.openlca.git.util.Constants;

public class HistoryTests extends AbstractRepositoryTests {

	@Test
	public void testContains() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		var history = History.of(repo, Constants.LOCAL_BRANCH);
		Assert.assertTrue(history.contains(repo.commits.get(commitIds[0])));
		Assert.assertTrue(history.contains(repo.commits.get(commitIds[1])));
		Assert.assertTrue(history.contains(repo.commits.get(commitIds[2])));
	}

	@Override
	protected BinaryResolver getBinaryResolver() {
		return new StaticBinaryResolver(ExampleData.PATH_TO_BINARY);
	}

}

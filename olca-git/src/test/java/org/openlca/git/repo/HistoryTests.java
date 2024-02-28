package org.openlca.git.repo;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.git.Tests.TmpConfig;
import org.openlca.git.util.Constants;

public class HistoryTests {

	private static TmpConfig config;
	private static ClientRepository repo;
	private static String[] commitIds;

	@BeforeClass
	public static void createRepo() throws IOException {
		config = TmpConfig.create();
		repo = config.repo();
		commitIds = new String[] {
				RepoData.commit(config.repo(), RepoData.EXAMPLE_COMMIT_1),
				RepoData.commit(config.repo(), RepoData.EXAMPLE_COMMIT_2),
				RepoData.commit(config.repo(), RepoData.EXAMPLE_COMMIT_3)
		};
	}

	@Test
	public void testContains() {
		var history = History.of(repo, Constants.LOCAL_BRANCH);
		Assert.assertTrue(history.contains(repo.commits.get(commitIds[0])));
		Assert.assertTrue(history.contains(repo.commits.get(commitIds[1])));
		Assert.assertTrue(history.contains(repo.commits.get(commitIds[2])));
	}

	@Test
	public void testAheadOf() {
		var history = History.of(repo, Constants.LOCAL_BRANCH);
		Assert.assertFalse(history.isAheadOf(repo.commits.get(commitIds[0]), Constants.LOCAL_BRANCH));
		Assert.assertFalse(history.isAheadOf(repo.commits.get(commitIds[1]), Constants.LOCAL_BRANCH));
		Assert.assertFalse(history.isAheadOf(repo.commits.get(commitIds[2]), Constants.LOCAL_BRANCH));
	}

	@Test
	public void testBehindOf() {
		var history = History.of(repo, Constants.LOCAL_BRANCH);
		Assert.assertTrue(history.isBehindOf(repo.commits.get(commitIds[0]), Constants.LOCAL_BRANCH));
		Assert.assertTrue(history.isBehindOf(repo.commits.get(commitIds[1]), Constants.LOCAL_BRANCH));
		Assert.assertFalse(history.isBehindOf(repo.commits.get(commitIds[2]), Constants.LOCAL_BRANCH));
	}

	@AfterClass
	public static void closeRepo() {
		config.close();
	}

}

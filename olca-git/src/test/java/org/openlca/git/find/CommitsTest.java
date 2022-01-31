package org.openlca.git.find;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.git.model.Commit;

public class CommitsTest extends AbstractRepoTest {

	private Commits commits;

	@Before
	public void before() {
		commits = Commits.of(repo);
	}

	@Test
	public void testGet() {
		Commit commit = commits.get(commitIds[2]);
		Assert.assertNotNull(commit);
		Assert.assertEquals(commitIds[2], commit.id);
	}

	@Test
	public void testLatestId() {
		String lastId = commits.resolve("HEAD");
		Assert.assertNotNull(lastId);
		Assert.assertEquals(commitIds[commitIds.length - 1], lastId);
	}

	@Test
	public void testLatest() {
		Commit commit = commits.head();
		Assert.assertNotNull(commit);
		Assert.assertEquals(commitIds[commitIds.length - 1], commit.id);
	}

	@Test
	public void testFindLatestId() {
		String lastId = commits.find().latestId();
		Assert.assertNotNull(lastId);
		Assert.assertEquals(commitIds[commitIds.length - 1], lastId);
	}

	@Test
	public void testFindLatest() {
		Commit commit = commits.find().latest();
		Assert.assertNotNull(commit);
		Assert.assertEquals(commitIds[commitIds.length - 1], commit.id);
	}

	@Test
	public void testFindAll() {
		List<Commit> all = commits.find().all();
		Assert.assertEquals(commitIds.length, all.size());
		for (int i = 0; i < commitIds.length; i++) {
			Commit commit = all.get(i);
			Assert.assertEquals(commitIds[i], commit.id);
		}
	}

	@Test
	public void testFindAfter() {
		List<Commit> all = commits.find().after(commitIds[1]).all();
		Assert.assertEquals(commitIds.length - 2, all.size());
		for (int i = 0; i < commitIds.length - 2; i++) {
			Commit commit = all.get(i);
			Assert.assertEquals(commitIds[i + 2], commit.id);
		}
	}

	@Test
	public void testFindFrom() {
		List<Commit> all = commits.find().from(commitIds[1]).all();
		Assert.assertEquals(commitIds.length - 1, all.size());
		for (int i = 0; i < commitIds.length - 1; i++) {
			Commit commit = all.get(i);
			Assert.assertEquals(commitIds[i + 1], commit.id);
		}
	}

	@Test
	public void testFindUntil() {
		List<Commit> all = commits.find().until(commitIds[2]).all();
		Assert.assertEquals(3, all.size());
		for (int i = 0; i < 3; i++) {
			Commit commit = all.get(i);
			Assert.assertEquals(commitIds[i], commit.id);
		}
	}

	@Test
	public void testFindModel() {
		List<Commit> all = commits.find().model(ModelType.LOCATION, "af92823f-638d-36d7-8406-451a58f61543").all();
		Assert.assertEquals(1, all.size());
		Assert.assertEquals(commitIds[commitIds.length - 3], all.get(0).id);
	}

	@Test
	public void testFindLastModel() {
		Commit commit = commits.find().model(ModelType.LOCATION, "af92823f-638d-36d7-8406-451a58f61543").latest();
		Assert.assertNotNull(commit);
		Assert.assertEquals(commitIds[commitIds.length - 3], commit.id);
	}

	@Test
	public void testFindLastModelId() {
		String commitId = commits.find().model(ModelType.LOCATION, "af92823f-638d-36d7-8406-451a58f61543").latestId();
		Assert.assertNotNull(commitId);
		Assert.assertEquals(commitIds[commitIds.length - 3], commitId);
	}

	@Test
	public void testFindLastModelBefore() {
		Commit commit = commits.find().model(ModelType.LOCATION, "af92823f-638d-36d7-8406-451a58f61543")
				.before(commitIds[commitIds.length - 3]).latest();
		Assert.assertNull(commit);
		commit = commits.find().model(ModelType.LOCATION, "af92823f-638d-36d7-8406-451a58f61543")
				.before(commitIds[commitIds.length - 2]).latest();
		Assert.assertNotNull(commit);
		Assert.assertEquals(commitIds[commitIds.length - 3], commit.id);
	}

	@Test
	public void testFindLastModelUntil() {
		Commit commit = commits.find().model(ModelType.LOCATION, "af92823f-638d-36d7-8406-451a58f61543")
				.until(commitIds[commitIds.length - 4]).latest();
		Assert.assertNull(commit);
		commit = commits.find().model(ModelType.LOCATION, "af92823f-638d-36d7-8406-451a58f61543")
				.until(commitIds[commitIds.length - 3]).latest();
		Assert.assertNotNull(commit);
		Assert.assertEquals(commitIds[commitIds.length - 3], commit.id);
	}

}

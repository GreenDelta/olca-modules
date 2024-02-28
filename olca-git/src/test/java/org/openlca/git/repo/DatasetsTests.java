package org.openlca.git.repo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.git.Tests.TmpConfig;
import org.openlca.git.repo.RepoData.StaticBinaryResolver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class DatasetsTests {

	private static TmpConfig config;
	private static ClientRepository repo;
	private static String[] commitIds;

	@BeforeClass
	public static void createRepo() throws IOException {
		config = TmpConfig.create();
		repo = config.repo();
		commitIds = new String[] {
				RepoData.commit(repo, RepoData.EXAMPLE_COMMIT_1),
				RepoData.commit(repo, RepoData.EXAMPLE_COMMIT_2),
				RepoData.commit(repo, RepoData.EXAMPLE_COMMIT_3)
		};
	}

	@Test
	public void testGet() {
		var refId = "0aa39f5b-5021-4b6b-9330-739f082dfae0";
		var ref = repo.references.get(ModelType.ACTOR, refId, commitIds[0]);
		var ds = repo.datasets.get(ref);
		Assert.assertNotNull(ds);
		var obj = new Gson().fromJson(ds, JsonObject.class);
		Assert.assertNotNull(obj);
		Assert.assertEquals(refId, obj.get("@id").getAsString());
		Assert.assertEquals(new Version(0, 0, 0).toString(), obj.get("version").getAsString());

		ref = repo.references.get(ModelType.ACTOR, refId, commitIds[2]);
		ds = repo.datasets.get(ref);
		Assert.assertNotNull(ds);
		obj = new Gson().fromJson(ds, JsonObject.class);
		Assert.assertNotNull(obj);
		Assert.assertEquals(refId, obj.get("@id").getAsString());
		Assert.assertEquals(new Version(0, 0, 1).toString(), obj.get("version").getAsString());
	}

	@Test
	public void testVersion() {
		var refId = "0aa39f5b-5021-4b6b-9330-739f082dfae0";
		var ref = repo.references.get(ModelType.ACTOR, refId, commitIds[0]);
		var meta = repo.datasets.getVersionAndLastChange(ref);
		Assert.assertNotNull(meta);
		Assert.assertEquals(new Version(0, 0, 0).toString(), meta.get("version"));

		ref = repo.references.get(ModelType.ACTOR, refId, commitIds[2]);
		meta = repo.datasets.getVersionAndLastChange(ref);
		Assert.assertNotNull(meta);
		Assert.assertEquals(new Version(0, 0, 1).toString(), meta.get("version"));
	}

	@Test
	public void testBinary() throws UnsupportedEncodingException {
		var refId = "caa39f5b-5021-4b6b-9330-739f082dfae0";
		var ref = repo.references.get(ModelType.ACTOR, refId, commitIds[0]);
		var bin = repo.datasets.getBinary(ref, "test.txt");
		Assert.assertNotNull(bin);
		var str = new String(bin, "utf-8");
		Assert.assertEquals(StaticBinaryResolver.getContent("test.txt"), str);
	}

	@AfterClass
	public static void closeRepo() {
		config.close();
	}

}

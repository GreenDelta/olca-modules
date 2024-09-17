package org.openlca.git.repo;

import static org.openlca.git.repo.ExampleData.COMMIT_1;
import static org.openlca.git.repo.ExampleData.COMMIT_2;
import static org.openlca.git.repo.ExampleData.COMMIT_3;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Version;
import org.openlca.git.AbstractRepositoryTests;
import org.openlca.git.util.BinaryResolver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class DatasetsTests extends AbstractRepositoryTests {

	@Test
	public void testGet() throws IOException {
		var refId = "0aa39f5b-5021-4b6b-9330-739f082dfae0";
		repo.delete("ACTOR/" + refId + ".json");
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
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
	public void testVersion() throws IOException {
		var refId = "0aa39f5b-5021-4b6b-9330-739f082dfae0";
		repo.delete("ACTOR/" + refId + ".json");
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
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
	public void testBinary() throws IOException {
		var commitIds = new String[] { repo.commit(COMMIT_1), repo.commit(COMMIT_2), repo.commit(COMMIT_3) };
		var refId = "cAA39f5b-5021_bin1.json+39f082dfae0.";
		var ref = repo.references.get(ModelType.ACTOR, refId, commitIds[0]);
		var bin = repo.datasets.getBinary(ref, "test.txt");
		Assert.assertNotNull(bin);
		var str = new String(bin, "utf-8");
		Assert.assertEquals(StaticBinaryResolver.getContent("test.txt"), str);
	}

	@Override
	protected BinaryResolver getBinaryResolver() {
		return new StaticBinaryResolver(ExampleData.PATH_TO_BINARY);
	}

}

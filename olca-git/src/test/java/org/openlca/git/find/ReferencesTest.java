package org.openlca.git.find;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.git.model.Reference;

public class ReferencesTest extends AbstractRepoTest {

	private References references;

	@Before
	public void before() throws IOException, GitAPIException {
		references = References.of(repo);
	}

	@Test
	public void testGet() {
		Reference ref = references.get(ModelType.UNIT_GROUP, "da299c4d-1741-4da8-9fbd-5ccfb5e1d688",
				commitIds[0]);
		Assert.assertNotNull(ref);
		Assert.assertEquals(ModelType.UNIT_GROUP, ref.type);
		Assert.assertEquals("da299c4d-1741-4da8-9fbd-5ccfb5e1d688", ref.refId);
		Assert.assertEquals(commitIds[0], ref.commitId);
	}

	@Test
	public void testFindAll1() {
		List<Reference> refs = references.find().commit(commitIds[0]).all();
		Assert.assertEquals(27, refs.size());
		Reference ref = refs.get(0);
		Assert.assertEquals(ModelType.UNIT_GROUP, ref.type);
		Assert.assertEquals("da299c4d-1741-4da8-9fbd-5ccfb5e1d688", ref.refId);
		Assert.assertEquals(commitIds[0], ref.commitId);
	}

	@Test
	public void testFindAll2() {
		List<Reference> refs = references.find().commit(commitIds[1]).all();
		Assert.assertEquals(60, refs.size());
		Reference ref = refs.get(0);
		Assert.assertEquals(ModelType.FLOW_PROPERTY, ref.type);
		Assert.assertEquals("fdfecf14-ff8a-4e17-b2b2-f938c4b5cc27", ref.refId);
		Assert.assertEquals(commitIds[1], ref.commitId);
		ref = refs.get(33);
		Assert.assertEquals(ModelType.UNIT_GROUP, ref.type);
		Assert.assertEquals("da299c4d-1741-4da8-9fbd-5ccfb5e1d688", ref.refId);
		Assert.assertEquals(commitIds[1], ref.commitId);
	}

	@Test
	public void testFindTypeChanged() {
		List<Reference> refs = references.find().type(ModelType.FLOW_PROPERTY).commit(commitIds[1])
				.changedSince(commitIds[0]).all();
		Assert.assertEquals(33, refs.size());
		Reference ref = refs.get(0);
		Assert.assertEquals(ModelType.FLOW_PROPERTY, ref.type);
		Assert.assertEquals("fdfecf14-ff8a-4e17-b2b2-f938c4b5cc27", ref.refId);
		Assert.assertEquals(commitIds[1], ref.commitId);
	}

	@Test
	public void testFindPathChanged() {
		List<Reference> refs = references.find().path("FLOW_PROPERTY/Economic flow properties")
				.commit(commitIds[1]).changedSince(commitIds[0]).all();
		Assert.assertEquals(1, refs.size());
		Reference ref = refs.get(0);
		Assert.assertEquals(ModelType.FLOW_PROPERTY, ref.type);
		Assert.assertEquals("fdfecf14-ff8a-4e17-b2b2-f938c4b5cc27", ref.refId);
		Assert.assertEquals(commitIds[1], ref.commitId);
	}

	@Test
	public void testFindModelType() {
		List<Reference> refs = references.find().type(ModelType.FLOW_PROPERTY).all();
		Assert.assertEquals(33, refs.size());
		Reference ref = refs.get(0);
		Assert.assertEquals(ModelType.FLOW_PROPERTY, ref.type);
		Assert.assertEquals("fdfecf14-ff8a-4e17-b2b2-f938c4b5cc27", ref.refId);
	}

}

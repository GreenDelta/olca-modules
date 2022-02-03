package org.openlca.git.find;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.ModelType;
import org.openlca.git.model.Reference;

public class DatasetsTest extends AbstractRepoTest {

	private References references;
	private Datasets datasets;

	@Before
	public void before() throws IOException, GitAPIException {
		references = References.of(repo);
		datasets = Datasets.of(repo);
	}

	@Test
	public void testGet() {
		Reference ref = references.get(ModelType.FLOW, "00c3eaf0-7c2f-3f63-a756-37ffbd4f2b21", commitIds[5]);
		Assert.assertNotNull(ref);
		String data = datasets.get(ref);
		Assert.assertNotNull(data);
	}

}

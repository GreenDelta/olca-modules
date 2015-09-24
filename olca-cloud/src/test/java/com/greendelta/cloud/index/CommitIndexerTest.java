package com.greendelta.cloud.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.ModelType;

import com.greendelta.cloud.model.data.FileReference;
import com.greendelta.cloud.util.Directories;

public class CommitIndexerTest {

	private final static File DIRECTORY = new File("/opt/test/indices/commit");

	private CommitIndexer indexer;

	@Before
	public void before() throws IOException {
		if (!DIRECTORY.exists())
			DIRECTORY.mkdir();
		indexer = new CommitIndexer(DIRECTORY);
	}

	@Test
	public void testIndexing() {
		String commitId = UUID.randomUUID().toString();
		List<FileReference> expected = createFileReferences(100);
		indexer.index(commitId, expected);
		List<FileReference> actual = indexer.get(commitId);
		Assert.assertEquals(expected.size(), actual.size());
	}

	private List<FileReference> createFileReferences(int amount) {
		List<FileReference> references = new ArrayList<>();
		for (int i = 1; i <= amount; i++) {
			FileReference reference = new FileReference();
			reference.setType(ModelType.ACTOR);
			reference.setRefId(UUID.randomUUID().toString());
			references.add(reference);
		}
		return references;
	}

	@After
	public void after() {
		Directories.delete(DIRECTORY);
	}

}

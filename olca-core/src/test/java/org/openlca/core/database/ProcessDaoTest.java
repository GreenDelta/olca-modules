package org.openlca.core.database;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.ListUtils;
import org.openlca.core.TestSession;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.ProcessDescriptor;

import com.google.common.base.Optional;

public class ProcessDaoTest {

	private ProcessDao dao;
	private Process process;

	@Before
	public void setUp() throws Exception {
		dao = new ProcessDao(TestSession.getDefaultDatabase()
				.getEntityFactory());
		process = createProcess();
		dao.insert(process);
		TestSession.emptyCache();
	}

	@After
	public void tearDown() throws Exception {
		dao.delete(process);
	}

	private Process createProcess() {
		Process process = new Process();
		process.setRefId(UUID.randomUUID().toString());
		process.setName("name");
		return process;
	}

	@Test
	public void testGetDescriptorsForNullCategory() throws Exception {
		Category cat = null;
		List<ProcessDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(cat));
		ProcessDescriptor descriptor = ListUtils.findDescriptor(
				process.getRefId(), descriptors);
		Assert.assertNotNull(descriptor);
	}

	@Test
	public void testGetDescriptorsForCategory() throws Exception {
		Category category = addCategory();
		List<ProcessDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(category));
		ProcessDescriptor descriptor = ListUtils.findDescriptor(
				process.getRefId(), descriptors);
		Assert.assertNotNull(descriptor);
		TestSession.getDefaultDatabase().createDao(Category.class)
				.delete(category);
	}

	private Category addCategory() throws Exception {
		Category category = new Category();
		category.setRefId(UUID.randomUUID().toString());
		category.setName("test_category");
		category.setModelType(ModelType.ACTOR);
		BaseDao<Category> catDao = TestSession.getDefaultDatabase().createDao(
				Category.class);
		catDao.insert(category);
		process.setCategory(category);
		dao.update(process);
		return category;
	}

	@Test
	public void testQuery() throws Exception {
		Process process = createProcess();
		dao.insert(process);
		TestSession.emptyCache();
		String jpql = "select a from Process a where a.name = :name";
		List<Process> results = dao.getAll(jpql,
				Collections.singletonMap("name", "name"));
		dao.delete(process);
		Assert.assertTrue(results.size() > 0);
		boolean found = false;
		for (Process a : results)
			if (Objects.equals(a.getRefId(), process.getRefId()))
				found = true;
		Assert.assertTrue(found);
	}
}

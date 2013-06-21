package org.openlca.core.database;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.ListUtils;
import org.openlca.core.TestSession;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Project;
import org.openlca.core.model.descriptors.ProjectDescriptor;

import com.google.common.base.Optional;

public class ProjectDaoTest {

	private ProjectDao dao;
	private Project project;

	@Before
	public void setUp() throws Exception {
		dao = new ProjectDao(TestSession.getDefaultDatabase()
				.getEntityFactory());
		project = createProject();
		dao.insert(project);
		TestSession.emptyCache();
	}

	@After
	public void tearDown() throws Exception {
		dao.delete(project);
	}

	private Project createProject() {
		Project project = new Project();
		project.setId(UUID.randomUUID().toString());
		project.setName("name");
		return project;
	}

	@Test
	public void testGetDescriptorsForNullCategory() throws Exception {
		Category cat = null;
		List<ProjectDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(cat));
		ProjectDescriptor descriptor = ListUtils.findDescriptor(
				project.getId(), descriptors);
		Assert.assertNotNull(descriptor);
	}

	@Test
	public void testGetDescriptorsForCategory() throws Exception {
		Category category = addCategory();
		List<ProjectDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(category));
		ProjectDescriptor descriptor = ListUtils.findDescriptor(
				project.getId(), descriptors);
		Assert.assertNotNull(descriptor);
		TestSession.getDefaultDatabase().createDao(Category.class)
				.delete(category);
	}

	private Category addCategory() throws Exception {
		Category category = new Category();
		category.setId(UUID.randomUUID().toString());
		category.setName("test_category");
		category.setModelType(ModelType.FLOW_PROPERTY);
		BaseDao<Category> catDao = TestSession.getDefaultDatabase().createDao(
				Category.class);
		catDao.insert(category);
		project.setCategory(category);
		dao.update(project);
		return category;
	}

}

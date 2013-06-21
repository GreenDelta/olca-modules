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
import org.openlca.core.model.Source;
import org.openlca.core.model.descriptors.SourceDescriptor;

import com.google.common.base.Optional;

public class SourceDaoTest {

	private SourceDao dao;
	private Source source;

	@Before
	public void setUp() throws Exception {
		dao = new SourceDao(TestSession.getDefaultDatabase().getEntityFactory());
		source = createSource();
		dao.insert(source);
		TestSession.emptyCache();
	}

	@After
	public void tearDown() throws Exception {
		dao.delete(source);
	}

	private Source createSource() {
		Source source = new Source();
		source.setId(UUID.randomUUID().toString());
		source.setName("name");
		return source;
	}

	@Test
	public void testGetDescriptorsForNullCategory() throws Exception {
		Category cat = null;
		List<SourceDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(cat));
		SourceDescriptor descriptor = ListUtils.findDescriptor(source.getId(),
				descriptors);
		Assert.assertNotNull(descriptor);
	}

	@Test
	public void testGetDescriptorsForCategory() throws Exception {
		Category category = addCategory();
		List<SourceDescriptor> descriptors = dao.getDescriptors(Optional
				.fromNullable(category));
		SourceDescriptor descriptor = ListUtils.findDescriptor(source.getId(),
				descriptors);
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
		source.setCategory(category);
		dao.update(source);
		return category;
	}

}

package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.ModelType;

public abstract class BaseReferenceSearchTest {

	private List<Reference> expectedReferences = new ArrayList<>();

	@Before
	public void setup() {
		Tests.clearDb();
	}

	@After
	public void clear() {
		Tests.clearDb();
	}

	@Test
	public void testNoReferences() throws InstantiationException,
			IllegalAccessException {
		AbstractEntity minimalModel = (AbstractEntity) getModelClass()
				.newInstance();
		List<Reference> references = findReferences(minimalModel.getId());
		Assert.assertNotNull(references);
		Assert.assertEquals(0, references.size());
	}

	@Test
	public void testAllReferences() {
		AbstractEntity fullModel = createModel();
		List<Reference> references = findReferences(fullModel.getId());
		Assert.assertNotNull(references);
		for (Reference ref : expectedReferences) {
			Reference reference = find(ref, references);
			String text = ref.type.getName() + " " + ref.id + " not found";
			Assert.assertNotNull(text, reference);
			references.remove(reference);
		}
		for (Reference r : references) {
			String text = "Unexpected: " + r.type.getName() + " " + r.id;
			Assert.assertTrue(text, false);
		}
	}

	private Reference find(Reference reference, List<Reference> references) {
		for (Reference ref : references)
			if (ref.type == reference.type)
				if (ref.id == reference.id)
					return ref;
		return null;
	}

	protected List<Reference> findReferences(long id) {
		ModelType type = getModelType();
		IReferenceSearch<?> search = IReferenceSearch.FACTORY.createFor(type,
				Tests.getDb(), true);
		return search.findReferences(id);
	}

	protected <T extends CategorizedEntity> T insertAndAddExpected(T entity) {
		entity = Tests.insert(entity);
		expectedReferences
				.add(new Reference(entity.getClass(), entity.getId()));
		return entity;
	}

	protected <T extends AbstractEntity> T addExpected(T entity) {
		expectedReferences
				.add(new Reference(entity.getClass(), entity.getId()));
		return entity;
	}

	protected Class<?> getModelClass() {
		return getModelType().getModelClass();
	}

	protected abstract ModelType getModelType();

	protected abstract AbstractEntity createModel();

}

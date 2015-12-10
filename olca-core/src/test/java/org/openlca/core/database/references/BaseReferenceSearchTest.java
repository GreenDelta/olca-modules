package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

public abstract class BaseReferenceSearchTest {

	private IReferenceSearch<?> search;
	private List<Reference> expectedReferences = new ArrayList<>();

	@Before
	public void setup() {
		ModelType type = getModelType();
		search = IReferenceSearch.FACTORY.createFor(type, Tests.getDb(), true);
	}

	@Test
	public void testNoReferences() throws InstantiationException,
			IllegalAccessException {
		CategorizedEntity minimalModel = (CategorizedEntity) getModelType()
				.getModelClass().newInstance();
		List<CategorizedDescriptor> references = runSearch(minimalModel);
		Assert.assertNotNull(references);
		Assert.assertEquals(0, references.size());
	}

	@Test
	public void testAllReferences() {
		CategorizedEntity fullModel = createModel();
		List<CategorizedDescriptor> references = runSearch(fullModel);
		Assert.assertNotNull(references);
		for (Reference ref : expectedReferences) {
			CategorizedDescriptor descriptor = find(ref, references);
			String text = ref.type.name() + " " + ref.id + " not found";
			Assert.assertNotNull(text, descriptor);
			references.remove(descriptor);
		}
		for (CategorizedDescriptor d : references) {
			ModelType type = d.getModelType();
			String text = "Unexpected: " + type.name() + " " + d.getId();
			Assert.assertTrue(text, false);
		}
	}

	private CategorizedDescriptor find(Reference reference,
			List<CategorizedDescriptor> references) {
		for (CategorizedDescriptor descriptor : references)
			if (descriptor.getModelType() == reference.type)
				if (descriptor.getId() == reference.id)
					return descriptor;
		return null;
	}

	private List<CategorizedDescriptor> runSearch(CategorizedEntity model) {
		model = Tests.insert(model);
		List<CategorizedDescriptor> results = search.findReferences(model
				.getId());
		Tests.delete(model);
		return results;
	}

	protected <T extends CategorizedEntity> T addExpected(T entity) {
		ModelType type = ModelType.forModelClass(entity.getClass());
		entity = Tests.insert(entity);
		expectedReferences.add(new Reference(type, entity.getId()));
		return entity;
	}

	protected abstract ModelType getModelType();

	protected abstract CategorizedEntity createModel();

	private static class Reference {

		private ModelType type;
		private long id;

		private Reference(ModelType type, long id) {
			this.type = type;
			this.id = id;
		}

	}

}

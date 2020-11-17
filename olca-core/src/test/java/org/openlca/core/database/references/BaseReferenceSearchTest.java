package org.openlca.core.database.references;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

public abstract class BaseReferenceSearchTest {

	protected IDatabase db = Tests.getDb();
	private List<Reference> expectedReferences = new ArrayList<>();
	private Map<Long, List<Reference>> referencesByOwner = new HashMap<>();

	@Before
	public void setup() {
		db.clear();
	}

	@After
	public void clear() {
		db.clear();
	}

	@Test
	public void testNoReferences() throws Exception {
		var empty = getModelClass()
				.getDeclaredConstructor()
				.newInstance();
		List<Reference> references = findReferences(
				Collections.singleton(empty.id));
		Assert.assertNotNull(references);
		Assert.assertEquals(0, references.size());
	}

	@Test
	public void testAllReferencesSingleModel() {
		AbstractEntity fullModel = createModel();
		List<Reference> references = findReferences(Collections.singleton(fullModel.id));
		Assert.assertNotNull(references);
		for (Reference ref : expectedReferences) {
			Reference reference = find(ref, references, fullModel.id);
			String text = ref.getType().getName() + " " + ref.id + " not found";
			Assert.assertNotNull(text, reference);
			references.remove(reference);
		}
		for (Reference r : references) {
			if (r.optional && r.id == 0)
				continue;
			Assert.fail("Unexpected: " + r.getType().getName() + " " + r.id);
		}
	}

	@Test
	public void testAllReferencesMultipleModel() {
		Set<Long> ids = new HashSet<>();
		for (int i = 0; i < 3; i++) {
			AbstractEntity fullModel = createModel();
			referencesByOwner.put(fullModel.id, expectedReferences);
			expectedReferences = new ArrayList<>();
			ids.add(fullModel.id);
		}
		List<Reference> references = findReferences(ids);
		Assert.assertNotNull(references);
		for (long id : ids) {
			List<Reference> refs = new ArrayList<>();
			for (Reference reference : references)
				if (!isNestedSearchTest() && reference.ownerId == id)
					refs.add(reference);
				else if (isNestedSearchTest() && reference.nestedOwnerId == id)
					refs.add(reference);
			for (Reference ref : referencesByOwner.get(id)) {
				Reference reference = find(ref, refs, id);
				String text = ref.getType().getName() + " " + ref.id + " not found";
				Assert.assertNotNull(text, reference);
				refs.remove(reference);
			}
			for (Reference r : refs) {
				if (r.optional && r.id == 0)
					continue;
				Assert.fail("Unexpected: " + r.getType().getName() + " " + r.id);
			}
		}
	}

	private Reference find(Reference reference, List<Reference> references,
			long ownerId) {
		for (Reference ref : references) {
			if (!Strings.nullOrEqual(ref.property, reference.property))
				continue;
			if (ref.getType() != reference.getType())
				continue;
			if (ref.id != reference.id)
				continue;
			if (ref.getOwnerType() != reference.getOwnerType())
				continue;
			if (ref.ownerId != (reference.ownerId != 0L ? reference.ownerId : ownerId))
				continue;
			if (!Strings.nullOrEqual(ref.nestedProperty, reference.nestedProperty))
				continue;
			if (ref.getNestedOwnerType() != reference.getNestedOwnerType())
				continue;
			if (ref.nestedOwnerId != reference.nestedOwnerId)
				continue;
			return ref;
		}
		return null;
	}

	protected List<Reference> findReferences(Set<Long> ids) {
		ModelType type = getModelType();
		IReferenceSearch<?> search = IReferenceSearch.FACTORY.createFor(type, Tests.getDb(), true);
		return search.findReferences(ids);
	}

	protected final <T extends CategorizedEntity> T insertAndAddExpected(String property, T entity) {
		return insertAndAddExpected(property, entity, null, null, 0);
	}

	protected final <T extends CategorizedEntity> T insertAndAddExpected(String property, T entity,
			String nestedProperty, Class<? extends AbstractEntity> nestedOwnerType, long nestedOwnerId) {
		entity = db.insert(entity);
		expectedReferences.add(new Reference(property, entity.getClass(), entity.id, getModelClass(), 0,
				nestedProperty, nestedOwnerType, nestedOwnerId, false));
		return entity;
	}

	protected final void addExpected(String property, AbstractEntity entity) {
		addExpected(property, entity, null, null, 0);
	}

	protected final void addExpected(Reference reference) {
		expectedReferences.add(reference);
	}

	protected final void addExpected(String property, AbstractEntity entity, String nestedProperty,
			Class<? extends AbstractEntity> nestedOwnerType, long nestedOwnerId) {
		expectedReferences.add(new Reference(property, entity.getClass(), entity.id, getModelClass(), 0,
				nestedProperty, nestedOwnerType, nestedOwnerId, false));
	}

	protected Class<? extends AbstractEntity> getModelClass() {
		return getModelType().getModelClass();
	}

	protected String generateName() {
		return "P" + UUID.randomUUID().toString().replace("-", "");
	}

	protected boolean isNestedSearchTest() {
		return false;
	}

	protected abstract ModelType getModelType();

	protected abstract AbstractEntity createModel();

}

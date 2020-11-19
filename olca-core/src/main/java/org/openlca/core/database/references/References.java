package org.openlca.core.database.references;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

public final class References {

	private References() {
	}

	public static <T extends CategorizedEntity> List<Reference> of(
			IDatabase db, T owner) {
		return owner == null
				? Collections.emptyList()
				: of(db, owner.getClass(), owner.id);
	}

	public static <D extends CategorizedDescriptor> List<Reference> of(
			IDatabase db, Collection<D> owners) {
		ModelType type = null;
		var ids = new HashSet<Long>();
		for (var d : owners) {
			if (d == null || d.id == 0)
				continue;
			if (type == null) {
				type = d.type;
			} else if (type != d.type) {
				throw new IllegalArgumentException("Mixed descriptor " +
						"types are not allowed in reference search");
			}
			ids.add(d.id);
		}
		return type == null || ids.isEmpty()
				? Collections.emptyList()
				: of(db, type, ids);
	}

	public static <D extends CategorizedDescriptor> List<Reference> of(
			IDatabase db, D owner) {
		return owner == null
				? Collections.emptyList()
				: of(db, owner.type, owner.id);
	}

	public static List<Reference> of(IDatabase db, Class<?> type, long id) {
		if (type == null || id == 0L)
			return Collections.emptyList();
		var modelType = ModelType.forModelClass(type);
		return modelType == null
				? Collections.emptyList()
				: of(db, modelType, id);
	}

	public static List<Reference> of(IDatabase db, ModelType type, long id) {
		return type == null || id == 0L
				? Collections.emptyList()
				: of(db, type, Collections.singleton(id));
	}

	public static List<Reference> of(
			IDatabase db, ModelType type, Set<Long> owners) {
		if (type == null || owners == null || owners.isEmpty())
			return Collections.emptyList();
		var search = searchOf(db, type);
		return search == null
				? Collections.emptyList()
				: search.findReferences(owners);
	}

	private static IReferenceSearch<?> searchOf(IDatabase db, ModelType type) {
		if (type == null)
			return null;

		switch (type) {
			case UNIT_GROUP:
				return new UnitGroupReferenceSearch(db, true);
			case FLOW_PROPERTY:
				return new FlowPropertyReferenceSearch(db, true);
			case FLOW:
				return new FlowReferenceSearch(db, true);
			case PROCESS:
				return new ProcessReferenceSearch(db, true);
			case PRODUCT_SYSTEM:
				return new ProductSystemReferenceSearch(db, true);
			case PROJECT:
				return new ProjectReferenceSearch(db, true);
			case IMPACT_METHOD:
				return new ImpactMethodReferenceSearch(db, true);
			case CURRENCY:
				return new CurrencyReferenceSearch(db, true);
			case SOCIAL_INDICATOR:
				return new SocialIndicatorReferenceSearch(db, true);
			case CATEGORY:
				return new CategoryReferenceSearch(db, true);
			case PARAMETER:
				return new ParameterReferenceSearch(db, true);
			case DQ_SYSTEM:
				return new DQSystemReferenceSearch(db, true);
			default:
				return null;
		}
	}

}

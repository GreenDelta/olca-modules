package org.openlca.core.database.references;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

/** Search of used entities within an entity. */
public interface IReferenceSearch<T extends CategorizedDescriptor> {

	/**
	 * Returns a list of descriptors of entities that are used in the entity
	 * with the given id
	 */
	public List<CategorizedDescriptor> findReferences(long id);

	public List<CategorizedDescriptor> findReferences(T entity);

	public List<CategorizedDescriptor> findReferences(List<T> entity);

	public List<CategorizedDescriptor> findReferences(Set<Long> entity);

	public static final Factory FACTORY = new Factory();

	public class Factory {

		@SuppressWarnings("unchecked")
		public <T extends CategorizedDescriptor> IReferenceSearch<T> createFor(
				ModelType type, IDatabase db, boolean includeOptional) {
			switch (type) {
			case UNIT_GROUP:
				return (IReferenceSearch<T>) new UnitGroupReferenceSearch(db,
						includeOptional);
			case FLOW_PROPERTY:
				return (IReferenceSearch<T>) new FlowPropertyReferenceSearch(
						db, includeOptional);
			case FLOW:
				return (IReferenceSearch<T>) new FlowReferenceSearch(db,
						includeOptional);
			case PROCESS:
				return (IReferenceSearch<T>) new ProcessReferenceSearch(db,
						includeOptional);
			case PRODUCT_SYSTEM:
				return (IReferenceSearch<T>) new ProductSystemReferenceSearch(
						db, includeOptional);
			case PROJECT:
				return (IReferenceSearch<T>) new ProjectReferenceSearch(db,
						includeOptional);
			case IMPACT_METHOD:
				return (IReferenceSearch<T>) new ImpactMethodReferenceSearch(
						db, includeOptional);
			case CURRENCY:
				return (IReferenceSearch<T>) new CurrencyReferenceSearch(db,
						includeOptional);
			case SOCIAL_INDICATOR:
				return (IReferenceSearch<T>) new SocialIndicatorReferenceSearch(
						db, includeOptional);
			case CATEGORY:
				return (IReferenceSearch<T>) new CategoryReferenceSearch(db,
						includeOptional);
			case PARAMETER: 
				return (IReferenceSearch<T>) new ParameterReferenceSearch(db,
						includeOptional);
			default:
				return new EmptyReferenceSearch<T>();
			}
		}
	}

}

package org.openlca.core.database.references;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.CategorizedDescriptor;

/** Search of used entities within an entity. */
public interface IReferenceSearch<T extends CategorizedDescriptor> {

	public List<Reference> findReferences();

	/**
	 * Returns a list of descriptors of entities that are used in the entity
	 * with the given id
	 */
	public List<Reference> findReferences(long id);

	public List<Reference> findReferences(T entity);

	public List<Reference> findReferences(List<T> entity);

	public List<Reference> findReferences(Set<Long> entity);

	public static final Factory FACTORY = new Factory();

	public class Factory {

		@SuppressWarnings("unchecked")
		public <T extends CategorizedDescriptor> IReferenceSearch<T> createFor(ModelType type, IDatabase db,
				boolean includeOptional) {
			switch (type) {
			case UNIT_GROUP:
				return (IReferenceSearch<T>) new UnitGroupReferenceSearch(db, includeOptional);
			case FLOW_PROPERTY:
				return (IReferenceSearch<T>) new FlowPropertyReferenceSearch(db, includeOptional);
			case FLOW:
				return (IReferenceSearch<T>) new FlowReferenceSearch(db, includeOptional);
			case PROCESS:
				return (IReferenceSearch<T>) new ProcessReferenceSearch(db, includeOptional);
			case PRODUCT_SYSTEM:
				return (IReferenceSearch<T>) new ProductSystemReferenceSearch(db, includeOptional);
			case PROJECT:
				return (IReferenceSearch<T>) new ProjectReferenceSearch(db, includeOptional);
			case IMPACT_METHOD:
				return (IReferenceSearch<T>) new ImpactMethodReferenceSearch(db, includeOptional);
			case CURRENCY:
				return (IReferenceSearch<T>) new CurrencyReferenceSearch(db, includeOptional);
			case SOCIAL_INDICATOR:
				return (IReferenceSearch<T>) new SocialIndicatorReferenceSearch(db, includeOptional);
			case CATEGORY:
				return (IReferenceSearch<T>) new CategoryReferenceSearch(db, includeOptional);
			case PARAMETER:
				return (IReferenceSearch<T>) new ParameterReferenceSearch(db, includeOptional);
			case DQ_SYSTEM:
				return (IReferenceSearch<T>) new DQSystemReferenceSearch(db, includeOptional);
			default:
				return new EmptyReferenceSearch<T>();
			}
		}
	}

	public class Reference implements Serializable {

		private static final long serialVersionUID = -3036634720068312246L;

		public final String property;
		private final String type;
		public final long id;
		private final String ownerType;
		public final long ownerId;
		public final String nestedProperty;
		private final String nestedOwnerType;
		public final long nestedOwnerId;
		public final boolean optional;
		
		public Reference(String property, Class<? extends AbstractEntity> type, long id,
				Class<? extends AbstractEntity> ownerType, long ownerId) {
			this(property, type, id, ownerType, ownerId, null, null, 0l, false);
		}

		public Reference(String property, Class<? extends AbstractEntity> type, long id,
				Class<? extends AbstractEntity> ownerType, long ownerId,
				boolean optional) {
			this(property, type, id, ownerType, ownerId, null, null, 0l, optional);
		}

		public Reference(String property, Class<? extends AbstractEntity> type, long id,
				Class<? extends AbstractEntity> ownerType, long ownerId,
				String nestedProperty, Class<? extends AbstractEntity> nestedOwnerType,
				long nestedOwnerId, boolean optional) {
			this.property = property;
			this.type = type.getCanonicalName();
			this.id = id;
			this.ownerType = ownerType.getCanonicalName();
			this.ownerId = ownerId;
			this.nestedProperty = nestedProperty;
			this.nestedOwnerType = nestedOwnerType != null ? nestedOwnerType
					.getCanonicalName() : null;
			this.nestedOwnerId = nestedOwnerId;
			this.optional = optional;
		}

		@SuppressWarnings("unchecked")
		public Class<? extends AbstractEntity> getType() {
			try {
				return (Class<? extends AbstractEntity>) Class.forName(type);
			} catch (ClassNotFoundException e) {
				return null;
			}
		}

		@SuppressWarnings("unchecked")
		public Class<? extends AbstractEntity> getOwnerType() {
			try {
				return (Class<? extends AbstractEntity>) Class
						.forName(ownerType);
			} catch (ClassNotFoundException e) {
				return null;
			}
		}

		@SuppressWarnings("unchecked")
		public Class<? extends AbstractEntity> getNestedOwnerType() {
			if (nestedOwnerType == null)
				return null;
			try {
				return (Class<? extends AbstractEntity>) Class
						.forName(nestedOwnerType);
			} catch (ClassNotFoundException e) {
				return null;
			}
		}

	}

}

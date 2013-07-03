package org.openlca.core.database;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.descriptors.BaseDescriptor;

import com.google.common.base.Optional;

public abstract class RootEntityDao<T extends CategorizedEntity> extends
		BaseDao<T> {

	public RootEntityDao(Class<T> clazz, EntityManagerFactory factory) {
		super(clazz, factory);
	}

	abstract List<? extends BaseDescriptor> getDescriptors(
			Optional<Category> category);

	public T getForRefId(String refId) {
		if (refId == null)
			return null;
		String jpql = "select e from " + entityType.getSimpleName()
				+ " e where e.refId = :refId";
		try {
			return Query.on(getEntityFactory()).getFirst(entityType, jpql,
					Collections.singletonMap("refId", refId));
		} catch (Exception e) {
			log.error("failed to get instance for refId " + refId, e);
			return null;
		}
	}

}

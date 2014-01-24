package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

/** The DAO class for impact assessment methods. */
public class ImpactMethodDao extends
		CategorizedEntityDao<ImpactMethod, ImpactMethodDescriptor> {

	public ImpactMethodDao(IDatabase database) {
		super(ImpactMethod.class, ImpactMethodDescriptor.class, database);
	}

	public List<ImpactCategoryDescriptor> getCategoryDescriptors(long methodId) {
		try {
			String jpql = "select cat.id, cat.name, cat.referenceUnit, "
					+ "cat.description from ImpactMethod m join m.impactCategories "
					+ "cat where m.id = :methodId ";
			List<Object[]> vals = Query.on(getDatabase()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("methodId", methodId));
			List<ImpactCategoryDescriptor> list = new ArrayList<>();
			for (Object[] val : vals) {
				ImpactCategoryDescriptor d = new ImpactCategoryDescriptor();
				d.setId((Long) val[0]);
				d.setName((String) val[1]);
				d.setReferenceUnit((String) val[2]);
				d.setDescription((String) val[3]);
				list.add(d);
			}
			return list;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
					"Failed to load impact category descriptors", e);
			return Collections.emptyList();
		}
	}

}

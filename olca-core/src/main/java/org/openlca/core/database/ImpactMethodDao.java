package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

/** The DAO class for impact assessment methods. */
public class ImpactMethodDao extends CategorizedEnitityDao<ImpactMethod> {

	public ImpactMethodDao(IDatabase database) {
		super(ImpactMethod.class, database);
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
			log.error("Failed to load impact category descriptors", e);
			return Collections.emptyList();
		}
	}

	public List<NormalizationWeightingSet> getNwSetDescriptors(
			ImpactMethodDescriptor methodDescriptor) {
		if (methodDescriptor == null)
			return Collections.emptyList();
		String jpql = "select n.id, n.referenceSystem, n.unit from "
				+ "NormalizationWeightingSet n, ImpactMethod m "
				+ "where n member of m.normalizationWeightingSets and m.id = :methodId";
		EntityManager em = createManager();
		try {
			TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
			query.setParameter("methodId", methodDescriptor.getId());
			return fetchNwSets(query);
		} catch (Exception e) {
			log.error("Failed to get nw-sets", e);
			return Collections.emptyList();
		} finally {
			em.close();
		}
	}

	private List<NormalizationWeightingSet> fetchNwSets(
			TypedQuery<Object[]> query) {
		List<Object[]> objects = query.getResultList();
		List<NormalizationWeightingSet> results = new ArrayList<>();
		for (Object[] object : objects) {
			NormalizationWeightingSet nwSet = new NormalizationWeightingSet();
			nwSet.setReferenceSystem((String) object[1]);
			nwSet.setUnit((String) object[2]);
			results.add(nwSet);
		}
		return results;
	}
}

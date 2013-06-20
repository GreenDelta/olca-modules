package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.openlca.core.model.Category;
import org.openlca.core.model.LCIAMethod;
import org.openlca.core.model.NormalizationWeightingSet;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

/** The DAO class for impact assessment methods. */
public class ImpactMethodDao extends BaseDao<LCIAMethod> implements
		IRootEntityDao<LCIAMethod> {

	public ImpactMethodDao(EntityManagerFactory factory) {
		super(LCIAMethod.class, factory);
	}

	public List<ImpactMethodDescriptor> getDescriptors(Category category) {
		log.trace("get impact methods for category {}", category);
		String jpql = "select m.id, m.name, m.description from LCIAMethod m "
				+ "where m.category = :category";
		try {
			List<Object[]> results = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("category", category));
			return createDescriptors(results);
		} catch (Exception e) {
			log.error("failed to get impact methods for category " + category,
					e);
			return Collections.emptyList();
		}
	}

	private List<ImpactMethodDescriptor> createDescriptors(
			List<Object[]> results) {
		List<ImpactMethodDescriptor> descriptors = new ArrayList<>();
		for (Object[] result : results) {
			ImpactMethodDescriptor descriptor = new ImpactMethodDescriptor();
			descriptor.setId((String) result[0]);
			descriptor.setName((String) result[1]);
			descriptor.setDescription((String) result[2]);
			descriptors.add(descriptor);
		}
		return descriptors;
	}

	public List<ImpactMethodDescriptor> getDescriptors() {
		try {
			String jpql = "select m.id, m.name, m.description from LCIAMethod m";
			List<Object[]> list = query().getAll(Object[].class, jpql);
			List<ImpactMethodDescriptor> descriptors = new ArrayList<>();
			for (Object[] vals : list) {
				ImpactMethodDescriptor descriptor = createDescriptor(vals);
				descriptors.add(descriptor);
			}
			return descriptors;
		} catch (Exception e) {
			log.error("Failed to load method descriptors", e);
			return Collections.emptyList();
		}
	}

	public ImpactMethodDescriptor getDescriptor(String methodId) {
		try {
			String jpql = "select m.id, m.name, m.description from LCIAMethod m where "
					+ "m.id = :methodId";
			Object[] val = Query.on(getEntityFactory()).getFirst(
					Object[].class, jpql,
					Collections.singletonMap("methodId", methodId));
			if (val == null)
				return null;
			return createDescriptor(val);
		} catch (Exception e) {
			log.error("Failed to load method descriptor", e);
			return null;
		}
	}

	private ImpactMethodDescriptor createDescriptor(Object[] vals) {
		ImpactMethodDescriptor descriptor = new ImpactMethodDescriptor();
		descriptor.setId((String) vals[0]);
		descriptor.setName((String) vals[1]);
		descriptor.setDescription((String) vals[2]);
		descriptor.getImpactCategories().addAll(
				getCategoryDescriptors((String) vals[0]));
		return descriptor;
	}

	public List<ImpactCategoryDescriptor> getCategoryDescriptors(String methodId) {
		try {
			String jpql = "select cat.id, cat.name, cat.referenceUnit, "
					+ "cat.description from LCIAMethod m join m.lciaCategories "
					+ "cat where m.id = :methodId ";
			List<Object[]> vals = Query.on(getEntityFactory()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("methodId", methodId));
			List<ImpactCategoryDescriptor> list = new ArrayList<>();
			for (Object[] val : vals) {
				ImpactCategoryDescriptor d = new ImpactCategoryDescriptor();
				d.setId((String) val[0]);
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
				+ "NormalizationWeightingSet n, LCIAMethod m "
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
			nwSet.setId((String) object[0]);
			nwSet.setReferenceSystem((String) object[1]);
			nwSet.setUnit((String) object[2]);
			results.add(nwSet);
		}
		return results;
	}
}

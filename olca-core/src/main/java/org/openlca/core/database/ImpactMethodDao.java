package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;

/** The DAO class for impact assessment methods. */
public class ImpactMethodDao extends
		CategorizedEntityDao<ImpactMethod, ImpactMethodDescriptor> {

	public ImpactMethodDao(IDatabase database) {
		super(ImpactMethod.class, ImpactMethodDescriptor.class, database);
	}

	public List<ImpactCategoryDescriptor> getCategoryDescriptors(long methodId) {
		return getCategoryDescriptors("id", methodId);
	}

	public List<ImpactCategoryDescriptor> getCategoryDescriptors(String methodId) {
		return getCategoryDescriptors("refId", methodId);
	}

	private List<ImpactCategoryDescriptor> getCategoryDescriptors(String idField, Object methodId) {
		try {
			String jpql = "select cat.id, cat.refId, cat.name, cat.referenceUnit, "
					+ "cat.description, cat.version, cat.lastChange from ImpactMethod m join m.impactCategories "
					+ "cat where m." + idField + " = :methodId ";
			List<Object[]> vals = Query.on(getDatabase()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("methodId", methodId));
			List<ImpactCategoryDescriptor> list = new ArrayList<>();
			for (Object[] val : vals) {
				ImpactCategoryDescriptor d = new ImpactCategoryDescriptor();
				d.setId((Long) val[0]);
				d.setRefId((String) val[1]);
				d.setName((String) val[2]);
				d.setReferenceUnit((String) val[3]);
				d.setDescription((String) val[4]);
				if (val[5] != null)
					d.setVersion((long) val[5]);
				if (val[6] != null)
					d.setLastChange((long) val[6]);
				list.add(d);
			}
			return list;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
					"Failed to load impact category descriptors", e);
			return Collections.emptyList();
		}
	}

	public List<NwSetDescriptor> getNwSetDescriptors(long methodId) {
		return getNwSetDescriptors("id", methodId);
	}

	public List<NwSetDescriptor> getNwSetDescriptors(String methodId) {
		return getNwSetDescriptors("refId", methodId);
	}

	private List<NwSetDescriptor> getNwSetDescriptors(String idField, Object methodId) {
		try {
			String jpql = "select nw.id, nw.refId, nw.name, nw.weightedScoreUnit, "
					+ "nw.description, nw.version, nw.lastChange from ImpactMethod m join m.nwSets "
					+ "nw where m." + idField + " = :methodId ";
			List<Object[]> vals = Query.on(getDatabase()).getAll(
					Object[].class, jpql,
					Collections.singletonMap("methodId", methodId));
			List<NwSetDescriptor> list = new ArrayList<>();
			for (Object[] val : vals) {
				NwSetDescriptor d = new NwSetDescriptor();
				d.setId((Long) val[0]);
				d.setRefId((String) val[1]);
				d.setName((String) val[2]);
				d.setWeightedScoreUnit((String) val[3]);
				d.setDescription((String) val[4]);
				if (val[5] != null)
					d.setVersion((long) val[5]);
				if (val[6] != null)
					d.setLastChange((long) val[6]);
				list.add(d);
			}
			return list;
		} catch (Exception e) {
			DatabaseException.logAndThrow(log,
					"Failed to load nw set descriptors", e);
			return Collections.emptyList();
		}
	}
}

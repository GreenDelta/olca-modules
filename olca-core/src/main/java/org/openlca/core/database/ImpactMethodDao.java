package org.openlca.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
		return getImpactCategories("m.id = " + methodId);
	}

	public List<ImpactCategoryDescriptor> getCategoryDescriptors(String methodId) {
		if (methodId == null)
			return Collections.emptyList();
		return getImpactCategories("m.ref_id = '" + methodId + "'");
	}

	private List<ImpactCategoryDescriptor> getImpactCategories(
			String whereClause) {
		String sql = "select " +
				"  ic.id,            " +
				"  ic.ref_id,        " +
				"  ic.name,          " +
				"  ic.description,   " +
				"  ic.version,       " +
				"  ic.last_change,   " +
				"  ic.f_category,    " +
				"  ic.reference_unit " +
				"from tbl_impact_categories ic     " +
				"inner join tbl_impact_links link  " +
				"on ic.id = link.f_impact_category " +
				"inner join tbl_impact_methods m   " +
				"on m.id = link.f_impact_method    " +
				"where " + whereClause;
		try {
			ImpactCategoryDao cdao = new ImpactCategoryDao(database);
			List<Object[]> list = selectAll(sql,
					cdao.getDescriptorFields(), Collections.emptyList());
			return list.stream()
					.map(cdao::createDescriptor)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new RuntimeException(
					"failed to get method indicators: " + sql, e);
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
				d.id = (Long) val[0];
				d.refId = (String) val[1];
				d.name = (String) val[2];
				d.weightedScoreUnit = (String) val[3];
				d.description = (String) val[4];
				if (val[5] != null)
					d.version = (long) val[5];
				if (val[6] != null)
					d.lastChange = (long) val[6];
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

package org.openlca.core.matrices;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.UncertaintyDistributionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpactFactorTable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final HashMap<Long, List<CalcImpactFactor>> factors = new HashMap<>();

	public ImpactFactorTable(IDatabase database, List<Long> impactCategoryIds) {
		init(database, impactCategoryIds);
	}

	private void init(IDatabase database, List<Long> impactCategoryIds) {
		if (impactCategoryIds.isEmpty())
			return;
		log.trace("create impact factor table for {} impact categories",
				impactCategoryIds.size());
		try (Connection con = database.createConnection()) {
			String query = "select * from tbl_impact_factors where f_impact_category in "
					+ Indices.asSql(impactCategoryIds);
			ResultSet result = con.createStatement().executeQuery(query);
			while (result.next()) {
				CalcImpactFactor factor = nextFactor(result);
				index(factor);
			}
			result.close();
		} catch (Exception e) {
			log.error("failed to load impact factors", e);
		}
	}

	private void index(CalcImpactFactor factor) {
		if (factor == null)
			return;
		Long categoryId = factor.getImactCategoryId();
		List<CalcImpactFactor> list = factors.get(categoryId);
		if (list == null) {
			list = new ArrayList<>();
			factors.put(categoryId, list);
		}
		list.add(factor);
	}

	private CalcImpactFactor nextFactor(ResultSet r) throws Exception {
		CalcImpactFactor f = new CalcImpactFactor();
		f.setAmount(r.getDouble("value"));
		f.setConversionFactor(1d); // TODO: add to table
		f.setFlowId(r.getLong("f_flow"));
		f.setImactCategoryId(r.getLong("f_impact_category"));
		f.setParameter1(r.getDouble("uncertainty_parameter_1"));
		f.setParameter2(r.getDouble("uncertainty_parameter_2"));
		f.setParameter3(r.getDouble("uncertainty_parameter_3"));
		String uncertaintyType = r.getString("uncertainy_type");
		if (uncertaintyType != null)
			f.setUncertaintyType(UncertaintyDistributionType
					.valueOf(uncertaintyType));
		return f;
	}

	public List<CalcImpactFactor> getImpactFactors(long impactCategoryId) {
		List<CalcImpactFactor> list = factors.get(impactCategoryId);
		if (list == null)
			return Collections.emptyList();
		return list;
	}

}

package org.openlca.core.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.BlockFetch;
import org.openlca.core.database.BlockFetch.QueryFunction;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.Query;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a matrix with impact assessment factors.
 * 
 */
public class ImpactMatrixBuilder {

	private IDatabase database;
	private Logger log = LoggerFactory.getLogger(getClass());

	public ImpactMatrixBuilder(IDatabase database) {
		this.database = database;
	}

	public ImpactMatrix build(ImpactMethodDescriptor methodDescriptor,
			FlowIndex flowIndex) {
		log.trace("Build impact factor matrix for method {}", methodDescriptor);
		Index<ImpactCategoryDescriptor> categoryIndex = buildCategoryIndex(methodDescriptor);
		if (categoryIndex.isEmpty() || flowIndex.isEmpty())
			return null;
		ImpactMatrix matrix = new ImpactMatrix();
		matrix.setCategoryIndex(categoryIndex);
		matrix.setFlowIndex(flowIndex);
		IMatrix values = MatrixFactory.create(categoryIndex.size(),
				flowIndex.size());
		matrix.setValues(values);
		fill(matrix, getFlowIds(flowIndex));
		log.trace("Impact factor matrix ready");
		return matrix;
	}

	private Index<ImpactCategoryDescriptor> buildCategoryIndex(
			ImpactMethodDescriptor methodDescriptor) {
		Index<ImpactCategoryDescriptor> index = new Index<>(
				ImpactCategoryDescriptor.class);
		ImpactMethodDao dao = new ImpactMethodDao(database.getEntityFactory());
		for (ImpactCategoryDescriptor cat : dao
				.getCategoryDescriptors(methodDescriptor.getRefId()))
			index.put(cat);
		return index;
	}

	private void fill(ImpactMatrix matrix, List<String> flowIds) {
		FlowIndex flowIndex = matrix.getFlowIndex();
		Index<ImpactCategoryDescriptor> categoryIndex = matrix
				.getCategoryIndex();
		IMatrix values = matrix.getValues();
		for (ImpactCategoryDescriptor category : categoryIndex.getItems()) {
			FactorQuery query = new FactorQuery(category);
			BlockFetch<ImpactFactor> fetch = new BlockFetch<>(query);
			List<ImpactFactor> factors = fetch.doFetch(flowIds);
			for (ImpactFactor factor : factors) {
				Flow flow = factor.getFlow();
				if (!flowIndex.contains(flow))
					continue;
				int row = categoryIndex.getIndex(category);
				int col = flowIndex.getIndex(flow);
				double val = factor.getConvertedValue();
				if (flowIndex.isInput(flow))
					val = -val;
				values.setEntry(row, col, val);
			}
		}
	}

	private List<String> getFlowIds(FlowIndex index) {
		List<String> ids = new ArrayList<>(index.size() + 2);
		for (Flow flow : index.getFlows())
			ids.add(flow.getRefId());
		return ids;
	}

	private class FactorQuery implements QueryFunction<ImpactFactor> {

		private ImpactCategoryDescriptor category;

		public FactorQuery(ImpactCategoryDescriptor category) {
			this.category = category;
		}

		@Override
		public List<ImpactFactor> fetchChunk(List<String> flowIds) {
			try {
				String jpql = "select factor from LCIACategory cat join "
						+ "cat.lciaFactors factor where cat.id = :catId and "
						+ "factor.flow.id in :flowIds";
				Map<String, Object> params = new HashMap<>();
				params.put("catId", category.getRefId());
				params.put("flowIds", flowIds);
				return Query.on(database).getAll(ImpactFactor.class, jpql,
						params);
			} catch (Exception e) {
				log.error("Failed to load LCIA factors", e);
				return Collections.emptyList();
			}
		}
	}

}

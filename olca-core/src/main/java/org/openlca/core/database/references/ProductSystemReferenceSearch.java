package org.openlca.core.database.references;

import java.util.List;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.Search.Reference;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;

public class ProductSystemReferenceSearch extends
		BaseReferenceSearch<ProductSystemDescriptor> {

	private final static Reference[] references = { 
		// don't include reference process, because it is also included in 
		// list of all processes (avoid duplicate reference)
		new Reference(ModelType.CATEGORY, "f_category", true),
		new Reference(ModelType.UNKNOWN, "f_target_flow_property_factor"),
		new Reference(ModelType.UNIT, "f_target_unit"),
	};
	// must split in two calls because it can not be differentiated between flow
	// property factors and exchanges otherwise. Since there will be in most
	// cases not too many product systems this is ok from performance
	// perspective so it is not necessary to use a custom SQL call here
	private final static Reference[] productReferences = { 
		new Reference(ModelType.UNKNOWN, "f_reference_exchange")
	};
	private final static Reference[] processReferences= {
		new Reference(ModelType.PROCESS, "f_process")
	};
	private final static Reference[] flowReferences= {
		new Reference(ModelType.FLOW, "f_flow")
	};


	public ProductSystemReferenceSearch(IDatabase database, boolean includeOptional) {
		super(database, includeOptional);
	}

	@Override
	public List<CategorizedDescriptor> findReferences(Set<Long> ids) {
		List<BaseDescriptor> mixed = findMixedReferences("tbl_product_systems",
				"id", ids, references);
		List<CategorizedDescriptor> results = filterCategorized(mixed);
		List<BaseDescriptor> factors = filterUnknown(mixed);
		results.addAll(findFlowProperties(factors));
		List<UnitDescriptor> units = filterUnits(mixed);
		results.addAll(findUnitGroups(units));
		results.addAll(findReferenceFlows(ids));
		results.addAll(findProcesses(ids));
		results.addAll(findFlows(ids));
		results.addAll(findGlobalParameterRedefs(ids));
		return results;
	}
	
	
	private List<CategorizedDescriptor> findProcesses(Set<Long> ids) {
		return findReferences("tbl_product_system_processes",
				"f_product_system", ids, processReferences);
	}
	
	private List<CategorizedDescriptor> findFlows(
			Set<Long> ids) {
		return findReferences("tbl_process_links", "f_product_system", ids,
				flowReferences);
	}
	
	private List<CategorizedDescriptor> findReferenceFlows(Set<Long> ids) {
		List<BaseDescriptor> exchanges = findMixedReferences(
				"tbl_product_systems", "id", ids, productReferences);
		Set<Long> exchangeIds = toIdSet(exchanges);
		return findReferences("tbl_exchanges", "id", exchangeIds,
				flowReferences);
	}	
	
}

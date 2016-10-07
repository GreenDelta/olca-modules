package org.openlca.core.database.references;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.references.IReferenceSearch.Reference;
import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;

public class FlowPropertyFactorReferenceSearch {

	private final static Ref[] references = { 
		new Ref(FlowProperty.class, "flowProperty", "f_flow_property") 
	};

	private final IDatabase database;
	private final Map<Long, Class<? extends AbstractEntity>> ownerTypes;
	private final Map<Long, Long> ownerIds;

	private static final Map<Class<? extends AbstractEntity>, Map<Class<? extends AbstractEntity>, String>> nestedProperties = new HashMap<>();

	static {
		putNestedProperty(Flow.class, "flowPropertyFactors");
		putNestedProperty(Process.class, "flowPropertyFactor");
		putNestedProperty(ImpactFactor.class, "flowPropertyFactor");
		putNestedProperty(Project.class, "flowPropertyFactor");
		putNestedProperty(ProductSystem.class, "targetFlowPropertyFactor");
	}

	private static void putNestedProperty(
			Class<? extends AbstractEntity> ownerType, String nestedProperty) {
		for (Ref ref : references) {
			Map<Class<? extends AbstractEntity>, String> inner = nestedProperties
					.get(ownerType);
			if (inner == null)
				nestedProperties.put(ownerType, inner = new HashMap<>());
			inner.put(ref.type, nestedProperty);
		}
	}
	public FlowPropertyFactorReferenceSearch(IDatabase database) {
		this(database, new HashMap<>(), new HashMap<>());
	}

	public FlowPropertyFactorReferenceSearch(IDatabase database,
			Map<Long, Class<? extends AbstractEntity>> ownerTypes,
			Map<Long, Long> ownerIds) {
		this.database = database;
		this.ownerTypes = ownerTypes; 
		this.ownerIds = ownerIds; 
	}

	public List<Reference> findReferences(Set<Long> ids) {
		List<Reference> refs = Search.on(database, FlowPropertyFactor.class)
				.findReferences("tbl_flow_property_factors", "id", ids,
						references, true);
		return Search.applyOwnerMaps(refs, ownerTypes, ownerIds, nestedProperties);
	}

}

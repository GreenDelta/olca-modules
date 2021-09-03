package org.openlca.io.xls.systems;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;

class SystemExportConfig {

	public final ProductSystem system;
	public final IDatabase database;
	public ImpactMethodDescriptor impactMethod;
	public AllocationMethod allocationMethod;
	public String olcaVersion = "1.8";

	private EntityCache entityCache;

	public SystemExportConfig(
			ProductSystem system,
			IDatabase database) {
		this.system = system;
		this.database = database;
	}

	EntityCache getEntityCache() {
		if (entityCache == null) {
			entityCache = EntityCache.create(database);
		}
		return entityCache;
	}

}

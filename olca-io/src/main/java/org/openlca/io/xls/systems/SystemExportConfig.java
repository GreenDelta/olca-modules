package org.openlca.io.xls.systems;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.math.IMatrixFactory;
import org.openlca.core.matrix.cache.MatrixCache;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.BaseDescriptor;

public class SystemExportConfig {

	private String olcaVersion = "1.4";
	private final ProductSystem system;
	private final IDatabase database;
	private final IMatrixFactory<?> matrixFactory;
	private MatrixCache matrixCache;
	private EntityCache entityCache;
	private AllocationMethod allocationMethod;
	private BaseDescriptor impactMethod;

	public SystemExportConfig(ProductSystem system, IDatabase database,
			IMatrixFactory<?> matrixFactory) {
		this.system = system;
		this.database = database;
		this.matrixFactory = matrixFactory;
	}

	public void setOlcaVersion(String olcaVersion) {
		this.olcaVersion = olcaVersion;
	}

	public String getOlcaVersion() {
		return olcaVersion;
	}

	public void setMatrixCache(MatrixCache matrixCache) {
		this.matrixCache = matrixCache;
	}

	public MatrixCache getMatrixCache() {
		if (matrixCache == null)
			matrixCache = MatrixCache.createLazy(database);
		return matrixCache;
	}

	public void setEntityCache(EntityCache entityCache) {
		this.entityCache = entityCache;
	}

	public EntityCache getEntityCache() {
		if (entityCache == null)
			entityCache = EntityCache.create(database);
		return entityCache;
	}

	public AllocationMethod getAllocationMethod() {
		return allocationMethod;
	}

	public void setAllocationMethod(AllocationMethod allocationMethod) {
		this.allocationMethod = allocationMethod;
	}

	public BaseDescriptor getImpactMethod() {
		return impactMethod;
	}

	public void setImpactMethod(BaseDescriptor impactMethod) {
		this.impactMethod = impactMethod;
	}

	public ProductSystem getSystem() {
		return system;
	}

	public IDatabase getDatabase() {
		return database;
	}

	public IMatrixFactory<?> getMatrixFactory() {
		return matrixFactory;
	}

}

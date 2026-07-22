package org.openlca.core.matrix.cache;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.descriptors.LocationDescriptor;

import gnu.trove.map.hash.TLongObjectHashMap;

@NullMarked
public class MatrixBuildContext {

	private final IDatabase db;
	private @Nullable ConversionTable conversions;
	private @Nullable FlowTable flowTable;
	private @Nullable TLongObjectHashMap<FlowType> flowTypes;
	private @Nullable TLongObjectHashMap<LocationDescriptor> locations;
	private @Nullable ProviderMap providers;

	private MatrixBuildContext(IDatabase db) {
		this.db = db;
	}

	public static MatrixBuildContext of(IDatabase db) {
		return new MatrixBuildContext(db);
	}

	public IDatabase db() {
		return db;
	}

	public ConversionTable conversions() {
		if (conversions == null) {
			conversions = ConversionTable.create(db);
		}
		return conversions;
	}

	public FlowTable flowTable() {
		if (flowTable == null) {
			flowTable = FlowTable.create(db);
		}
		return flowTable;
	}

	public TLongObjectHashMap<FlowType> flowTypes() {
		if (flowTypes == null) {
			flowTypes = FlowTable.getTypes(db);
		}
		return flowTypes;
	}

	public TLongObjectHashMap<LocationDescriptor> locations() {
		if (locations == null) {
			var dao = new LocationDao(db);
			locations = dao.descriptorMap();
		}
		return locations;
	}

	public ProviderMap providers() {
		if (providers == null) {
			providers = ProviderMap.create(db);
		}
		return providers;
	}
}

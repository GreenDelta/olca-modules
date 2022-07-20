package org.openlca.core.matrix.io.index;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.model.FlowProperty;
import org.openlca.util.Categories;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Provides a reusable database context that caches elements for faster index
 * serialization.
 */
public final class IxContext {

	private final IDatabase db;

	private Map<Long, String> _locationCodes;
	private Categories.PathBuilder _categories;
	private Map<Long, FlowProperty> _quantities;

	private IxContext(IDatabase db) {
		this.db = db;
	}

	public static IxContext of(IDatabase db) {
		return new IxContext(db);
	}

	Categories.PathBuilder categories() {
		if (_categories == null) {
			_categories = Categories.pathsOf(db);
		}
		return _categories;
	}

	Map<Long, FlowProperty> quantities() {
		if (_quantities == null) {
			_quantities = new FlowPropertyDao(db)
				.getAll()
				.stream()
				.collect(Collectors.toMap(q -> q.id, q -> q));
		}
		return _quantities;
	}

	Map<Long, String> locationCodes() {
		if (_locationCodes == null) {
			_locationCodes = new LocationDao(db).getCodes();
		}
		return _locationCodes;
	}

}

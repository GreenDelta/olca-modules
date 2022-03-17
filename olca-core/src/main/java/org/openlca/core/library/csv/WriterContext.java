package org.openlca.core.library.csv;

import java.util.Map;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.library.Library;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.Categories;

public class WriterContext {

	private final Library library;
	private final IDatabase db;

	private Map<Long, String> _locationCodes;
	private Categories.PathBuilder _categories;
	private Map<Long, FlowProperty> _quantities;

	private WriterContext(IDatabase db, Library library) {
		this.db = db;
		this.library = library;
	}

	public static WriterContext create(IDatabase db, Library library) {
		return new WriterContext(db, library);
	}

	public Library library() {
		return library;
	}

	public IDatabase db() {
		return db;
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

	LibFlowInfo toLibFlow(FlowDescriptor d) {
		if (d == null)
			return LibFlowInfo.empty();
		var property = quantities().get(d.refFlowPropertyId);
		var unit = property != null
				? property.getReferenceUnit()
				: null;
		return new LibFlowInfo(
				d.refId,
				d.name,
				categories().pathOf(d.category),
				unit != null ? unit.name : null,
				d.flowType);
	}

	LibProcessInfo toLibProcess(CategorizedDescriptor d) {
		if (d == null)
			return LibProcessInfo.empty();
		String location = d instanceof ProcessDescriptor p && p.location != null
				? locationCodes().get(p.location)
				: null;
		return new LibProcessInfo(
				d.refId,
				d.name,
				categories().pathOf(d.category),
				location);
	}

	LibImpactInfo toLibImpact(ImpactDescriptor d) {
		if (d == null)
			return LibImpactInfo.empty();
		return new LibImpactInfo(d.refId, d.name, d.referenceUnit);
	}

	LibLocationInfo toLibLocation(LocationDescriptor d) {
		if (d == null)
			return LibLocationInfo.empty();
		return new LibLocationInfo(d.refId, d.name, d.code);
	}

}
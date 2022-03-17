package org.openlca.core.library.csv;

import java.util.Map;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.library.LibFlow;
import org.openlca.core.library.LibImpact;
import org.openlca.core.library.LibLocation;
import org.openlca.core.library.LibProcess;
import org.openlca.core.library.Library;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
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

	LibFlow toLibFlow(FlowDescriptor d) {
		if (d == null)
			return LibFlow.empty();
		var property = quantities().get(d.refFlowPropertyId);
		var unit = property != null
				? property.getReferenceUnit()
				: null;
		return new LibFlow(
				d.refId,
				d.name,
				categories().pathOf(d.category),
				unit != null ? unit.name : null,
				d.flowType);
	}

	LibProcess toLibProcess(RootDescriptor d) {
		if (d == null)
			return LibProcess.empty();
		String location = d instanceof ProcessDescriptor p && p.location != null
				? locationCodes().get(p.location)
				: null;
		return new LibProcess(
				d.refId,
				d.name,
				categories().pathOf(d.category),
				location);
	}

	LibImpact toLibImpact(ImpactDescriptor d) {
		if (d == null)
			return LibImpact.empty();
		return new LibImpact(d.refId, d.name, d.referenceUnit);
	}

	LibLocation toLibLocation(LocationDescriptor d) {
		if (d == null)
			return LibLocation.empty();
		return new LibLocation(d.refId, d.name, d.code);
	}

}

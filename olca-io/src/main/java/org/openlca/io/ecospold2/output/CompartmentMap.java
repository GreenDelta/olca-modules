package org.openlca.io.ecospold2.output;

import java.util.HashMap;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.io.maps.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;

import spold2.Compartment;
import spold2.ElementaryExchange;

class CompartmentMap {

	private Logger log = LoggerFactory.getLogger(getClass());
	private final HashMap<String, ExportRecord> map = new HashMap<>();

	public CompartmentMap(IDatabase database) {
		initMap(database);
	}

	private void initMap(IDatabase database) {
		try {
			CellProcessor[] processors = { null, null, null, null, null };
			List<List<Object>> rows = Maps.readAll(Maps.ES2_COMPARTMENT_EXPORT,
					database, processors);
			for (List<Object> row : rows) {
				String refId = Maps.getString(row, 0);
				ExportRecord record = new ExportRecord();
				record.subCompartmentId = Maps.getString(row, 2);
				record.compartment = Maps.getString(row, 3);
				record.subCompartment = Maps.getString(row, 4);
				map.put(refId, record);
			}
		} catch (Exception e) {
			log.error("failed to initialize compartment export map", e);
		}
	}

	public void apply(Category category, ElementaryExchange exchange) {
		if (category == null || exchange == null) {
			log.warn("could not set compartment; flow category or exchange is null");
			return;
		}
		ExportRecord record = map.get(category.getRefId());
		if (record == null) {
			log.warn(
					"category {} cannot be mapped to an EcoSpold 2 compartment",
					category);
			return;
		}
		exchange.compartment = createCompartment(record);
	}

	private Compartment createCompartment(ExportRecord record) {
		Compartment compartment = new Compartment();
		compartment.id = record.subCompartmentId;
		compartment.compartment = record.compartment;
		compartment.subCompartment = record.subCompartment;
		return compartment;
	}

	private class ExportRecord {

		String subCompartmentId;
		String subCompartment;
		String compartment;

		@Override
		public String toString() {
			return compartment + "/" + subCompartment + " [" + subCompartmentId
					+ "]";
		}
	}

}

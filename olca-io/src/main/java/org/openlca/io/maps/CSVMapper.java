package org.openlca.io.maps;

import org.openlca.core.database.IDatabase;
import org.openlca.io.maps.content.CSVCategoryContent;
import org.openlca.io.maps.content.CSVFlowContent;
import org.openlca.io.maps.content.CSVUnitContent;

public class CSVMapper extends AbstractMapper {

	public CSVMapper(IDatabase database) {
		super(database);
		fillMap(MapType.CSV_FLOW);
		fillMap(MapType.CSV_CATEGORY);
		fillMap(MapType.CSV_UNIT);
	}

	public CSVFlowContent getFlowContentForImport(String name, String unit,
			String type) {
		CSVFlowContent content = new CSVFlowContent(name, unit, type);
		return (CSVFlowContent) getForImport(content);
	}

	public CSVFlowContent getFlowContentForExport(String olcaId) {
		return (CSVFlowContent) getForExport(new CSVFlowContent(), olcaId);
	}

	public CSVUnitContent getUnitContentForImport(String unit) {
		CSVUnitContent content = new CSVUnitContent(unit);
		return (CSVUnitContent) getForImport(content);
	}

	public CSVUnitContent getUnitContentForExport(String olcaId) {
		return (CSVUnitContent) getForExport(new CSVUnitContent(), olcaId);
	}

	public CSVCategoryContent getCategoryContentForImport(String category) {
		CSVCategoryContent content = new CSVCategoryContent(category);
		return (CSVCategoryContent) getForImport(content);
	}

	public CSVCategoryContent getCategoryContentForExport(String olcaId) {
		return (CSVCategoryContent) getForExport(new CSVCategoryContent(),
				olcaId);
	}
}

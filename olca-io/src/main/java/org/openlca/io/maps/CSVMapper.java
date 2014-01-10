package org.openlca.io.maps;

import org.openlca.core.database.IDatabase;
import org.openlca.io.maps.content.CSVCategoryContent;
import org.openlca.io.maps.content.CSVElementaryFlowContent;
import org.openlca.io.maps.content.CSVProductFlowContent;
import org.openlca.io.maps.content.CSVUnitContent;
import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.SubCompartment;

public class CSVMapper extends AbstractMapper {

	public CSVMapper(IDatabase database) {
		super(database);
		fillMap(MapType.CSV_ELEMENTARY_FLOW);
		fillMap(MapType.CSV_PRODUCT_FLOW);
		fillMap(MapType.CSV_CATEGORY);
		fillMap(MapType.CSV_UNIT);
	}

	public CSVElementaryFlowContent getElementaryFlowContentForImport(
			String name, String unit, ElementaryFlowType type,
			SubCompartment subCompartment) {
		CSVElementaryFlowContent content = new CSVElementaryFlowContent(name,
				unit, type, subCompartment);
		return (CSVElementaryFlowContent) getForImport(content);
	}

	public CSVElementaryFlowContent getElementaryFlowContentForExport(
			String olcaId) {
		return (CSVElementaryFlowContent) getForExport(
				new CSVElementaryFlowContent(), olcaId);
	}

	public CSVProductFlowContent getProductFlowContentForImport(String name) {
		CSVProductFlowContent content = new CSVProductFlowContent(name);
		return (CSVProductFlowContent) getForImport(content);
	}

	public CSVElementaryFlowContent getProductFlowContentForExport(String olcaId) {
		return (CSVElementaryFlowContent) getForExport(
				new CSVElementaryFlowContent(), olcaId);
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

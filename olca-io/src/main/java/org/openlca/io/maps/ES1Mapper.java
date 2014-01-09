package org.openlca.io.maps;

import org.openlca.core.database.IDatabase;
import org.openlca.io.maps.content.ES1CategoryContent;
import org.openlca.io.maps.content.ES1FlowContent;
import org.openlca.io.maps.content.ES1UnitContent;

public class ES1Mapper extends AbstractMapper {

	public ES1Mapper(IDatabase database) {
		super(database);
		fillMap(MapType.ES1_FLOW);
		fillMap(MapType.ES1_UNIT);
		fillMap(MapType.ES1_CATEGORY);
	}

	public ES1FlowContent getFlowContentForImport(String name, String unit,
			String category, String subCategory, String casNumber) {
		ES1FlowContent content = new ES1FlowContent(name, unit, category,
				subCategory, casNumber);
		return (ES1FlowContent) getForImport(content);
	}

	public ES1FlowContent getFlowContentForExport(String olcaId) {
		return (ES1FlowContent) getForExport(new ES1FlowContent(), olcaId);
	}

	public ES1UnitContent getUnitContentForImport(String unit) {
		ES1UnitContent content = new ES1UnitContent(unit);
		return (ES1UnitContent) getForImport(content);
	}

	public ES1UnitContent getUnitContentForExport(String olcaId) {
		return (ES1UnitContent) getForExport(new ES1UnitContent(), olcaId);
	}

	public ES1CategoryContent getCategoryContentForImport(String category,
			String subCategory) {
		ES1CategoryContent content = new ES1CategoryContent(category,
				subCategory);
		return (ES1CategoryContent) getForImport(content);
	}

	public ES1CategoryContent getCategoryContentForExport(String olcaId) {
		return (ES1CategoryContent) getForExport(new ES1CategoryContent(),
				olcaId);
	}

}

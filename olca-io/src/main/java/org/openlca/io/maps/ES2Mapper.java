package org.openlca.io.maps;

import org.openlca.core.database.IDatabase;
import org.openlca.io.maps.content.ES2CategoryContent;
import org.openlca.io.maps.content.ES2FlowContent;
import org.openlca.io.maps.content.ES2UnitContent;

public class ES2Mapper extends AbstractMapper {

	public ES2Mapper(IDatabase database) {
		super(database);
		fillMap(MapType.ES2_FLOW);
		fillMap(MapType.ES2_UNIT);
		fillMap(MapType.ES2_CATEGORY);
	}

	public ES2FlowContent getFlowContentForImport(String id) {
		ES2FlowContent content = new ES2FlowContent(id);
		return (ES2FlowContent) getForImport(content);
	}

	public ES2FlowContent getFlowContentForExport(String olcaId) {
		return (ES2FlowContent) getForExport(new ES2FlowContent(), olcaId);
	}

	public ES2UnitContent getUnitContentForImport(String unit) {
		ES2UnitContent content = new ES2UnitContent(unit);
		return (ES2UnitContent) getForImport(content);
	}

	public ES2UnitContent getUnitContentForExport(String olcaId) {
		return (ES2UnitContent) getForExport(new ES2UnitContent(), olcaId);
	}

	public ES2CategoryContent getCategoryContentForImport(String compartmentId,
			String subCompartmentId) {
		ES2CategoryContent content = new ES2CategoryContent(compartmentId,
				subCompartmentId);
		return (ES2CategoryContent) getForImport(content);
	}

	public ES2CategoryContent getCategoryContentForExport(String olcaId) {
		return (ES2CategoryContent) getForExport(new ES2CategoryContent(),
				olcaId);
	}

}

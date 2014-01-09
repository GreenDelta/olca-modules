package org.openlca.io.maps;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.io.maps.content.ILCDCategoryContent;
import org.openlca.io.maps.content.ILCDFlowContent;
import org.openlca.io.maps.content.ILCDUnitContent;

public class ILCDMapper extends AbstractMapper {

	public ILCDMapper(IDatabase database) {
		super(database);
		fillMap(MapType.ILCD_CATEGORY);
		fillMap(MapType.ILCD_FLOW);
		fillMap(MapType.ILCD_UNIT);
	}

	public ILCDFlowContent getFlowContentForImport(String id, String version) {
		ILCDFlowContent content = new ILCDFlowContent(id, version);
		return (ILCDFlowContent) getForImport(content);
	}

	public ILCDFlowContent getFlowContentForExport(String olcaId) {
		return (ILCDFlowContent) getForExport(new ILCDFlowContent(), olcaId);
	}

	public ILCDUnitContent getUnitContentForImport(String unit) {
		ILCDUnitContent content = new ILCDUnitContent(unit);
		return (ILCDUnitContent) getForImport(content);
	}

	public ILCDUnitContent getUnitContentForExport(String olcaId) {
		return (ILCDUnitContent) getForExport(new ILCDUnitContent(), olcaId);
	}

	public ILCDCategoryContent getCategoryContentForImport(
			List<String> cateorgies) {
		ILCDCategoryContent content = new ILCDCategoryContent(cateorgies);
		return (ILCDCategoryContent) getForImport(content);
	}

	public ILCDCategoryContent getCategoryContentForExport(String olcaId) {
		return (ILCDCategoryContent) getForExport(new ILCDCategoryContent(),
				olcaId);
	}

}

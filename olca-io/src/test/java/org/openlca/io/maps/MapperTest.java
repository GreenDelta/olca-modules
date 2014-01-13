package org.openlca.io.maps;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.mysql.MySQLDatabase;
import org.openlca.io.maps.content.CSVCategoryContent;
import org.openlca.io.maps.content.CSVElementaryFlowContent;
import org.openlca.io.maps.content.ILCDCategoryContent;
import org.openlca.io.maps.content.ILCDFlowContent;
import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.SubCompartment;

public class MapperTest {

	public static void main(String[] args) {
		// CSVCategoryContent content = new CSVCategoryContent();
		// content.setCategory("waste");
		// content.setForImport(true);
		// content.setOlcaId("6e54f561-d63a-4a49-bec9-55468f56a698");

		IDatabase database = new MySQLDatabase(
				"jdbc:mysql://localhost:3306/olca14CSV", "root", "");
		CSVMapper mapper = new CSVMapper(database);

		// CSVCategoryContent content = mapper
		// .getCategoryContentForImport("waste");
		// CSVElementaryFlowContent flowContent = new
		// CSVElementaryFlowContent("flow", "KG",
		// "Emission to air");
		//
		CSVElementaryFlowContent flowContent2 = new CSVElementaryFlowContent(
				"Water, cooling, unspecified natural origin/m3", "m3", ElementaryFlowType.RESOURCE,
				SubCompartment.RAW_MATERIAL_IN_WATER);

		System.out.println(flowContent2.toJson());

		CSVElementaryFlowContent testContent = mapper
				.getElementaryFlowContentForImport("Water, cooling, unspecified natural origin/m3", "m3",
						ElementaryFlowType.RESOURCE,
						SubCompartment.RAW_MATERIAL_IN_WATER);
		
		
		System.out.println(testContent);

		// System.out.println("equals:" + flowContent.equals(flowContent2));

		// List<String> categories = new ArrayList<>();
		// categories.add("cat1");
		// categories.add("cat2");
		// categories.add("cat3");
		// categories.add("cat4");
		//
		// ILCDCategoryContent ilcdCategoryContent = new ILCDCategoryContent(
		// categories);
		// ilcdCategoryContent
		// .setOlcaRefId("6e54f561-d63a-4a49-bec9-55468f56a698");
		//
		// ILCDMapper ilcdMapper = new ILCDMapper(database);
		// ilcdMapper.add(ilcdCategoryContent);

	}
}

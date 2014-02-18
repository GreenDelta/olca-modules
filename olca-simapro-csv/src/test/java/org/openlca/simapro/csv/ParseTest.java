package org.openlca.simapro.csv;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.simapro.csv.model.SPDataEntry;
import org.openlca.simapro.csv.model.SPElementaryFlow;
import org.openlca.simapro.csv.model.SPProcess;
import org.openlca.simapro.csv.model.SPProduct;
import org.openlca.simapro.csv.model.SPProductFlow;
import org.openlca.simapro.csv.model.SPReferenceData;
import org.openlca.simapro.csv.model.SPSubstance;
import org.openlca.simapro.csv.model.SPWasteScenario;
import org.openlca.simapro.csv.model.SPWasteTreatment;
import org.openlca.simapro.csv.model.types.SubCompartment;
import org.openlca.simapro.csv.parser.CSVParser;

public class ParseTest {

	public static void main(String[] args) {
		File file = new File("/Users/imo/Desktop/elemflows.csv");
		CSVParser parser = new CSVParser(file);

		try {
			parser.start();
			SPReferenceData data = parser.getReferenceData();

			Set<String> set = new HashSet<>();

			for (SPSubstance substance : data.getSubstances().values()) {
				for (SubCompartment subCompartment : substance.getFlowType()
						.getSubCompartments()) {
					StringBuilder builder = new StringBuilder();
					builder.append("\"");
					builder.append(substance.getName());
					builder.append("\";\"");
					builder.append(substance.getReferenceUnit());
					builder.append("\";\"");
					builder.append(substance.getFlowType().getValue());
					builder.append("\";\"");
					builder.append(subCompartment.getValue());
					builder.append("\";\"");
					builder.append(substance.getCASNumber());
					builder.append("\"");
					set.add(builder.toString());
				}
			}

			Map<String, String[]> index = parser.getIndex();

			for (Map.Entry<String, String[]> entry : index.entrySet()) {
				// System.out.println(entry.getKey());
				// for (String s : entry.getValue())
				// System.out.println(s);
				// System.out.println();
			}

			while (parser.hasNext()) {
				SPDataEntry dataEntry = parser.next();
				if (dataEntry instanceof SPProcess
						|| dataEntry instanceof SPWasteTreatment) {
					for (SPElementaryFlow flow : dataEntry.getElementaryFlows()) {
						StringBuilder builder = new StringBuilder();
						builder.append("\"");
						builder.append(flow.getName());
						builder.append("\";\"");
						builder.append(flow.getUnit());
						builder.append("\";\"");
						builder.append(flow.getType().getValue());
						builder.append("\";\"");
						builder.append(flow.getSubCompartment().getValue());
						builder.append("\"");
						set.add(builder.toString());
					}

					//
					// for (SPProductFlow flow : dataEntry.getProductFlows())
					// set.add(flow.getName());
					// if (dataEntry instanceof SPProcess) {
					// SPProcess process = (SPProcess) dataEntry;
					// for (SPProduct p : process.getByProducts())
					// set.add(p.getName());
					// set.add(process.getReferenceProduct().getName());
					//
					// } else if (dataEntry instanceof SPWasteTreatment) {
					// SPWasteTreatment treatment = (SPWasteTreatment)
					// dataEntry;
					// set.add(treatment.getWasteSpecification().getName());
					// }

				} else if (dataEntry instanceof SPWasteScenario) {
					// System.out.println("Waste Scenario");
				}
			}
			parser.close();

			for (String s : set)
				System.out.println(s);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

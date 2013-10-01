package org.openlca.simapro.csv.parser;

import java.util.Queue;

import org.openlca.simapro.csv.model.SPSystemDescription;

final class SystemDescription {

	static SPSystemDescription parse(Queue<String> lines, String csvSeperator) {

		SPSystemDescription systemDescription = new SPSystemDescription(null,
				null);

		while (!lines.isEmpty()) {

			String caseLine = lines.poll();
			// TODO check why replace
			String valueLine = Utils.replaceCSVSeperator(lines.peek(),
					csvSeperator);

			switch (caseLine) {
			case "Name":
				systemDescription.setName(valueLine);
				lines.remove();
				break;

			case "Category":
				systemDescription.setCategory(valueLine);
				lines.remove();
				break;

			case "Description":
				systemDescription.setDescription(valueLine);
				lines.remove();
				break;

			case "Sub-systems":
				systemDescription.setSubSystems(valueLine);
				lines.remove();
				break;

			case "Cut-off rules":
				systemDescription.setCutOffRules(valueLine);
				lines.remove();
				break;

			case "Energy model":
				systemDescription.setEnergyModel(valueLine);
				lines.remove();
				break;

			case "Transport model":
				systemDescription.setTransportModel(valueLine);
				lines.remove();
				break;

			case "Other assumptions":
				systemDescription.setOtherAssumptions(valueLine);
				lines.remove();
				break;

			case "Other information":
				systemDescription.setOtherInformation(valueLine);
				lines.remove();
				break;

			case "Allocation rules":
				systemDescription.setAllocationRules(valueLine);
				lines.remove();
				break;

			}
		}

		return systemDescription;
	}
}

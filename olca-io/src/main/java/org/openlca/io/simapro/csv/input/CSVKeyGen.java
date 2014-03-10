package org.openlca.io.simapro.csv.input;

import org.openlca.io.KeyGen;
import org.openlca.simapro.csv.model.enums.SubCompartment;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;
import org.openlca.simapro.csv.model.refdata.UnitRow;

public class CSVKeyGen {

	public static String forProcess(String name) {
		return KeyGen.get(name);
	}

	public static String forElementaryFlow(ElementaryExchangeRow elementaryFlow) {
		if (elementaryFlow == null)
			KeyGen.get("");

		String[] vals = new String[5];
		vals[0] = elementaryFlow.getType().getSubstance();
		if (elementaryFlow.getSubCompartment() != null)
			vals[1] = elementaryFlow.getSubCompartment().getValue();
		else
			vals[1] = SubCompartment.UNSPECIFIED.getValue();
		vals[2] = elementaryFlow.getName();
		vals[3] = elementaryFlow.getUnit();
		return KeyGen.get(vals);
	}

	// public static String forProductFlow(SPProductFlow productFlow) {
	// if (productFlow == null || !productFlow.hasReferenceData())
	// return KeyGen.get("");
	//
	// String[] vals = new String[3];
	// vals[0] = productFlow.getName();
	// vals[1] = productFlow.getReferenceCategory();
	// vals[2] = productFlow.getProcessCategory().getValue();
	// return KeyGen.get(vals);
	// }

	public static String forProduct(String name) {
		if (name == null)
			return KeyGen.get("");
		return KeyGen.get(name);
	}

//	public static String forWasteSpecification(
//			SPWasteSpecification wasteSpecification, ProcessCategory category) {
//		if (wasteSpecification == null)
//			return KeyGen.get("");
//
//		String[] vals = new String[3];
//		vals[0] = wasteSpecification.getName();
//		vals[1] = wasteSpecification.getCategory();
//		vals[2] = category.getValue();
//		return KeyGen.get(vals);
//	}

	public static String forSource(LiteratureReferenceBlock literatureReference) {
		if (literatureReference == null)
			return KeyGen.get("");
		String[] vals = new String[2];
		vals[0] = literatureReference.getName();
		vals[1] = literatureReference.getCategory();
		return KeyGen.get(vals);
	}

	public static String forUnit(UnitRow unit) {
		if (unit == null)
			return KeyGen.get("");
		return KeyGen.get(unit.getName());
	}

}

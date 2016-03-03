package org.openlca.io.ecospold1.input;

import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.IReferenceFunction;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.io.DataSet;
import org.openlca.util.KeyGen;

/**
 * Generator for name-based UUIDs (version 3) for EcoSpold 01 entities.
 * 
 * See <a href=
 * "http://openlca.org/documentation/index.php/UUIDs_for_EcoSpold_01_data_sets">
 * http
 * ://openlca.org/documentation/index.php/UUIDs_for_EcoSpold_01_data_sets</a>.
 */
public class ES1KeyGen {

	private ES1KeyGen() {
	}

	public static String forFlow(IExchange exchange) {
		if (exchange == null)
			return KeyGen.get("");
		if (exchange.isElementaryFlow())
			return forElementaryFlow(exchange);
		return forProductFlow(exchange);
	}

	public static String forElementaryFlow(IExchange exchange) {
		if (exchange == null)
			return KeyGen.get("");
		String[] vals = new String[4];
		vals[0] = exchange.getCategory();
		vals[1] = exchange.getSubCategory();
		vals[2] = exchange.getName();
		vals[3] = exchange.getUnit();
		return KeyGen.get(vals);
	}

	public static String forProductFlow(IExchange exchange) {
		if (exchange == null)
			return KeyGen.get("");
		String[] vals = new String[6];
		vals[0] = exchange.getCategory();
		vals[1] = exchange.getSubCategory();
		vals[2] = exchange.getName();
		vals[3] = exchange.getUnit();
		vals[4] = exchange.getLocation() != null ? exchange.getLocation()
				: "GLO";
		boolean inf = exchange.isInfrastructureProcess() != null ? exchange
				.isInfrastructureProcess() : false;
		vals[5] = inf ? "true" : "false";
		return KeyGen.get(vals);
	}

	public static String forProduct(DataSet dataSet) {
		if (dataSet == null || dataSet.getReferenceFunction() == null)
			return KeyGen.get("");
		String[] vals = new String[6];
		fillDatasetAttributes(dataSet, vals);
		return KeyGen.get(vals);
	}

	public static String forProcess(DataSet dataSet) {
		if (dataSet == null || dataSet.getReferenceFunction() == null)
			return KeyGen.get("");
		String[] vals = new String[7];
		fillDatasetAttributes(dataSet, vals);
		String type = "U";
		if (dataSet.getDataSetInformation() != null) {
			int t = dataSet.getDataSetInformation().getType();
			if (t != 1)
				type = "S";
		}
		vals[6] = type;
		return KeyGen.get(vals);
	}

	private static void fillDatasetAttributes(DataSet dataSet, String[] vals) {
		IReferenceFunction refFun = dataSet.getReferenceFunction();
		vals[0] = refFun.getCategory();
		vals[1] = refFun.getSubCategory();
		vals[2] = refFun.getName();
		vals[3] = refFun.getUnit();
		String location = dataSet.getGeography() != null ? dataSet
				.getGeography().getLocation() : "GLO";
		vals[4] = location;
		boolean inf = refFun.isInfrastructureProcess();
		vals[5] = inf ? "true" : "false";
	}

	public static String forImpactMethod(DataSet dataset) {
		if (dataset == null || dataset.getReferenceFunction() == null)
			return KeyGen.get("");
		return KeyGen.get(dataset.getReferenceFunction().getCategory());
	}

	public static String forPerson(IPerson person) {
		if (person == null)
			return KeyGen.get("");
		return KeyGen.get(person.getName(), person.getAddress());
	}

	public static String forSource(ISource source) {
		if (source == null)
			return KeyGen.get("");
		String[] vals = new String[3];
		vals[0] = source.getFirstAuthor();
		vals[1] = source.getTitle();
		vals[2] = source.getYear() != null ? Integer.toString(source.getYear()
				.getYear()) : "";
		return KeyGen.get(vals);
	}

}

package org.openlca.io.ecospold1.input;

import org.openlca.ecospold.IExchange;
import org.openlca.ecospold.IPerson;
import org.openlca.ecospold.ISource;
import org.openlca.ecospold.io.DataSet;
import org.openlca.util.KeyGen;

/**
 * Generator for name-based (version 3) UUIDs for EcoSpold 01 entities.
 */
public class ES1KeyGen {

	private ES1KeyGen() {
	}

	public static String forFlow(IExchange exchange) {
		if (exchange == null)
			return KeyGen.get("");
		return exchange.isElementaryFlow()
				? forElementaryFlow(exchange)
				: forProductFlow(exchange);
	}

	public static String forElementaryFlow(IExchange e) {
		if (e == null)
			return KeyGen.get("");
		var vals = new String[4];
		vals[0] = e.getCategory();
		vals[1] = e.getSubCategory();
		vals[2] = e.getName();
		vals[3] = e.getUnit();
		return KeyGen.get(vals);
	}

	public static String forProductFlow(IExchange e) {
		if (e == null)
			return KeyGen.get("");
		var vals = new String[6];
		vals[0] = e.getCategory();
		vals[1] = e.getSubCategory();
		vals[2] = e.getName();
		vals[3] = e.getUnit();
		vals[4] = e.getLocation() != null
				? e.getLocation()
				: "GLO";
		boolean inf = e.isInfrastructureProcess() != null
				? e.isInfrastructureProcess()
				: false;
		vals[5] = inf ? "true" : "false";
		return KeyGen.get(vals);
	}

	public static String forProduct(DataSet ds) {
		if (ds == null || ds.getReferenceFunction() == null)
			return KeyGen.get("");
		var vals = new String[6];
		fillDatasetAttributes(ds, vals);
		return KeyGen.get(vals);
	}

	public static String forProcess(DataSet dataSet) {
		if (dataSet == null || dataSet.getReferenceFunction() == null)
			return KeyGen.get("");
		var vals = new String[7];
		fillDatasetAttributes(dataSet, vals);
		var type = "U";
		if (dataSet.getDataSetInformation() != null) {
			int t = dataSet.getDataSetInformation().getType();
			if (t != 1)
				type = "S";
		}
		vals[6] = type;
		return KeyGen.get(vals);
	}

	private static void fillDatasetAttributes(DataSet dataSet, String[] vals) {
		var refFun = dataSet.getReferenceFunction();
		vals[0] = refFun.getCategory();
		vals[1] = refFun.getSubCategory();
		vals[2] = refFun.getName();
		vals[3] = refFun.getUnit();
		vals[4] = dataSet.getGeography() != null
				? dataSet.getGeography().getLocation()
				: "GLO";
		boolean inf = refFun.isInfrastructureProcess();
		vals[5] = inf ? "true" : "false";
	}

	/**
	 * For LCIA methods we generate the ref. ID from the method name which is
	 * stored in the `category` field of the reference function.
	 */
	public static String forImpactMethod(DataSet ds) {
		return ds == null || ds.getReferenceFunction() == null
				? KeyGen.get("")
				: KeyGen.get(ds.getReferenceFunction().getCategory());
	}

	/**
	 * For LCIA categories we generate the ref. ID from the indicator name,
	 * which is stored in the `subCategory` and `name` field of the reference
	 * function, and from the reference unit.
	 */
	public static String forImpactCategory(DataSet ds) {
		var ref = ds == null
				? null
				: ds.getReferenceFunction();
		return ref == null
				? KeyGen.get("")
				: KeyGen.get(ref.getSubCategory(), ref.getName(), ref.getUnit());
	}

	public static String forPerson(IPerson person) {
		return person != null
				? KeyGen.get(person.getName(), person.getAddress())
				: KeyGen.get("");
	}

	public static String forSource(ISource source) {
		if (source == null)
			return KeyGen.get("");
		var vals = new String[3];
		vals[0] = source.getFirstAuthor();
		vals[1] = source.getTitle();
		vals[2] = source.getYear() != null
				? Integer.toString(source.getYear().getYear())
				: "";
		return KeyGen.get(vals);
	}

}

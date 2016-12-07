package org.openlca.ecospold2;

import java.util.Collections;
import java.util.List;

/**
 * A utility class for working with EcoSpold 2 data sets. Basically, there are
 * two types of methods in this class: (1) the get-methods return the property
 * from the document (2) the other methods create the property and all the
 * intermediate data structures if it does not yet exist.
 */
public class Spold2 {

	private Spold2() {
	}

	public static String getId(DataSet ds) {
		Activity a = getActivity(ds);
		return a == null ? null : a.id;
	}

	public static Activity getActivity(DataSet ds) {
		if (ds == null || ds.description == null)
			return null;
		return ds.description.activity;
	}

	public static Geography getGeography(DataSet ds) {
		if (ds == null || ds.description == null)
			return null;
		return ds.description.geography;
	}

	public static TimePeriod getTime(DataSet ds) {
		if (ds == null || ds.description == null)
			return null;
		return ds.description.timePeriod;
	}

	public static Technology getTechnology(DataSet ds) {
		if (ds == null || ds.description == null)
			return null;
		return ds.description.technology;
	}

	public static List<Classification> getClassifications(DataSet ds) {
		if (ds == null || ds.description == null)
			return Collections.emptyList();
		return ds.description.classifications;
	}

	public static List<IntermediateExchange> getProducts(DataSet ds) {
		if (ds == null || ds.flowData == null)
			return Collections.emptyList();
		return ds.flowData.intermediateExchanges;
	}

	public static List<ElementaryExchange> getElemFlows(DataSet ds) {
		if (ds == null || ds.flowData == null)
			return Collections.emptyList();
		return ds.flowData.elementaryExchanges;
	}

	public static List<Parameter> getParameters(DataSet ds) {
		if (ds == null || ds.flowData == null)
			return Collections.emptyList();
		return ds.flowData.parameters;
	}

	public static Representativeness getRepresentativeness(DataSet ds) {
		if (ds == null || ds.validation == null)
			return null;
		return ds.validation.representativeness;
	}

	public static Validation validation(DataSet ds) {
		if (ds.validation == null)
			ds.validation = new Validation();
		return ds.validation;
	}

	public static Representativeness representativeness(DataSet ds) {
		Validation v = validation(ds);
		if (v.representativeness == null)
			v.representativeness = new Representativeness();
		return v.representativeness;
	}

}

package spold2;

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

	public static Time getTime(DataSet ds) {
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

	/**
	 * Returns the reference product of the data set (intermediate exchange with
	 * outputGroup=0).
	 */
	public static IntermediateExchange getReferenceProduct(DataSet ds) {
		if (ds == null)
			return null;
		IntermediateExchange candidate = null;
		for (IntermediateExchange e : getProducts(ds)) {
			Integer og = e.outputGroup;
			if (og == null || og.intValue() != 0)
				continue;
			Double a = e.amount;
			if (a != null && a.doubleValue() != 0)
				return e;
			candidate = e;
		}
		return candidate;
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

	public static AdminInfo adminInfo(DataSet ds) {
		if (ds.adminInfo == null)
			ds.adminInfo = new AdminInfo();
		return ds.adminInfo;
	}

	public static FileAttributes fileAttributes(DataSet ds) {
		AdminInfo ai = adminInfo(ds);
		if (ai.fileAttributes == null)
			ai.fileAttributes = new FileAttributes();
		return ai.fileAttributes;
	}

	public static DataEntry dataEntry(DataSet ds) {
		AdminInfo ai = adminInfo(ds);
		if (ai.dataEntry == null)
			ai.dataEntry = new DataEntry();
		return ai.dataEntry;
	}

	public static DataGenerator dataGenerator(DataSet ds) {
		AdminInfo ai = adminInfo(ds);
		if (ai.dataGenerator == null)
			ai.dataGenerator = new DataGenerator();
		return ai.dataGenerator;
	}

}

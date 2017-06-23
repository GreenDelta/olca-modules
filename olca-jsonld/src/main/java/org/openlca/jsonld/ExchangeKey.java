package org.openlca.jsonld;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Uncertainty;
import org.openlca.util.KeyGen;

public class ExchangeKey {

	public static String get(String processRefId, String providerRefId,
			Exchange e) {
		List<String> kg = new ArrayList<>();
		kg.add(processRefId);
		kg.add(providerRefId);
		kg.add(toString(e.isInput));
		kg.add(toString(e.isAvoided));
		kg.add(toString(e.flow));
		kg.add(toString(e.flowPropertyFactor));
		kg.add(toString(e.unit));
		kg.add(e.amountFormula);
		kg.add(e.dqEntry);
		kg.add(toString(e.amount));
		kg.add(toString(e.baseUncertainty));
		kg.addAll(toString(e.uncertainty));
		kg.add(e.costFormula);
		kg.add(toString(e.costs));
		kg.add(toString(e.currency));
		return KeyGen.get(kg.toArray(new String[kg.size()]));
	}

	private static String toString(RootEntity e) {
		if (e == null)
			return null;
		return e.getRefId();
	}
	
	private static String toString(FlowPropertyFactor f) {
		if (f == null)
			return null;
		return toString(f.getFlowProperty());
	}

	private static List<String> toString(Uncertainty u) {
		List<String> kg = new ArrayList<>();
		if (u == null) {
			for (int i = 0; i < 7; i++)
				kg.add(null);
			return kg;
		}
		kg.add(toString(u.getDistributionType()));
		kg.add(u.getParameter1Formula());
		kg.add(toString(u.getParameter1Value()));
		kg.add(u.getParameter2Formula());
		kg.add(toString(u.getParameter2Value()));
		kg.add(u.getParameter3Formula());
		kg.add(toString(u.getParameter3Value()));
		return kg;
	}

	private static String toString(Enum<?> e) {
		if (e == null)
			return null;
		return e.name();
	}

	private static String toString(Double v) {
		if (v == null)
			return null;
		return Double.toString(v);
	}

	private static String toString(Boolean v) {
		if (v == null)
			return null;
		return Boolean.toString(v);
	}
}

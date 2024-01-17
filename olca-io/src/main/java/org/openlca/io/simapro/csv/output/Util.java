package org.openlca.io.simapro.csv.output;

import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;
import org.openlca.core.model.Uncertainty;
import org.openlca.simapro.csv.enums.ProcessCategory;
import org.openlca.util.Exchanges;
import org.openlca.util.Strings;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiConsumer;

class Util {
	private Util() {
	}

	/**
	 * Converts the given uncertainty into a SimaPro entry. Passing
	 * {@code null} into this function is fine. Note that SimaPro does some
	 * validation checks in the import (e.g. {@code min <= mean <= max}),
	 * so that we have to pass also the mean value into this function.
	 */
	static Object[] uncertainty(double mean, Uncertainty u, double factor) {
		var row = new Object[]{"Undefined", 0, 0, 0};
		if (u == null || u.distributionType == null)
			return row;
		switch (u.distributionType) {
			case LOG_NORMAL -> {
				row[0] = "Lognormal";
				row[1] = u.parameter2 != null
						? Math.pow(u.parameter2, 2)
						: 1;
				return row;
			}
			case NORMAL -> {
				row[0] = "Normal";
				row[1] = u.parameter2 != null
						? 2 * factor * u.parameter2
						: 0;
				return row;
			}
			case TRIANGLE -> {
				var tmin = u.parameter1 == null ? 0 : factor * u.parameter1;
				var tmax = u.parameter3 == null ? 0 : factor * u.parameter3;
				if (tmin > mean || tmax < mean)
					return row;
				row[0] = "Triangle";
				row[2] = tmin;
				row[3] = tmax;
				return row;
			}
			case UNIFORM -> {
				var umin = u.parameter1 == null ? 0 : factor * u.parameter1;
				var umax = u.parameter2 == null ? 0 : factor * u.parameter2;
				if (umin > mean || umax < mean)
					return row;
				row[0] = "Uniform";
				row[2] = umin;
				row[3] = umax;
				return row;
			}
			default -> {
				return row;
			}
		}
	}

	static ProcessCategory categoryTypeOf(SimaProExport exp, Process p) {
		for (var e : p.exchanges) {
			if (Exchanges.isProviderFlow(e) && Exchanges.isWaste(e))
				return ProcessCategory.WASTE_TREATMENT;
		}
		return CategoryPath.of(exp, p).type();
	}

	static String commentOf(Process p) {
		var sections = new ArrayList<String>();
		var texts = new ArrayList<String>();
		BiConsumer<String, String> fn = (title, text) -> {
			if (Strings.nullOrEmpty(text))
				return;
			sections.add(title);
			texts.add(text);
		};

		fn.accept("Description", p.description);
		if (p.documentation != null) {
			var doc = p.documentation;
			fn.accept("Time", doc.time);
			fn.accept("Geography", doc.geography);
			fn.accept("Technology", doc.technology);
			fn.accept("Intended application", doc.intendedApplication);
			if (doc.dataOwner != null) {
				fn.accept("Data set owner", doc.dataOwner.name);
			}
			if (doc.publication != null) {
				fn.accept("Publication", doc.publication.name);
			}
			fn.accept("Access and use restrictions", doc.accessRestrictions);
			fn.accept("Project", doc.project);
			fn.accept("Copyright", doc.copyright ? "Yes" : "No");
			fn.accept("Modeling constants", doc.modelingConstants);
			fn.accept("Data completeness", doc.dataCompleteness);
			fn.accept("Data selection", doc.dataSelection);
			fn.accept("Reviewer", reviewerOf(p));
		}

		if (texts.isEmpty())
			return "";
		if (texts.size() == 1)
			return texts.get(0);

		var buff = new StringBuilder();
		for (int i = 0; i < sections.size(); i++) {
			buff.append("# ")
					.append(sections.get(i))
					.append('\n')
					.append(texts.get(i))
					.append("\n\n");
		}

		return buff.toString();
	}

	static String reviewDetailsOf(Process p) {
		if (p == null || p.documentation == null)
			return null;
		for (var rev : p.documentation.reviews) {
			if (Strings.notEmpty(rev.details))
				return rev.details;
		}
		return null;
	}

	static String reviewerOf(Process p) {
		if (p == null || p.documentation == null)
			return null;
		for (var rev : p.documentation.reviews) {
			if (!rev.reviewers.isEmpty())
				return rev.reviewers.get(0).name;
		}
		return null;
	}


	/**
	 * In SimaPro you cannot have multiple flow properties for a flow. Thus, we
	 * convert everything into the reference flow property and unit. Otherwise,
	 * the SimaPro import will throw errors when the same flow is present with
	 * units from different quantities.
	 */
	static Exchange toReferenceAmount(Exchange e) {
		if (e == null || e.flow == null)
			return e;
		var refProp = e.flow.getReferenceFactor();
		var refUnit = e.flow.getReferenceUnit();
		if (Objects.equals(refProp, e.flowPropertyFactor)
				&& Objects.equals(refUnit, e.unit))
			return e;
		var clone = e.copy();
		clone.flowPropertyFactor = refProp;
		clone.unit = refUnit;
		clone.amount = ReferenceAmount.get(e);
		if (e.amount == 0) {
			return clone;
		}
		var factor = clone.amount / e.amount;
		if (Strings.notEmpty(clone.formula)) {
			clone.formula = factor + " * (" + clone.formula + ")";
		}
		if (clone.uncertainty != null) {
			clone.uncertainty.scale(factor);
		}
		return clone;
	}
}

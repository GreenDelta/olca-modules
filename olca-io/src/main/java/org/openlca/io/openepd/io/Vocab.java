package org.openlca.io.openepd.io;

import org.openlca.util.Strings;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Contains openEPD vocabulary constants and matching rules for mapping these
 * constants to or from typical names in LCA databases.
 */
public class Vocab {

	public enum Method {

		TRACI_2_1("TRACI 2.1", "(?i)(?=.*\\btraci\\b)(?=.*\\bv?2\\.1\\b).*"),
		TRACI_2_0("TRACI 2.0", "(?i)(?=.*\\btraci\\b)(?=.*\\bv?2\\.0\\b).*"),
		TRACI_1_0("TRACI 1.0", "(?i)(?=.*\\btraci\\b)(?=.*\\bv?1\\.0\\b).*"),

		EF_3_0("EF 3.0",
			"(?i)(?:(?=.*\\benvironmental\\b)(?=.*\\bfootprint\\b)" +
				"|(?=.*\\bef\\b))(?=.*\\bv?3\\.0\\b).*"),
		EF_2_0("EF 2.0, 2018",
			"(?i)(?:(?=.*\\benvironmental\\b)(?=.*\\bfootprint\\b)" +
				"|(?=.*\\bef\\b))(?=.*\\bv?2\\.0\\b).*"),

		IPCC_AR5("IPCC AR5", "(?i)(?=.*\\bipcc\\b)(?=.*\\bar5\\b).*"),

		CML_2016("CML 2016", "(?i)(?=.*\\bcml\\b)(?=.*\\b2016\\b).*"),
		CML_2012("CML 2012", "(?i)(?=.*\\bcml\\b)(?=.*\\b2012\\b).*"),
		CML_2007("CML 2007", "(?i)(?=.*\\bcml\\b)(?=.*\\b2007\\b).*"),
		CML_2001("CML 2001", "(?i)(?=.*\\bcml\\b)(?=.*\\b2001\\b).*"),
		CML_1992("CML 1992", "(?i)(?=.*\\bcml\\b)(?=.*\\b1992\\b).*"),

		RECIPE_2016("ReCiPe 2016", "(?i)(?=.*\\brecipe\\b)(?=.*\\b2016\\b).*"),
		RECIPE_2008("ReCiPe 2008", "(?i)(?=.*\\brecipe\\b)(?=.*\\b2008\\b).*"),

		UNKNOWN_LCIA("Unknown LCIA", "(?i)(?=.*\\bunknown\\b).*");

		private final String code;
		private final Pattern pattern;

		Method(String code, String pattern) {
			this.code = code;
			this.pattern = Pattern.compile(pattern);
		}

		static Optional<Method> of(String code) {
			for (var method : values()) {
				if (codesEqual(code, method.code()))
					return Optional.of(method);
			}
			return Optional.empty();
		}

		public String code() {
			return code;
		}

		public double matchScoreOf(String name) {
			return scoreOf(pattern, name);
		}

		@Override
		public String toString() {
			return code;
		}

	}

	public enum IndicatorType {

		/**
		 * Life cycle impact assessment indicators (e.g. global warming potential).
		 */
		LCIA,

		/**
		 * Life cycle inventory input indicators (e.g. resources).
		 */
		LCI_IN,

		/**
		 * Life cycle inventory output indicators (e.g. waste flows).
		 */
		LCI_OUT,
	}

	/**
	 * The list of openEPD indicators.
	 */
	public enum Indicator {

		GWP(
			"Global warming potential",
			IndicatorType.LCIA,
			"gwp",
			"kgCO2e",

			"(?i)(?:\\s*IPCC\\s*(?:20\\d{2})?\\s*)?(?:\\s*global\\s+warming\\s"
				+ "*(?:potential|pot\\.)?\\s*(?:,|\\-)?\\s*(?:total)?\\s*\\(?\\s"
				+ "*(?:gwp)?\\s*\\(?\\s*(?:100)?\\s*a?\\s*\\)?\\s*(?:,|\\-)?\\s*"
				+ "(?:total)?\\s*\\)?\\s*|\\s*climate\\s+change\\s*(?:potential|"
				+ "pot\\.)?\\s*(?:,|\\-)?\\s*(?:total)?\\s*\\(?\\s*(?:gwp)?\\s*\\"
				+ "(?\\s*(?:100)?\\s*a?\\s*\\)?\\s*(?:,|\\-)?\\s*(?:total)?\\s*\\"
				+ ")?\\s*|\\s*\\(?\\s*gwp\\s*(?:,|\\-)?\\s*(?:total)?\\s*\\)?\\s"
				+ "*\\(?\\s*(?:100)?\\s*a?\\s*\\)?\\s*)",

			UnitMatch.of(
				"kgCO2e",
				"(?i)\\s*kg\\s*CO2\\s*\\-?\\s*(?:(?:eq?\\.?)|equivalents)?\\s*")),

		AP(
			"Acidification potential",
			IndicatorType.LCIA,
			"ap",
			"kgSO2e",

			"(?i)(?:\\s*\\(?\\s*ap\\s*(?:,|\\-)?\\s*(?:total)?\\s*\\)?\\s*|\\s"
				+ "*acidification\\s*(?:potential|pot\\.)?\\s*\\(?\\s*(?:ap)?\\s"
				+ "*(?:,|\\-)?\\s*(?:total)?\\s*\\)?\\s*)",

			UnitMatch.of(
				"kgSO2e",
				"(?i)\\s*kg\\s*SO2\\s*\\-?\\s*(?:(?:eq?\\.?)|equivalents)?\\s*")),

		ODP(
			"Ozone depletion potential",
			IndicatorType.LCIA,
			"odp",
			"kgCFC11e",

			"(?i)(?:\\s*\\(?\\s*odp\\s*(?:,|\\-)?\\s*(?:total)?\\s*\\)?\\s*|\\"
				+ "s*(?:stratospheric)?\\s*ozone\\s*(?:layer)?\\s*depletion\\s*("
				+ "?:potential|pot\\.)?\\s*\\(?\\s*(?:odp)?\\s*(?:,|\\-)?\\s*(?:"
				+ "total)?\\s*\\)?\\s*)",

			UnitMatch.of(
				"kgCFC11e",
				"(?i)\\s*kg\\s*CFC\\s*\\-?\\s*11\\s*\\-?\\s*(?:(?:eq?\\.?)|equival"
					+ "ents)?\\s*")),

		EP(
			"Eutrophication potential, marine",
			IndicatorType.LCIA,
			"ep",
			"kgNe",

			"(?i)(?:\\s*\\(?\\s*ep\\s*(?:,|\\-)?\\s*(?:marine)?\\s*\\)?\\s*|\\"
				+ "s*(?:aquatic)?\\s*(?:marine)?\\s*eutrophication\\s*(?:potenti"
				+ "al|pot\\.)?\\s*(?:,|\\-)?\\s*(?:marine)?\\s*\\(?\\s*(?:ep)?\\"
				+ "s*\\)?\\s*)",

			UnitMatch.of(
				"kgNe",
				"(?i)\\s*kg\\s*N\\s*\\-?\\s*(?:(?:eq?\\.?)|equivalents)?\\s*")),

		POCP(
			"Photochemical ozone creation",
			IndicatorType.LCIA,
			"pocp",
			"kgO3e",

			"(?i)(?:(?=.*ozone)(?=.*(?:formation|creation)).*|(?:^|\\W+)pocp(?:$|\\W+))",
			UnitMatch.of(
				"kgO3e",
				"(?i)(?:^|\\W*)kg\\W*O3\\W*eq?\\.?(?:quivalents)?(?:$|\\W*)")),

		GWP_FOSSIL(
			"Global warming potential, fossil sources",
			IndicatorType.LCIA,
			"gwp-fossil",
			"kgCO2e",

			"(?i)(?:\\W*climate\\W*change\\W*(?:potential)?\\W*(?:gwp)?\\W*" +
				"(?:100)?\\W*|\\W*global\\W*warming\\W*(?:potential)?\\W*" +
				"(?:gwp)?\\W*(?:100)?\\W*|\\W*gwp\\W*(?:100)?\\W*)fossil\\W*",

			UnitMatch.of(
				"kgCO2e",
				"(?i)\\s*kg\\s*CO2\\s*\\-?\\s*(?:(?:eq?\\.?)|equivalents)?\\s*")),

		GWP_BIO(
			"Global warming potential, biogenic sources",
			IndicatorType.LCIA,
			"gwp-biogenic",
			"kgCO2e",

			"(?i)(?:(?=.*climate)(?=.*change).*|(?=.*global)(?=.*warming).*" +
				"|.*gwp.*)biogenic",

			UnitMatch.of(
				"kgCO2e",
				"(?i)\\s*kg\\s*CO2\\s*\\-?\\s*(?:(?:eq?\\.?)|equivalents)?\\s*")),

		GWP_LAND(
			"Global warming potential, land use",
			IndicatorType.LCIA,
			"gwp-luluc",
			"kgCO2e",

			"(?i)(?:(?=.*climate)(?=.*change).*|(?=.*global)(?=.*warming).*" +
				"|.*(?:^|\\W+)gwp(?:$|\\W+).*)(?:land|luluc)",

			UnitMatch.of(
				"kgCO2e",
				"(?i)\\s*kg\\s*CO2\\s*\\-?\\s*(?:(?:eq?\\.?)|equivalents)?\\s*")),

		EP_FRESH(
			"Eutrophication potential, fresh water",
			IndicatorType.LCIA,
			"ep-fresh",
			"kg PO4e",

			"(?i)(?=.*fresh).*(?:\\beutrophication\\b|\\bep\\b).*",

			UnitMatch.of(
				"kg PO4e",
				"(?i)(?:^|\\W*)kg\\W*PO4\\W*(?:3\\-)?\\W*(?:(?:eq?\\.?)|equivalents)\\W*(?:$|\\W*)")
		),

		EP_TERR(
			"Eutrophication potential, terrestrial",
			IndicatorType.LCIA,
			"ep-terr",
			"molNe",

			"(?i)(?=.*terr(?:estrial)?).*(?:\\beutrophication\\b|\\bep\\b).*",

			UnitMatch.of(
				"molNe",
				"(?i)(?:^|\\W*)mol\\W*N\\W*eq?\\.?(?:quivalents)?(?:$|\\W*)")
		),

		RPRE(
			"Renewable primary resources, energy",
			IndicatorType.LCI_IN,
			"RPRe",
			"MJ",

			"(?i)(?:^|\\W+)(?:pere|rpre)(?:$|\\W+)",

			UnitMatch.of("MJ", "MJ")),


		RPRM(
			"Renewable primary resources, material",
			IndicatorType.LCI_IN,
			"RPRm",
			"MJ",

			"(?i)(?:^|\\W+)(?:perm|rprm)(?:$|\\W+)",

			UnitMatch.of("MJ", "MJ")),


		NRPRE(
			"Non-renewable primary resources, energy",
			IndicatorType.LCI_IN,
			"NRPRe",
			"MJ",

			"(?i)(?:^|\\W+)(?:penre|nrpre)(?:$|\\W+)",

			UnitMatch.of("MJ", "MJ")),


		NRPRM(
			"Non-renewable primary resources, material",
			IndicatorType.LCI_IN,
			"NRPRm",
			"MJ",

			"(?i)(?:^|\\W+)(?:penrm|nrprm)(?:$|\\W+)",

			UnitMatch.of("MJ", "MJ")),


		SM(
			"Secondary materials",
			IndicatorType.LCI_IN,
			"sm",
			"kg",

			"(?i)(?:^|\\W+)sm(?:$|\\W+)",

			UnitMatch.of("kg", "kg")),


		RSF(
			"Renewable materials, fuel input",
			IndicatorType.LCI_IN,
			"rsf",
			"MJ",

			"(?i)(?:^|\\W+)rsf(?:$|\\W+)",

			UnitMatch.of("MJ", "MJ")),


		NRSF(
			"Non-renewable secondary fuels",
			IndicatorType.LCI_IN,
			"nrsf",
			"MJ",

			"(?i)(?:^|\\W+)nrsf(?:$|\\W+)",

			UnitMatch.of("MJ", "MJ")),


		RE(
			"Recovered energy",
			IndicatorType.LCI_IN,
			"re",
			"MJ",

			"(?i)(?:^|\\W+)re(?:$|\\W+)",

			UnitMatch.of("MJ", "MJ")),


		FW(
			"Fresh water resources",
			IndicatorType.LCI_IN,
			"fw",
			"m3",

			"(?i)(?:^|\\W+)fw(?:$|\\W+)",

			UnitMatch.of("m3", "m3(?:FW)?")),


		HWD(
			"Hazardous waste, disposed",
			IndicatorType.LCI_OUT,
			"hwd",
			"kg",
			"(?i)(?:^|\\W+)hwd(?:$|\\W+)",

			UnitMatch.of("kg", "kg")
		),


		NHWD(
			"Non-hazardous waste, disposed",
			IndicatorType.LCI_OUT,
			"nhwd",
			"kg",

			"(?i)(?:^|\\W+)nhwd(?:$|\\W+)",

			UnitMatch.of("kg", "kg")
		),


		HLRW(
			"High-level radioactive waste",
			IndicatorType.LCI_OUT,
			"hlrw",
			"kg",

			"(?i)(?:^|\\W+)(?:hlrw|rwd)(?:$|\\W+)",

			UnitMatch.of("kg", "kg")
		),


		ILLRW(
			"Intermediate- and low-level radioactive waste",
			IndicatorType.LCI_OUT,
			"illrw",
			"kg",
			"(?i)(?:^|\\W+)illrw(?:$|\\W+)",

			UnitMatch.of("kg", "kg")
		),


		CRU(
			"Components for re-use",
			IndicatorType.LCI_OUT,
			"cru",
			"kg",
			"(?i)(?:^|\\W+)cru(?:$|\\W+)",

			UnitMatch.of("kg", "kg")
		),


		MR(
			"Materials for recycling",
			IndicatorType.LCI_OUT,
			"mr",
			"kg",
			"(?i)(?:^|\\W+)(?:mr|mfr)(?:$|\\W+)",

			UnitMatch.of("kg", "kg")
		),


		MER(
			"Materials for energy recovery",
			IndicatorType.LCI_OUT,
			"mer",
			"kg",
			"(?i)(?:^|\\W+)mer(?:$|\\W+)",

			UnitMatch.of("kg", "kg")
		),


		EE(
			"Recovered energy, exported",
			IndicatorType.LCI_OUT,
			"ee",
			"MJ",
			"(?i)(?:^|\\W+)eee?(?:$|\\W+)",

			UnitMatch.of("MJ", "MJ")
		),


		EH(
			"Recovered heat, exported",
			IndicatorType.LCI_OUT,
			"eh",
			"MJ",
			"(?i)(?:^|\\W+)(?:eh|eet)(?:$|\\W+)",

			UnitMatch.of("MJ", "MJ")
		);

		private final String description;
		private final IndicatorType type;
		private final String code;
		private final String unit;
		private final Pattern pattern;
		private final UnitMatch[] units;

		Indicator(
			String description,
			IndicatorType type,
			String code,
			String unit,
			String pattern,
			UnitMatch... units) {
			this.description = description;
			this.type = type;
			this.code = code;
			this.unit = unit;
			this.pattern = Pattern.compile(pattern);
			this.units = units;
		}

		public static Optional<Indicator> of(String code) {
			for (var indicator : values()) {
				if (codesEqual(code, indicator.code))
					return Optional.of(indicator);
			}
			return Optional.empty();
		}

		/**
		 * Returns the indicator description.
		 */
		public String description() {
			return description;
		}

		public IndicatorType type() {
			return type;
		}

		/**
		 * Returns the openEPD indicator code.
		 */
		public String code() {
			return code;
		}

		/**
		 * Returns the openEPD indicator unit.
		 */
		public String unit() {
			return unit;
		}

		/**
		 * Returns a matching-score for the given name.
		 */
		public double matchScoreOf(String name) {
			return scoreOf(pattern, name);
		}

		public Optional<UnitMatch> unitMatchOf(String unit) {
			if (Strings.nullOrEmpty(unit))
				return Optional.empty();
			for (var u : units) {
				if (u.matches(unit))
					return Optional.of(u);
			}
			return Optional.empty();
		}

		@Override
		public String toString() {
			return code;
		}
	}

	public record UnitMatch(
		String name, Pattern pattern, double factor) {

		static UnitMatch of(String name, String pattern) {
			return of(name, pattern, 1.0);
		}

		static UnitMatch of(String name, String pattern, double factor) {
			return new UnitMatch(name, Pattern.compile(pattern), factor);
		}

		public boolean matches(String unit) {
			if (Strings.nullOrEmpty(unit))
				return false;
			var matcher = pattern.matcher(unit);
			return matcher.matches();
		}

	}

	private static double scoreOf(Pattern pattern, String s) {
		if (Strings.nullOrEmpty(s))
			return 0;
		var matcher = pattern.matcher(s);
		if (matcher.matches())
			return 1;
		matcher.reset();
		if (!matcher.find())
			return 0;
		var g = matcher.group();
		return (double) g.length() / (double) s.length();
	}

	public enum Scope {

		A1toA3(
			"A1A2A3",
			"(?i)(?:(?:\\s*a\\s*1\\s*a\\s*2\\s*a\\s*3\\s*)" +
				"|(?:\\s*a\\s*1\\s*\\-\\s*a\\s*3\\s*)" +
				"|(?:\\s*a\\s*1\\s*to\\s*a\\s*3\\s*))"),

		A1("A1", "(?i)\\s*a\\s*1\\s*"),
		A2("A2", "(?i)\\s*a\\s*2\\s*"),
		A3("A3", "(?i)\\s*a\\s*3\\s*"),
		A4("A4", "(?i)\\s*a\\s*4\\s*"),
		A5("A5", "(?i)\\s*a\\s*5\\s*"),

		B2toB7(
			"B2toB7",
			"(?i)(?:(?:\\s*b\\s*2\\s*\\-\\s*b\\s*7\\s*)" +
				"|(?:\\s*b\\s*2\\s*to\\s*b\\s*7\\s*))"),

		B1("B1", "(?i)\\s*b\\s*1\\s*"),
		B2("B2", "(?i)\\s*b\\s*2\\s*"),
		B3("B3", "(?i)\\s*b\\s*3\\s*"),
		B4("B4", "(?i)\\s*b\\s*4\\s*"),
		B5("B5", "(?i)\\s*b\\s*5\\s*"),
		B6("B6", "(?i)\\s*b\\s*6\\s*"),
		B7("B7", "(?i)\\s*b\\s*7\\s*"),

		C1("C1", "(?i)\\s*c\\s*1\\s*"),
		C2("C2", "(?i)\\s*c\\s*2\\s*"),
		C3("C3", "(?i)\\s*c\\s*3\\s*"),
		C4("C4", "(?i)\\s*c\\s*4\\s*"),

		D("D", "(?i)\\s*d\\s*");


		private final String code;
		private final Pattern pattern;

		Scope(String code, String pattern) {
			this.code = code;
			this.pattern = Pattern.compile(pattern);
		}

		public String code() {
			return code;
		}

		@Override
		public String toString() {
			return code;
		}
	}

	public static Optional<Scope> findScope(String name) {
		if (Strings.nullOrEmpty(name))
			return Optional.empty();
		for (var scope : Scope.values()) {
			if (scope.pattern.matcher(name).matches())
				return Optional.of(scope);
		}
		return Optional.empty();
	}

	static boolean codesEqual(String code1, String code2) {
		return code1 != null && code2 != null
			&& code1.trim().equalsIgnoreCase(code2.trim());
	}
}

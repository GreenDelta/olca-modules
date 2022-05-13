package org.openlca.io.openepd;

import org.openlca.util.Strings;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Contains openEPD vocabulary constants and matching rules for mapping these
 * constants to or from typical names in LCA databases.
 */
public class Vocab {

	public enum Method {

			TRACI_2_1("TRACI 2.1", "(?i)\\W*traci\\W*2\\.1\\W*"),
			EF_3_0("EF 3.0", "(?i)\\W*ef\\W*3\\.0\\W*"),
			CML_2016("CML 2016", "(?i)\\W*cml\\W*2016\\W*"),
			CML_2012("CML 2012", "(?i)\\W*cml\\W*2012\\W*"),
			RECIPE_2016("ReCiPe 2016", "(?i)\\W*recipe\\W*2016\\W*"),
			UNKNOWN_LCIA("Unknown LCIA", "");

			private final String code;
			private final Pattern pattern;

			Method(String code, String pattern) {
				this.code = code;
				this.pattern = Pattern.compile(pattern);
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

  /**
   * The list of openEPD indicators.
   */
  public enum Indicator {

    GWP(
      "Global climate change impact over 100 years",
      true,
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
        "kg CO2 eq.",
        "(?i)\\s*kg\\s*CO2\\s*\\-?\\s*(?:(?:eq?\\.?)|equivalents)?\\s*")),

    AP(
      "Acidification potential in water or air",
      true,
      "ap",
      "kgSO2e",

      "(?i)(?:\\s*\\(?\\s*ap\\s*(?:,|\\-)?\\s*(?:total)?\\s*\\)?\\s*|\\s"
        + "*acidification\\s*(?:potential|pot\\.)?\\s*\\(?\\s*(?:ap)?\\s"
        + "*(?:,|\\-)?\\s*(?:total)?\\s*\\)?\\s*)",

      UnitMatch.of(
        "kg SO2 eq.",
        "(?i)\\s*kg\\s*SO2\\s*\\-?\\s*(?:(?:eq?\\.?)|equivalents)?\\s*")),

    ODP(
      "Global ozone depletion potential",
      true,
      "odp",
      "kgCFC11e",

      "(?i)(?:\\s*\\(?\\s*odp\\s*(?:,|\\-)?\\s*(?:total)?\\s*\\)?\\s*|\\"
        + "s*(?:stratospheric)?\\s*ozone\\s*(?:layer)?\\s*depletion\\s*("
        + "?:potential|pot\\.)?\\s*\\(?\\s*(?:odp)?\\s*(?:,|\\-)?\\s*(?:"
        + "total)?\\s*\\)?\\s*)",

      UnitMatch.of(
        "kg CFC11 eq.",
        "(?i)\\s*kg\\s*CFC\\s*\\-?\\s*11\\s*\\-?\\s*(?:(?:eq?\\.?)|equival"
        + "ents)?\\s*")),

    EP(
      "Eutrophication potential in marine ecosystems",
      true,
      "ep",
      "kgNe",

      "(?i)(?:\\s*\\(?\\s*ep\\s*(?:,|\\-)?\\s*(?:marine)?\\s*\\)?\\s*|\\"
        + "s*(?:aquatic)?\\s*(?:marine)?\\s*eutrophication\\s*(?:potenti"
        + "al|pot\\.)?\\s*(?:,|\\-)?\\s*(?:marine)?\\s*\\(?\\s*(?:ep)?\\"
        + "s*\\)?\\s*)",

      UnitMatch.of(
        "kg N eq.",
        "(?i)\\s*kg\\s*N\\s*\\-?\\s*(?:(?:eq?\\.?)|equivalents)?\\s*")),

    RPRE(
      "Renewable primary resources used as energy carrier (fuel)",
      false,
      "RPRe",
      "MJ",

      "(?i)(?:^|\\W+)pere(?:$|\\W+)",

      UnitMatch.of("MJ", "MJ")),


    RPRM(
      "Renewable primary resources with energy content used as material",
      false,
      "RPRm",
      "MJ",

      "(?i)(?:^|\\W+)perm(?:$|\\W+)",

      UnitMatch.of("MJ", "MJ")),


    NRPRE(
      "Non-renewable primary resources used as an energy carrier (fuel)",
      false,
      "NRPRe",
      "MJ",

      "(?i)(?:^|\\W+)penre(?:$|\\W+)",

      UnitMatch.of("MJ", "MJ")),


    NRPRM(
      "Non-renewable primary resources with energy content used as material",
      false,
      "NRPRm",
      "MJ",

      "(?i)(?:^|\\W+)penrm(?:$|\\W+)",

      UnitMatch.of("MJ", "MJ")),


    SM(
      "Secondary materials: materials recycled from previous use or waste",
      false, "sm", "kg",

      "(?i)(?:^|\\W+)sm(?:$|\\W+)",

      UnitMatch.of("MJ", "MJ")),


    RSF(
      "Renewable materials with energy content",
      false,
      "rsf",
      "MJ",

      "(?i)(?:^|\\W+)rsf(?:$|\\W+)",

      UnitMatch.of("MJ", "MJ")),


    NRSF(
      "Non-renewable secondary fuels",
      false,
      "nrsf",
      "MJ",

      "(?i)(?:^|\\W+)nrsf(?:$|\\W+)",

      UnitMatch.of("MJ", "MJ")),


    RE(
      "Recovered energy",
      false,
      "re",
      "MJ",

      "(?i)(?:^|\\W+)re(?:$|\\W+)",

      UnitMatch.of("MJ", "MJ")),


    FW(
      "Use of net fresh water resources",
      false,
      "fw",
      "m3",

      "(?i)(?:^|\\W+)fw(?:$|\\W+)",

      UnitMatch.of("m3", "m3(?:FW)?")),

    ;

    private final String description;
    private final boolean isLcia;
    private final String code;
    private final String unit;
    private final Pattern pattern;
    private final UnitMatch[] units;

    Indicator(
      String description,
      boolean isLcia,
      String code,
      String unit,
      String pattern,
      UnitMatch...units) {
      this.description = description;
      this.isLcia = isLcia;
      this.code = code;
      this.unit = unit;
      this.pattern = Pattern.compile(pattern);
      this.units = units;
    }

    /**
     * Returns the indicator description.
     */
    public String description() {
      return description;
    }

    public boolean isInventoryIndicator() {
      return !isLcia;
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
}

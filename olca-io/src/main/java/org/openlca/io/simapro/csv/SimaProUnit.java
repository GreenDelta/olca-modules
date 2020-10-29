package org.openlca.io.simapro.csv;

public enum SimaProUnit {

	acre("acre", 4046.856, "m2", "Area", null),
	Bq("Bq", 1, "Bq", "Radioactivity", null),
	Btu("Btu", 0.001055696, "MJ", "Energy", null),
	CHF2005("CHF2005", 1.02, "USD", "Currency", null),
	cm("cm", 0.01, "m", "Length", null),
	cm2("cm2", 0.0001, "m2", "Area", null),
	cm2a("cm2a", 0.0001, "m2a", "Land use", null),
	cm3("cm3", 0.000001, "m3", "Volume", null),
	cm3y("cm3y", 0.000001, "m3y", "Volume.Time", null),
	cu_in("cu.in", 0.00001638706, "m3", "Volume", null),
	cu_yd("cu.yd", 0.7645549, "m3", "Volume", null),
	cuft("cuft", 0.02831685, "m3", "Volume", null),
	day("day", 86400, "s", "Time", null),
	DKK99("DKK99", 0.138, "USD", "Currency", null),
	dm("dm", 0.1, "m", "Length", null),
	dm2("dm2", 0.01, "m2", "Area", null),
	dm3("dm3", 0.001, "m3", "Volume", null),
	EUR2003("EUR2003", 1.14, "USD", "Currency", null),
	ft("ft", 0.3048, "m", "Length", null),
	g("g", 0.001, "kg", "Mass", null),
	gal_("gal*", 0.003785412, "m3", "Volume", null),
	GJ("GJ", 1000, "MJ", "Energy", null),
	GPt("GPt", 1000000000, "Pt", "Indicator", null),
	ha_a("ha a", 10000, "m2a", "Land use", null),
	ha("ha", 10000, "m2", "Area", null),
	hr("hr", 3600, "s", "Time", new String[] { "hour", "h" }),
	inch("inch", 0.0254, "m", "Length", null),
	J("J", 0.000001, "MJ", "Energy", null),
	kBq("kBq", 1000, "Bq", "Radioactivity", null),
	kcal("kcal", 0.0041855, "MJ", "Energy", null),
	kDKK99("kDKK99", 138, "USD", "Currency", null),
	kg("kg", 1, "kg", "Mass", null),
	kg_day("kg*day", 0.00274, "kgy", "Mass.Time", null),
	kgkm("kgkm", 0.001, "tkm", "Transport", null),
	kgy("kgy", 1, "kgy", "Mass.Time", null),
	kJ("kJ", 0.001, "MJ", "Energy", null),
	km("km", 1000, "m", "Length", null),
	km2("km2", 1000000, "m2", "Area", null),
	km2a("km2a", 1000000, "m2a", "Land use", null),
	kmy("kmy", 1000, "my", "Length.Time", null),
	kPt("kPt", 1000, "Pt", "Indicator", null),
	ktkm("ktkm", 1000, "tkm", "Transport", null),
	kton("kton", 1000000, "kg", "Mass", null),
	kWh("kWh", 3.6, "MJ", "Energy", null),
	kWp("kWp", 3.6, "MJ", "Energy", null),
	l("l", 0.001, "m3", "Volume", null),
	l_day("l*day", 0.0000027397, "m3y", "Volume.Time", null),
	lb("lb", 0.4535924, "kg", "Mass", null),
	m("m", 1, "m", "Length", null),
	M$("M$", 1000000, "USD", "Currency", null),
	m2("m2", 1, "m2", "Area", null),
	m2_year("m2*year", 1, "m2", "Area", null),
	m2a("m2a", 1, "m2a", "Land use", new String[] { "m2*a" }),
	m3("m3", 1, "m3", "Volume", null),
	m3day("m3day", 0.0027397, "m3y", "Volume.Time", null),
	m3km("m3km", 1.0, "m3km", "Volume.Length", new String[] {"m3*km"}),
	m3y("m3y", 1, "m3y", "Volume.Time", new String[] { "m3*a" }),
	mBq("mBq", 0.001, "Bq", "Radioactivity", null),
	mg("mg", 0.000001, "kg", "Mass", null),
	mile("mile", 1609.35, "m", "Length", null),
	min("min", 60, "s", "Time", null),
	miy("miy", 1609.35, "my", "Length.Time", null),
	MJ("MJ", 1, "MJ", "Energy", null),
	mm("mm", 0.001, "m", "Length", null),
	mm2("mm2", 0.000001, "m2", "Area", null),
	mm2a("mm2a", 0.000001, "m2a", "Land use", null),
	mm3("mm3", 0.000000001, "m3", "Volume", null),
	mPt("mPt", 0.001, "Pt", "Indicator", null),
	MPt("MPt", 1000000, "Pt", "Indicator", null),
	Mtn("Mtn", 1000000000, "kg", "Mass", null),
	MWh("MWh", 3600, "MJ", "Energy", null),
	my("my", 1, "my", "Length.Time", null),
	nBq("nBq", 0.000000001, "Bq", "Radioactivity", null),
	ng("ng", 0.000000000001, "kg", "Mass", null),
	Nm3("Nm3", 1, "m3", "Volume", null),
	nPt("nPt", 0.000000001, "Pt", "Indicator", null),
	oz("oz", 0.02834952, "kg", "Mass", null),
	p("p", 1, "p", "Amount", new String[] { "Item(s)" }),
	personkm("personkm", 1, "personkm", "Person.Distance", null),
	pg("pg", 1E-015, "kg", "Mass", null),
	PJ("PJ", 1000000000, "MJ", "Energy", null),
	pmi("pmi", 1.60935, "personkm", "Person.Distance", null),
	Pt("Pt", 1, "Pt", "Indicator", null),
	s("s", 1, "s", "Time", null),
	sq_ft("sq.ft", 0.09290304, "m2", "Area", null),
	sq_in("sq.in", 0.00064516, "m2", "Area", null),
	sq_mi("sq.mi", 2589988, "m2", "Area", null),
	sq_yd("sq.yd", 0.8361273, "m2", "Area", null),
	t("t", 1000, "kg", "Mass", null),
	TJ("TJ", 1000000, "MJ", "Energy", null),
	tkm("tkm", 1, "tkm", "Transport", null),
	tmi_("tmi*", 1.45997, "tkm", "Transport", null),
	tn_lg("tn.lg", 1016.047, "kg", "Mass", null),
	tn_sh("tn.sh", 907.1848, "kg", "Mass", null),
	ton("ton", 1000, "kg", "Mass", null),
	USD("USD", 1, "USD", "Currency", null),
	USD2002("USD2002", 1.18, "USD", "Currency", null),
	Wh("Wh", 0.0036, "MJ", "Energy", null),
	yard("yard", 0.9144, "m", "Length", null),
	year("year", 31536000, "s", "Time", null),
	Yen2000("Yen2000", 0.0093, "USD", "Currency", null),
	microBq("µBq", 0.000001, "Bq", "Radioactivity", null),
	microg("µg", 0.000000001, "kg", "Mass", null),
	microm("µm", 0.000001, "m", "Length", null),
	microPt("µPt", 0.000001, "Pt", "Indicator", null);

	public final String symbol;
	public final double factor;
	public final String refUnit;
	public final String quantity;

	private final String[] synonyms;

	SimaProUnit(String symbol,
			double factor,
			String refUnit,
			String quantity,
			String[] synonyms) {
		this.symbol = symbol;
		this.factor = factor;
		this.refUnit = refUnit;
		this.quantity = quantity;
		this.synonyms = synonyms;
	}

	public static SimaProUnit find(String name) {
		if (name == null)
			return null;
		String n = name.trim();
		for (SimaProUnit u : values()) {
			if (n.equals(u.symbol))
				return u;
			if (u.synonyms == null)
				continue;
			for (String syn : u.synonyms) {
				if (n.equals(syn))
					return u;
			}
		}
		return null;
	}

}

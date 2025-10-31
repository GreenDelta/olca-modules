package org.openlca.io.smartepd;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openlca.jsonld.Json;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

public enum SmartIndicator {

	/// Global warming potential, total
	GWP_TOTAL(SmartIndicatorType.IMPACT, "GWP-total", "kg CO2 eq"),

	/// Global warming potential, total, excluding CO2 from biogenic and land use and land use change
	GWP_TOTAL_EXCL_BIOGENIC_(SmartIndicatorType.IMPACT, "GWP-total (excl biogenic)", "kg CO2 eq"),

	/// Global warming potential, total, including CO2 from biogenic and land use and land use change
	GWP_TOTAL_INCL_BIOGENIC_(SmartIndicatorType.IMPACT, "GWP-total (incl biogenic)", "kg CO2 eq"),

	/// Global warming potential, fossil
	GWP_FOSSIL(SmartIndicatorType.IMPACT, "GWP-fossil", "kg CO2 eq"),

	/// Global warming potential, biogenic
	GWP_BIOGENIC(SmartIndicatorType.IMPACT, "GWP-biogenic", "kg CO2 eq"),

	/// Global warming potential, land use and land use change
	GWP_LULUC(SmartIndicatorType.IMPACT, "GWP-luluc", "kg CO2 eq"),

	/// Ozone depletion potential
	ODP(SmartIndicatorType.IMPACT, "ODP", "kg CFC 11 eq"),

	/// Acidification Potential
	AP(SmartIndicatorType.IMPACT, "AP", "kg SO2 eq"),

	/// Eutrophication potential
	EP(SmartIndicatorType.IMPACT, "EP", "kg N eq"),

	/// Photochemical oxidant creation potential, Smog formation potential
	POCP(SmartIndicatorType.IMPACT, "POCP", "kg O3 eq"),

	/// Human toxicity potential, carcinogenics
	HTP_C(SmartIndicatorType.IMPACT, "HTP-c", "CTUh"),

	/// Human toxicity potential, non carcinogenics
	HTP_NC(SmartIndicatorType.IMPACT, "HTP-nc", "CTUh"),

	/// Ecotoxicity potential
	ETP_FW(SmartIndicatorType.IMPACT, "ETP-fw", "CTUe"),

	/// Respiratory effects, Particulate Matter
	PM(SmartIndicatorType.IMPACT, "PM", "kg PM2.5 eq"),

	/// Water deprivation potential
	WDP(SmartIndicatorType.IMPACT, "WDP", "m3"),

	/// Freshwater eutrophication
	EP_FW(SmartIndicatorType.IMPACT, "EP-fw", "kg P eq"),

	/// Marine eutrophication
	EP_MARINE(SmartIndicatorType.IMPACT, "EP-marine", "kg N eq"),

	/// Freshwater consumption
	FW(SmartIndicatorType.RESOURCE, "FW", "m3"),

	/// Ionizing radiation
	IRP(SmartIndicatorType.IMPACT, "IRP", "kg Bq (Cobalt 60) eq"),

	/// Fine particulate matter formation
	PM2_5(SmartIndicatorType.IMPACT, "PM2.5", "kg PM2.5 eq"),

	/// Abiotic depletion potential for fossil resources
	ADP_FOSSIL(SmartIndicatorType.IMPACT, "ADP-fossil", "MJ"),

	/// Mineral resource scarcity
	ADP_MINERALS_METALS(SmartIndicatorType.IMPACT, "ADP-minerals&metals", "kg Sb eq"),

	/// Land Use
	LU(SmartIndicatorType.IMPACT, "LU", "m2 yr"),

	/// Marine ecotoxicity
	ETP_MAR(SmartIndicatorType.IMPACT, "ETP-mar", "kg 1,4-DCB eq"),

	/// Terrestrial ecotoxicity
	ETP_TER(SmartIndicatorType.IMPACT, "ETP-ter", "kg 1,4-DCB eq"),

	/// Human toxicity
	HTP(SmartIndicatorType.IMPACT, "HTP", "kg 1,4-DCB eq"),

	/// Global warming potential, excluding biogenic CO2 uptake and emissions and biogenic CO2 stored in product
	GWP_GHG(SmartIndicatorType.IMPACT, "GWP-GHG", "kg CO2 eq"),

	/// Ecotoxicity, freshwater_inorganics
	ETP_FWIO(SmartIndicatorType.IMPACT, "ETP-fwio", "CTUe"),

	/// Ecotoxicity, freshwater_organics
	ETP_FWO(SmartIndicatorType.IMPACT, "ETP-fwo", "CTUe"),

	/// Ecotoxicity, freshwater_total
	ETP_FW_TOTAL(SmartIndicatorType.IMPACT, "ETP-fw-total", "CTUe"),

	/// Human toxicity, cancer_inorganics
	HTP_CIO(SmartIndicatorType.IMPACT, "HTP-cio", "CTUh"),

	/// Human toxicity, cancer_organics
	HTP_CO(SmartIndicatorType.IMPACT, "HTP-co", "CTUh"),

	/// Human toxicity, cancer_total
	HTP_C_TOTAL(SmartIndicatorType.IMPACT, "HTP-c-total", "CTUh"),

	/// Human toxicity, noncancer_inorganics
	HTP_NCIO(SmartIndicatorType.IMPACT, "HTP-ncio", "CTUh"),

	/// Human toxicity, noncancer_organics
	HTP_NCO(SmartIndicatorType.IMPACT, "HTP-nco", "CTUh"),

	/// Human toxicity, noncancer_total
	HTP_NC_TOTAL(SmartIndicatorType.IMPACT, "HTP-nc-total", "CTUh"),

	/// Eutrophication potential, accumulated exceedance
	EP_TERRESTRIAL(SmartIndicatorType.IMPACT, "EP-terrestrial", "mol N eq"),

	/// Land Use
	SQI(SmartIndicatorType.IMPACT, "SQI", "dimensionless"),

	/// Renewable primary resources used as energy carrier (fuel)
	PERE(SmartIndicatorType.RESOURCE, "PERE", "MJ"),

	/// Renewable primary resources with energy content used as a material
	PERM(SmartIndicatorType.RESOURCE, "PERM", "MJ"),

	/// Total use of renewable primary resources with energy content
	PERT(SmartIndicatorType.RESOURCE, "PERT", "MJ"),

	/// Non-renewable primary resources used as an energy carrier (fuel)
	PENRE(SmartIndicatorType.RESOURCE, "PENRE", "MJ"),

	/// Non-renewable primary resources with energy content used as a material
	PENRM(SmartIndicatorType.RESOURCE, "PENRM", "MJ"),

	/// Total non-renewable primary resources with energy content
	PENRT(SmartIndicatorType.RESOURCE, "PENRT", "MJ"),

	/// Renewable primary resources used as energy carrier (fuel)
	RPRE(SmartIndicatorType.RESOURCE, "RPRE", "MJ"),

	/// Renewable primary resources with energy content used as a material
	RPRM(SmartIndicatorType.RESOURCE, "RPRM", "MJ"),

	/// Total use of renewable primary resources with energy content
	RPRT(SmartIndicatorType.RESOURCE, "RPRT", "MJ"),

	/// Non-renewable primary resources used as an energy carrier (fuel)
	NRPRE(SmartIndicatorType.RESOURCE, "NRPRE", "MJ"),

	/// Non-renewable primary resources with energy content used as a material
	NRPRM(SmartIndicatorType.RESOURCE, "NRPRM", "MJ"),

	/// Total non-renewable primary resources with energy content
	NRPRT(SmartIndicatorType.RESOURCE, "NRPRT", "MJ"),

	/// Secondary materials
	SM(SmartIndicatorType.RESOURCE, "SM", "kg"),

	/// Renewable secondary fuels
	RSF(SmartIndicatorType.RESOURCE, "RSF", "MJ"),

	/// Non-renewable secondary fuels
	NRSF(SmartIndicatorType.RESOURCE, "NRSF", "MJ"),

	/// Hazardous waste disposed
	HWD(SmartIndicatorType.OUTPUT, "HWD", "kg"),

	/// Non-hazardous waste disposed
	NHWD(SmartIndicatorType.OUTPUT, "NHWD", "kg"),

	/// Radioactive waste disposed
	RWD(SmartIndicatorType.OUTPUT, "RWD", "kg"),

	/// High-level radiactive waste
	HLRW(SmartIndicatorType.OUTPUT, "HLRW", "kg"),

	/// Intermediate- and low-level radiative waste
	ILLRW(SmartIndicatorType.OUTPUT, "ILLRW", "kg"),

	/// Component for re-use
	CRU(SmartIndicatorType.OUTPUT, "CRU", "kg"),

	/// Materials for recycling
	MFR(SmartIndicatorType.OUTPUT, "MFR", "kg"),

	/// Materials for energy recovery
	MER(SmartIndicatorType.OUTPUT, "MER", "kg"),

	/// Materials for incineration, no energy recovery
	MNER(SmartIndicatorType.OUTPUT, "MNER", "kg"),

	/// Exported energy, electricity, Recovered energy exported from the product system
	EEE(SmartIndicatorType.OUTPUT, "EEE", "MJ"),

	/// Exported thermal energy
	EET(SmartIndicatorType.OUTPUT, "EET", "MJ");

	private final SmartIndicatorType type;
	private final String id;
	private final String defaultUnit;

	SmartIndicator(SmartIndicatorType type, String id, String defaultUnit) {
		this.type = type;
		this.id = id;
		this.defaultUnit = defaultUnit;
	}

	public String id() {
		return id;
	}

	public String defaultUnit() {
		return defaultUnit;
	}

	public String unitFor(SmartMethod method) {
		if (method == null)
			return defaultUnit;
		var units = Unit.map.get(this);
		if (units == null)
			return defaultUnit;
		for (var u : units) {
			if (u.method == method)
				return u.symbol;
		}
		return defaultUnit;
	}

	public SmartIndicatorType type() {
		return type;
	}

	public static Optional<SmartIndicator> of(String id) {
		if (Strings.isBlank(id))
			return Optional.empty();
		var s = id.strip();
		for (var i : values()) {
			if (i.id.equalsIgnoreCase(s))
				return Optional.of(i);
		}
		return Optional.empty();
	}

	public String toString() {
		return id;
	}

	public boolean isImpact() {
		return type == SmartIndicatorType.IMPACT;
	}

	public boolean isResource() {
		return type == SmartIndicatorType.RESOURCE;
	}

	public boolean isOutput() {
		return type == SmartIndicatorType.OUTPUT;
	}

	private record Unit(SmartMethod method, String symbol) {

		static final Map<SmartIndicator, List<Unit>> map;

		static {
			map = new EnumMap<>(SmartIndicator.class);
			var in = Unit.class.getResourceAsStream("indicator-units.json");
			if (in != null) {
				try (in) {
					var json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
					var array = new Gson().fromJson(json, JsonArray.class);
					for (var e : array) {
						if (!e.isJsonObject())
							continue;

						var obj = e.getAsJsonObject();
						var methodId = Json.getString(obj, "method");
						if (Strings.isBlank(methodId) || methodId.equals("_"))
							continue;
						var method = SmartMethod.of(methodId).orElse(null);
						if (method == null)
							continue;

						var indicator = SmartIndicator
								.of(Json.getString(obj, "indicator"))
								.orElse(null);
						if (indicator == null)
							continue;

						var unit = Json.getString(obj, "unit");
						if (Strings.isBlank(unit))
							continue;
						map.computeIfAbsent(indicator, k -> new ArrayList<>())
								.add(new Unit(method, unit));
					}
				} catch (Exception e) {
					var log = LoggerFactory.getLogger(Unit.class);
					log.error("failed to read indicator units", e);
				}
			}
		}

	}
}

package org.openlca.jsonld.output;

import java.util.Objects;
import java.util.function.Consumer;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.core.model.Uncertainty;
import org.openlca.jsonld.Dates;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class ProcessWriter extends Writer<Process> {

	private Process process;
	private Consumer<RootEntity> refFn;

	@Override
	JsonObject write(Process process, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(process, refFn);
		if (obj == null)
			return null;
		this.process = process;
		this.refFn = refFn;
		ProcessType type = process.getProcessType();
		if (type != null)
			obj.addProperty("processTyp", type.name());
		obj.addProperty("defaultAllocationMethod", getAllocationType(
				process.getDefaultAllocationMethod()));
		obj.add("location", createRef(process.getLocation(), refFn));
		obj.add("processDocumentation", createDoc());
		mapExchanges(obj);
		return obj;
	}

	private void mapExchanges(JsonObject obj) {
		JsonArray exchanges = new JsonArray();
		for (Exchange e : process.getExchanges()) {
			JsonObject eObj = new JsonObject();
			mapExchange(e, eObj);
			if (Objects.equals(process.getQuantitativeReference(), e))
				eObj.addProperty("quantitativeReference", true);
			exchanges.add(eObj);
		}
		obj.add("exchanges", exchanges);
	}

	private JsonObject createDoc() {
		ProcessDocumentation d = process.getDocumentation();
		if (d == null)
			return null;
		JsonObject o = new JsonObject();
		o.addProperty("@type", "ProcessDocumentation");
		mapSimpleDocFields(d, o);
		o.add("reviewer", createRef(d.getReviewer(), refFn));
		o.add("dataDocumentor", createRef(d.getDataDocumentor(), refFn));
		o.add("dataGenerator", createRef(d.getDataGenerator(), refFn));
		o.add("dataSetOwner", createRef(d.getDataSetOwner(), refFn));
		o.add("publication", createRef(d.getPublication(), refFn));
		JsonArray sources = new JsonArray();
		for (Source source : d.getSources())
			sources.add(createRef(source, refFn));
		o.add("sources", sources);
		return o;
	}

	private void mapSimpleDocFields(ProcessDocumentation d, JsonObject o) {
		o.addProperty("timeDescription", d.getTime());
		o.addProperty("technologyDescription", d.getTechnology());
		o.addProperty("dataCollectionDescription", d.getDataCollectionPeriod());
		o.addProperty("completenessDescription", d.getCompleteness());
		o.addProperty("dataSelectionDescription", d.getDataSelection());
		o.addProperty("reviewDetails", d.getReviewDetails());
		o.addProperty("dataTreatmentDescription", d.getDataTreatment());
		o.addProperty("inventoryMethodDescription", d.getInventoryMethod());
		o.addProperty("modelingConstantsDescription", d.getModelingConstants());
		o.addProperty("samplingDescription", d.getSampling());
		o.addProperty("restrictionsDescription", d.getRestrictions());
		o.addProperty("copyright", d.isCopyright());
		o.addProperty("validFrom", Dates.toString(d.getValidFrom()));
		o.addProperty("validUntil", Dates.toString(d.getValidUntil()));
		o.addProperty("creationDate", Dates.toString(d.getCreationDate()));
		o.addProperty("intendedApplication", d.getIntendedApplication());
		o.addProperty("projectDescription", d.getProject());
		o.addProperty("geographyDescription", d.getGeography());
	}

	private String getAllocationType(AllocationMethod method) {
		if (method == null)
			return null;
		switch (method) {
		case CAUSAL:
			return "CAUSAL_ALLOCATION";
		case ECONOMIC:
			return "ECONOMIC_ALLOCATION";
		case PHYSICAL:
			return "PHYSICAL_ALLOCATION";
		default:
			return null;
		}
	}

	private void mapExchange(Exchange e, JsonObject obj) {
		if (e == null || obj == null)
			return;
		obj.addProperty("@type", "Exchange");
		obj.addProperty("avoidedProduct", e.isAvoidedProduct());
		obj.addProperty("input", e.isInput());
		obj.addProperty("baseUncertainty", e.getBaseUncertainty());
		obj.addProperty("amount", e.getAmountValue());
		obj.addProperty("amountFormula", e.getAmountFormula());
		obj.addProperty("pedigreeUncertainty", e.getPedigreeUncertainty());
		mapExchangeRefs(e, obj);
	}

	private void mapExchangeRefs(Exchange e, JsonObject obj) {
		// TODO: default providers
		obj.add("flow", createRef(e.getFlow(), refFn));
		obj.add("unit", createRef(e.getUnit()));
		FlowPropertyFactor propFac = e.getFlowPropertyFactor();
		if (propFac != null) {
			JsonObject ref = createRef(propFac.getFlowProperty(), refFn);
			obj.add("flowProperty", ref);
		}
		Uncertainty uncertainty = e.getUncertainty();
		if (uncertainty != null) {
			JsonObject uncertaintyObj = new JsonObject();
			Uncertainties.map(uncertainty, uncertaintyObj);
			obj.add("uncertainty", uncertaintyObj);
		}
	}

}

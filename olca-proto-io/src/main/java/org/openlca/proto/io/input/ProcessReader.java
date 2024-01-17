
package org.openlca.proto.io.input;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDoc;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.doc.Review;
import org.openlca.proto.ProtoProcess;
import org.openlca.proto.ProtoProcessDocumentation;
import org.openlca.proto.ProtoProcessType;
import org.openlca.util.Strings;

public class ProcessReader implements EntityReader<Process, ProtoProcess> {

	private final EntityResolver resolver;
	private final TIntObjectHashMap<Exchange> exchanges = new TIntObjectHashMap<>();

	public ProcessReader(EntityResolver resolver) {
		this.resolver = resolver;
	}

	@Override
	public Process read(ProtoProcess proto) {
		var process = new Process();
		update(process, proto);
		return process;
	}

	@Override
	public void update(Process p, ProtoProcess proto) {
		Util.mapBase(p, ProtoBox.of(proto), resolver);
		p.processType = proto.getProcessType() == ProtoProcessType.LCI_RESULT
			? ProcessType.LCI_RESULT
			: ProcessType.UNIT_PROCESS;
		p.infrastructureProcess = proto.getIsInfrastructureProcess();
		p.defaultAllocationMethod = Util.allocationMethodOf(
			proto.getDefaultAllocationMethod());
		p.documentation = mapDoc(proto.getProcessDocumentation());
		p.location = Util.getLocation(resolver, proto.getLocation());

		// DQ systems
		p.dqSystem = Util.getDQSystem(resolver, proto.getDqSystem());
		p.dqEntry = proto.getDqEntry();
		p.exchangeDqSystem = Util.getDQSystem(resolver, proto.getExchangeDqSystem());
		p.socialDqSystem = Util.getDQSystem(resolver, proto.getSocialDqSystem());

		mapParameters(p, proto);
		mapExchanges(p, proto);
		mapSocialAspects(p, proto);
		mapAllocationFactors(p, proto);
	}

	private ProcessDoc mapDoc(ProtoProcessDocumentation proto) {
		var doc = new ProcessDoc();
		doc.time = proto.getTimeDescription();
		doc.technology = proto.getTechnologyDescription();
		doc.dataCollectionPeriod = proto.getDataCollectionDescription();
		doc.dataCompleteness = proto.getCompletenessDescription();
		doc.dataSelection = proto.getDataSelectionDescription();
		doc.dataTreatment = proto.getDataTreatmentDescription();
		doc.inventoryMethod = proto.getInventoryMethodDescription();
		doc.modelingConstants = proto.getModelingConstantsDescription();
		doc.samplingProcedure = proto.getSamplingDescription();
		doc.accessRestrictions = proto.getRestrictionsDescription();
		doc.intendedApplication = proto.getIntendedApplication();
		doc.project = proto.getProjectDescription();
		doc.geography = proto.getGeographyDescription();
		doc.copyright = proto.getIsCopyrightProtected();
		doc.validFrom = Util.dateOf(proto.getValidFrom()).orElse(null);
		doc.validUntil = Util.dateOf(proto.getValidUntil()).orElse(null);
		doc.creationDate = Util.dateOf(proto.getCreationDate()).orElse(null);

		doc.dataDocumentor = Util.getActor(resolver, proto.getDataDocumentor());
		doc.dataGenerator = Util.getActor(resolver, proto.getDataGenerator());
		doc.dataOwner = Util.getActor(resolver, proto.getDataSetOwner());
		doc.publication = Util.getSource(resolver, proto.getPublication());

		mapReview(proto, doc);

		for (int i = 0; i < proto.getSourcesCount(); i++) {
			var ref = proto.getSources(i);
			var source = Util.getSource(resolver, ref);
			if (source != null) {
				doc.sources.add(source);
			}
		}
		return doc;
	}

	private void mapReview(ProtoProcessDocumentation proto, ProcessDoc doc) {
		var details = proto.getReviewDetails();
		var reviewer = Util.getActor(resolver, proto.getReviewer());
		if (Strings.nullOrEmpty(details) && reviewer == null)
			return;
		var review = new Review();
		review.details = details;
		if (reviewer != null) {
			review.reviewers.add(reviewer);
		}
		doc.reviews.add(review);
	}

	private void mapParameters(Process p, ProtoProcess proto) {
		p.parameters.clear();
		for (int i = 0; i < proto.getParametersCount(); i++) {
			var protoParam = proto.getParameters(i);
			var param = new Parameter();
			ParameterReader.mapFields(param, protoParam, resolver);
			param.scope = ParameterScope.PROCESS;
			p.parameters.add(param);
		}
	}

	private void mapExchanges(Process p, ProtoProcess proto) {

		// index the old exchanges, that we may update
		var oldIdx = new TIntObjectHashMap<Exchange>();
		for (var old : p.exchanges) {
			oldIdx.put(old.internalId, old);
		}
		p.quantitativeReference = null;
		p.exchanges.clear();

		p.lastInternalId = proto.getLastInternalId();
		for (int i = 0; i < proto.getExchangesCount(); i++) {
			var protoEx = proto.getExchanges(i);

			int internalId = protoEx.getInternalId();
			var e = oldIdx.get(internalId);
			if (e == null) {
				e = new Exchange();
				e.internalId = internalId;
			}
			exchanges.put(e.internalId, e);

			// provider
			if (protoEx.hasDefaultProvider()) {
				var providerId = protoEx.getDefaultProvider().getId();
				resolver.resolveProvider(providerId, e);
			} else {
				e.defaultProviderId = 0;
			}

			// flow and quantity
			e.flow = Util.getFlow(resolver, protoEx.getFlow());
			var quantity = Quantity.of(e.flow)
				.withProperty(protoEx.getFlowProperty())
				.withUnit(protoEx.getUnit())
				.get();
			e.flowPropertyFactor = quantity.factor();
			e.unit = quantity.unit();

			// general attributes
			e.isInput = protoEx.getIsInput();
			e.amount = protoEx.getAmount();
			e.formula = protoEx.getAmountFormula();
			e.isAvoided = protoEx.getIsAvoidedProduct();
			e.description = protoEx.getDescription();
			e.dqEntry = protoEx.getDqEntry();
			e.baseUncertainty = protoEx.getBaseUncertainty();
			e.uncertainty = Util.uncertaintyOf(protoEx.getUncertainty());
			e.location = Util.getLocation(resolver, protoEx.getLocation());

			// costs
			e.costFormula = protoEx.getCostFormula();
			if (protoEx.hasCostValue()) {
				e.costs = protoEx.getCostValue();
			}
			e.currency = Util.getCurrency(resolver, protoEx.getCurrency());

			p.exchanges.add(e);
			if (protoEx.getIsQuantitativeReference()) {
				p.quantitativeReference = e;
			}
		}
	}

	private void mapSocialAspects(Process p, ProtoProcess proto) {
		p.socialAspects.clear();
		for (int i = 0; i < proto.getSocialAspectsCount(); i++) {
			var protoAsp = proto.getSocialAspects(i);
			var a = new SocialAspect();
			a.indicator = Util.getSocialIndicator(
				resolver, protoAsp.getSocialIndicator());
			a.comment = protoAsp.getComment();
			a.quality = protoAsp.getQuality();
			a.rawAmount = protoAsp.getRawAmount();
			a.activityValue = protoAsp.getActivityValue();
			a.riskLevel = Util.riskLevelOf(protoAsp.getRiskLevel());
			a.source = Util.getSource(resolver, protoAsp.getSource());
			p.socialAspects.add(a);
		}
	}

	private void mapAllocationFactors(Process p, ProtoProcess proto) {
		p.allocationFactors.clear();
		for (int i = 0; i < proto.getAllocationFactorsCount(); i++) {
			var protoFac = proto.getAllocationFactors(i);
			var product = Util.getFlow(resolver, protoFac.getProduct());
			if (product == null)
				continue;

			var factor = new AllocationFactor();
			factor.productId = product.id;
			if (protoFac.hasExchange()) {
				int exchangeId = protoFac.getExchange().getInternalId();
				factor.exchange = exchanges.get(exchangeId);
			}
			factor.value = protoFac.getValue();
			factor.formula = Strings.nullIfEmpty(protoFac.getFormula());
			factor.method = Util.allocationMethodOf(protoFac.getAllocationType());
			p.allocationFactors.add(factor);
		}
	}
}

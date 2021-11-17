package org.openlca.proto.io.input;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Actor;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessDocumentation;
import org.openlca.core.model.RiskLevel;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;
import org.openlca.proto.ProtoAllocationFactor;
import org.openlca.proto.ProtoExchange;
import org.openlca.proto.ProtoProcess;
import org.openlca.proto.ProtoProcessDocumentation;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.ProtoRiskLevel;
import org.openlca.proto.ProtoSocialAspect;
import org.openlca.util.Strings;

class ProcessImport implements Import<Process> {

  private final ProtoImport imp;

  ProcessImport(ProtoImport imp) {
    this.imp = imp;
  }

  static ImportStatus<Process> of(ProtoImport imp, String id) {
    return new ProcessImport(imp).of(id);
  }

  @Override
  public ImportStatus<Process> of(String id) {
    var process = imp.get(Process.class, id);

    // check if we are in update mode
    var inUpdateMode = false;
    if (process != null) {
      inUpdateMode = imp.shouldUpdate(process);
      if(!inUpdateMode) {
        return ImportStatus.skipped(process);
      }
    }

    // resolve the proto object
    var proto = imp.reader.getProcess(id);
    if (proto == null)
      return process != null
        ? ImportStatus.skipped(process)
        : ImportStatus.error("Could not resolve Process " + id);

    var wrap = ProtoWrap.of(proto);
    if (inUpdateMode) {
      if (imp.skipUpdate(process, wrap))
        return ImportStatus.skipped(process);
    }

    // map the data
    if (process == null) {
      process = new Process();
      process.refId = id;
    }
    wrap.mapTo(process, imp);
    map(proto, process, inUpdateMode);

    // insert or update it
    var dao = new ProcessDao(imp.db);
    process = inUpdateMode
      ? dao.update(process)
      : dao.insert(process);
    imp.putHandled(process);
    return inUpdateMode
      ? ImportStatus.updated(process)
      : ImportStatus.created(process);
  }

  private void map(ProtoProcess proto, Process p, boolean inUpdateMode) {

    p.processType = In.processTypeOf(proto.getProcessType());
    p.infrastructureProcess = proto.getInfrastructureProcess();
    p.defaultAllocationMethod = In.allocationMethod(
      proto.getDefaultAllocationMethod());
    p.documentation = doc(proto.getProcessDocumentation());

    // location
    var locID = proto.getLocation().getId();
    if (Strings.notEmpty(locID)) {
      p.location = new LocationImport(imp).of(locID).model();
    }

    // DQ systems
    p.dqEntry = proto.getDqEntry();
    p.dqSystem = dqSystem(proto.getDqSystem());
    p.exchangeDqSystem = dqSystem(proto.getExchangeDqSystem());
    p.socialDqSystem = dqSystem(proto.getSocialDqSystem());

    // parameters
    p.parameters.clear();
    for (var protoParam : proto.getParametersList()) {
      var param = new Parameter();
      ProtoWrap.of(protoParam).mapTo(param, imp);
      ParameterImport.map(protoParam, param);
      p.parameters.add(param);
    }

    // social apsects
    p.socialAspects.clear();
    proto.getSocialAspectsList()
      .stream()
      .map(this::socialAspect)
      .forEach(p.socialAspects::add);

    // when we are in update mode, we want to keep the
    // IDs of existing exchanges because they are may
    // linked in product systems
    Map<Integer, Exchange> oldExchanges = null;
    if (inUpdateMode) {
      var m = new HashMap<Integer, Exchange>();
      p.exchanges.forEach(e -> {
        if (e.internalId != 0) {
          m.put(e.internalId, e);
        }
      });
      oldExchanges = m;
      p.exchanges.clear();
      p.quantitativeReference = null;
    }

    p.lastInternalId = proto.getLastInternalId();
    for (var protoEx : proto.getExchangesList()) {
      Exchange e = null;
      if (oldExchanges != null) {
        e = oldExchanges.get(protoEx.getInternalId());
      }
      if (e == null) {
        e = new Exchange();
        e.internalId = protoEx.getInternalId();
        if (e.internalId == 0) {
          p.lastInternalId++;
          e.internalId = p.lastInternalId;
        }
      }

      mapExchange(protoEx, e);
      if (protoEx.getQuantitativeReference()) {
        p.quantitativeReference = e;
      }

      // register a possible provider
      e.defaultProviderId = 0L;
      var providerID = protoEx.getDefaultProvider().getId();
      if (Strings.notEmpty(providerID)) {
        imp.providerQueue().add(providerID, e);
      }

      p.exchanges.add(e);
    }

    // allocation factors
    // note that creating the allocation factors must be
    // done after the exchanges where created because
    // we need to reference them in case of causal
    // allocation factors
    p.allocationFactors.clear();
    proto.getAllocationFactorsList()
      .stream()
      .map(a -> allocationFactor(a, p))
      .filter(Objects::nonNull)
      .forEach(p.allocationFactors::add);
  }

  private ProcessDocumentation doc(ProtoProcessDocumentation proto) {
    var doc = new ProcessDocumentation();

    // simple string fields
    doc.completeness = proto.getCompletenessDescription();
    doc.dataCollectionPeriod = proto.getDataCollectionDescription();
    doc.dataSelection = proto.getDataSelectionDescription();
    doc.dataTreatment = proto.getDataTreatmentDescription();
    doc.geography = proto.getGeographyDescription();
    doc.intendedApplication = proto.getIntendedApplication();
    doc.inventoryMethod = proto.getInventoryMethodDescription();
    doc.modelingConstants = proto.getModelingConstantsDescription();
    doc.project = proto.getProjectDescription();
    doc.restrictions = proto.getRestrictionsDescription();
    doc.reviewDetails = proto.getReviewDetails();
    doc.sampling = proto.getSamplingDescription();
    doc.technology = proto.getTechnologyDescription();
    doc.time = proto.getTimeDescription();

    // references
    doc.dataDocumentor = actor(proto.getDataDocumentor());
    doc.dataGenerator = actor(proto.getDataGenerator());
    doc.dataSetOwner = actor(proto.getDataSetOwner());
    doc.publication = source(proto.getPublication());
    doc.reviewer = actor(proto.getReviewer());
    for (var ref : proto.getSourcesList()) {
      var source = source(ref);
      if (source != null) {
        doc.sources.add(source);
      }
    }

    doc.copyright = proto.getCopyright();
    doc.creationDate = Json.parseDate(proto.getCreationDate());
    doc.validFrom = Json.parseDate(proto.getValidFrom());
    doc.validUntil = Json.parseDate(proto.getValidUntil());
    return doc;
  }

  private Actor actor(ProtoRef ref) {
    if (ref == null || Strings.nullOrEmpty(ref.getId()))
      return null;
    return new ActorImport(imp).of(ref.getId()).model();
  }

  private Source source(ProtoRef ref) {
    if (ref == null || Strings.nullOrEmpty(ref.getId()))
      return null;
    return new SourceImport(imp).of(ref.getId()).model();
  }

  private DQSystem dqSystem(ProtoRef ref) {
    if (ref == null || Strings.nullOrEmpty(ref.getId()))
      return null;
    return new DqSystemImport(imp).of(ref.getId()).model();
  }

  private void mapExchange(ProtoExchange proto, Exchange e) {
    e.isAvoided = proto.getAvoidedProduct();
    e.isInput = proto.getInput();
    e.baseUncertainty = proto.getBaseUncertainty() == 0
      ? null
      : proto.getBaseUncertainty();
    e.amount = proto.getAmount();
    e.formula = Strings.nullIfEmpty(proto.getAmountFormula());
    e.dqEntry = Strings.nullIfEmpty(proto.getDqEntry());
    e.description = Strings.nullIfEmpty(proto.getDescription());
    e.uncertainty = In.uncertainty(proto.getUncertainty());

    // costs
    e.costFormula = Strings.nullIfEmpty(proto.getCostFormula());
    e.costs = proto.getCostValue() == 0
      ? null
      : proto.getCostValue();
    var currencyID = proto.getCurrency().getId();
    if (Strings.notEmpty(currencyID)) {
      e.currency = new CurrencyImport(imp).of(currencyID).model();
    }

    // flow references
    var flowID = proto.getFlow().getId();
    e.flow = new FlowImport(imp).of(flowID).model();
    if (e.flow == null)
      return;

    var propID = proto.getFlowProperty().getId();
    if (Strings.nullOrEmpty(propID)) {
      e.flowPropertyFactor = e.flow.getReferenceFactor();
    } else {
      e.flowPropertyFactor = e.flow.flowPropertyFactors.stream()
        .filter(f -> f.flowProperty != null
          && Objects.equals(f.flowProperty.refId, propID))
        .findAny()
        .orElse(null);
    }

    if (e.flowPropertyFactor == null)
      return;
    var prop = e.flowPropertyFactor.flowProperty;
    if (prop == null || prop.unitGroup == null)
      return;
    var unitID = proto.getUnit().getId();
    if (Strings.nullOrEmpty(unitID)) {
      e.unit = prop.unitGroup.referenceUnit;
    } else {
      e.unit = prop.unitGroup.units.stream()
        .filter(u -> Objects.equals(u.refId, unitID))
        .findAny()
        .orElse(null);
    }
  }

  private SocialAspect socialAspect(ProtoSocialAspect proto) {
    var a = new SocialAspect();
    var indicatorID = proto.getSocialIndicator().getId();
    if (Strings.notEmpty(indicatorID)) {
      a.indicator = new SocialIndicatorImport(imp)
        .of(indicatorID)
        .model();
    }
    a.comment = Strings.nullIfEmpty(proto.getComment());
    a.quality = Strings.nullIfEmpty(proto.getQuality());
    a.rawAmount = Strings.nullIfEmpty(proto.getRawAmount());
    a.activityValue = proto.getActivityValue();
    a.riskLevel = riskLevel(proto.getRiskLevel());
    var sourceID = proto.getSource().getId();
    if (Strings.notEmpty(sourceID)) {
      a.source = new SourceImport(imp).of(sourceID).model();
    }
    return a;
  }

  private RiskLevel riskLevel(ProtoRiskLevel proto) {
    if (proto == null)
      return null;
    // !note: we could match the enums via reflection
    // as both have the same items but we do not do this
    // because the items may change in the future.
    return switch (proto) {
      case NO_OPPORTUNITY -> RiskLevel.NO_OPPORTUNITY;
      case HIGH_OPPORTUNITY -> RiskLevel.HIGH_OPPORTUNITY;
      case MEDIUM_OPPORTUNITY -> RiskLevel.MEDIUM_OPPORTUNITY;
      case LOW_OPPORTUNITY -> RiskLevel.LOW_OPPORTUNITY;
      case NO_RISK -> RiskLevel.NO_RISK;
      case VERY_LOW_RISK -> RiskLevel.VERY_LOW_RISK;
      case LOW_RISK -> RiskLevel.LOW_RISK;
      case MEDIUM_RISK -> RiskLevel.MEDIUM_RISK;
      case HIGH_RISK -> RiskLevel.HIGH_RISK;
      case VERY_HIGH_RISK -> RiskLevel.VERY_HIGH_RISK;
      case NO_DATA -> RiskLevel.NO_DATA;
      case NOT_APPLICABLE -> RiskLevel.NOT_APPLICABLE;
      default -> null;
    };
  }

  private AllocationFactor allocationFactor(
    ProtoAllocationFactor proto, Process process) {

    var f = new AllocationFactor();

    // find the ID of the allocated product
    var productID = proto.getProduct().getId();
    if (Strings.nullOrEmpty(productID))
      return null;
    f.productId = process.exchanges.stream()
      .filter(e -> e.flow != null
        && Objects.equals(e.flow.refId, productID))
      .mapToLong(e -> e.flow.id)
      .findAny()
      .orElse(0L);

    if (f.productId == 0L)
      return null;

    f.method = In.allocationMethod(proto.getAllocationType());
    f.value = proto.getValue();
    f.formula = Strings.nullIfEmpty(proto.getFormula());

    // find the related exchange in case of
    // a causal allocation factor
    if (f.method != AllocationMethod.CAUSAL)
      return f;
    var exchangeID = proto.getExchange().getInternalId();
    if (exchangeID <= 0)
      return null;
    f.exchange = process.exchanges.stream()
      .filter(e -> e.internalId == exchangeID)
      .findAny()
      .orElse(null);

    return f.exchange != null ? f : null;
  }

}

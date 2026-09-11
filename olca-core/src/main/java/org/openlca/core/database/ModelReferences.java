package org.openlca.core.database;

import java.io.Serial;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.openlca.commons.Strings;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProviderType;
import org.openlca.core.model.TypedRefId;
import org.openlca.util.TypedRefIdMap;

/**
 * Scans all database tables and collects model references and usages
 * (non-transitively)
 */
public class ModelReferences {

	private static final Set<ModelType> PROVIDER_TYPES = EnumSet.of(
		ModelType.PROCESS, ModelType.PRODUCT_SYSTEM, ModelType.RESULT);

	private final IDatabase database;
	private final TypedRefIdMap<IdAndLibrary> refIdToId = new TypedRefIdMap<>();
	private final EnumMap<ModelType, Map<Long, String>> idToRefId = new EnumMap<>(ModelType.class);
	private final ReferenceMap references = new ReferenceMap();
	private final ReferenceMap usages = new ReferenceMap();
	private final Map<String, Long> nameToParameter = new HashMap<>();

	private ModelReferences(IDatabase database) {
		this.database = database;
	}

	public static ModelReferences scan(IDatabase database) {
		var refs = new ModelReferences(database);
		refs.init();
		refs.scan();
		return refs;
	}

	public void iterateReferences(TypedRefId pair, Consumer<ModelReference> consumer) {
		iterateReferences(pair, ref -> {
			consumer.accept(ref);
			return true;
		});
	}

	public void iterateReferences(TypedRefId pair, Function<ModelReference, Boolean> consumer) {
		iterate(references, pair, consumer);
	}

	public void iterateUsages(TypedRefId pair, Consumer<ModelReference> consumer) {
		iterateUsages(pair, ref -> {
			consumer.accept(ref);
			return true;
		});
	}

	public void iterateUsages(TypedRefId pair, Function<ModelReference, Boolean> consumer) {
		iterate(usages, pair, consumer);
	}

	public String getLibrary(TypedRefId pair) {
		var idAndLib = refIdToId.get(pair);
		if (idAndLib == null)
			return null;
		return idAndLib.library;
	}

	private void iterate(ReferenceMap map, TypedRefId pair, Function<ModelReference, Boolean> consumer) {
		var typeMap = map.get(pair.type);
		if (typeMap == null)
			return;
		var idAndLib = refIdToId.get(pair);
		if (idAndLib == null)
			return;
		var idMap = typeMap.get(idAndLib.id);
		if (idMap == null)
			return;
		for (var type : idMap.keySet()) {
			for (var id : idMap.get(type)) {
				var refId = idToRefId.get(type).get(id);
				if (Strings.isBlank(refId))
					continue;
				var library = refIdToId.get(type, refId).library;
				if (!consumer.apply(new ModelReference(type, id, refId, library)))
					return;
			}
		}
	}

	private void init() {
		var query = "SELECT id, name FROM tbl_parameters WHERE scope = 'GLOBAL'";
		NativeSql.on(database).query(query, rs -> {
			nameToParameter.put(rs.getString(2), rs.getLong(1));
			return true;
		});
	}

	private void scan() {
		scanLocations();
		scanSources();
		scanActors();
		scanCurrencies();
		scanUnitGroups();
		scanFlowProperties();
		scanDQSystems();
		scanGlobalParameters();
		scanSocialIndicators();
		scanImpactCategories();
		scanImpactMethods();
		scanResults();
		scanEpds();
		scanFlows();
		scanProcesses();
		scanProductSystems();
		scanProjects();
	}

	private void scanLocations() {
		scanTable("tbl_locations", true,
			new ModelField(ModelType.LOCATION, "id"));
	}

	private void scanSources() {
		scanTable("tbl_sources", true,
			new ModelField(ModelType.SOURCE, "id"));
	}

	private void scanActors() {
		scanTable("tbl_actors", true,
			new ModelField(ModelType.ACTOR, "id"));
	}

	private void scanCurrencies() {
		scanTable("tbl_currencies", true,
			new ModelField(ModelType.CURRENCY, "id"),
			new ModelField(ModelType.CURRENCY, "f_reference_currency"));
	}

	private void scanUnitGroups() {
		scanTable("tbl_unit_groups", true,
			new ModelField(ModelType.UNIT_GROUP, "id"),
			new ModelField(ModelType.FLOW_PROPERTY, "f_default_flow_property"));
	}

	private void scanFlowProperties() {
		scanTable("tbl_flow_properties", true,
			new ModelField(ModelType.FLOW_PROPERTY, "id"),
			new ModelField(ModelType.UNIT_GROUP, "f_unit_group"));
	}

	private void scanDQSystems() {
		scanTable("tbl_dq_systems", true,
			new ModelField(ModelType.DQ_SYSTEM, "id"),
			new ModelField(ModelType.SOURCE, "f_source"));
	}

	private void scanGlobalParameters() {
		var query = "SELECT id, ref_id, library FROM tbl_parameters WHERE scope = 'GLOBAL'";
		NativeSql.on(database).query(query, rs -> {
			var id = rs.getLong(1);
			var refId = rs.getString(2);
			var lib = rs.getString(3);
			putRefId(ModelType.PARAMETER, id, refId, lib);
			return true;
		});
	}

	private void scanSocialIndicators() {
		scanTable("tbl_social_indicators", true,
			new ModelField(ModelType.SOCIAL_INDICATOR, "id"),
			new ModelField(ModelType.FLOW_PROPERTY, "f_activity_quantity"));
	}

	private void scanImpactCategories() {
		scanTable("tbl_impact_categories", true,
			new ModelField(ModelType.IMPACT_CATEGORY, "id"),
			new ModelField(ModelType.SOURCE, "f_source"));
		scanTable("tbl_impact_factors", false,
			new ModelField(ModelType.IMPACT_CATEGORY, "f_impact_category"),
			new ModelField(ModelType.FLOW, "f_flow"),
			new ModelField(ModelType.LOCATION, "f_location"));
	}

	private void scanImpactMethods() {
		scanTable("tbl_impact_methods", true,
			new ModelField(ModelType.IMPACT_METHOD, "id"),
			new ModelField(ModelType.SOURCE, "f_source"));
		scanTable("tbl_impact_links", false,
			new ModelField(ModelType.IMPACT_METHOD, "f_impact_method"),
			new ModelField(ModelType.IMPACT_CATEGORY, "f_impact_category"));
	}

	private void scanFlows() {
		scanTable("tbl_flows", true,
			new ModelField(ModelType.FLOW, "id"),
			new ModelField(ModelType.FLOW_PROPERTY, "f_reference_flow_property"),
			new ModelField(ModelType.LOCATION, "f_location"));
		scanTable("tbl_flow_property_factors", false,
			new ModelField(ModelType.FLOW, "f_flow"),
			new ModelField(ModelType.FLOW_PROPERTY, "f_flow_property"));
	}

	private void scanProcesses() {
		var docsToProcess = scanTable("tbl_processes", true, "f_process_doc",
			new ModelField(ModelType.PROCESS, "id"),
			new ModelField(ModelType.LOCATION, "f_location"),
			new ModelField(ModelType.DQ_SYSTEM, "f_dq_system"),
			new ModelField(ModelType.DQ_SYSTEM, "f_exchange_dq_system"),
			new ModelField(ModelType.DQ_SYSTEM, "f_social_dq_system"));
		scanTable("tbl_process_docs", false,
			new ModelField(ModelType.PROCESS, "id", docsToProcess::get),
			new ModelField(ModelType.ACTOR, "f_data_documentor"),
			new ModelField(ModelType.ACTOR, "f_data_generator"),
			new ModelField(ModelType.ACTOR, "f_data_owner"),
			new ModelField(ModelType.SOURCE, "f_publication"));
		scanTable("tbl_source_links", false,
			new ModelField(ModelType.PROCESS, "f_owner", docsToProcess::get),
			new ModelField(ModelType.SOURCE, "f_source"));
		scanTable("tbl_exchanges", false,
			new ModelField(ModelType.PROCESS, "f_owner"),
			new ModelField(
				new Condition("default_provider_type", this::getProviderType),
				"f_default_provider"),
			new ModelField(ModelType.FLOW, "f_flow"),
			new ModelField(ModelType.LOCATION, "f_location"),
			new ModelField(ModelType.CURRENCY, "f_currency"));
		scanTable("tbl_social_aspects", false,
			new ModelField(ModelType.PROCESS, "f_process"),
			new ModelField(ModelType.SOCIAL_INDICATOR, "f_indicator"),
			new ModelField(ModelType.SOURCE, "f_source"));
		scanTable("tbl_compliance_declarations", false,
			new ModelField(ModelType.PROCESS, "f_owner", docsToProcess::get),
			new ModelField(ModelType.SOURCE, "f_system"));

		// the reviewers of a review; the owner of a row in the actor links
		// is the ID of a review which we need to map to the process of the
		// related process documentation
		var reviewToDoc = scanTable("tbl_reviews", false, "id",
			new ModelField(ModelType.PROCESS, "f_owner", docsToProcess::get),
			new ModelField(ModelType.SOURCE, "f_report"));
		scanTable("tbl_actor_links", false,
			new ModelField(ModelType.PROCESS, "f_owner", reviewId -> {
				var docId = reviewToDoc.get(reviewId);
				return docId == null ? null : docsToProcess.get(docId);
			}),
			new ModelField(ModelType.ACTOR, "f_actor"));
	}

	private void scanProductSystems() {
		scanTable("tbl_product_systems", true,
			new ModelField(ModelType.PRODUCT_SYSTEM, "id"),
			new ModelField(ModelType.PROCESS, "f_reference_process"));
		scanTable("tbl_process_links", false,
			new ModelField(ModelType.PRODUCT_SYSTEM, "f_product_system"),
			new ModelField(ModelType.PROCESS, "f_process"),
			new ModelField(new Condition("provider_type", this::getProviderType), "f_provider"),
			new ModelField(ModelType.FLOW, "f_flow"));
		scanUnlinkedProviders();
		var setToSystem = scanTable("tbl_parameter_redef_sets", false, "id",
			new ModelField(ModelType.PRODUCT_SYSTEM, "f_product_system"));
		scanParameterRedefs(ModelType.PRODUCT_SYSTEM, setToSystem::get);
	}

	/// The providers of a product system are stored as plain IDs without type
	/// information in `tbl_product_system_processes`. Entries that are already
	/// registered through the process links (or the reference process) are
	/// skipped; for the others the type is resolved from the already scanned
	/// entities which _process_ in most cases and by default.
	private void scanUnlinkedProviders() {
		var sql = "SELECT f_product_system, f_process " +
			"FROM tbl_product_system_processes";
		NativeSql.on(database).query(sql, rs -> {
			var systemId = rs.getLong(1);
			var entityId = rs.getLong(2);
			if (!isProviderOf(systemId, entityId)) {
				var type = providerTypeOf(entityId);
				putRef(ModelType.PRODUCT_SYSTEM, systemId, type, entityId);
			}
			return true;
		});
	}

	/// Returns true if the entity with the given ID is already registered as
	/// a provider or reference process of the given product system.
	private boolean isProviderOf(long systemId, long entityId) {
		var systemIds = references.get(ModelType.PRODUCT_SYSTEM);
		if (systemIds == null)
			return false;
		var systemRefs = systemIds.get(systemId);
		if (systemRefs == null)
			return false;
		for (var type : PROVIDER_TYPES) {
			var ids = systemRefs.get(type);
			if (ids != null && ids.contains(entityId))
				return true;
		}
		return false;
	}

	/// The IDs in the provider collections of a product system can refer to
	/// processes, sub-systems, or results. Unknown IDs (e.g. of deleted
	/// entities) are mapped to `PROCESS` as this is the default case.
	private ModelType providerTypeOf(long entityId) {
		if (exists(ModelType.PRODUCT_SYSTEM, entityId))
			return ModelType.PRODUCT_SYSTEM;
		if (exists(ModelType.RESULT, entityId))
			return ModelType.RESULT;
		return ModelType.PROCESS;
	}

	private boolean exists(ModelType type, long id) {
		var ids = idToRefId.get(type);
		return ids != null && ids.containsKey(id);
	}

	private ModelType getProviderType(Object type) {
		return type instanceof Number num
			? ProviderType.toModelType(num.byteValue())
			: ModelType.PROCESS;
	}

	private void scanProjects() {
		scanTable("tbl_projects", true,
			new ModelField(ModelType.PROJECT, "id"),
			new ModelField(ModelType.IMPACT_METHOD, "f_impact_method"));
		var variantToProject = scanTable("tbl_project_variants", false, "id",
			new ModelField(ModelType.PROJECT, "f_project"),
			new ModelField(ModelType.PRODUCT_SYSTEM, "f_product_system"));
		scanParameterRedefs(ModelType.PROJECT, variantToProject::get);
	}

	private void scanEpds() {
		scanTable("tbl_epds", true,
			new ModelField(ModelType.EPD, "id"),
			new ModelField(ModelType.ACTOR, "f_manufacturer"),
			new ModelField(ModelType.ACTOR, "f_verifier"),
			new ModelField(ModelType.ACTOR, "f_program_operator"),
			new ModelField(ModelType.ACTOR, "f_data_generator"),
			new ModelField(ModelType.SOURCE, "f_pcr"),
			new ModelField(ModelType.SOURCE, "f_original_epd"),
			new ModelField(ModelType.LOCATION, "f_location"),
			new ModelField(ModelType.FLOW_PROPERTY, "f_flow_property"),
			new ModelField(ModelType.FLOW, "f_flow"));
		scanTable("tbl_epd_modules", false,
			new ModelField(ModelType.EPD, "f_epd"),
			new ModelField(ModelType.RESULT, "f_result"));
	}

	private void scanResults() {
		scanTable("tbl_results", true,
			new ModelField(ModelType.RESULT, "id"),
			new ModelField(ModelType.PRODUCT_SYSTEM, "f_product_system"),
			new ModelField(ModelType.IMPACT_METHOD, "f_impact_method"));
		scanTable("tbl_flow_results", false,
			new ModelField(ModelType.RESULT, "f_result"),
			new ModelField(ModelType.FLOW, "f_flow"),
			new ModelField(ModelType.LOCATION, "f_location"));
		scanTable("tbl_impact_results", false,
			new ModelField(ModelType.RESULT, "f_result"),
			new ModelField(ModelType.IMPACT_CATEGORY, "f_impact_category"));
	}

	private void scanParameterRedefs(ModelType ownerType, Function<Long, Long> mediator) {
		var query = "SELECT f_owner, name FROM tbl_parameter_redefs WHERE context_type IS NULL";
		NativeSql.on(database).query(query, rs -> {
			var ownerId = rs.getLong(1);
			var actualOwnerId = mediator.apply(ownerId);
			if (actualOwnerId == null)
				return true;
			var name = rs.getString(2);
			var parameterId = nameToParameter.get(name);
			if (parameterId == null)
				return true;
			putRef(ownerType, ownerId, ModelType.PARAMETER, parameterId);
			return true;
		});
	}

	private void scanTable(String table, boolean isRootEntity, ModelField source, ModelField... targets) {
		scanTable(table, isRootEntity, null, source, targets);
	}

	/// If `idField` is not `null`, `idField` is queried additionally and a map
	/// between the value of `source.field` and value of `idField` is returned,
	/// otherwise an empty map is returned.
	private Map<Long, Long> scanTable(
		String table, boolean isRootEntity, String idField, ModelField source,
		ModelField... targets
	) {
		var map = new HashMap<Long, Long>();
		query(table, isRootEntity, source, idField, targets, values -> {
			var col = 0;

			long sourceId = longOf(values[col++]);
			if (idField != null) {
				map.put(longOf(values[col++]), sourceId);
			}
			if (source.idMapper != null) {
				Long mapped = source.idMapper.apply(sourceId);
				if (mapped == null)
					return;
				sourceId = mapped;
			}

			if (targets == null)
				return;
			for (var target : targets) {
				long targetId = longOf(values[col++]);
				var conditionValue = target.condition != null
					? values[col++]
					: null;
				if (targetId == 0L)
					continue;
				if (target.idMapper != null) {
					Long mapped = target.idMapper.apply(targetId);
					if (mapped == null)
						continue;
					targetId = mapped;
				}
				var targetType = target.type;
				if (target.condition != null) {
					targetType = target.condition.typeMapper.apply(conditionValue);
				}
				putRef(source.type, sourceId, targetType, targetId);
			}
		});
		return map;
	}

	private long longOf(Object obj) {
		return obj instanceof Number num
			? num.longValue()
			: 0;
	}

	private void query(
		String table, boolean isRootEntity, ModelField sourceField, String idField,
		ModelField[] targets, ResultHandler handler
	) {
		var fields = new ArrayList<String>();
		var conditionIndices = new HashSet<Integer>();
		fields.add(sourceField.field);
		if (idField != null) {
			fields.add(idField);
		}
		if (targets != null) {
			var conditionIndex = fields.size();
			for (var target : targets) {
				fields.add(target.field);
				conditionIndex++;
				if (target.condition != null) {
					fields.add(target.condition.field);
					conditionIndices.add(conditionIndex++);
				}
			}
		}
		var query = "SELECT " + String.join(",", fields)
			+ (isRootEntity ? ",ref_id,library " : "")
			+ " FROM " + table;
		NativeSql.on(database).query(query, rs -> {
			var values = new Object[fields.size()];
			for (var i = 0; i < fields.size(); i++) {
				if (conditionIndices.contains(i)) {
					values[i] = rs.getObject(i + 1);
				} else {
					values[i] = rs.getLong(i + 1);
				}
			}
			var id = (long) values[0];
			if (isRootEntity) {
				var refId = rs.getString(fields.size() + 1);
				var lib = rs.getString(fields.size() + 2);
				putRefId(sourceField.type, id, refId, lib);
			}
			handler.handle(values);
			return true;
		});
	}

	private void putRef(ModelType sourceType, long sourceId, ModelType targetType, long targetId) {
		references.computeIfAbsent(sourceType, _ -> new HashMap<>())
			.computeIfAbsent(sourceId, _ -> new EnumMap<>(ModelType.class))
			.computeIfAbsent(targetType, _ -> new HashSet<>())
			.add(targetId);
		usages.computeIfAbsent(targetType, _ -> new HashMap<>())
			.computeIfAbsent(targetId, _ -> new EnumMap<>(ModelType.class))
			.computeIfAbsent(sourceType, _ -> new HashSet<>())
			.add(sourceId);
	}

	private void putRefId(ModelType type, long id, String refId, String library) {
		refIdToId.put(new TypedRefId(type, refId), new IdAndLibrary(id, library));
		idToRefId.computeIfAbsent(type, _ -> new HashMap<>()).put(id, refId);
	}

	private record ModelField(
		ModelType type,
		String field,
		Condition condition,
		Function<Long, Long> idMapper
	) {

		ModelField(ModelType type, String field) {
			this(type, field, null, null);
		}

		ModelField(Condition condition, String field) {
			this(null, field, condition, null);
		}

		ModelField(ModelType type, String field, Function<Long, Long> idMapper) {
			this(type, field, null, idMapper);
		}
	}

	private record Condition(
		String field, Function<Object, ModelType> typeMapper
	) {
	}

	public static class ModelReference extends TypedRefId {

		public final long id;
		public final String library;

		private ModelReference(ModelType type, long id, String refId, String library) {
			super(type, refId);
			this.id = id;
			this.library = library;
		}

	}

	private static class ReferenceMap extends EnumMap<ModelType, Map<Long, EnumMap<ModelType, Set<Long>>>> {

		@Serial
		private static final long serialVersionUID = 8651176950184800797L;

		public ReferenceMap() {
			super(ModelType.class);
		}

	}

	private interface ResultHandler {

		void handle(Object[] values);

	}

	private record IdAndLibrary(long id, String library) {
	}

}

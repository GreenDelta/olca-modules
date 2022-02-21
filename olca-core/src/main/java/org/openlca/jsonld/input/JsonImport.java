package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor.Mode;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.JsonStoreReader;
import org.openlca.jsonld.upgrades.Upgrades;

public class JsonImport implements Runnable {

	final JsonStoreReader reader;
	UpdateMode updateMode = UpdateMode.NEVER;
	private Consumer<RootEntity> callback;

	@Deprecated
	final Db db;
	private final ExchangeProviderQueue providers;
	private final Map<ModelType, Set<String>> visited = new HashMap<>();

	public JsonImport(JsonStoreReader reader, IDatabase db) {
		this.reader = Upgrades.chain(reader);
		this.db = new Db(db);
		this.providers = ExchangeProviderQueue.create(db);
	}

	public JsonImport setUpdateMode(UpdateMode updateMode) {
		this.updateMode = updateMode;
		return this;
	}

	public JsonImport setCallback(Consumer<RootEntity> callback) {
		this.callback = callback;
		return this;
	}

	void visited(ModelType type, String refId) {
		var set = visited.computeIfAbsent(type, k -> new HashSet<>());
		set.add(refId);
	}

	public ExchangeProviderQueue providers() {
		return providers;
	}

	void imported(RootEntity entity) {
		if (callback == null)
			return;
		callback.accept(entity);
	}

	boolean hasVisited(ModelType type, String refId) {
		Set<String> set = visited.get(type);
		return set != null && set.contains(refId);
	}

	public void run(ModelType type, String id) {
		if (type == null || id == null)
			return;
		switch (type) {
			case ACTOR -> ActorImport.run(id, this);
			case CATEGORY -> CategoryImport.run(id, this);
			case CURRENCY -> CurrencyImport.run(id, this);
			case DQ_SYSTEM -> DQSystemImport.run(id, this);
			case EPD -> EpdImport.run(id, this);
			case FLOW -> FlowImport.run(id, this);
			case FLOW_PROPERTY -> FlowPropertyImport.run(id, this);
			case IMPACT_CATEGORY -> ImpactCategoryImport.run(id, this);
			case IMPACT_METHOD -> ImpactMethodImport.run(id, this);
			case LOCATION -> LocationImport.run(id, this);
			case PARAMETER -> ParameterImport.run(id, this);
			case PROCESS -> ProcessImport.run(id, this);
			case PRODUCT_SYSTEM -> ProductSystemImport.run(id, this);
			case PROJECT -> ProjectImport.run(id, this);
			case RESULT -> ResultImport.run(id, this);
			case SOCIAL_INDICATOR -> SocialIndicatorImport.run(id, this);
			case SOURCE -> SourceImport.run(id, this);
			case UNIT_GROUP -> UnitGroupImport.run(id, this);
			default -> {
			}
		}
	}

	@Override
	public void run() {
		var typeOrder = new ModelType[]{
			ModelType.ACTOR,
			ModelType.CATEGORY,
			ModelType.CURRENCY,
			ModelType.DQ_SYSTEM,
			ModelType.EPD,
			ModelType.FLOW,
			ModelType.FLOW_PROPERTY,
			ModelType.IMPACT_CATEGORY,
			ModelType.IMPACT_METHOD,
			ModelType.LOCATION,
			ModelType.PARAMETER,
			ModelType.PROCESS,
			ModelType.PRODUCT_SYSTEM,
			ModelType.PROJECT,
			ModelType.RESULT,
			ModelType.SOCIAL_INDICATOR,
			ModelType.SOURCE,
			ModelType.UNIT_GROUP,
		};
		for (var type : typeOrder) {
			for (var id : reader.getRefIds(type)) {
				run(type, id);
			}
		}
	}


}

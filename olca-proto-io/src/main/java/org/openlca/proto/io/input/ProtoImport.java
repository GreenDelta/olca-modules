package org.openlca.proto.io.input;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RefEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.proto.io.ProtoReader;
import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.util.Strings;

public class ProtoImport implements Runnable {

  final ProtoReader reader;
  final IDatabase db;

	UpdateMode updateMode = UpdateMode.NEVER;

	private ExchangeProviderQueue providerQueue;
	private Consumer<ImportStatus<?>> statusHandler;

  /**
   * Contains mapped category IDs. When inserting or updating a
   * category into an openLCA database using the CategoryDao,
   * the ID is calculated from the path. This ID is then maybe
   * different than the ID that is used in the external data
   * store. We hold the mapping `store ID -> db ID` in this map.
   */
  final Map<String, String> mappedCategories = new HashMap<>();

  /**
   * Cache the database IDs of the inserted and updated entities. This
   * is used to avoid updating the same thing again and again and to
   * cache the database IDs for a bit faster retrieval from the database.
   */
  private final Map<Class<?>, Map<String, Long>> handled = new HashMap<>();

  public ProtoImport(ProtoReader reader, IDatabase db) {
    this.reader = reader;
    this.db = db;
  }

  public ProtoImport withUpdateMode(UpdateMode mode) {
    this.updateMode = mode;
    return this;
  }

  public ProtoImport withStatusHandler(Consumer<ImportStatus<?>> handler) {
    this.statusHandler = handler;
    return this;
  }

	public ExchangeProviderQueue providerQueue() {
		if (providerQueue == null) {
			providerQueue = ExchangeProviderQueue.create(db);
		}
		return providerQueue;
	}

	/**
   * Returns true if the given existing entity should be updated. If this is
   * not the case, we mark it as handled.
   */
  boolean shouldUpdate(RefEntity entity) {
    if (entity == null)
      return false;
    if (isHandled(entity))
      return false;
    if (updateMode == null || updateMode == UpdateMode.NEVER) {
      putHandled(entity);
      return false;
    }
    return true;
  }

  /**
   * Returns true if an update of an existing entity should be skipped. This is
   * true when we are not in update mode `always` and the version or last
   * change date of the incoming object are smaller. If true is returned, the
   * entity is also marked as handled so there is no need to call the
   * `putHandled` method again after this call.
   */
  boolean skipUpdate(RootEntity existing, ProtoWrap incoming) {
    if (existing == null)
      return true;
    if (updateMode == UpdateMode.ALWAYS)
      return false;

    // check the version
    long version = Strings.notEmpty(incoming.version())
      ? Version.fromString(incoming.version()).getValue()
      : 0;
    if (version > existing.version)
      return false;
    if (version < existing.version) {
      putHandled(existing);
      return true;
    }

    // equal version => check the date
    var date = Strings.notEmpty(incoming.lastChange())
      ? Json.parseDate(incoming.lastChange())
      : null;
    var lastChange = date != null
      ? date.getTime()
      : 0;
    if (lastChange <= existing.lastChange) {
      putHandled(existing);
      return true;
    }
    return false;
  }

  void putHandled(RefEntity e) {
    if (e == null || e.refId == null)
      return;
		if (e instanceof Process p) {
			providerQueue().pop(p);
		}
    var map = handled.computeIfAbsent(
      e.getClass(), c -> new HashMap<>());
    map.put(e.refId, e.id);
  }

  boolean isHandled(RefEntity e) {
    if (e == null || e.refId == null)
      return false;
    var map = handled.get(e.getClass());
    if (map == null)
      return false;
    var id = map.get(e.refId);
    return id != null;
  }

  /**
   * Get the entity with the given type and ID from the database
   * if it exists. It first checks the cache of imported (handled)
   * data sets if we have a fast ID for that entity. If not, it
   * does not update this cache and searches the database for
   * a matching ref. ID.
   */
  @SuppressWarnings("unchecked")
  <T extends RefEntity> T get(Class<T> type, String refID) {

    // try to use a cached ID first
    var map = handled.get(type);
    var id = map != null
      ? map.get(refID)
      : null;
    if (id != null)
      return Daos.base(db, type).getForId(id);

    // try to load it with the refID
    var dao = Daos.refDao(db, ModelType.forModelClass(type));
    return dao != null
      ? (T) dao.getForRefId(refID)
      : null;
  }

  @SuppressWarnings("unchecked")
  public <T extends RootEntity> Import<T> getImport(ModelType type) {
    if (type == null || !type.isRoot())
      return null;
    return getImport((Class<T>) type.getModelClass());
  }

  @SuppressWarnings("unchecked")
  public <T extends RootEntity> Import<T> getImport(Class<T> type) {
    // the comparisons are sorted by typical frequencies to minimize
    // comparisons
    // see https://gist.github.com/msrocka/b6a18064fbb76c8a8c3f1204839dd614

    if (type == Flow.class)
      return (Import<T>) new FlowImport(this);
    if (type == Process.class)
      return (Import<T>) new ProcessImport(this);
    if (type == Category.class)
      return (Import<T>) new CategoryImport(this);
    if (type == Location.class)
      return (Import<T>) new LocationImport(this);
    if (type == ImpactCategory.class)
      return (Import<T>) new ImpactCategoryImport(this);
    if (type == Actor.class)
      return (Import<T>) new ActorImport(this);
    if (type == Source.class)
      return (Import<T>) new SourceImport(this);
    if (type == Parameter.class)
      return (Import<T>) new ParameterImport(this);
    if (type == FlowProperty.class)
      return (Import<T>) new FlowPropertyImport(this);
    if (type == UnitGroup.class)
      return (Import<T>) new UnitGroupImport(this);
    if (type == Currency.class)
      return (Import<T>) new CurrencyImport(this);
    if (type == DQSystem.class)
      return (Import<T>) new DqSystemImport(this);
    if (type == ImpactMethod.class)
      return (Import<T>) new ImpactMethodImport(this);
    if (type == ProductSystem.class)
      return (Import<T>) new ProductSystemImport(this);
    if (type == Project.class)
      return (Import<T>) new ProjectImport(this);
    if (type == SocialIndicator.class)
      return (Import<T>) new SocialIndicatorImport(this);
    return null;
  }

  @Override
  public void run() {
    // the import order of the types in important here
    var types = new ModelType[]{
      ModelType.CATEGORY,
      ModelType.ACTOR,
      ModelType.SOURCE,
      ModelType.CURRENCY,
      ModelType.DQ_SYSTEM,
      ModelType.LOCATION,
      ModelType.UNIT_GROUP,
      ModelType.FLOW_PROPERTY,
      ModelType.FLOW,
      ModelType.PARAMETER,
      ModelType.SOCIAL_INDICATOR,
      ModelType.PROCESS,
      ModelType.IMPACT_CATEGORY,
      ModelType.IMPACT_METHOD,
      ModelType.PRODUCT_SYSTEM,
      ModelType.PROJECT,
    };
    for (var type : types) {
      var ids = reader.getIds(type);
      if (ids.isEmpty())
        continue;
      var imp = getImport(type);
      if (imp == null)
        continue;
      for (var id : reader.getIds(type)) {
        var status = imp.of(id);
        if (statusHandler != null) {
          statusHandler.accept(status);
        }
      }
    }
  }
}

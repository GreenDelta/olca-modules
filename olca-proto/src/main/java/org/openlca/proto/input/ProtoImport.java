package org.openlca.proto.input;

import java.util.HashMap;
import java.util.Map;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.proto.ProtoStore;
import org.openlca.util.Strings;

public class ProtoImport implements Runnable {

  final ProtoStore store;
  final IDatabase db;
  final ProviderUpdate providerUpdate;
  UpdateMode updateMode = UpdateMode.NEVER;

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

  public ProtoImport(ProtoStore store, IDatabase db) {
    this.store = store;
    this.db = db;
    this.providerUpdate = new ProviderUpdate(db);
  }

  public ProtoImport withUpdateMode(UpdateMode mode) {
    this.updateMode = mode;
    return this;
  }

  /**
   * Returns true if the given existing entity should be updated. If this is
   * not the case, we mark it as handled.
   */
  boolean shouldUpdate(RootEntity entity) {
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

  void putHandled(RootEntity e) {
    if (e == null || e.refId == null)
      return;
    var map = handled.computeIfAbsent(
      e.getClass(), c -> new HashMap<>());
    map.put(e.refId, e.id);
  }

  boolean isHandled(RootEntity e) {
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
  <T extends RootEntity> T get(Class<T> type, String refID) {

    // try to use a cached ID first
    var map = handled.get(type);
    var id = map != null
      ? map.get(refID)
      : null;
    if (id != null)
      return Daos.base(db, type).getForId(id);

    // try to load it with the refID
    var dao = Daos.root(db, ModelType.forModelClass(type));
    return (T) dao.getForRefId(refID);
  }

  @Override
  public void run() {
    for (String id : store.getIDs("categories")) {
      new CategoryImport(this).of(id);
    }
    for (String id : store.getIDs("actors")) {
      new ActorImport(this).of(id);
    }
    for (String id : store.getIDs("sources")) {
      new SourceImport(this).of(id);
    }
    for (String id : store.getIDs("locations")) {
      new LocationImport(this).of(id);
    }
    for (String id : store.getIDs("unit_groups")) {
      new UnitGroupImport(this).of(id);
    }
    for (String id : store.getIDs("flow_properties")) {
      new FlowPropertyImport(this).of(id);
    }
    for (String id : store.getIDs("flows")) {
      new FlowImport(this).of(id);
    }
    for (String id : store.getIDs("social_indicators")) {
      new SocialIndicatorImport(this).of(id);
    }
    for (String id : store.getIDs("currencies")) {
      new CurrencyImport(this).of(id);
    }
    for (String id : store.getIDs("parameters")) {
      new ParameterImport(this).of(id);
    }
    for (String id : store.getIDs("dq_systems")) {
      new DqSystemImport(this).of(id);
    }

    for (String id : store.getIDs("processes")) {
      new ProcessImport(this).of(id);
    }
    // it is important to call the provider update
    // when the processes have been imported or
    // updated
    providerUpdate.run();

    for (String id : store.getIDs("lcia_categories")) {
      new ImpactCategoryImport(this).of(id);
    }
    for (String id : store.getIDs("lcia_methods")) {
      new ImpactMethodImport(this).of(id);
    }
    for (String id : store.getIDs("product_systems")) {
      new ProductSystemImport(this).of(id);
    }
    for (String id : store.getIDs("projects")) {
      new ProjectImport(this).of(id);
    }
  }
}

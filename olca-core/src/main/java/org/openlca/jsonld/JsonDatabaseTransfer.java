package org.openlca.jsonld;

import jakarta.persistence.Table;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProductSystem;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;
import org.openlca.util.ProductSystems;
import org.openlca.util.Strings;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Transfers data sets from one database to another using JSON serialization.
 * This is like exporting data sets from a source database to a JSON package
 * and importing that package into the target database. A use case for this
 * would be for example, when a foreground system of the same background
 * database should be transferred, e.g. to a library based target database.
 * Note that this works without updates only, means it will never update an
 * already existing data set in the target database.
 */
public class JsonDatabaseTransfer {

	private final IDatabase source;
	private final Map<ModelType, Set<String>> transfers = new EnumMap<>(ModelType.class);
	private IDatabase target;

	private JsonDatabaseTransfer(IDatabase source) {
		this.source = Objects.requireNonNull(source);
	}

	public static JsonDatabaseTransfer from(IDatabase source) {
		return new JsonDatabaseTransfer(source);
	}

	public JsonDatabaseTransfer to(IDatabase target) {
		this.target = target;
		return this;
	}

	public JsonDatabaseTransfer add(ModelType type, String refId) {
		if (type == null || refId == null)
			return this;
		var ids = transfers.computeIfAbsent(type, $ -> new HashSet<>());
		ids.add(refId);
		return this;
	}

	public JsonDatabaseTransfer addAllOf(ModelType type) {
		if (type == null || type == ModelType.CATEGORY || target == null)
			return this;

		var table = type.getModelClass()
				.getAnnotation(Table.class)
				.name();
		var query = type == ModelType.PARAMETER
				? "select ref_id from " + table + " where scope = 'GLOBAL'"
				: "select ref_id from " + table;

		var existing = new HashSet<String>();
		NativeSql.on(target).query(query, r -> {
			var id = r.getString(1);
			if (Strings.isNotBlank(id)) {
				existing.add(id);
			}
			return true;
		});

		NativeSql.on(source).query(query, r -> {
			var id = r.getString(1);
			if (Strings.isNotBlank(id) && !existing.contains(id)) {
				add(type, id);
			}
			return true;
		});
		return this;
	}

	public JsonDatabaseTransfer addAll() {
		for (var type : ModelType.values()) {
			addAllOf(type);
		}
		return this;
	}

	public void run() {
		if (target == null)
			return;
		if (transfers.isEmpty()) {
			addAll();
		}
		checkParameters();

		var store = JsonDatabaseStore.of(source);
		var imp = new JsonImport(store, target)
				.setUpdateMode(UpdateMode.NEVER);

		for (var type : ModelType.values()) {
			var ids = transfers.get(type);
			if (ids == null)
				continue;
			var clazz = type.getModelClass();
			for (var id : ids) {
				var model = imp.get(clazz, id);

				// transferred systems may need to be cleaned up
				// (e.g. when transferring to a library based database)
				if (model instanceof ProductSystem system) {
					ProductSystems.tidy(system);
					target.update(system);
				}
			}
		}
	}

	private void checkParameters() {
		if (transfers.get(ModelType.PARAMETER) != null)
			return;
		// if these types are transferred, we also should
		// transfer global parameters
		var types = List.of(
				ModelType.PROJECT,
				ModelType.PRODUCT_SYSTEM,
				ModelType.PROCESS,
				ModelType.IMPACT_METHOD,
				ModelType.IMPACT_CATEGORY);
		boolean doTransfer = false;
		for (var type : types) {
			if (transfers.get(type) != null) {
				doTransfer = true;
				break;
			}
		}
		if (doTransfer) {
			addAllOf(ModelType.PARAMETER);
		}
	}
}

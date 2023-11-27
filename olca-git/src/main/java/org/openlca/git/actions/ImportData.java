package org.openlca.git.actions;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.git.model.Change;
import org.openlca.git.util.ProgressMonitor;
import org.openlca.jsonld.input.BatchImport;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;

class ImportData {

	private final GitStoreReader gitStore;
	private IDatabase database;
	private ProgressMonitor progressMonitor;

	private ImportData(GitStoreReader gitStore) {
		this.gitStore = gitStore;
	}

	static ImportData from(GitStoreReader gitStore) {
		return new ImportData(gitStore);
	}

	ImportData with(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
		return this;
	}

	ImportData into(IDatabase database) {
		this.database = database;
		return this;
	}

	static final ModelType[] TYPE_ORDER = new ModelType[] {
			ModelType.DQ_SYSTEM,
			ModelType.LOCATION,
			ModelType.ACTOR,
			ModelType.SOURCE,
			ModelType.PARAMETER,
			ModelType.UNIT_GROUP,
			ModelType.FLOW_PROPERTY,
			ModelType.CURRENCY,
			ModelType.FLOW,
			ModelType.IMPACT_CATEGORY,
			ModelType.IMPACT_METHOD,
			ModelType.SOCIAL_INDICATOR,
			ModelType.PROCESS,
			ModelType.PRODUCT_SYSTEM,
			ModelType.PROJECT,
			ModelType.RESULT,
			ModelType.EPD
	};

	List<Change> run() {
		var jsonImport = new JsonImport(gitStore, database);
		jsonImport.setUpdateMode(UpdateMode.ALWAYS);
		for (var type : ImportData.TYPE_ORDER) {
			var changes = gitStore.getChanges(type);
			if (changes.isEmpty())
				continue;
			progressMonitor.beginTask("Importing " + getLabel(type), changes.size());
			var batchSize = BatchImport.batchSizeOf(type);
			var batchImport = new BatchImport<>(jsonImport, type.getModelClass(), batchSize);
			for (var change : changes) {
				if (change == null) {
					progressMonitor.worked(1);
					continue;
				}
				progressMonitor.subTask(change.refId);
				if (change.isCategory) {
					jsonImport.getCategory(change.type, change.getCategoryPath());
				} else if (batchSize == 1) {
					jsonImport.run(type, change.refId);
				} else {
					batchImport.run(change.refId);
				}
				progressMonitor.worked(1);
			}
			if (batchSize != 1) {
				batchImport.close();
			}
		}
		return gitStore.resolvedConflicts;
	}

	private String getLabel(ModelType type) {
		if (type == ModelType.PROCESS)
			return "processes";
		if (type == ModelType.IMPACT_CATEGORY)
			return "impact categories";
		if (type == ModelType.FLOW_PROPERTY)
			return "flow properties";
		return type.name().toLowerCase().replace("_", " ") + "s";
	}

}

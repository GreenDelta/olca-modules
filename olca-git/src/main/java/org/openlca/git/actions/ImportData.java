package org.openlca.git.actions;

import org.openlca.core.database.IDatabase;
import org.openlca.core.library.Retagger;
import org.openlca.core.model.ModelType;
import org.openlca.git.actions.GitStoreReader.MergedDataImpl;
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

	MergedDataImpl run() {
		var jsonImport = new JsonImport(gitStore, database);
		jsonImport.setUpdateMode(UpdateMode.ALWAYS);
		progressMonitor.beginTask("Importing data sets", gitStore.size());
		for (var type : ImportData.TYPE_ORDER) {
			var changes = gitStore.getChanges(type);
			if (changes.isEmpty())
				continue;
			var batchSize = type == ModelType.UNIT_GROUP ? 1 : BatchImport.batchSizeOf(type);
			var batchImport = new BatchImport<>(jsonImport, type.getModelClass(), batchSize);
			for (var change : changes) {
				if (change == null) {
					progressMonitor.worked(1);
					continue;
				}
				progressMonitor.subTask(change);
				if (change.isCategory) {
					jsonImport.getCategory(change.type, change.getCategoryPath());
				} else if (batchSize == 1) {
					jsonImport.run(type, change.refId);
				} else {
					batchImport.run(change.refId);
				}
				progressMonitor.worked(1);
			}
			batchImport.close();
		}
		if (gitStore.dataPackage != null) {
			for (var type : gitStore.tag.types()) {
				Retagger.updateAllOf(database, type, gitStore.tag.refIds(), gitStore.dataPackage.name());
			}
		}
		return gitStore.mergedData;
	}

}

package org.openlca.git.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.Compatibility;
import org.openlca.git.GitIndex;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.actions.ImportResults.ImportState;
import org.openlca.git.find.Entries;
import org.openlca.git.find.References;
import org.openlca.git.model.Entry.EntryType;
import org.openlca.git.model.ModelRef;
import org.openlca.git.util.Descriptors;
import org.openlca.git.util.ProgressMonitor;
import org.openlca.jsonld.input.BatchImport;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;

class ImportHelper {

	private final Repository repo;
	final References references;
	final Entries entries;
	final IDatabase database;
	final GitIndex gitIndex;
	final ProgressMonitor progressMonitor;
	final Descriptors descriptors;
	ConflictResolver conflictResolver = ConflictResolver.NULL;

	ImportHelper(Repository repo, IDatabase database, Descriptors descriptors, GitIndex gitIndex,
			ProgressMonitor progressMonitor) {
		this.repo = repo;
		this.references = References.of(repo);
		this.entries = Entries.of(repo);
		this.database = database;
		this.gitIndex = gitIndex;
		this.progressMonitor = progressMonitor;
		this.descriptors = descriptors;
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

	private String getLabel(ModelType type) {
		if (type == ModelType.PROCESS)
			return "processes";
		if (type == ModelType.IMPACT_CATEGORY)
			return "impact categories";
		if (type == ModelType.FLOW_PROPERTY)
			return "flow properties";
		return type.name().toLowerCase().replace("_", " ") + "s";
	}

	void runImport(GitStoreReader gitStore) throws IOException {
		Compatibility.checkRepositoryClientVersion(repo);
		var jsonImport = new JsonImport(gitStore, database);
		jsonImport.setUpdateMode(UpdateMode.ALWAYS);
		for (var type : ImportHelper.TYPE_ORDER) {
			var changes = gitStore.getChanges(type);
			if (changes.isEmpty())
				continue;
			progressMonitor.subTask("Importing " + getLabel(type));
			var batchSize = BatchImport.batchSizeOf(type);
			var batchImport = new BatchImport<>(jsonImport, type.getModelClass(), batchSize);
			for (var change : changes) {
				if (change == null)
					continue;
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
	}

	void delete(List<? extends ModelRef> remoteDeletions) {
		remoteDeletions.stream()
				.filter(ref -> !ref.isCategory)
				.forEach(ref -> {
					progressMonitor.subTask("Deleting", ref);
					if (keepLocal(ref)) {
						remoteDeletions.remove(ref);
					} else if (!ref.isCategory) {
						delete(Daos.root(database, ref.type), ref.refId);
					}
					progressMonitor.worked(1);
				});
		var categoryDao = new CategoryDao(database);
		var deleted = new ArrayList<Category>();
		remoteDeletions.stream()
				.filter(ref -> ref.isCategory)
				.sorted(new CategoryDepthComparator())
				.forEach(ref -> {
					var category = categoryDao.getForPath(ref.type, ref.getCategoryPath());
					progressMonitor.subTask("Deleting category " + category.refId);
					if (isEmptyCategory(deleted, category)) {
						categoryDao.delete(category);
						deleted.add(category);
					}
					progressMonitor.worked(1);
				});
	}

	private boolean isEmptyCategory(List<Category> deleted, Category category) {
		// cant use cache, because elements might have been deleted before
		if (!category.childCategories.isEmpty())
			for (var child : category.childCategories)
				if (!deleted.contains(child))
					return false;
		var models = Daos.root(database, category.modelType).getDescriptors(Optional.ofNullable(category));
		if (!models.isEmpty())
			return false;
		return true;
	}

	private boolean keepLocal(ModelRef ref) {
		if (!conflictResolver.isConflict(ref))
			return false;
		var resolution = conflictResolver.resolveConflict(ref, null);
		return resolution.type == ConflictResolutionType.KEEP;
	}

	private <T extends RootEntity, V extends RootDescriptor> void delete(RootEntityDao<T, V> dao,
			String refId) {
		if (!dao.contains(refId))
			return;
		dao.delete(dao.getForRefId(refId));
	}

	void updateGitIndex(String commitId, ImportResults result, boolean applyStash) throws IOException {
		if (gitIndex == null)
			return;
		entries.iterate(commitId, entry -> {
			if (entry.typeOfEntry == EntryType.DATASET)
				return;
			gitIndex.put(entry.path, entry.objectId);
		});
		result.get(ImportState.UPDATED).forEach(ref -> {
			if (applyStash) {
				gitIndex.invalidate(ref.path);
			} else {
				var d = descriptors.get(ref.path);
				gitIndex.put(ref.path, d.version, d.lastChange, ref.objectId);
			}
		});
		result.get(ImportState.DELETED).forEach(ref -> gitIndex.remove(ref.path));
		gitIndex.putRoot(ObjectId.fromString(commitId));
		gitIndex.save();
	}

	private static class CategoryDepthComparator implements Comparator<ModelRef> {

		@Override
		public int compare(ModelRef c1, ModelRef c2) {
			return getDepth(c2) - getDepth(c1);
		}

		private int getDepth(ModelRef c) {
			var path = c.getCategoryPath();
			var depth = 0;
			while (path.contains("/")) {
				depth++;
				path = path.substring(0, path.lastIndexOf("/"));
			}
			return depth;
		}

	}

}

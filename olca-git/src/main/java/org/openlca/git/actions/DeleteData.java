package org.openlca.git.actions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.git.actions.ConflictResolver.ConflictResolutionType;
import org.openlca.git.model.Diff;
import org.openlca.git.model.Reference;
import org.openlca.git.util.ProgressMonitor;

class DeleteData {

	private final CategoryDao categoryDao;
	private final List<Category> deleted = new ArrayList<>();
	private final List<Diff> resolvedConflicts = new ArrayList<>();
	private final EnumMap<ModelType, RootEntityDao<?, ?>> daos = new EnumMap<>(ModelType.class);
	private List<Reference> categories;
	private List<Reference> models;
	private ProgressMonitor progressMonitor;
	private ConflictResolver conflictResolver = ConflictResolver.NULL;

	private DeleteData(IDatabase database) {
		for (var type : ModelType.values()) {
			daos.put(type, Daos.root(database, type));
		}
		this.categoryDao = (CategoryDao) daos.get(ModelType.CATEGORY);
	}

	static DeleteData from(IDatabase database) {
		return new DeleteData(database);
	}

	DeleteData with(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor != null ? progressMonitor : ProgressMonitor.NULL;
		return this;
	}

	DeleteData with(ConflictResolver conflictResolver) {
		this.conflictResolver = conflictResolver != null ? conflictResolver : ConflictResolver.NULL;
		return this;
	}

	DeleteData data(List<Reference> remoteDeletions) {
		this.models = remoteDeletions.stream()
				.filter(ref -> !ref.isCategory)
				.toList();
		this.categories = remoteDeletions.stream()
				.filter(ref -> ref.isCategory)
				.sorted(new CategoryDepthComparator())
				.toList();
		return this;
	}

	List<Diff> run() {
		progressMonitor.beginTask("Deleting data sets", models.size() + categories.size());
		models.stream().forEach(this::deleteModel);
		categories.stream().forEach(this::deleteCategory);
		return resolvedConflicts;
	}

	private void deleteModel(Reference ref) {
		progressMonitor.subTask(ref);
		if (!keepLocal(ref)) {
			delete(daos.get(ref.type), ref.refId);
		}
		progressMonitor.worked(1);
	}

	private void deleteCategory(Reference ref) {
		progressMonitor.subTask(ref);
		var category = categoryDao.getForPath(ref.type, ref.getCategoryPath());
		if (category == null) {
			progressMonitor.worked(1);
			return;
		}
		if (isEmptyCategory(deleted, category)) {
			categoryDao.delete(category);
			deleted.add(category);
		} else {
			resolvedConflicts.add(Diff.added(ref));
		}
		progressMonitor.worked(1);
	}

	private boolean isEmptyCategory(List<Category> deleted, Category category) {
		// can't use cache, because elements might have been deleted before
		if (!category.childCategories.isEmpty())
			for (var child : category.childCategories)
				if (!deleted.contains(child))
					return false;
		var models = daos.get(category.modelType).getDescriptors(Optional.ofNullable(category));
		if (!models.isEmpty())
			return false;
		return true;
	}

	private boolean keepLocal(Reference ref) {
		if (!conflictResolver.isConflict(ref))
			return false;
		var resolution = conflictResolver.resolveConflict(ref, null);
		if (resolution == null)
			throw new ConflictException(ref);
		if (resolution.type == ConflictResolutionType.OVERWRITE) {
			resolvedConflicts.add(Diff.deleted(ref));
		}
		return resolution.type == ConflictResolutionType.KEEP;
	}

	private <T extends RootEntity, V extends RootDescriptor> void delete(RootEntityDao<T, V> dao,
			String refId) {
		if (!dao.contains(refId))
			return;
		dao.delete(dao.getForRefId(refId));
	}

	private static class CategoryDepthComparator implements Comparator<Reference> {

		@Override
		public int compare(Reference c1, Reference c2) {
			return getDepth(c2) - getDepth(c1);
		}

		private int getDepth(Reference c) {
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

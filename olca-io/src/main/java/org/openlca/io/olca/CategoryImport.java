package org.openlca.io.olca;

import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CategoryImport implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private CategoryDao sourceDao;
	private CategoryDao destDao;
	private Sequence seq;

	public CategoryImport(IDatabase source, IDatabase destination, Sequence seq) {
		this.sourceDao = new CategoryDao(source);
		this.destDao = new CategoryDao(destination);
		this.seq = seq;
	}

	@Override
	public void run() {
		ModelType[] rootTypes = { ModelType.PROJECT, ModelType.PRODUCT_SYSTEM,
				ModelType.IMPACT_METHOD, ModelType.PROCESS, ModelType.FLOW,
				ModelType.FLOW_PROPERTY, ModelType.UNIT_GROUP,
				ModelType.SOURCE, ModelType.ACTOR };
		for (ModelType type : rootTypes) {
			synchCategories(type);
		}
	}

	private void synchCategories(ModelType type) {
		log.trace("synch categories for type {}", type);
		List<Category> sourceRoots = sourceDao.getRootCategories(type);
		List<Category> destRoots = destDao.getRootCategories(type);
		for (Category sourceRoot : sourceRoots) {
			Category destRoot = find(sourceRoot, destRoots);
			if (destRoot != null) {
				synchCategories(sourceRoot, destRoot);
				destDao.update(destRoot);
			} else {
				destRoot = copy(sourceRoot);
				synchCategories(sourceRoot, destRoot);
				destDao.insert(destRoot);
			}
			index(destRoot);
		}
	}

	private void synchCategories(Category srcCat, Category destCat) {
		for (Category srcChild : srcCat.getChildCategories()) {
			Category destChild = find(srcChild, destCat.getChildCategories());
			if (destChild == null) {
				destChild = copy(srcChild);
				destCat.getChildCategories().add(destChild);
				destChild.setParentCategory(destCat);
			}
			synchCategories(srcChild, destChild);
		}
	}

	private Category find(Category srcCat, List<Category> destCategories) {
		for (Category destCat : destCategories) {
			if (Strings.nullOrEqual(srcCat.getName(), destCat.getName())) {
				return destCat;
			}
		}
		return null;
	}

	/**
	 * Creates a flat copy of the given category. Note that the clone function
	 * in the category class creates a deep copy.
	 */
	private Category copy(Category srcCat) {
		Category copy = new Category();
		copy.setDescription(srcCat.getDescription());
		copy.setName(srcCat.getName());
		copy.setRefId(srcCat.getRefId());
		copy.setModelType(srcCat.getModelType());
		return copy;
	}

	private void index(Category destRoot) {
		seq.put(seq.CATEGORY, destRoot.getRefId(), destRoot.getId());
		for (Category destChild : destRoot.getChildCategories()) {
			index(destChild);
		}
	}

}

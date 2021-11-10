package org.openlca.io.ecospold2.input;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.io.ecospold2.input.IsicTree.IsicNode;
import org.openlca.util.KeyGen;

/**
 * Adds ISIC top-level categories to existing ISIC categories. In ecoinvent 3
 * data sets are provided with ISIC categories but only on a flat level (no
 * parent categories are added). This class adds such ISIC parent categories to
 * the category tree.
 */
public class IsicCategoryTreeSync implements Runnable {

	private final CategoryDao dao;
	private final ModelType type;

	public IsicCategoryTreeSync(IDatabase db, ModelType type) {
		this.dao = new CategoryDao(db);
		this.type = type;
	}

	@Override
	public void run() {
		IsicTree isicTree = IsicTree.fromFile(
				getClass().getResourceAsStream(
				"isic_codes_rev4.txt"));
		List<Category> roots = dao.getRootCategories(type);
		List<IsicNode> assignedNodes = new ArrayList<>();
		for (Category root : roots) {
			IsicNode node = findNode(root, isicTree);
			if (node == null)
				continue;
			node.category = root;
			assignedNodes.add(node);
		}
		for (IsicNode assignedNode : assignedNodes)
			syncPath(assignedNode);
		for (IsicNode root : isicTree.getRoots())
			syncWithDatabase(root);
	}

	private void syncWithDatabase(IsicNode root) {
		Category category = root.category;
		if (category == null)
			return;
		if (category.id == 0L) {
			category = dao.insert(category);
			root.category = category;
		}
		for (IsicNode childNode : root.childs) {
			if (childNode.category != null) {
				syncWithDatabase(childNode);
				category.childCategories.add(childNode.category);
				childNode.category.category = category;
			}
		}
		category = dao.update(category);
		root.category = category;
	}

	private void syncPath(IsicNode node) {
		if (node.parent == null)
			return;
		IsicNode parent = node.parent;
		while (parent != null) {
			Category parentCategory = parent.category;
			if (parentCategory == null) {
				parentCategory = createCategory(parent);
				parent.category = parentCategory;
			}
			parent = parent.parent;
		}
	}

	/**
	 * Finds the ISIC node for the given category.
	 */
	private IsicNode findNode(Category category, IsicTree isicTree) {
		if (!category.name.contains(":"))
			return null;
		String code = category.name.split(":")[0];
		return isicTree.findNode(code);
	}

	private Category createCategory(IsicNode node) {
		Category c = new Category();
		c.modelType = type;
		c.name = node.code + ":" + node.name;
		c.refId = KeyGen.get(type.name() + "/" + c.name);
		return c;
	}

}

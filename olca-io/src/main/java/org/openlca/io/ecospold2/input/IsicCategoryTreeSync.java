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

	private CategoryDao dao;
	private ModelType type;

	public IsicCategoryTreeSync(IDatabase database, ModelType type) {
		this.dao = new CategoryDao(database);
		this.type = type;
	}

	@Override
	public void run() {
		IsicTree isicTree = IsicTree.fromFile(getClass().getResourceAsStream(
				"isic_codes_rev4.txt"));
		List<Category> roots = dao.getRootCategories(type);
		List<IsicNode> assignedNodes = new ArrayList<>();
		for (Category root : roots) {
			IsicNode node = findNode(root, isicTree);
			if (node == null)
				continue;
			node.setCategory(root);
			assignedNodes.add(node);
		}
		for (IsicNode assignedNode : assignedNodes)
			syncPath(assignedNode);
		for (IsicNode root : isicTree.getRoots())
			syncWithDatabase(root);
	}

	private void syncWithDatabase(IsicNode root) {
		Category category = root.getCategory();
		if (category == null)
			return;
		if (category.getId() == 0L) {
			category = dao.insert(category);
			root.setCategory(category);
		}
		for (IsicNode childNode : root.getChilds()) {
			if (childNode.getCategory() != null) {
				syncWithDatabase(childNode);
				category.getChildCategories().add(childNode.getCategory());
				childNode.getCategory().setCategory(category);
			}
		}
		category = dao.update(category);
		root.setCategory(category);
	}

	private void syncPath(IsicNode node) {
		if (node.getParent() == null)
			return;
		IsicNode parent = node.getParent();
		while (parent != null) {
			Category parentCategory = parent.getCategory();
			if (parentCategory == null) {
				parentCategory = createCategory(parent);
				parent.setCategory(parentCategory);
			}
			parent = parent.getParent();
		}
	}

	/**
	 * Finds the ISIC node for the given category.
	 */
	private IsicNode findNode(Category category, IsicTree isicTree) {
		if (!category.getName().contains(":"))
			return null;
		String code = category.getName().split(":")[0];
		return isicTree.findNode(code);
	}

	private Category createCategory(IsicNode node) {
		Category c = new Category();
		c.setModelType(type);
		c.setName(node.getCode() + ":" + node.getName());
		c.setRefId(KeyGen.get(type.name() + "/" + c.getName()));
		return c;
	}

}

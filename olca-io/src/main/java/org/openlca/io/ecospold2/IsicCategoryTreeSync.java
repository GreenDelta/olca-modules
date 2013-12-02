package org.openlca.io.ecospold2;

import java.util.List;
import java.util.Stack;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.io.ecospold2.IsicTree.IsicNode;

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
		for (Category root : roots) {
			IsicNode node = findNode(root, isicTree);
			if (node == null || node.getCategory() != null)
				continue;
			syncPath(node, root);
		}
	}

	private void syncPath(IsicNode node, Category oldRoot) {
		node.setCategory(oldRoot);
		if (node.getParent() == null)
			return;
		Stack<IsicNode> stack = new Stack<>();
		IsicNode root = node;
		while (root.getParent() != null) {
			stack.push(root.getParent());
			root = root.getParent();
		}
		createCategories(stack);
		Category newParentCategory = node.getParent().getCategory();
		oldRoot.setParentCategory(newParentCategory);
		newParentCategory.getChildCategories().add(oldRoot);
		dao.update(root.getCategory());
	}

	private void createCategories(Stack<IsicNode> stack) {
		while (!stack.isEmpty()) {
			IsicNode n = stack.pop();
			if (n.getCategory() != null)
				continue;
			Category category = toCategory(n);
			dao.insert(category);
			n.setCategory(category);
			if (n.getParent() != null) {
				category.setParentCategory(n.getParent().getCategory());
				n.getParent().getCategory().getChildCategories().add(category);
				dao.update(n.getParent().getCategory());
			}
		}
	}

	/** Finds the ISIC node for the given category. */
	private IsicNode findNode(Category category, IsicTree isicTree) {
		if (!category.getName().contains(":"))
			return null;
		String code = category.getName().split(":")[0];
		return isicTree.findNode(code);
	}

	private Category toCategory(IsicNode node) {
		Category category = new Category();
		category.setModelType(type);
		category.setName(node.getCode() + ":" + node.getName());
		category.setRefId(org.openlca.io.KeyGen.get(category.getName()));
		return category;
	}
}

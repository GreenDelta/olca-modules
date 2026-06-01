package org.openlca.io.ecospold1.output;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.TreeSet;

import org.openlca.commons.Strings;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.ecospold.model.Category;
import org.openlca.ecospold.model.CategoryDocument;
import org.openlca.ecospold.model.SubCategory;

import jakarta.xml.bind.JAXB;

class CategoryFile implements Closeable {

	private final File file;
	private final CategoryDocument doc;
	private final TreeSet<Long> handled = new TreeSet<>();

	public CategoryFile(File file) {
		this.file = file;
		this.doc = new CategoryDocument();
	}

	public void addCategoriesOf(Process process) {
		if (process == null)
			return;
		for (var exchange : process.exchanges) {
			var flow = exchange.flow;
			int type = getType(flow);
			if (type == -1 || flow.category == null)
				continue;
			var category = flow.category;
			long id = idOf(category);
			if (handled.contains(id))
				continue;
			add(convert(category, type));
			handled.add(id);
		}
	}

	private long idOf(org.openlca.core.model.Category c) {
		if (c == null) return 0;
		return c.id != 0 ? c.id : System.identityHashCode(c);
	}

	private void add(Category category) {
		for (var existing : doc.getCategories()) {
			if (!equal(category, existing))
				continue;
			for (var sub : category.getSubCategories()) {
				addSub(existing, sub);
			}
			return;
		}
		doc.getCategories().add(category);
	}

	private void addSub(Category parent, SubCategory subCategory) {
		for (var sub : parent.getSubCategories()) {
			if (equal(subCategory, sub)) {
				return;
			}
		}
		parent.getSubCategories().add(subCategory);
	}

	private Category convert(
		org.openlca.core.model.Category category, int type) {
		var cat = new Category();
		cat.setType(type);
		var parent = category.category;
		if (parent == null) {
			cat.setName(category.name);
			cat.setLocalName(category.name);
		} else {
			cat.setName(parent.name);
			cat.setLocalName(parent.name);
			var sub = new SubCategory();
			cat.getSubCategories().add(sub);
			sub.setName(category.name);
			sub.setLocalName(category.name);
		}
		return cat;
	}

	private int getType(Flow flow) {
		if (flow == null) return -1;
		FlowType flowType = flow.flowType;
		if (flowType == null) return -1;
		return flowType == FlowType.ELEMENTARY_FLOW ? 1 : 0;
	}

	private boolean equal(Category a, Category b) {
		if (a == b) return true;
		if (a == null || b == null) return false;
		return a.getType() == b.getType()
			&& Objects.equals(a.getName(), b.getName());
	}

	private boolean equal(SubCategory a, SubCategory b) {
		if (a == b) return true;
		if (a == null || b == null) return false;
		return Objects.equals(a.getName(), b.getName());
	}

	/// Closing the file will write it with sorted categories.
	@Override
	public void close() throws IOException {
		doc.getCategories().sort((a, b) -> {
			if (a == null || b == null)
				return 0;
			return a.getType() != b.getType()
				? a.getType() - b.getType()
				: Strings.compareIgnoreCase(a.getName(), b.getName());
		});
		JAXB.marshal(doc, file);
	}
}

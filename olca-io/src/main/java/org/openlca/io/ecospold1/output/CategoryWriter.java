package org.openlca.io.ecospold1.output;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeSet;

import javax.xml.bind.JAXB;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.ecospold.Category;
import org.openlca.ecospold.CategoryDocument;
import org.openlca.ecospold.SubCategory;
import org.openlca.util.Strings;

class CategoryWriter implements Closeable {

	private File file;
	private CategoryDocument doc;
	private TreeSet<Long> handled = new TreeSet<>();

	public CategoryWriter(File file, ExportConfig config) {
		this.file = file;
		this.doc = new CategoryDocument();
	}

	public void takeFrom(Process process) {
		if (process == null)
			return;
		for (Exchange exchange : process.getExchanges()) {
			Flow flow = exchange.flow;
			int type = getType(flow);
			if (flow == null || type == -1)
				continue;
			org.openlca.core.model.Category category = flow.getCategory();
			if (category == null || handled.contains(category.getId()))
				continue;
			register(convert(category, type));
			handled.add(category.getId());
		}
	}

	private void register(Category category) {
		boolean found = false;
		for (Category existing : doc.getCategories()) {
			if (!equal(category, existing))
				continue;
			found = true;
			if (category.getSubCategories().isEmpty())
				continue;
			SubCategory sub = category.getSubCategories().get(0);
			register(sub, existing);
		}
		if (!found)
			doc.getCategories().add(category);
	}

	private void register(SubCategory subCategory, Category existing) {
		boolean found = false;
		for (SubCategory existingSub : existing.getSubCategories()) {
			if (equal(subCategory, existingSub)) {
				found = true;
				break;
			}
		}
		if (!found)
			existing.getSubCategories().add(subCategory);
	}

	private Category convert(
			org.openlca.core.model.Category category, int type) {
		Category cat = new Category();
		cat.setType(type);
		if (category.getCategory() == null) {
			cat.setName(category.getName());
			cat.setLocalName(category.getName());
		} else {
			org.openlca.core.model.Category parent = category
					.getCategory();
			cat.setName(parent.getName());
			cat.setLocalName(parent.getName());
			SubCategory sub = new SubCategory();
			cat.getSubCategories().add(sub);
			sub.setName(category.getName());
			sub.setLocalName(category.getName());
		}
		return cat;
	}

	private int getType(Flow flow) {
		if (flow == null)
			return -1;
		FlowType flowType = flow.getFlowType();
		if (flowType == null)
			return -1;
		if (flowType == FlowType.ELEMENTARY_FLOW)
			return 1;
		else
			return 0;
	}

	private boolean equal(Category c1, Category c2) {
		if (c1 == c2)
			return true;
		if (c1 == null || c2 == null)
			return false;
		return c1.getType() == c2.getType()
				&& Objects.equals(c1.getName(), c2.getName());
	}

	private boolean equal(SubCategory c1, SubCategory c2) {
		if (c1 == c2)
			return true;
		if (c1 == null || c2 == null)
			return false;
		return Objects.equals(c1.getName(), c2.getName());
	}

	/**
	 * Finally writes the category file.
	 *
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		Collections.sort(doc.getCategories(), new Comparator<Category>() {
			@Override
			public int compare(Category o1, Category o2) {
				if (o1 == null || o2 == null)
					return 0;
				if (o1.getType() != o2.getType())
					return o1.getType() - o2.getType();
				else
					return Strings.compare(o1.getName(), o2.getName());
			}
		});
		JAXB.marshal(doc, file);
	}

}

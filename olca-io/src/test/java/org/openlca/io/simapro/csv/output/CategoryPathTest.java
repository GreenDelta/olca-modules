package org.openlca.io.simapro.csv.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.io.Tests;
import org.openlca.simapro.csv.enums.ProcessCategory;

public class CategoryPathTest {

	@Test
	public void testDefault() {
		assertTrue(isDefault(CategoryPath.of(noTypeInfer(), new Process())));
		assertTrue(isDefault(CategoryPath.of(noTypeInfer(), new Flow())));
		var category = category("Material");
		var flow = new Flow();
		flow.category = category;
		var process = new Process();
		process.category = category;
		assertTrue(isDefault(CategoryPath.of(typeInfer(), flow)));
		assertTrue(isDefault(CategoryPath.of(typeInfer(), process)));
	}

	@Test
	public void testTypeInfer() {
		var flow = new Flow();
		flow.category = category("energy", "some", "sub", "folder");
		var process = new Process();
		process.quantitativeReference = process.output(flow, 1);
		var path = CategoryPath.of(typeInfer(), process);
		assertEquals(ProcessCategory.ENERGY, path.type());
		assertEquals("some\\sub\\folder", path.path());
	}

	@Test
	public void testNoTypeInfer() {
		var flow = new Flow();
		flow.category = category("energy", "some", "sub", "folder");
		var path = CategoryPath.of(noTypeInfer(), flow);
		assertEquals(ProcessCategory.MATERIAL, path.type());
		assertEquals("energy\\some\\sub\\folder", path.path());
	}

	@Test
	public void testNoMatchingType() {
		var process = new Process();
		process.category = category("electricity", "some", "sub", "folder");
		var path = CategoryPath.of(typeInfer(), process);
		assertEquals(ProcessCategory.MATERIAL, path.type());
		assertEquals("electricity\\some\\sub\\folder", path.path());
	}

	private Category category(String... segments) {
		Category c = null;
		for (var segment : segments) {
			c = c == null
					? Category.of(segment, ModelType.FLOW)
					: Category.childOf(c, segment);
		}
		return c;
	}

	private SimaProExport noTypeInfer() {
		return SimaProExport.of(Tests.getDb(), List.of())
				.withTopCategoryAsType(false);
	}

	private SimaProExport typeInfer() {
		return SimaProExport.of(Tests.getDb(), List.of())
				.withTopCategoryAsType(true);
	}

	private boolean isDefault(CategoryPath path) {
		return path.type() == ProcessCategory.MATERIAL
				&& path.path().equals("Other");
	}

}

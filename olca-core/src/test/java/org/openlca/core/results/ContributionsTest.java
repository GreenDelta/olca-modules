package org.openlca.core.results;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.Location;

public class ContributionsTest {

	@Test
	public void testSetNullAsRest() {
		Location loc = new Location();
		Set<Location> items = new HashSet<>();
		items.add(loc);
		items.add(null);
		List<Contribution<Location>> set = Contributions.calculate(
				items, 2, l -> {
					return 1;
				});
		Contribution<?> item = Contributions.get(set, null);
		Assert.assertNull(item.item);
		Assert.assertEquals(1, item.amount, 1e-27);
		Assert.assertEquals(0.5, item.share, 1e-27);
		Assert.assertTrue(item.isRest);
	}

	@Test
	public void testSort() {
		List<Contribution<String>> items = createSampleItems();
		Contributions.sortAscending(items);
		checkAmounts(items, 0, 1, 1, 2, 2);
		Contributions.sortDescending(items);
		checkAmounts(items, 2, 2, 1, 1, 0);
	}

	@Test
	public void testTopWithRest() {
		List<Contribution<String>> rawItems = createSampleItems();
		List<Contribution<String>> items = Contributions
				.topWithRest(rawItems, 6);
		checkAmounts(items, 2, 2, 1, 1, 0);
		items = Contributions.topWithRest(rawItems, 3);
		checkAmounts(items, 2, 2, 2);
		Assert.assertEquals(true, items.get(2).isRest);
		items = Contributions.topWithRest(rawItems, 1);
		checkAmounts(items, 6);
		Assert.assertEquals(true, items.get(0).isRest);
	}

	private <T> void checkAmounts(List<Contribution<T>> items,
			double... values) {
		Assert.assertEquals(values.length, items.size());
		for (int i = 0; i < items.size(); i++)
			Assert.assertEquals(values[i], items.get(i).amount, 1e-16);
	}

	private List<Contribution<String>> createSampleItems() {
		List<Contribution<String>> items = new ArrayList<>();
		for (int i = 1; i < 6; i++) {
			Contribution<String> item = new Contribution<>();
			item.amount = i % 3;
			item.item = "item_" + i;
			items.add(item);
		}
		return items;
	}

}

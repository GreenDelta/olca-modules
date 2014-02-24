package org.openlca.core.results;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.model.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContributionsTest {

	@Test
	public void testSetNullAsRest() {
		Location loc = new Location();
		Set<Location> items = new HashSet<>();
		items.add(loc);
		items.add(null);
		ContributionSet<Location> set = Contributions.calculate(items, 2,
				new Contributions.Function<Location>() {
					@Override
					public double value(Location t) {
						return 1;
					}
				});
		ContributionItem<?> item = set.getContribution(null);
		Assert.assertNull(item.getItem());
		Assert.assertEquals(1, item.getAmount(), 1e-27);
		Assert.assertEquals(0.5, item.getShare(), 1e-27);
		Assert.assertTrue(item.isRest());
	}

	@Test
	public void testSort() {
		List<ContributionItem<String>> items = createSampleItems();
		Contributions.sortAscending(items);
		checkAmounts(items, 0, 1, 1, 2, 2);
		Contributions.sortDescending(items);
		checkAmounts(items, 2, 2, 1, 1, 0);
	}

	@Test
	public void testTopWithRest() {
		List<ContributionItem<String>> rawItems = createSampleItems();
		List<ContributionItem<String>> items = Contributions.topWithRest(rawItems, 6);
		checkAmounts(items, 2, 2, 1, 1, 0);
		items = Contributions.topWithRest(rawItems, 3);
		checkAmounts(items, 2, 2, 2);
		Assert.assertEquals(true, items.get(2).isRest());
		items = Contributions.topWithRest(rawItems, 1);
		checkAmounts(items, 6);
		Assert.assertEquals(true, items.get(0).isRest());
	}

	private <T> void checkAmounts(List<ContributionItem<T>> items,
	                              double... values) {
		Assert.assertEquals(values.length, items.size());
		for (int i = 0; i < items.size(); i++)
			Assert.assertEquals(values[i], items.get(i).getAmount(), 1e-16);
	}

	private List<ContributionItem<String>> createSampleItems() {
		List<ContributionItem<String>> items = new ArrayList<>();
		for (int i = 1; i < 6; i++) {
			ContributionItem<String> item = new ContributionItem<>();
			item.setAmount(i % 3);
			item.setItem("item_" + i);
			items.add(item);
		}
		return items;
	}

}

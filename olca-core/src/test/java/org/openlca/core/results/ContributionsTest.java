package org.openlca.core.results;

import java.util.HashSet;
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

}

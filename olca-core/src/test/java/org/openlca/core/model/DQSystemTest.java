package org.openlca.core.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DQSystemTest {

	private DQSystem system;

	@Before
	public void prepare() {
		system = new DQSystem();
		for (int i = 1; i <= 5; i++) {
			DQIndicator indicator = new DQIndicator();
			indicator.position = i;
			system.indicators.add(indicator);
			for (int j = 1; j <= 5; j++) {
				DQScore score = new DQScore();
				score.position = j;
				indicator.scores.add(score);
			}
		}
	}

	@Test
	public void testToStringComplete() {
		int[] values = new int[] { 3, 2, 5, 1, 4 };
		String result = system.toString(values);
		Assert.assertEquals("(3;2;5;1;4)", result);
	}

	@Test
	public void testToStringIncomplete() {
		int[] values = new int[] { 3, 2, 4, 1 };
		String result = system.toString(values);
		Assert.assertEquals("(3;2;4;1;n.a.)", result);
	}

	@Test
	public void testToStringMoreThanIndicators() {
		int[] values = new int[] { 3, 2, 4, 1, 5, 1, 4 };
		String result = system.toString(values);
		Assert.assertEquals("(3;2;4;1;5)", result);
	}

	@Test
	public void testToStringInvalidScores() {
		int[] values = new int[] { 3, 6, 4, 7, -1 };
		String result = system.toString(values);
		Assert.assertEquals("(3;n.a.;4;n.a.;n.a.)", result);
	}

	@Test
	public void testToValuesComplete() {
		String s = "(3;2;5;1;4)";
		int[] result = system.toValues(s);
		Assert.assertArrayEquals(new int[] { 3, 2, 5, 1, 4 }, result);
	}

	@Test
	public void testToValuesIncomplete() {
		String s = " ( 3 ; n.a. ; 5 ; 1 ) ";
		int[] result = system.toValues(s);
		Assert.assertArrayEquals(new int[] { 3, 0, 5, 1, 0 }, result);
	}

	@Test
	public void testToValuesMoreThanIndicators() {
		String s = "(3;2;5;1;4;1;3)";
		int[] result = system.toValues(s);
		Assert.assertArrayEquals(new int[] { 3, 2, 5, 1, 4 }, result);
	}

	@Test
	public void testToValuesInvalidScores() {
		String s = " (3; 7; 5; -1; 0) ";
		int[] result = system.toValues(s);
		Assert.assertArrayEquals(new int[] { 3, 0, 5, 0, 0 }, result);
	}

}

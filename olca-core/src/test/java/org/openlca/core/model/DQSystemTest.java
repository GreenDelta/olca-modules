package org.openlca.core.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DQSystemTest {

	private DQSystem system;

	@Before
	public void setup() {
		system = new DQSystem();
		for (int i = 1; i <= 5; i++) {
			var indicator = new DQIndicator();
			indicator.position = i;
			system.indicators.add(indicator);
			for (int j = 1; j <= 5; j++) {
				var score = new DQScore();
				score.position = j;
				score.label = String.valueOf((char) (64 + j));
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

	@Test
	public void testApplyScoreLabels() {
		Assert.assertEquals("", system.applyScoreLabels(null));
		Assert.assertEquals("", system.applyScoreLabels(""));
		Assert.assertEquals("(A; B; C; D; E)",
				system.applyScoreLabels("(1;2;3;4;5)"));
		Assert.assertEquals("(E; D; C; B; A)",
				system.applyScoreLabels("(5;4;3;2;1)"));
		Assert.assertEquals("(E; n.a.; C; B; n.a.)",
				system.applyScoreLabels("(5;n.a.;3;2;n.a.)"));
	}

}

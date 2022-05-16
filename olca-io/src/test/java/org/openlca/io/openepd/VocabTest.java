package org.openlca.io.openepd;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.io.openepd.Vocab.Indicator;

import java.util.List;

public class VocabTest {

	@Test
	public void testGwp() {
    var gwp = Indicator.GWP;
		var names = List.of(
			"gwp",
			"GWP",
			"gwp 100",
			"GWP 100a",
			"GWP 100 a",
			"climate change",
			"Climate change 100a",
			"Global warming",
			"Global warming potential (GWP100a)",
      "IPCC 2021 GWP 100",
      "GWP - total",
      "climate change, GWP total");
		for (var name : names) {
      var score = gwp.matchScoreOf(name);
      assertEquals(1.0, score, 1e-10);
		}

    var units = List.of(
      "kgCO2e",
      "kg CO2 eq",
      "kg CO2 eq.",
      "kg CO2-eq.",
      "kg CO2 Equivalents",
      "kg CO2-Equivalents");
    for (var unit : units) {
      var match = gwp.unitMatchOf(unit).orElseThrow();
      assertEquals(1.0, match.factor(), 1e-10);
    }
	}

	@Test
	public void testScopeMatching() {
		var searches = new String[] {
			"A1 - A3", "A1A2A3", " a1 to a3",
			"A1", "a1", " A 1 ",
			"B1", "B 2", " b 3 ",
			"B2 - B7", "b2 to B7", " B2toB7 ",
		};
		var expected = new Vocab.Scope[] {
			Vocab.Scope.A1toA3, Vocab.Scope.A1toA3, Vocab.Scope.A1toA3,
			Vocab.Scope.A1, Vocab.Scope.A1, Vocab.Scope.A1,
			Vocab.Scope.B1, Vocab.Scope.B2, Vocab.Scope.B3,
			Vocab.Scope.B2toB7, Vocab.Scope.B2toB7, Vocab.Scope.B2toB7,
		};

		for (int i = 0; i < searches.length; i++) {
			var found = Vocab.findScope(searches[i]);
			assertTrue("could not find " + searches[i], found.isPresent());
			assertEquals(expected[i], found.get());
		}

		for (var scope : Vocab.Scope.values()) {
			var s = Vocab.findScope(scope.code()).orElseThrow();
			assertEquals(scope, s);
			s = Vocab.findScope(scope.name()).orElseThrow();
			assertEquals(scope, s);
			s = Vocab.findScope(scope.toString()).orElseThrow();
			assertEquals(scope, s);
		}
	}
}

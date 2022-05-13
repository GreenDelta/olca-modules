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


}

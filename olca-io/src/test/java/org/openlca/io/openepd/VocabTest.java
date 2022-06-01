package org.openlca.io.openepd;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.io.openepd.io.Vocab;
import org.openlca.io.openepd.io.Vocab.Indicator;
import org.openlca.util.Pair;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

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
	public void testOekoBaudatIndicators() {
		expectMatch(Indicator.AP,
			"Acidification potential, Accumulated Exceedance (AP) - 2017");
		expectMatch(Indicator.AP,
			"Acidification potential of soil and water (AP)");
		expectMatch(Indicator.AP,
			"Acidification potential, Accumulated Exceedance (AP) - 2017");
		expectMatch(Indicator.CRU,
			"Components for re-use (CRU)");
		expectMatch(Indicator.ODP,
			"Depletion potential of the stratospheric ozone layer (ODP)");
		expectMatch(Indicator.ODP,
			"Depletion potential of the stratospheric ozone layer (ODP) - 2017");
		expectMatch(Indicator.EP,
			"Eutrophication potential (EP)");
		expectMatch(Indicator.EP_FRESH,
			"Eutrophication potential - freshwater (EP-freshwater) - 2017");
		expectMatch(Indicator.EP,
			"Eutrophication potential - marine (EP-marine) - 2017");
		expectMatch(Indicator.EP_TERR,
			"Eutrophication potential - terrestrial (EP-terrestrial) - 2107");
		expectMatch(Indicator.EE,
			"Exported electrical energy (EEE)");
		expectMatch(Indicator.EH,
			"Exported thermal energy (EET)");
		expectMatch(Indicator.POCP,
			"Formation potential of tropospheric ozone (POCP)");
		expectMatch(Indicator.GWP_BIO,
			"Global Warming Potential - biogenic (GWP-biogenic) - 2017");
		expectMatch(Indicator.GWP_FOSSIL,
			"Global Warming Potential - fossil fuels (GWP-fossil) - 2017");
		expectMatch(Indicator.GWP_LAND,
			"Global Warming Potential - land use and land use change (GWP-luluc) - 2017");
		expectMatch(Indicator.GWP,
			"Global Warming Potential - total (GWP-total) - 2017");
		expectMatch(Indicator.GWP,
			"Global warming potential (GWP)");
		expectMatch(Indicator.HWD,
			"Hazardous waste disposed (HWD)");
		expectMatch(Indicator.MER,
			"Materials for energy recovery (MER)");
		expectMatch(Indicator.MR,
			"Materials for recycling (MFR)");
		expectMatch(Indicator.NHWD,
			"Non hazardous waste dispose (NHWD)");
		expectMatch(Indicator.HLRW,
			"Radioactive waste disposed (RWD)");
		expectMatch(Indicator.FW,
			"Use of net fresh water (FW)");
		expectMatch(Indicator.NRPRE,
			"Use of non renewable primary energy (PENRE)");
		expectMatch(Indicator.NRPRM,
			"Use of non renewable primary energy resources used as raw materials (PENRM)");
		expectMatch(Indicator.NRSF,
			"Use of non renewable secondary fuels (NRSF)");
		expectMatch(Indicator.RPRE,
			"Use of renewable primary energy (PERE)");
		expectMatch(Indicator.RPRM,
			"Use of renewable primary energy resources used as raw materials (PERM)");
		expectMatch(Indicator.RSF,
			"Use of renewable secondary fuels (RSF)");
		expectMatch(Indicator.SM,
			"Use of secondary material (SM)");
	}

	@Test
	public void testOlcaIndicators() {
		expectMatch(Indicator.AP, "Acidification");
		expectMatch(Indicator.AP, "Acidification (fate not incl.)");
		expectMatch(Indicator.AP,
			"Acidification terrestrial and freshwater");
		expectMatch(Indicator.AP,
			"Aquatic acidification");

		expectMatch(Indicator.EP,
			"Aquatic eutrophication");
		expectMatch(Indicator.EP,
			"Aquatic eutrophication EP(N)");
		expectMatch(Indicator.EP,
			"Aquatic eutrophication EP(P)");

		expectMatch(Indicator.GWP,
			"Climate change");
		expectMatch(Indicator.GWP_BIO,
			"Climate change - biogenic");
		expectMatch(Indicator.GWP_FOSSIL,
			"Climate change - fossil");
		expectMatch(Indicator.GWP_LAND,
			"Climate change - land use and transform.");

		expectMatch(Indicator.EP,
			"Eutrophication");
		expectMatch(Indicator.EP,
			"Eutrophication (incl. fate)");
		expectMatch(Indicator.EP_FRESH,
			"Eutrophication freshwater");
		expectMatch(Indicator.EP,
			"Eutrophication marine");
		expectMatch(Indicator.EP_TERR,
			"Eutrophication terrestrial");
		expectMatch(Indicator.EP_FRESH,
			"Freshwater eutrophication");

		expectMatch(Indicator.GWP,
			"Global warming");
		expectMatch(Indicator.GWP,
			"Global warming (GWP100a)");
		expectMatch(Indicator.GWP,
			"Global warming 100a");
		expectMatch(Indicator.GWP,
			"Global warming 100a (incl. NMVOC av.)");
		expectMatch(Indicator.GWP,
			"IPCC GWP 100a");

		expectMatch(Indicator.EP,
			"Marine eutrophication");

		expectMatch(Indicator.POCP,
			"Ozone formation (Human)");
		expectMatch(Indicator.POCP,
			"Ozone formation (Vegetation)");
		expectMatch(Indicator.POCP,
			"Ozone formation, Human health");
		expectMatch(Indicator.POCP,
			"Ozone formation, Terrestrial ecosystems");

		expectMatch(Indicator.ODP,
			"Ozone layer depletion");
		expectMatch(Indicator.ODP,
			"Ozone layer depletion (ODP)");
		expectMatch(Indicator.ODP,
			"Ozone layer depletion (ODP) (optional)");
		expectMatch(Indicator.ODP,
			"Ozone layer depletion (incl. NMVOC av.)");
		expectMatch(Indicator.ODP,
			"Ozone layer depletion 10a");
		expectMatch(Indicator.ODP,
			"Ozone layer depletion 15a");

		expectMatch(Indicator.POCP,
			"Photochemical ozone formation");
		expectMatch(Indicator.POCP,
			"Photochemical ozone formation, HH");

		expectMatch(Indicator.EP_TERR,
			"Terrestrial eutrophication");
	}

	@Test
	public void testMatchIndicatorCodes() {
		for (var indicator : Indicator.values()) {
			expectMatch(indicator, indicator.code());
			// expectMatch(indicator, indicator.description());
		}
	}

	private void expectMatch(Indicator indicator, String name) {
		var match = Arrays.stream(Indicator.values())
			.map(i -> Pair.of(i, i.matchScoreOf(name)))
			.filter(p -> p.second > 1e-4)
			.sorted(Comparator.comparingDouble(p -> -p.second))
			.map(p -> p.first)
			.findFirst()
			.orElse(null);
		assertEquals(
			String.format("expected indicator: %s for name: %s",
				indicator.code(), name), indicator, match);
	}

	@Test
	public void testUnitMatching() {
		for (var indicator : Indicator.values()) {
			var match = indicator.unitMatchOf(indicator.unit());
			assertTrue(String.format("indicator: %s unit: %s",
				indicator.code(), indicator.unit()), match.isPresent());
			assertEquals(1.0, match.get().factor(), 1e-10);
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

	@Test
	public void testMethodMatching() {

		Function<String, Vocab.Method> match = s -> {
			Vocab.Method candidate = null;
			double score = 0;
			for (var m : Vocab.Method.values()) {
				var nextScore = m.matchScoreOf(s);
				if (nextScore > score) {
					candidate = m;
					score = nextScore;
				}
			}
			return candidate;
		};

		// test method codes
		for (var method : Vocab.Method.values()) {
			assertEquals(method, match.apply(method.code()));
			assertEquals(method, match.apply(method.code().toLowerCase()));
		}

		assertEquals(Vocab.Method.EF_3_0,
			match.apply("ei - EF v3.0 EN15804"));
		assertEquals(Vocab.Method.EF_3_0,
			match.apply("environmental footprint; 3.0"));
		assertEquals(Vocab.Method.TRACI_2_1,
			match.apply("TRACI, v2.1"));
	}

}

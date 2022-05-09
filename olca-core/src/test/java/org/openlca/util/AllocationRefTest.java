package org.openlca.util;

import java.util.stream.Collectors;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Currency;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Process;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

public class AllocationRefTest {

	private Process process;

	@Before
	public void setup() {

		// units and flow properties
		var volUnits = UnitGroup.of("Units of volume", "m3");
		volUnits.units.add(Unit.of("L", 0.001));
		var massUnits = UnitGroup.of("Units of mass", "kg");
		var volume = FlowProperty.of("Volume", volUnits);
		var dryVolume = FlowProperty.of("Dry-Volume", volUnits);
		var mass = FlowProperty.of("Mass", massUnits);

		// currencies
		var usd = Currency.of("USD");
		var eur = Currency.of("EUR");
		eur.referenceCurrency = usd;
		eur.conversionFactor = 1.2;

		// flows
		var product = Flow.product("p", volume);
		product.id = 1;
		product.flowPropertyFactors.add(FlowPropertyFactor.of(dryVolume, 0.8));
		product.flowPropertyFactors.add(FlowPropertyFactor.of(mass, 255));
		var waste = Flow.waste("w", mass);
		waste.id = 2;
		var elem = Flow.elementary("e", mass);
		elem.id = 3;

		// the process
		process = Process.of("process", product);

		// product: 500 L, dry volume, 20 EUR
		var qref = process.quantitativeReference;
		qref.flowPropertyFactor = product.getFactor(dryVolume);
		qref.unit = volUnits.getUnit("L");
		qref.amount = 500;
		qref.costs = -20.0;
		qref.currency = eur;

		// waste: 200 kg, mass, 10 USD
		var w = process.input(waste, 200);
		w.costs = -10.0;
		w.currency = usd;

		// elem. flow, 3 kg, mass
		process.output(elem, 5);

	}

	@Test
	public void testFlowTypes() {
		var providers = AllocationUtils.getProviderFlows(process)
			.stream()
			.map(e -> e.flow.name)
			.collect(Collectors.toSet());
		assertEquals(2, providers.size());
		assertTrue(providers.contains("p"));
		assertTrue(providers.contains("w"));

		var nonProviders = AllocationUtils.getNonProviderFlows(process)
			.stream()
			.map(e -> e.flow.name)
			.collect(Collectors.toSet());
		assertEquals(1, nonProviders.size());
		assertTrue(nonProviders.contains("e"));
	}

	@Test
	public void testCommonProperty() {
		var props = AllocationUtils.allocationPropertiesOf(process);
		assertEquals(1, props.size());
		var prop = props.iterator().next();
		assertEquals("Mass", prop.name);
	}

	@Test
	public void testMassFactors() {
		var mass = AllocationUtils.allocationPropertiesOf(process)
			.iterator()
			.next();
		var factors = AllocationRef.of(AllocationMethod.PHYSICAL, mass)
			.apply(process);
		assertEquals(2, factors.size());

		var productMass = (500 * 0.001 / 0.8) * 255;
		var totalMass = productMass + 200;
		for (var factor : factors) {
			assertEquals(AllocationMethod.PHYSICAL, factor.method);
			if (factor.productId == 1) {
				assertEquals(
					productMass / totalMass,
					factor.value, 1e-11);
			} else {
				assertEquals(
					200 / totalMass,
					factor.value, 1e-11);
			}
		}
	}

	@Test
	public void testCurrencyFactors() {
		var factors = AllocationRef.ofCosts(AllocationMethod.ECONOMIC)
			.apply(process);
		assertEquals(2, factors.size());
		for (var factor : factors) {
			assertEquals(AllocationMethod.ECONOMIC, factor.method);
			if (factor.productId == 1) {
				assertEquals(
					1.2 * 20 / (1.2 * 20 + 10),
					factor.value, 1e-11);
			} else {
				assertEquals(
					10 / (1.2 * 20 + 10),
					factor.value, 1e-11);
			}
		}
	}

}

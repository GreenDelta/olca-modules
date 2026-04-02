package org.openlca.io.olca;

import static org.junit.Assert.*;

import java.util.EnumMap;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.CurrencyDao;
import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Currency;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.SocialAspect;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.UnitGroup;

public class DatabaseImportTest {

	private IDatabase source;
	private IDatabase target;

	@Before
	public void setup() {
		source = Derby.createInMemory();
		target = Derby.createInMemory();
	}

	@After
	public void cleanup() throws Exception {
		source.close();
		target.close();
	}

	@Test
	public void testUnitGroupPropertyCycle() {
		var units = UnitGroup.of("Mass units", "kg");
		var mass = FlowProperty.of("Mass", units);
		source.insert(units, mass);
		units.defaultFlowProperty = mass;
		source.update(units);
		new DatabaseImport(source, target).run();

		var unitsCopy = target.getForName(UnitGroup.class, "Mass units");
		assertNotNull(unitsCopy);
		assertEquals(1, unitsCopy.units.size());
		assertEquals(units.referenceUnit.refId, unitsCopy.referenceUnit.refId);
		assertEquals("kg", unitsCopy.referenceUnit.name);

		var massCopy = target.getForName(FlowProperty.class, "Mass");
		assertNotNull(massCopy);
		assertEquals(massCopy.id, unitsCopy.defaultFlowProperty.id);
		assertEquals(unitsCopy.id, massCopy.unitGroup.id);
		assertEquals(1, target.getAll(UnitGroup.class).size());
		assertEquals(1, target.getAll(FlowProperty.class).size());
	}

	@Test
	public void testCurrenciesReferenceDefaultCurrency() {
		var eur = Currency.of("EUR");
		eur.referenceCurrency = eur;
		var usd = Currency.of("USD");
		usd.conversionFactor = 1.08;
		usd.referenceCurrency = eur;
		source.insert(eur, usd);

		new DatabaseImport(source, target).run();

		var eurCopy = target.getForName(Currency.class, "EUR");
		var usdCopy = target.getForName(Currency.class, "USD");
		assertNotNull(eurCopy.referenceCurrency);
		assertNotNull(usdCopy.referenceCurrency);
		assertEquals(eurCopy.id, eurCopy.referenceCurrency.id);
		assertEquals(eurCopy.id, usdCopy.referenceCurrency.id);
		assertEquals(eurCopy.id, new CurrencyDao(target).getReferenceCurrency().id);
	}

	@Test
	public void testCopySocialAspects() {
		var indicator = new SocialIndicator();
		indicator.refId = "si";
		source.insert(indicator);

		var process = new Process();
		process.refId = "pr";
		var aspect = SocialAspect.of(process, indicator);
		source.insert(process);

		new DatabaseImport(source, target).run();
		indicator = target.get(SocialIndicator.class, "si");
		assertNotNull(indicator);
		process = target.get(Process.class, "pr");
		assertEquals(1, process.socialAspects.size());
		aspect = process.socialAspects.getFirst();
		assertNotNull(aspect.indicator);
		assertEquals("si", aspect.indicator.refId);
	}

	@Test
	public void testCopyLocations() {
		var location = Location.of("US");
		location.category = CategoryDao.sync(
				source, ModelType.LOCATION, "some", "countries");
		source.insert(location);
		new DatabaseImport(source, target).run();
		location = target.get(Location.class, location.refId);
		assertEquals("some/countries", location.category.toPath());
	}

	@Test
	public void testCategories() throws Exception {
		var ids = new EnumMap<ModelType, String>(ModelType.class);
		for (var type : ModelType.values()) {
			if (type == ModelType.CATEGORY)
				continue;
			var e = type.getModelClass()
					.getConstructor()
					.newInstance();
			e.refId = UUID.randomUUID().toString();
			e.category = CategoryDao.sync(
					source, type, "some", "more", "categories");
			if (e instanceof Currency currency) {
				currency.referenceCurrency = currency;
			}
			source.insert(e);
			ids.put(type, e.refId);
		}

		new DatabaseImport(source, target).run();

		for (var type : ModelType.values()) {
			if (type == ModelType.CATEGORY)
				continue;
			var id = ids.get(type);
			var e = target.get(type.getModelClass(), id);
			assertNotNull("copy failed for " + type, e);
			assertEquals(
					"category test failed for " + type,
					"some/more/categories", e.category.toPath());
		}

	}
}

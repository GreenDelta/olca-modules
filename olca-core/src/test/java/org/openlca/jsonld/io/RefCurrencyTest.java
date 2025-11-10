package org.openlca.jsonld.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Currency;
import org.openlca.jsonld.MemStore;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.output.JsonExport;

public class RefCurrencyTest {

	private final IDatabase db = Tests.getDb();

	@Test
	public void testRefLink() {
		// the error did not show up everytime so we try a few more
		for (int i = 1; i < 10; i++) {

			var eur = Currency.of("EUR");
			eur.referenceCurrency = eur;
			var pln = Currency.of("PLN");
			pln.referenceCurrency = eur;
			pln.conversionFactor = 0.25;
			db.insert(eur, pln);

			var store = new MemStore();
			var exp = new JsonExport(db, store);
			exp.write(eur);
			exp.write(pln);
			db.clear();

			new JsonImport(store, db).run();

			var cs = db.getAll(Currency.class);
			assertEquals(2, cs.size());
			eur = cs.stream()
					.filter(c -> c.name.equals("EUR"))
					.findFirst()
					.orElseThrow();
			pln = cs.stream()
					.filter(c -> c.name.equals("PLN"))
					.findFirst()
					.orElseThrow();

			assertSame(eur, eur.referenceCurrency);
			assertSame(eur, pln.referenceCurrency);

			db.delete(eur, pln);
		}
	}
}


package org.openlca.io.simapro.csv.input;

import org.openlca.simapro.csv.Numeric;
import org.openlca.simapro.csv.UncertaintyRecord;
import org.openlca.simapro.csv.process.ElementaryExchangeRow;
import org.openlca.simapro.csv.process.ExchangeRow;
import org.openlca.simapro.csv.process.ProductOutputRow;
import org.openlca.simapro.csv.process.RefExchangeRow;
import org.openlca.simapro.csv.process.TechExchangeRow;
import org.openlca.simapro.csv.process.WasteTreatmentRow;

/**
 * A simple wrapper type to make the exchange reading a bit easier. It simply
 * returns {@code null} for fields that are not supperted by the underlying
 * exchange type.
 */
record CsvExchange(ExchangeRow row) implements ExchangeRow {

	static CsvExchange of(ExchangeRow row) {
		return new CsvExchange(row);
	}

	@Override
	public String name() {
		return row.name();
	}

	@Override
	public String unit() {
		return row.unit();
	}

	@Override
	public Numeric amount() {
		return row.amount();
	}

	@Override
	public String comment() {
		return row.comment();
	}

	Numeric allocation() {
			return row instanceof ProductOutputRow product
				? product.allocation()
				: null;
	}

	String category() {
		return row instanceof RefExchangeRow refRow
			? refRow.category()
			: null;
	}

	String wasteType() {
		if (row instanceof ProductOutputRow p)
			return p.wasteType();
		if (row instanceof WasteTreatmentRow w)
			return w.wasteType();
		return null;
	}

	UncertaintyRecord uncertainty() {
		if (row instanceof ElementaryExchangeRow e)
			return e.uncertainty();
		if (row instanceof TechExchangeRow t)
			return t.uncertainty();
		return null;
	}
}

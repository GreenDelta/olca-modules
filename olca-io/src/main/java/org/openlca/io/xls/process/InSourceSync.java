package org.openlca.io.xls.process;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;

public class InSourceSync {

	private final InConfig config;

	private InSourceSync(InConfig config) {
		this.config = config;
	}

	public static void sync(InConfig config) {
		new InSourceSync(config).sync();
	}

	private void sync() {
		var sheet = config.getSheet(Tab.SOURCES);
		if (sheet == null)
			return;
		sheet.eachRow(row -> {
			var refId = row.str(Field.UUID);
			config.index().sync(Source.class, refId, () -> create(row));
		});
	}

	private Source create(RowReader row) {
		var source = new Source();
		In.mapBase(row, source);
		source.category = row.syncCategory(config.db(), ModelType.SOURCE);
		source.url = row.str(Field.URL);
		source.textReference = row.str(Field.TEXT_REFERENCE);
		var year = row.num(Field.YEAR);
		if (year > 0) {
			source.year = (short) year;
		}
		return source;
	}
}

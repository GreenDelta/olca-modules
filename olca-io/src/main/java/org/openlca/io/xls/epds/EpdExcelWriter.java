package org.openlca.io.xls.epds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.commons.Res;
import org.openlca.core.model.Epd;

/// Writes an EPD to an Excel file.
public class EpdExcelWriter {

	private final Epd epd;
	private final File file;

	public EpdExcelWriter(Epd epd, File file) {
		this.epd = epd;
		this.file = file;
	}

	public Res<Void> write() {
		if (epd == null || file == null)
			return Res.error("No EPD or file given.");
		try (var wb = new XSSFWorkbook();
				 var out = new FileOutputStream(file)) {
			var ctx = new WriterContext(wb, epd);
			new InfoSheet(ctx).write();
			new ResultsSheet(ctx).write();
			new ModulesSheet(ctx).write();
			new IndicatorsSheet(ctx).write();
			new SourcesSheet(ctx).write();
			new ContactsSheet(ctx).write();
			wb.write(out);
			return Res.ok();
		} catch (IOException e) {
			return Res.error("EPD to Excel export failed", e);
		}
	}
}

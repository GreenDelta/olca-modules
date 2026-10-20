package org.openlca.io.xls.epds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlca.core.model.Epd;

/// Writes an EPD to an Excel file.
public class EpdExcelWriter {

	private final Epd epd;
	private final File file;

	public EpdExcelWriter(Epd epd, File file) {
		this.epd = epd;
		this.file = file;
	}

	public void write() {
		if (epd == null || file == null)
			return;
		try (var wb = new XSSFWorkbook();
				 var out = new FileOutputStream(file)) {
			var ctx = new WriterContext(wb, epd);
			new InfoSheet(ctx).write();
			ctx.createSheet(Tab.EPD_RESULTS);
			ctx.createSheet(Tab.EPD_MODULES);
			ctx.createSheet(Tab.INDICATORS);
			ctx.createSheet(Tab.SOURCES);
			ctx.createSheet(Tab.CONTACTS);
			wb.write(out);
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to write EPD " + epd + " to file " + file, e);
		}
	}
}

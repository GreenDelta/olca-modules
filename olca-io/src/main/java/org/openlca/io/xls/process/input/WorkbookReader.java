package org.openlca.io.xls.process.input;

import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openlca.io.xls.process.Tab;
import org.slf4j.LoggerFactory;

class WorkbookReader implements AutoCloseable {

	private final Workbook wb;

	WorkbookReader(Workbook wb) {
		this.wb = Objects.requireNonNull(wb);
	}

	static Optional<WorkbookReader> open(File file) {
		try (var fis = new FileInputStream(file)) {
			var wb = WorkbookFactory.create(fis);
			var reader = new WorkbookReader(wb);
      return Optional.of(reader);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	Optional<SheetReader> getSheet(Tab tab) {
		if (tab == null)
			return Optional.empty();
		var sheet = wb.getSheet(tab.label());
		if (sheet == null)
			return Optional.empty();
		return Optional.of(new SheetReader(sheet));
	}

	@Override
	public void close() {
		try {
			wb.close();
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass())
					.error("failed to close workbook", e);
		}
	}
}

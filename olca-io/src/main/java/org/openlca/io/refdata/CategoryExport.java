package org.openlca.io.refdata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

class CategoryExport implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());
	private File file;
	private IDatabase database;

	public CategoryExport(File file, IDatabase database) {
		this.file = file;
		this.database = database;
	}

	@Override
	public void run() {
		try (FileOutputStream fos = new FileOutputStream(file);
				OutputStreamWriter writer = new OutputStreamWriter(fos, "utf-8");
				BufferedWriter buffer = new BufferedWriter(writer);
				CSVWriter csvWriter = new CSVWriter(buffer, ';', '"')) {
			writeCategories(csvWriter);
		} catch (Exception e) {
			log.error("failed to write categories", e);
		}
	}

	private void writeCategories(CSVWriter csvWriter) {
		log.trace("write categories to file {}", file);
		CategoryDao dao = new CategoryDao(database);
		List<Category> categories = dao.getAll();
		for (Category category : categories) {
			String[] line = createLine(category);
			csvWriter.writeNext(line);
		}
		log.trace("{} categories written", categories.size());
	}

	private String[] createLine(Category category) {
		String[] line = new String[5];
		line[0] = category.getRefId();
		line[1] = category.getName();
		line[2] = category.getDescription();
		if (category.getModelType() != null)
			line[3] = category.getModelType().name();
		if (category.getParentCategory() != null)
			line[4] = category.getParentCategory().getRefId();
		return line;
	}

}

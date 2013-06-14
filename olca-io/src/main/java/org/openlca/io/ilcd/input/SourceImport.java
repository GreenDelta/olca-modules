package org.openlca.io.ilcd.input;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Category;
import org.openlca.core.model.Source;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.util.SourceBag;

public class SourceImport {

	private IDatabase database;
	private DataStore dataStore;
	private SourceBag ilcdSource;
	private Source source;

	public SourceImport(DataStore dataStore, IDatabase database) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public Source run(org.openlca.ilcd.sources.Source source)
			throws ImportException {
		this.ilcdSource = new SourceBag(source);
		Source oSource = findExisting(ilcdSource.getId());
		if (oSource != null)
			return oSource;
		return createNew();
	}

	public Source run(String sourceId) throws ImportException {
		Source source = findExisting(sourceId);
		if (source != null)
			return source;
		org.openlca.ilcd.sources.Source iSource = tryGetSource(sourceId);
		ilcdSource = new SourceBag(iSource);
		return createNew();
	}

	private Source findExisting(String sourceId) throws ImportException {
		try {
			return database.createDao(Source.class).getForId(sourceId);
		} catch (Exception e) {
			String message = String.format("Search for source %s failed.",
					sourceId);
			throw new ImportException(message, e);
		}
	}

	private Source createNew() throws ImportException {
		source = new Source();
		importAndSetCategory();
		setDescriptionAttributes();
		saveInDatabase();
		return source;
	}

	private org.openlca.ilcd.sources.Source tryGetSource(String sourceId)
			throws ImportException {
		try {
			org.openlca.ilcd.sources.Source iSource = dataStore.get(
					org.openlca.ilcd.sources.Source.class, sourceId);
			if (iSource == null) {
				throw new ImportException("No ILCD source for ID " + sourceId
						+ " found");
			}
			return iSource;
		} catch (Exception e) {
			throw new ImportException(e.getMessage(), e);
		}
	}

	private void setDescriptionAttributes() {
		source.setId(ilcdSource.getId());
		source.setName(ilcdSource.getShortName());
		source.setDescription(ilcdSource.getComment());
		source.setTextReference(ilcdSource.getSourceCitation());
	}

	private void importAndSetCategory() throws ImportException {
		CategoryImport categoryImport = new CategoryImport(database,
				Source.class);
		Category category = categoryImport.run(ilcdSource.getSortedClasses());
		source.setCategoryId(category.getId());
	}

	private void saveInDatabase() throws ImportException {
		try {
			database.createDao(Source.class).insert(source);
		} catch (Exception e) {
			String message = String.format(
					"Cannot save source %s in database.", source.getId());
			throw new ImportException(message, e);
		}
	}
}

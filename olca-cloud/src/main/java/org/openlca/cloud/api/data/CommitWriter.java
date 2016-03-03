package org.openlca.cloud.api.data;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.openlca.cloud.model.data.Dataset;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.Category;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Version;
import org.openlca.jsonld.EntityStore;
import org.openlca.jsonld.output.JsonExport;

public class CommitWriter extends DataWriter {

	private String commitMessage;
	private JsonExport export;

	public CommitWriter(IDatabase database) {
		super(database);
		export = new JsonExport(database, entityStore);
		export.setExportReferences(false);
	}

	public void putForRemoval(Dataset descriptor) {
		putDescriptor(descriptor);
	}

	public void setCommitMessage(String message) {
		this.commitMessage = message;
	}

	@Override
	protected void writeMetaData(FileSystem zip) throws IOException {
		if (commitMessage == null)
			commitMessage = "";
		Files.write(zip.getPath("message.txt"), commitMessage.getBytes(),
				StandardOpenOption.CREATE);
	}

	public void put(CategorizedEntity entity) {
		export.write(entity);
		Dataset descriptor = toDescriptor(entity);
		putDescriptor(descriptor);
		if (entity instanceof ImpactMethod)
			putRelated((ImpactMethod) entity);
	}

	private void putRelated(ImpactMethod method) {
		for (ImpactCategory category : method.getImpactCategories()) {
			Dataset descriptor = toDescriptor(category);
			putDescriptor(descriptor);
		}
		for (NwSet set : method.getNwSets()) {
			Dataset descriptor = toDescriptor(set);
			putDescriptor(descriptor);
		}
	}

	private Dataset toDescriptor(CategorizedEntity entity) {
		Dataset descriptor = toDescriptor((RootEntity) entity);
		if (entity.getCategory() != null)
			descriptor.setCategoryRefId(entity.getCategory().getRefId());
		if (entity instanceof Category)
			descriptor.setCategoryType(((Category) entity).getModelType());
		else
			descriptor.setCategoryType(ModelType.forModelClass(entity
					.getClass()));
		descriptor.setFullPath(getFullPath(entity));
		return descriptor;
	}

	private Dataset toDescriptor(RootEntity entity) {
		Dataset descriptor = new Dataset();
		descriptor.setLastChange(entity.getLastChange());
		descriptor.setRefId(entity.getRefId());
		descriptor.setName(entity.getName());
		descriptor.setType(ModelType.forModelClass(entity.getClass()));
		descriptor.setVersion(new Version(entity.getVersion()).toString());
		return descriptor;
	}

	private String getFullPath(CategorizedEntity entity) {
		String path = entity.getName();
		Category category = entity.getCategory();
		while (category != null) {
			path = category.getName() + "/" + path;
			category = category.getCategory();
		}
		return path;
	}

	public EntityStore getEntityStore() {
		return entityStore;
	}

}

package org.openlca.jsonld.output;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Callback;
import org.openlca.core.model.Callback.Message;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.EntityStore;

import com.google.gson.JsonObject;

/**
 * Writes entities to an entity store (e.g. a document or zip file). It also
 * writes the referenced entities to this store if they are not yet contained.
 */
public class JsonExport {

	private ExportConfig conf;

	public JsonExport(IDatabase database, EntityStore store) {
		conf = ExportConfig.create(database, store);
	}

	public <T extends RootEntity> void write(T entity) {
		write(entity, null);
	}

	public <T extends RootEntity> void write(T entity, Callback cb) {
		if (entity == null)
			return;
		ModelType type = ModelType.forModelClass(entity.getClass());
		if (type == null || entity.getRefId() == null) {
			warn(cb, "no refId, or type is unknown", entity);
			return;
		}
		if (conf.hasVisited(type, entity.getId()))
			return;
		Writer<T> writer = getWriter(entity, conf);
		if (writer == null) {
			warn(cb, "no writer found for type " + type, entity);
			return;
		}
		try {
			conf.refFn = ref -> {
				write(ref, cb);
			};
			JsonObject obj = writer.write(entity);
			conf.store.put(type, obj);
			if (writer.isExportExternalFiles())
				writeExternalFiles(entity, type, cb);
			if (cb != null)
				cb.apply(Message.info("data set exported"), entity);
		} catch (Exception e) {
			e.printStackTrace();
			if (cb != null)
				cb.apply(Message.error("failed to export data set", e), entity);
		}
	}

	private void warn(Callback cb, String message, RootEntity entity) {
		if (cb == null)
			return;
		cb.apply(Message.warn(message), entity);
	}

	private void writeExternalFiles(RootEntity entity, ModelType type,
			Callback cb) {
		if (entity == null || conf.db == null
				|| conf.db.getFileStorageLocation() == null
				|| conf.store == null)
			return;
		FileStore fs = new FileStore(conf.db.getFileStorageLocation());
		File dir = fs.getFolder(entity);
		if (dir == null || !dir.exists())
			return;
		try {
			Path dbDir = dir.toPath();
			Copy copy = new Copy(entity.getRefId(), type, dbDir);
			Files.walkFileTree(dir.toPath(), copy);
		} catch (Exception e) {
			cb.apply(Message.error("failed to copy external files", e), entity);
		}
	}

	public static <T extends RootEntity> JsonObject toJson(T entity,
			IDatabase database) {
		if (entity == null)
			return new JsonObject();
		Writer<T> writer = getWriter(entity, ExportConfig.create(database));
		return writer.write(entity);
	}

	public static <T extends RootEntity> JsonObject toJson(T entity) {
		if (entity == null)
			return new JsonObject();
		Writer<T> writer = getWriter(entity, ExportConfig.create());
		return writer.write(entity);
	}

	@SuppressWarnings("unchecked")
	private static <T extends RootEntity> Writer<T> getWriter(T entity,
			ExportConfig conf) {
		if (entity == null)
			return null;
		if (entity instanceof Actor)
			return Writer.class.cast(new ActorWriter(conf));
		if (entity instanceof Category)
			return Writer.class.cast(new CategoryWriter(conf));
		if (entity instanceof Currency)
			return Writer.class.cast(new CurrencyWriter(conf));
		if (entity instanceof FlowProperty)
			return Writer.class.cast(new FlowPropertyWriter(conf));
		if (entity instanceof Flow)
			return Writer.class.cast(new FlowWriter(conf));
		if (entity instanceof NwSet)
			return Writer.class.cast(new NwSetWriter(conf));
		if (entity instanceof ImpactCategory)
			return Writer.class.cast(new ImpactCategoryWriter(conf));
		if (entity instanceof ImpactMethod)
			return Writer.class.cast(new ImpactMethodWriter(conf));
		if (entity instanceof Location)
			return Writer.class.cast(new LocationWriter(conf));
		if (entity instanceof Parameter)
			return Writer.class.cast(new ParameterWriter(conf));
		if (entity instanceof Process)
			return Writer.class.cast(new ProcessWriter(conf));
		if (entity instanceof Source)
			return Writer.class.cast(new SourceWriter(conf));
		if (entity instanceof UnitGroup)
			return Writer.class.cast(new UnitGroupWriter(conf));
		if (entity instanceof SocialIndicator)
			return Writer.class.cast(new SocialIndicatorWriter(conf));
		if (entity instanceof ProductSystem)
			return Writer.class.cast(new ProductSystemWriter(conf));
		if (entity instanceof Project)
			return Writer.class.cast(new ProjectWriter(conf));
		if (entity instanceof DQSystem)
			return Writer.class.cast(new DQSystemWriter(conf));
		return null;
	}

	public void setExportDefaultProviders(boolean value) {
		conf.exportProviders = value;
	}

	public void setExportReferences(boolean value) {
		conf.exportReferences = value;
	}

	private class Copy extends SimpleFileVisitor<Path> {

		private String refId;
		private ModelType type;
		private Path dbDir;

		Copy(String refId, ModelType type, Path dbDir) {
			this.refId = refId;
			this.dbDir = dbDir;
			this.type = type;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			String path = dbDir.relativize(file).toString().replace('\\', '/');
			byte[] data = Files.readAllBytes(file);
			conf.store.putBin(type, refId, path, data);
			return FileVisitResult.CONTINUE;
		}

	}
}

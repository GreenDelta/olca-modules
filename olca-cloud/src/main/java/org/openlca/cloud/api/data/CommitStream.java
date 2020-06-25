package org.openlca.cloud.api.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.cloud.model.data.Dataset;
import org.openlca.core.database.Daos;
import org.openlca.core.database.FileStore;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.output.JsonExport;
import org.openlca.util.BinUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class CommitStream extends ModelStream<Dataset> {

	private final IDatabase db;
	private final Gson gson = new Gson();

	public CommitStream(IDatabase db, String commitMessage, Set<Dataset> datasets, Consumer<Dataset> callback) {
		super(commitMessage, datasets.iterator(), datasets.size(), callback);
		this.db = db;
	}

	@Override
	protected File getBinaryFilesLocation(Dataset dataset) {
		FileStore fs = new FileStore(db.getFileStorageLocation());
		return fs.getFolder(dataset.type, dataset.refId);
	}

	@Override
	protected byte[] getData(Dataset dataset) throws IOException {
		RootEntity entity = Daos.root(db, dataset.type).getForRefId(dataset.refId);
		if (entity == null)
			return new byte[0];
		JsonObject object = JsonExport.toJson(entity, db);
		String json = gson.toJson(object);
		byte[] data = json.getBytes(CHARSET);
		data = BinUtils.gzip(data);
		return data;
	}

	@Override
	protected byte[] getBinaryData(Path file) throws IOException {
		byte[] data = Files.readAllBytes(file);
		return BinUtils.gzip(data);
	}

}

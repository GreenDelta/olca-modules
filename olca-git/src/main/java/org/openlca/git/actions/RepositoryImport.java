package org.openlca.git.actions;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Entry.EntryType;
import org.openlca.git.repo.OlcaRepository;
import org.openlca.jsonld.JsonStoreReader;
import org.openlca.jsonld.input.JsonImport;
import org.openlca.jsonld.input.UpdateMode;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RepositoryImport implements JsonStoreReader {

	private static final ModelType[] TYPE_ORDER = new ModelType[] {
			ModelType.DQ_SYSTEM,
			ModelType.LOCATION,
			ModelType.ACTOR,
			ModelType.SOURCE,
			ModelType.PARAMETER,
			ModelType.UNIT_GROUP,
			ModelType.FLOW_PROPERTY,
			ModelType.CURRENCY,
			ModelType.FLOW,
			ModelType.IMPACT_CATEGORY,
			ModelType.IMPACT_METHOD,
			ModelType.SOCIAL_INDICATOR,
			ModelType.PROCESS,
			ModelType.PRODUCT_SYSTEM,
			ModelType.PROJECT,
			ModelType.RESULT,
			ModelType.EPD
	};

	private static final Gson gson = new Gson();
	private final OlcaRepository repo;
	private final Commit commit;

	public RepositoryImport(OlcaRepository repo) {
		this(repo, repo.commits.find().latest());
	}

	public RepositoryImport(OlcaRepository repo, Commit commit) {
		this.repo = repo;
		this.commit = commit;
	}

	public void into(IDatabase database) {
		if (commit == null)
			return;
		JsonImport importer = new JsonImport(this, database);
		importer.setUpdateMode(UpdateMode.ALWAYS);
		for (var type : TYPE_ORDER) {
			repo.entries.iterate(commit.id, type.name(), entry -> {
				if (entry.typeOfEntry == EntryType.CATEGORY) {
					importer.getCategory(entry.type, entry.getCategoryPath());
				} else if (entry.typeOfEntry == EntryType.DATASET) {
					importer.run(entry.type, entry.refId);
				}
			});
		}
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> getFiles(String dir) {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getBytes(String path) {
		if (RepositoryInfo.FILE_NAME.equals(path))
			return repo.datasets.getRepositoryInfo(commit);
		throw new UnsupportedOperationException();
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		if (commit == null)
			return null;
		var ref = repo.references.get(type, refId, commit.id);
		var data = repo.datasets.get(ref);
		return gson.fromJson(data, JsonObject.class);
	}

}

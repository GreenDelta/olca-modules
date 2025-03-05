package org.openlca.git.repo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectStream;
import org.openlca.git.RepositoryInfo;
import org.openlca.git.model.Commit;
import org.openlca.git.model.Reference;
import org.openlca.git.util.FieldDefinition;
import org.openlca.git.util.MetaDataParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class Datasets {

	private static final Logger log = LoggerFactory.getLogger(Datasets.class);
	private final OlcaRepository repo;

	static Datasets of(OlcaRepository repo) {
		return new Datasets(repo);
	}

	private Datasets(OlcaRepository repo) {
		this.repo = repo;
	}

	public String getName(Reference ref) {
		if (ref.isCategory) {
			if (!ref.path.contains("/"))
				return null;
			return ref.path.substring(ref.path.indexOf("/") + 1);
		}
		var data = parse(ref, "name");
		var name = data.get("name");
		return name != null ? name.toString() : null;
	}

	public Map<String, Object> parse(Reference ref, String... fields) {
		return stream(ref, new HashMap<>(), stream -> MetaDataParser.parse(stream, fields));
	}

	public Map<String, Object> parse(Reference ref, List<FieldDefinition> defs) {
		return stream(ref, new HashMap<>(), stream -> MetaDataParser.parse(stream, defs));
	}

	public Map<String, Object> parse(Reference ref, FieldDefinition... defs) {
		return stream(ref, new HashMap<>(), stream -> MetaDataParser.parse(stream, defs));
	}

	private <T> T stream(Reference ref, T defaultValue, Function<ObjectStream, T> consumer) {
		if (ref == null || ref.objectId == null || ref.isCategory)
			return defaultValue;
		try (var reader = repo.getObjectDatabase().newReader();
				var stream = reader.open(ref.objectId).openStream()) {
			return consumer.apply(stream);
		} catch (IOException e) {
			log.error("Error loading " + ref.objectId);
			return null;
		}
	}

	public byte[] getBytes(Reference ref) {
		if (ref == null || ref.isCategory)
			return null;
		return getBytes(ref.objectId);
	}

	private byte[] getBytes(ObjectId id) {
		if (id == null || id.equals(ObjectId.zeroId()))
			return null;
		try (var reader = repo.getObjectDatabase().newReader()) {
			var loader = reader.open(id);
			// large objects will throw an exception if getBytes() is used
			// so use getBytes(loader.getSize()) to circumvent this
			if (loader.isLarge() && loader.getSize() <= Integer.MAX_VALUE)
				return loader.getBytes((int) loader.getSize());
			return loader.getBytes();
			// will throw exception if file is bigger then 2147483647 bytes
		} catch (IOException e) {
			log.error("Error loading " + id);
			return null;
		}
	}

	public String get(Reference ref) {
		if (ref == null || ref.isCategory)
			return null;
		return get(ref.objectId);
	}

	private String get(ObjectId id) {
		var data = getBytes(id);
		if (data == null)
			return null;
		try {
			return new String(data, "utf-8");
		} catch (IOException e) {
			log.error("Error loading " + id);
			return null;
		}
	}

	public byte[] getBinary(Reference ref, String filepath) {
		if (ref == null || ref.isCategory || ref.isDataPackage || filepath == null || filepath.isEmpty())
			return null;
		var id = repo.references.get(ref.getBinariesPath() + "/" + filepath, ref.commitId);
		return getBytes(id);
	}

	public byte[] getRepositoryInfo(Commit commit) {
		if (repo.commits.find().latest() == null)
			return new Gson().toJson(RepositoryInfo.create().json()).getBytes(StandardCharsets.UTF_8);
		return getBytes(repo.references.get(RepositoryInfo.FILE_NAME, commit.id));
	}

	public Map<String, Object> getVersionAndLastChange(Reference ref) {
		return stream(ref, new HashMap<String, Object>(),
				stream -> MetaDataParser.parseTop(stream, "version", "lastChange"));
	}

}

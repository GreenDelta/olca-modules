package org.openlca.git.find;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectStream;
import org.eclipse.jgit.lib.Repository;
import org.openlca.git.model.Reference;
import org.openlca.git.util.FieldDefinition;
import org.openlca.git.util.MetaDataParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Datasets {

	private static final Logger log = LoggerFactory.getLogger(Datasets.class);
	private final Repository repo;

	public static Datasets of(Repository repo) {
		return new Datasets(repo);
	}

	private Datasets(Repository repo) {
		this.repo = repo;
	}

	public String getName(ObjectId id) {
		var data = parse(id, "name");
		var name = data.get("name");
		return name != null ? name.toString() : null;
	}

	public Map<String, Object> parse(ObjectId id, String... fields) {
		return stream(id, new HashMap<>(), stream -> MetaDataParser.parse(stream, fields));
	}

	public Map<String, Object> parse(ObjectId id, List<FieldDefinition> defs) {
		return stream(id, new HashMap<>(), stream -> MetaDataParser.parse(stream, defs));
	}

	private <T> T stream(ObjectId id, T defaultValue, Function<ObjectStream, T> consumer) {
		if (id == null)
			return defaultValue;
		try (var reader = repo.getObjectDatabase().newReader();
				var stream = reader.open(id).openStream()) {
			return consumer.apply(stream);
		} catch (IOException e) {
			log.error("Error loading " + id);
			return null;
		}
	}

	public byte[] getBytes(ObjectId id) {
		if (id == null || id.equals(ObjectId.zeroId()))
			return null;
		try (var reader = repo.getObjectDatabase().newReader()) {
			return reader.open(id).getBytes();
		} catch (IOException e) {
			log.error("Error loading " + id);
			return null;
		}
	}

	public String get(Reference ref) {
		if (ref == null)
			return null;
		return get(ref.objectId);
	}

	public String get(ObjectId id) {
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
		if (ref == null || filepath == null || filepath.isEmpty())
			return null;
		var id = Ids.of(repo).get(ref.getBinariesPath() + "/" + filepath, ref.commitId);
		return getBytes(id);
	}

}

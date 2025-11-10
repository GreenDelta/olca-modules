package org.openlca.core.model;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.openlca.util.BinUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;

/**
 * A categorized entity is a root entity with a category.
 */
@MappedSuperclass
public abstract class RootEntity extends RefEntity {

	// @Version
	@Column(name = "version")
	public long version;

	@Column(name = "last_change")
	public long lastChange;

	@OneToOne
	@JoinColumn(name = "f_category")
	public Category category;

	/**
	 * Tags are stored in a single string separated by commas `,`.
	 */
	@Column(name = "tags")
	public String tags;

	/**
	 * If a data set belongs to a library, this field must contain
	 * the identifier (which is typically a combination of library
	 * name and version, e.g. ecoinvent_apos_3.6).
	 */
	@Column(name = "library")
	public String library;

	@Lob
	@Column(name = "other_properties")
	public byte[] otherProperties;

	/**
	 * Returns true if this data set is from a library.
	 */
	public boolean isFromLibrary() {
		return library != null;
	}

	/**
	 * Reads other attached properties of this entity as a Json object. This
	 * returns an empty object if there are no such properties available.
	 */
	public JsonObject readOtherProperties() {
		if (otherProperties == null)
			return new JsonObject();
		var bytes = BinUtils.gunzip(otherProperties);
		try (var stream = new ByteArrayInputStream(bytes);
				 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			return new Gson().fromJson(reader, JsonObject.class);
		} catch (Exception e) {
			throw new RuntimeException("failed to parse entity Json of " + this, e);
		}
	}

	/**
	 * Attaches the data of the given Json object as additional properties to
	 * this entity. Note that this just converts the data but writes nothing to
	 * the database or something else.
	 */
	public void writeOtherProperties(JsonObject json) {
		if (json == null) {
			otherProperties = null;
			return;
		}
		var data = new Gson().toJson(json).getBytes(StandardCharsets.UTF_8);
		otherProperties = BinUtils.gzip(data);
	}
}

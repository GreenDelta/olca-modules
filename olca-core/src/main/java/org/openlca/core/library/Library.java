package org.openlca.core.library;

import java.util.ArrayList;
import java.util.List;

import org.openlca.util.Pair;

public class Library {

	/**
	 * like https://docs.npmjs.com/files/package.json#name
	 */
	public String name;

	/**
	 * like https://docs.npmjs.com/files/package.json#version
	 */
	public String version;

	/**
	 * like https://docs.npmjs.com/files/package.json#description-1
	 */
	public String description;

	/**
	 * A list of [name, version] pairs of libraries on which this library depends.
	 * 
	 * like https://docs.npmjs.com/files/package.json#dependencies
	 */
	public final List<Pair<String, String>> dependencies = new ArrayList<>();

}

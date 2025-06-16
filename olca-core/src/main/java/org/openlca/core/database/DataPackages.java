package org.openlca.core.database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.util.Strings;

public class DataPackages {

	private final Map<String, DataPackage> packages;

	public DataPackages() {
		this.packages = new HashMap<>();
	}

	public DataPackages(Set<DataPackage> dataPackages) {
		this.packages = dataPackages.stream()
				.collect(Collectors.toMap(
						p -> p.name(),
						p -> p));
	}

	public DataPackage get(String name) {
		return packages.get(name);
	}

	public boolean contains(String name) {
		return get(name) != null;
	}

	public boolean isEmpty() {
		return packages.isEmpty();
	}

	public boolean isFromLibrary(Descriptor d) {
		return d != null && isLibrary(d.dataPackage);
	}

	public boolean isFromLibrary(RootEntity e) {
		return e != null && isLibrary(e.dataPackage);
	}

	public boolean isLibrary(String name) {
		if (Strings.nullOrEmpty(name))
			return false;
		var p = get(name);
		// check defensive, if package is not found assume its a library
		if (p == null)
			return true;
		return p.isLibrary();
	}

	public Set<DataPackage> getAll() {
		return new HashSet<>(packages.values());
	}

	public Set<String> getLibraries() {
		return packages.values().stream()
				.filter(DataPackage::isLibrary)
				.map(DataPackage::name)
				.collect(Collectors.toSet());
	}

}
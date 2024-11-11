package org.openlca.core.library;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.ZipReader;
import org.openlca.npy.Npy;
import org.openlca.util.Dirs;

public record Library(File folder) {

	public Library {
		Dirs.createIfAbsent(folder);
	}

	public static Library of(File folder) {
		return new Library(folder);
	}

	public LibraryInfo getInfo() {
		var file = new File(folder, "library.json");
		if (!file.exists())
			return LibraryInfo.of(folder.getName());
		var obj = Json.readObject(file);
		return obj.map(LibraryInfo::fromJson)
				.orElseGet(() -> LibraryInfo.of(folder.getName()));
	}

	public String name() {
		return folder.getName();
	}

	/**
	 * Get the direct dependencies of this library.
	 */
	public Set<Library> getDirectDependencies() {
		var info = getInfo();
		if (info.dependencies().isEmpty())
			return Collections.emptySet();
		var libDir = new LibraryDir(folder.getParentFile());
		return info.dependencies()
				.stream()
				.map(libDir::getLibrary)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toSet());
	}

	/**
	 * Get all other libraries this library depends on.
	 */
	public Set<Library> getTransitiveDependencies() {
		var deps = new HashSet<Library>();
		var queue = new ArrayDeque<>(getDirectDependencies());
		while (!queue.isEmpty()) {
			var next = queue.pop();
			deps.add(next);
			queue.addAll(next.getDirectDependencies());
		}
		return deps;
	}

	public void addDependency(Library dependency) {
		if (dependency == null)
			return;
		var info = getInfo();
		var depID = dependency.name();
		if (info.dependencies().contains(depID))
			return;
		info.dependencies().add(depID);
		info.writeTo(this);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		var other = (Library) o;
		return Objects.equals(getInfo(), other.getInfo());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getInfo());
	}

	/**
	 * Returns {@code true} when this library has matrix data.
	 */
	public boolean hasMatrices() {
		// there are ony two types of matrix libraries: in process
		// based libraries the tech. matrix A must be present, and
		// in pure LCIA libraries the characterization matrix C
		return hasMatrix(LibMatrix.A) || hasMatrix(LibMatrix.C);
	}

	public boolean hasMatrix(LibMatrix m) {
		return m.isPresentIn(this);
	}

	public Optional<MatrixReader> getMatrix(LibMatrix m) {
		return m.readFrom(this);
	}

	public Optional<double[]> getCosts() {
		var file = new File(folder, "costs.npy");
		if (!file.exists())
			return Optional.empty();
		var data = Npy.read(file).asDoubleArray().data();
		return Optional.of(data);
	}

	public Optional<double[]> getColumn(LibMatrix m, int column) {
		return m.readColumnFrom(this, column);
	}

	/**
	 * Get the diagonal of the given library matrix.
	 */
	public Optional<double[]> getDiagonal(LibMatrix m) {
		return m.readDiagonalFrom(this);
	}

	/**
	 * Opens the zip-file that contains the JSON (meta-) data of this library.
	 *
	 * @return the opened meta-data store.
	 */
	public ZipReader openJsonZip() {
		return ZipReader.of(getJsonZip());
	}

	public File getJsonZip() {
		return new File(folder, "meta.zip");
	}
}

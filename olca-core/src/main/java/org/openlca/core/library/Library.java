package org.openlca.core.library;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ImpactCategoryDao;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.database.RootEntityDao;
import org.openlca.core.matrix.format.MatrixReader;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.matrix.io.index.IxEnviIndex;
import org.openlca.core.matrix.io.index.IxImpactIndex;
import org.openlca.core.matrix.io.index.IxTechIndex;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.ZipStore;
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
	 * Read the index of technosphere flows from this library. Note that this
	 * method returns an empty index if this library hase no technosphere flows.
	 */
	public IxTechIndex readTechIndex() {
		return IxTechIndex.readFromDir(this.folder);
	}

	/**
	 * Returns the index of technosphere flows in matrix order. If this library
	 * has no technosphere flows or if this index is not in sync with the
	 * database, an empty option is returned.
	 */
	public Optional<TechIndex> syncTechIndex(IDatabase db) {
		var processes = descriptors(new ProcessDao(db));
		var products = descriptors(new FlowDao(db));
		TechIndex index = null;
		var libIdx = readTechIndex();
		for (var i : libIdx.items()) {
			var process = processes.get(i.provider().id());
			var product = products.get(i.flow().id());
			if (process == null || product == null)
				return Optional.empty();
			if (index == null) {
				index = new TechIndex(TechFlow.of(process, product));
			} else {
				index.add(TechFlow.of(process, product));
			}
		}
		return Optional.ofNullable(index);
	}

	/**
	 * Read the index of environmental flows from this library. Note that this
	 * method returns an empty index if this library has no such flows.
	 */
	public IxEnviIndex readEnviIndex() {
		return IxEnviIndex.readFromDir(this.folder);
	}

	/**
	 * Returns the index of environmental flows of the library in matrix order. If
	 * this library has no environmental flows or if this index is not in sync
	 * with the database, an empty option is returned.
	 */
	public Optional<EnviIndex> syncEnviIndex(IDatabase db) {
		var libIdx = readEnviIndex();
		if (libIdx.isEmpty())
			return Optional.empty();

		var info = getInfo();
		var index = info.isRegionalized()
			? EnviIndex.createRegionalized()
			: EnviIndex.create();

		var flows = descriptors(new FlowDao(db));
		Map<String, LocationDescriptor> locations = info.isRegionalized()
			? descriptors(new LocationDao(db))
			: Collections.emptyMap();

		for (var i : libIdx.items()) {
			var flow = flows.get(i.flow().id());
			if (flow == null)
				return Optional.empty();
			var location = info.isRegionalized() && !i.location().isEmpty()
				? locations.get(i.location().id())
				: null;
			var enviFlow = i.isInput()
				? EnviFlow.inputOf(flow, location)
				: EnviFlow.outputOf(flow, location);
			index.add(enviFlow);
		}
		return Optional.of(index);
	}

	/**
	 * Read the index of impact indicators from this library. Note that this
	 * method returns an empty index if this library has no such indicators.
	 */
	public IxImpactIndex readImpactIndex() {
		return IxImpactIndex.readFromDir(this.folder);
	}

	/**
	 * Returns the index of impact categories of this library in matrix order. If
	 * this library has no impact categories or if this index is not in sync
	 * with the database, an empty option is returned.
	 */
	public Optional<ImpactIndex> syncImpactIndex(IDatabase db) {
		var libIdx = readImpactIndex();
		if (libIdx.isEmpty())
			return Optional.empty();

		var index = new ImpactIndex();
		var impacts = descriptors(new ImpactCategoryDao(db));
		for (var i : libIdx.items()) {
			var impact = impacts.get(i.impact().id());
			if (impact == null)
				return Optional.empty();
			index.add(impact);
		}
		return Optional.of(index);
	}

	private <T extends RootDescriptor> Map<String, T> descriptors(
		RootEntityDao<?, T> dao) {
		return dao.getDescriptors()
			.stream()
			.collect(Collectors.toMap(
				d -> d.refId,
				d -> d,
				(d1, d2) -> d1));
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
	 * Creates a list of exchanges from the library matrices that describe the
	 * inputs and outputs of the given library product. The meta-data of the
	 * exchanges are synchronized with the given databases. Thus, this library
	 * needs to be mounted to the given database.
	 */
	public List<Exchange> getExchanges(TechFlow product, IDatabase db) {
		return Exchanges.join(this, db).getFor(product);
	}

	public List<ImpactFactor> getImpactFactors(
		ImpactDescriptor impact, IDatabase db) {
		return ImpactFactors.join(this, db).getFor(impact);
	}

	/**
	 * Opens the zip-file that contains the JSON (meta-) data of this library.
	 * This file is created if it does not exist yet.
	 *
	 * @return the opened meta-data store.
	 */
	public ZipStore openJsonZip() {
		var zip = new File(folder, "meta.zip");
		try {
			return ZipStore.open(zip);
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to open library meta data zip: " + zip, e);
		}
	}
}

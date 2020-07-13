package org.openlca.core.library;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import org.openlca.core.database.CategorizedEntityDao;
import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.jsonld.Json;

public class Library {

	/**
	 * The folder where the library files are stored.
	 */
	public final File folder;
	private LibraryInfo info;

	public Library(File folder) {
		this.folder = folder;
	}

	public LibraryInfo getInfo() {
		if (info != null)
			return info;
		var file = new File(folder, "library.json");
		var obj = Json.readObject(file);
		if (obj.isEmpty())
			throw new RuntimeException("failed to read " + file);
		info = LibraryInfo.fromJson(obj.get());
		return info;
	}

	/**
	 * Returns the products of the library in corresponding matrix order. If this
	 * information is not present or something went wrong while synchronizing
	 * the product index with the database, an empty array is returned. Of
	 * course, this only works when this library is mounted to that database.
	 */
	public ProcessProduct[] syncProducts(IDatabase db) {
		var array = Json.readArray(new File(folder, "index_A.json"));
		if (array.isEmpty())
			return new ProcessProduct[0];
		var processes = descriptors(new ProcessDao(db));
		var products = descriptors(new FlowDao(db));
		var index = new ArrayList<ProcessProduct>();
		for (var elem : array.get()) {
			if (!elem.isJsonObject())
				return new ProcessProduct[0];
			var obj = elem.getAsJsonObject();
			var procID = Json.getRefId(obj, "process");
			var flowID = Json.getRefId(obj, "flow");
			if (procID == null || flowID == null)
				return new ProcessProduct[0];

			var process = processes.get(procID);
			var product = products.get(flowID);
			if (process == null || product == null)
				return new ProcessProduct[0];
			index.add(ProcessProduct.of(process, product));
		}
		return index.toArray(new ProcessProduct[0]);
	}

	/**
	 * Returns the elementary flows of the library in corresponding matrix
	 * order. If this information is not present or something went wrong
	 * while synchronizing the flow index with the database, an empty array
	 * is returned. Of course, this only works when this library is mounted
	 * to that database.
	 */
	public IndexFlow[] syncElementaryFlows(IDatabase db) {
		var array = Json.readArray(new File(folder, "index_B.json"));
		if (array.isEmpty())
			return new IndexFlow[0];
		var flows = descriptors(new FlowDao(db));
		var locations = descriptors(new LocationDao(db));

		var index = new ArrayList<IndexFlow>();
		for (var elem : array.get()) {
			if (!elem.isJsonObject())
				return new IndexFlow[0];
			var obj = elem.getAsJsonObject();
			var flowID = Json.getRefId(obj, "flow");
			if (flowID == null)
				return new IndexFlow[0];
			var flow = flows.get(flowID);

			var isInput = Json.getBool(obj, "isInput", false);
			var locationID = Json.getRefId(obj, "location");
			if (locationID != null) {
				var location = locations.get(locationID);
				if (location == null)
					return new IndexFlow[0];
				index.add(isInput
						? IndexFlow.ofInput(flow, location)
						: IndexFlow.ofOutput(flow, location));
				continue;
			}

			index.add(isInput
					? IndexFlow.ofInput(flow)
					: IndexFlow.ofOutput(flow));
		}
		return index.toArray(new IndexFlow[0]);
	}

	private <T extends CategorizedDescriptor> Map<String, T> descriptors(
			CategorizedEntityDao<?, T> dao) {
		return dao.getDescriptors()
				.stream()
				.collect(Collectors.toMap(
						d -> d.refId,
						d -> d));
	}
}

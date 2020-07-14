package org.openlca.core.library;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.CategoryPathBuilder;
import org.openlca.util.Exceptions;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IndexWriter implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final File folder;
	private final IDatabase db;
	private final MatrixData data;

	private final Map<Long, String> locationCodes;
	private CategoryPathBuilder categories;

	IndexWriter(File folder, MatrixData data, IDatabase db) {
		this.folder = folder;
		this.data = data;
		this.db = db;
		this.locationCodes = new LocationDao(db).getCodes();
		this.categories = new CategoryPathBuilder(db);
	}

	@Override
	public void run() {
		log.info("write matrix indices");
		var products = Proto.ProductIndex.newBuilder();
		data.techIndex.each((index, product) -> {
			var entry = Proto.ProductEntry.newBuilder();
			entry.setIndex(index);
			entry.setProcess(process(product.process));
			entry.setProduct(flow(product.flow));
		});
		write("index_A.bin", out -> products.build().writeTo(out));
	}

	private Proto.Process process(CategorizedDescriptor d) {
		var proto = Proto.Process.newBuilder();
		if (d == null)
			return proto.build();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		var category = categories.build(d.category);
		proto.setCategory(Strings.orEmpty(category));
		if (d instanceof ProcessDescriptor) {
			var loc = ((ProcessDescriptor) d).location;
			if (loc != null) {
				var code = locationCodes.get(loc);
				proto.setLocation(Strings.orEmpty(code));
			}
		}
		return proto.build();
	}

	private Proto.Flow flow(FlowDescriptor d) {
		var proto = Proto.Flow.newBuilder();
		if (d == null)
			return proto.build();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		var category = categories.build(d.category);
		proto.setCategory(Strings.orEmpty(category));
		proto.setUnit(Strings.orEmpty(d.))
	}

	private void write(String file, Output fn) {
		var f = new File(folder, file);
		try (var stream = new FileOutputStream(f);
			 var buffer = new BufferedOutputStream(stream)) {
			fn.accept(buffer);
		} catch (Exception e) {
			Exceptions.unchecked("failed to write file " + f, e);
		}
	}

	@FunctionalInterface
	interface Output {
		void accept(OutputStream out) throws IOException;
	}
}

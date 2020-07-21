package org.openlca.core.library;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.stream.Collectors;

import org.openlca.core.database.FlowPropertyDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.LocationDao;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.CategoryPathBuilder;
import org.openlca.util.Exceptions;
import org.openlca.util.Strings;

class IndexWriter implements Runnable {

	private final File folder;
	private final MatrixData data;

	private final Map<Long, String> locationCodes;
	private final CategoryPathBuilder categories;
	private final Map<Long, FlowProperty> quantities;

	IndexWriter(File folder, MatrixData data, IDatabase db) {
		this.folder = folder;
		this.data = data;
		this.locationCodes = new LocationDao(db).getCodes();
		this.categories = new CategoryPathBuilder(db);
		this.quantities = new FlowPropertyDao(db)
				.getAll()
				.stream()
				.collect(Collectors.toMap(q -> q.id, q -> q));
	}

	@Override
	public void run() {
		writeProductIndex();
		writeElemFlowIndex();
		writeImpactIndex();
	}

	private void writeProductIndex() {
		if (data.techIndex == null)
			return;
		var products = Proto.ProductIndex.newBuilder();
		data.techIndex.each((index, product) -> {
			var entry = Proto.ProductEntry.newBuilder();
			entry.setIndex(index);
			entry.setProcess(process(product.process));
			entry.setProduct(flow(product.flow));
			products.addProduct(entry.build());
		});
		write("index_A.bin", out -> products.build().writeTo(out));
	}

	private void writeElemFlowIndex() {
		if (data.flowIndex == null)
			return;
		var elemFlows = Proto.ElemFlowIndex.newBuilder();
		data.flowIndex.each((index, iFlow) -> {
			var entry = Proto.ElemFlowEntry.newBuilder();
			entry.setIndex(index);
			entry.setFlow(flow(iFlow.flow));
			if (iFlow.location != null) {
				entry.setLocation(location(iFlow.location));
			}
			entry.setIsInput(iFlow.isInput);
			elemFlows.addFlow(entry.build());
		});
		write("index_B.bin", out -> elemFlows.build().writeTo(out));
	}

	private void writeImpactIndex() {
		if (data.impactIndex == null)
			return;
		var impacts = Proto.ImpactIndex.newBuilder();
		data.impactIndex.each((index, impact) -> {
			var entry = Proto.ImpactEntry.newBuilder();
			entry.setIndex(index);
			entry.setImpact(impact(impact));
			impacts.addImpact(entry);
		});
		write("index_C.bin", out -> impacts.build().writeTo(out));
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
				proto.setLocationCode(Strings.orEmpty(code));
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
		if (d.flowType != null) {
			proto.setType(d.flowType.name());
		}
		var fp = quantities.get(d.refFlowPropertyId);
		if (fp != null && fp.unitGroup != null) {
			var unit = fp.unitGroup.referenceUnit;
			if (unit != null) {
				proto.setUnit(Strings.orEmpty(unit.name));
			}
		}
		return proto.build();
	}

	private Proto.Location location(LocationDescriptor d) {
		var proto = Proto.Location.newBuilder();
		if (d == null)
			return proto.build();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		proto.setCode(Strings.orEmpty(d.code));
		return proto.build();
	}

	private Proto.Impact impact(ImpactCategoryDescriptor d) {
		var proto = Proto.Impact.newBuilder();
		if (d == null)
			return proto.build();
		proto.setId(Strings.orEmpty(d.refId));
		proto.setName(Strings.orEmpty(d.name));
		proto.setUnit(Strings.orEmpty(d.referenceUnit));
		return proto.build();
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

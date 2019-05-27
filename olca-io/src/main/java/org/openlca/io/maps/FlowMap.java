package org.openlca.io.maps;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openlca.core.database.FlowDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.UnitDescriptor;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ParseDouble;

public class FlowMap extends BaseDescriptor {

	/** Description of the source system. */
	public BaseDescriptor source;

	/** Description of the target system. */
	public BaseDescriptor target;

	public final List<FlowMapEntry> entries = new ArrayList<>();

	private Map<String, FlowMapEntry> index;

	/**
	 * Get the mapping entry for the source flow with the given ID.
	 */
	public FlowMapEntry getEntry(String sourceFlowID) {
		if (index == null) {
			index = new HashMap<>();
			for (FlowMapEntry e : entries) {
				String sid = e.sourceFlowID();
				if (sid != null) {
					index.put(sid, e);
				}
			}
		}
		return index.get(sourceFlowID);
	}

	/**
	 * Reads the flow map with the given identifier from this package or the
	 * given database.
	 * 
	 * @deprecated we should remove implicit mappings that are loaded from this
	 *             package and also loading mappings from the database
	 */
	@Deprecated
	public static FlowMap of(String map, IDatabase db) {
		Logger log = LoggerFactory.getLogger(FlowMap.class);
		log.trace("Initialize flow assignment map {}.", map);
		FlowMap m = new FlowMap();
		m.name = map;
		try {
			HashSet<String> dbIDs = new HashSet<>();
			new FlowDao(db).getDescriptors().stream()
					.forEach(d -> dbIDs.add(d.refId));
			Maps.readAll(map, db, null, null, new ParseDouble()).forEach(r -> {
				String sourceID = Maps.getString(r, 0);
				String targetID = Maps.getString(r, 1);
				if (targetID == null || !dbIDs.contains(targetID))
					return;
				FlowMapEntry e = new FlowMapEntry();
				e.sourceFlow = new FlowRef();
				e.sourceFlow.flow = new FlowDescriptor();
				e.sourceFlow.flow.refId = sourceID;
				e.targetFlow = new FlowRef();
				e.targetFlow.flow = new FlowDescriptor();
				e.targetFlow.flow.refId = targetID;
				e.factor = Maps.getDouble(r, 2);
				m.entries.add(e);
			});
		} catch (Exception e) {
			log.error("Error while reading mapping file", e);
		}
		return m;
	}

	public static FlowMap fromCsv(File file) {
		FlowMap fm = new FlowMap();
		Maps.each(file, row -> {

			FlowMapEntry e = new FlowMapEntry();
			fm.entries.add(e);
			e.factor = Maps.getDouble(row, 2);

			// source flow
			String sid = Maps.getString(row, 0);
			if (Strings.notEmpty(sid)) {
				e.sourceFlow = new FlowRef();
				e.sourceFlow.flow = new FlowDescriptor();
				e.sourceFlow.flow.refId = sid;
				e.sourceFlow.flow.name = Maps.getString(row, 3);
				e.sourceFlow.categoryPath = Maps.getString(row, 4);
				// TODO: location: think about the descriptor fields

				// flow property
				String sprop = Maps.getString(row, 9);
				if (Strings.notEmpty(sprop)) {
					e.sourceFlow.property = new FlowPropertyDescriptor();
					e.sourceFlow.property.refId = sprop;
					e.sourceFlow.property.name = Maps.getString(row, 10);
				}

				// unit
				String sunit = Maps.getString(row, 13);
				if (Strings.notEmpty(sunit)) {
					e.sourceFlow.unit = new UnitDescriptor();
					e.sourceFlow.unit.refId = sunit;
					e.sourceFlow.unit.name = Maps.getString(row, 14);
				}
			}

			// target flow
			String tid = Maps.getString(row, 1);
			if (Strings.notEmpty(tid)) {
				e.targetFlow = new FlowRef();
				e.targetFlow.flow = new FlowDescriptor();
				e.targetFlow.flow.refId = tid;
				e.targetFlow.flow.name = Maps.getString(row, 6);
				e.targetFlow.categoryPath = Maps.getString(row, 7);
				// TODO: location: think about the descriptor fields

				// flow property
				String tprop = Maps.getString(row, 11);
				if (Strings.notEmpty(tprop)) {
					e.targetFlow.property = new FlowPropertyDescriptor();
					e.targetFlow.property.refId = tprop;
					e.targetFlow.property.name = Maps.getString(row, 12);
				}

				// unit
				String tunit = Maps.getString(row, 15);
				if (Strings.notEmpty(tunit)) {
					e.targetFlow.unit = new UnitDescriptor();
					e.targetFlow.unit.refId = tunit;
					e.targetFlow.unit.name = Maps.getString(row, 16);
				}

				// provider
				String prov = Maps.getString(row, 17);
				if (Strings.notEmpty(prov)) {
					e.targetFlow.provider = new ProcessDescriptor();
					e.targetFlow.provider.refId = prov;
					e.targetFlow.provider.name = Maps.getString(row, 18);
					// TODO: category, location: think about the descriptor
					// fields
				}
			}
		});
		return fm;
	}

	public static void toCsv(FlowMap fm, File file) {
		if (fm == null || file == null)
			return;
		Maps.write(file, fm.entries.stream().map(e -> {
			Object[] row = new Object[21];
			row[2] = e.factor;

			// source flow
			FlowRef s = e.sourceFlow;
			if (s != null) {

				// flow
				if (s.flow != null) {
					row[0] = s.flow.refId;
					row[3] = s.flow.name;
					row[4] = s.categoryPath;
					// TODO: location code
				}

				// flow property
				if (s.property != null) {
					row[9] = s.property.refId;
					row[10] = s.property.name;
				}

				// unit
				if (s.unit != null) {
					row[13] = s.unit.refId;
					row[14] = s.unit.name;
				}
			}

			// target flow
			FlowRef t = e.targetFlow;
			if (t != null) {

				// flow
				if (t.flow != null) {
					row[1] = t.flow.refId;
					row[6] = t.flow.name;
					row[7] = t.categoryPath;
					// TODO: location code
				}

				// flow property
				if (t.property != null) {
					row[11] = t.property.refId;
					row[12] = t.property.name;
				}

				// unit
				if (t.unit != null) {
					row[15] = t.unit.refId;
					row[16] = t.unit.name;
				}

				if (t.provider != null) {
					row[17] = t.provider.refId;
					row[18] = t.provider.name;
					// TODO: category & location
				}
			}
			return row;
		}));
	}
}

package org.openlca.io.xls.process;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openlca.commons.Strings;
import org.openlca.core.database.IDatabase;
import org.openlca.core.io.ExchangeProviderQueue;
import org.openlca.core.io.ImportLog;
import org.openlca.core.model.Process;
import org.openlca.core.model.Version;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.doc.ProcessDoc;
import org.openlca.jsonld.input.UpdateMode;

public class XlsProcessReader {

	private final IDatabase db;
	private final ExchangeProviderQueue providers;
	private final ImportLog log;
	private UpdateMode updates = UpdateMode.NEVER;

	private XlsProcessReader(IDatabase db) {
		this.db = Objects.requireNonNull(db);
		this.providers = ExchangeProviderQueue.create(db);
		this.log = new ImportLog();
	}

	public static XlsProcessReader of(IDatabase db) {
		return new XlsProcessReader(db);
	}

	public ImportLog log() {
		return log;
	}

	public XlsProcessReader withUpdates(UpdateMode mode) {
		if (mode != null) {
			this.updates = mode;
		}
		return this;
	}

	public List<ProcessDescriptor> syncAllFromFolder(File dir) {
		return dir != null && dir.isDirectory()
			? syncAll(dir.listFiles())
			: Collections.emptyList();
	}

	public List<ProcessDescriptor> syncAll(File[] files) {
		if (files == null)
			return Collections.emptyList();
		var synced = new ArrayList<ProcessDescriptor>();
		for (var file : files) {
			if (file == null)
				continue;
			var name = file.getName();
			if (!name.toLowerCase().endsWith(".xlsx"))
				continue;
			var process = sync(file).orElse(null);
			if (process == null)
				continue;
			synced.add(Descriptor.of(process));
		}
		return synced;
	}

	public Optional<Process> sync(File file) {
		try (var wb = WorkbookFactory.create(file)) {
			var info = readInfo(wb);
			if (info == null) {
				log.error("invalid workbook: " + file.getName());
				return Optional.empty();
			}
			var process = db.get(Process.class, info.refId);
			if (process != null) {
				if (skipUpdate(process, info))
					return Optional.of(process);
			} else {
				process = new Process();
			}

			// update info fields
			process.refId = info.refId;
			process.version = info.version;
			process.name = info.name;
			process.lastChange = info.lastChange;
			if (process.documentation == null) {
				process.documentation = new ProcessDoc();
			}

			// sync sheets
			var config = new InConfig(
				this, wb, process, new EntityIndex(db, log), db, log, providers);
			syncRefData(config);
			syncProcessData(config);

			var synced = process.id == 0
				? db.insert(process)
				: db.update(process);
			providers.pop(synced);
			return Optional.of(synced);
		} catch (IOException e) {
			throw new RuntimeException(
				"failed to read process from file: " + file);
		}
	}

	private boolean skipUpdate(Process current, ProcessDescriptor next) {
		return switch (updates) {
			case NEVER -> true;
			case ALWAYS -> false;
			case IF_NEWER -> current.version != next.version
				? current.version >= next.version
				: current.lastChange >= next.lastChange;
		};
	}

	private ProcessDescriptor readInfo(Workbook wb) {
		var sheet = wb.getSheet(Tab.GENERAL_INFO.label());
		if (sheet == null)
			return null;
		var reader = new SheetReader(sheet);
		var info = reader.read(Section.GENERAL_INFO);
		var refId = info.str(Field.UUID);
		if (Strings.isBlank(refId))
			return null;
		var d = new ProcessDescriptor();
		d.refId = refId;
		d.name = info.str(Field.NAME);
		d.version = Version.fromString(info.str(Field.VERSION)).getValue();
		var lastChange = info.date(Field.LAST_CHANGE);
		d.lastChange = lastChange != null
			? lastChange.getTime()
			: 0;
		return d;
	}

	private void syncRefData(InConfig config) {
		// order is important here
		InActorSync.sync(config);
		InSourceSync.sync(config);
		InUnitSync.sync(config);
		InCurrencySync.sync(config);
		InLocationSync.sync(config);
		InFlowSync.sync(config);
		InProviderSync.sync(config);
	}

	private void syncProcessData(InConfig config) {
		InInfoSync.sync(config);
		InExchangeSync.sync(config);
		InAllocationSync.sync(config);
		InParameterSync.sync(config);
		InDocSync.sync(config);
		InReviewSync.sync(config);
		InComplianceSync.sync(config);
	}
}

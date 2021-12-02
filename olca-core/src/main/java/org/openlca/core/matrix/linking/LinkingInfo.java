package org.openlca.core.matrix.linking;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * Tries to determine how the processes in a database should be linked; more
 * specifically, whether a full scan on the exchange table makes sense or not.
 */
public class LinkingInfo {

	// We take the current ID sequence number to decide if we have a large or
	// small database. This number indicates how many objects are in the database.
	// Ideally, we would look on the number of exchanges but executing a
	// count-query on the exchange table is very slow.
	private final long SMALL_SEQUENCE_LIMIT = 5_000_000L;

	private final IDatabase db;
	private final List<ProcessDescriptor> processes;
	private long sequenceCount;
	private long libraryCount;
	private long resultCount;

	// The number of unit processes. Note that input-output models typically
	// result in very large unit process databases which we want to link eagerly.
	private long unitCount;

	private LinkingInfo(IDatabase db) {
		this.db = db;
		var sql = NativeSql.on(db);

		var seqQuery = "select seq_count from sequence" +
			" where seq_name = 'entity_seq'";
		sql.query(seqQuery, r -> {
			sequenceCount = r.getLong(1);
			return false;
		});

		processes = new ProcessDao(db).getDescriptors();
		for (var d : processes) {
			if (d.isFromLibrary()) {
				libraryCount++;
			} else if (d.processType == ProcessType.LCI_RESULT) {
				resultCount++;
			} else {
				unitCount++;
			}
		}
	}

	public static LinkingInfo of(IDatabase db) {
		return new LinkingInfo(db);
	}

	public IDatabase db() {
		return db;
	}

	public List<ProcessDescriptor> processes() {
		return processes;
	}

	public boolean preferLazy() {
		// prefer lazy linking for databases with linked libraries note that the
		// sequence number is small in this case, but we still want lazy linking
		// here
		if (libraryCount > (unitCount + resultCount))
			return true;

		// prefer lazy linking if the database is large and is dominated by LCI
		// results and/or library processes (but not for large unit process
		// databases like big IO models)
		return sequenceCount > SMALL_SEQUENCE_LIMIT
			&& (libraryCount + resultCount) > unitCount;
	}

}


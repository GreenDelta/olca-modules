package org.openlca.io.olca.systransfer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessLink;
import org.openlca.io.olca.SeqMap;
import org.openlca.io.olca.TransferContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ExchangeFinder {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final IDatabase source;
	private final IDatabase target;
	private final SeqMap seq;

	private ExchangeFinder(IDatabase source, IDatabase target, SeqMap seq) {
		this.source = source;
		this.target = target;
		this.seq = seq;
	}

	static ExchangeFinder of(TransferContext ctx) {
		if (ctx == null)
			throw new IllegalArgumentException("TransferContext is null");
		return new ExchangeFinder(ctx.source(), ctx.target(), ctx.seq());
	}

	long find(ProcessLink link) {
		if (link == null || link.exchangeId == 0)
			return -1;

		var sourceRef = getSourceRef(link.exchangeId);
		if (sourceRef == null)
			return -1;

		long targetProcessId = seq.get(ModelType.PROCESS, sourceRef.processId);
		if (targetProcessId == 0)
			return -1;

		return getTargetExchangeId(targetProcessId, sourceRef.internalId);
	}

	private ExchangeRef getSourceRef(long exchangeId) {
		var sql = "select f_owner, internal_id from tbl_exchanges where id="
				+ exchangeId;
		try {
			var ref = new ExchangeRef[1];
			NativeSql.on(source).query(sql, r -> {
				ref[0] = new ExchangeRef(r.getLong(1), r.getInt(2));
				return false;
			});
			return ref[0];
		} catch (Exception e) {
			log.error("failed to query source exchange: {}", sql, e);
			return null;
		}
	}

	private long getTargetExchangeId(long processId, int internalId) {
		var sql = "select id from tbl_exchanges where f_owner=" + processId
				+ " and internal_id=" + internalId;
		try {
			long[] id = { -1 };
			NativeSql.on(target).query(sql, r -> {
				id[0] = r.getLong(1);
				return false;
			});
			return id[0];
		} catch (Exception e) {
			log.error("failed to query target exchange: {}", sql, e);
			return -1;
		}
	}

	private record ExchangeRef(long processId, int internalId) {
	}

}

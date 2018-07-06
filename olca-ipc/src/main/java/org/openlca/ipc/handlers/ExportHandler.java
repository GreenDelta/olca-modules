package org.openlca.ipc.handlers;

import java.io.File;

import org.openlca.core.database.EntityCache;
import org.openlca.core.results.ContributionResult;
import org.openlca.core.results.ContributionResultProvider;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.FullResultProvider;
import org.openlca.core.results.SimpleResult;
import org.openlca.core.results.SimpleResultProvider;
import org.openlca.io.xls.results.system.ResultExport;
import org.openlca.ipc.Responses;
import org.openlca.ipc.Rpc;
import org.openlca.ipc.RpcRequest;
import org.openlca.ipc.RpcResponse;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public class ExportHandler {

	private final HandlerContext context;

	public ExportHandler(HandlerContext context) {
		this.context = context;
	}

	@Rpc("export/excel")
	public RpcResponse excel(RpcRequest req) {
		if (req == null || req.params == null || !req.params.isJsonObject())
			return Responses.badRequest("No @id given", req);
		JsonObject obj = req.params.getAsJsonObject();
		String id = Json.getString(obj, "@id");
		if (id == null)
			return Responses.badRequest("No `@id` given", req);
		String path = Json.getString(obj, "path");
		if (path == null)
			return Responses.badRequest("No `path` given", req);
		Object val = context.cache.get(id);
		if (!(val instanceof CachedResult))
			return Responses.notImplemented("The Excel export is currently"
					+ " only implemented for calculation results", req);
		CachedResult r = (CachedResult) val;
		ResultExport export = new ResultExport(r.setup, getProvider(r.result),
				new File(path));
		export.run();
		if (export.doneWithSuccess())
			return Responses.ok("Exported to " + path, req);
		else
			return Responses.internalServerError("Export failed", req);
	}

	private SimpleResultProvider<?> getProvider(
			SimpleResult r) {
		EntityCache cache = EntityCache.create(context.db);
		if (r instanceof FullResult)
			return new FullResultProvider((FullResult) r, cache);
		if (r instanceof ContributionResult)
			return new ContributionResultProvider<>(
					(ContributionResult) r, cache);
		return new SimpleResultProvider<>(r, cache);
	}

}

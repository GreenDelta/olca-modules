package org.openlca.ipc.handlers;

import org.openlca.core.database.IDatabase;
import org.openlca.ipc.RpcRequest;
import org.openlca.jsonld.output.DbRefs;

record ResultRequest(RpcRequest req, IDatabase db, DbRefs refs) {
}

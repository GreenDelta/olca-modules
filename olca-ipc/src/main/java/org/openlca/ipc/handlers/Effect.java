package org.openlca.ipc.handlers;

import org.openlca.ipc.RpcResponse;

/**
 * An effect encodes the result of some internal IPC call. On success, it will
 * contain a value and an RPC error when the call failed.
 */
record Effect<T>(T value, RpcResponse error) {

	static <T> Effect<T> ok(T value) {
		return new Effect<>(value, null);
	}

	static <T> Effect<T> error(RpcResponse err) {
		return new Effect<>(null, err);
	}

	boolean isError() {
		return error != null;
	}

}

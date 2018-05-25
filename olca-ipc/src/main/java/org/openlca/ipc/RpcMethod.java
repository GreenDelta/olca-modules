package org.openlca.ipc;

public enum RpcMethod {

	CALCULATE("calculate"),
	DISPOSE("dispose"),

	GET_DESCRIPTOR("get/descriptor"),
	GET_DESCRIPTORS("get/descriptors"),

	GET_MODEL("get/model"),
	GET_MODELS("get/models"),
	INSERT_MODEL("insert/model"),
	UPDATE_MODEL("update/model"),
	DELETE_MODEL("delete/model");

	private final String value;

	RpcMethod(String value) {
		this.value = value;
	}

	public static RpcMethod of(RpcRequest req) {
		if (req == null || req.method == null)
			return null;
		for (RpcMethod m : RpcMethod.values()) {
			if (m.value.equalsIgnoreCase(req.method)) {
				return m;
			}
		}
		return null;
	}
}

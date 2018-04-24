package org.openlca.ipc;

public enum Method {

	GET_MODEL("get/model"),
	INSERT_MODEL("insert/model"),
	UPDATE_MODEL("update/model"),
	DELETE_MODEL("delete/model");

	private final String value;

	Method(String value) {
		this.value = value;
	}

	public Method of(RpcRequest req) {
		if (req == null ||req.method == null)
			return null;
		for (Method m : Method.values()) {
			if (m.value.equalsIgnoreCase(req.method)) {
				return m;
			}
		}
		return null;
	}
}

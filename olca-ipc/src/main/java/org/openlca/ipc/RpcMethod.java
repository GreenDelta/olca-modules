package org.openlca.ipc;

public enum RpcMethod {

	CALCULATE("calculate"), DISPOSE("dispose"),

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

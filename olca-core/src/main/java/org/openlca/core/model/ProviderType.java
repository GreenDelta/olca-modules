package org.openlca.core.model;

public enum ProviderType {

	PROCESS(0),

	SUB_SYSTEM(1),

	RESULT(2);

	public final byte value;

	ProviderType(int value) {
		this.value = (byte) value;
	}

	public static ProviderType of(byte value) {
		return switch (value) {
			case 1 -> SUB_SYSTEM;
			case 2 -> RESULT;
			default -> PROCESS;
		};
	}

	public ModelType toModelType() {
		return switch (this) {
			case SUB_SYSTEM -> ModelType.PRODUCT_SYSTEM;
			case RESULT -> ModelType.RESULT;
			default -> ModelType.PROCESS;
		};
	}
}

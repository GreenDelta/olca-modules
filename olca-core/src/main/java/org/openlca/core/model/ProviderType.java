package org.openlca.core.model;

/// The provider type in process links.
public interface ProviderType {

	/// PROCESS is always the default provider type if not specified.
	byte PROCESS = 0;
	byte SUB_SYSTEM = 1;
	byte RESULT = 2;

	static byte of(ModelType type) {
		if (type == null)
			return PROCESS;
		return switch (type) {
			case PRODUCT_SYSTEM -> SUB_SYSTEM;
			case RESULT -> RESULT;
			default -> PROCESS;
		};
	}

	static ModelType toModelType(byte type) {
		return switch (type) {
			case SUB_SYSTEM -> ModelType.PRODUCT_SYSTEM;
			case RESULT -> ModelType.RESULT;
			default -> ModelType.PROCESS;
		};
	}

	static Class<? extends RootEntity> toModelClass(byte type) {
		return switch (type) {
			case SUB_SYSTEM -> ProductSystem.class;
			case RESULT -> Result.class;
			default -> Process.class;
		};
	}
}

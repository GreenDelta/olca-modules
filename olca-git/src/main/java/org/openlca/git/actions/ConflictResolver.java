package org.openlca.git.actions;

import org.openlca.git.model.ModelRef;

import com.google.gson.JsonObject;

public interface ConflictResolver {

	static final ConflictResolver NULL = new ConflictResolver() {
		@Override
		public boolean isConflict(ModelRef ref) {
			return false;
		}

		@Override
		public ConflictResolutionType peekConflictResolution(ModelRef ref) {
			return null;
		}

		@Override
		public ConflictResolution resolveConflict(ModelRef ref, JsonObject fromHistory) {
			return null;
		}
	};

	boolean isConflict(ModelRef ref);

	ConflictResolutionType peekConflictResolution(ModelRef ref);

	ConflictResolution resolveConflict(ModelRef ref, JsonObject fromHistory);

	public class ConflictResolution {

		public final ConflictResolutionType type;
		public final JsonObject data;

		private ConflictResolution(ConflictResolutionType type, JsonObject mergedData) {
			this.type = type;
			this.data = mergedData;
		}

		public static ConflictResolution overwrite() {
			return new ConflictResolution(ConflictResolutionType.OVERWRITE, null);
		}

		public static ConflictResolution keep() {
			return new ConflictResolution(ConflictResolutionType.KEEP, null);
		}

		public static ConflictResolution isEqual() {
			return new ConflictResolution(ConflictResolutionType.IS_EQUAL, null);
		}

		public static ConflictResolution merge(JsonObject mergedData) {
			return new ConflictResolution(ConflictResolutionType.MERGE, mergedData);
		}

	}

	public enum ConflictResolutionType {
		OVERWRITE, KEEP, MERGE, IS_EQUAL;
	}

}

package org.openlca.git.actions;

import org.openlca.git.model.ModelRef;

import com.google.gson.JsonObject;

public interface ConflictResolver {

	boolean isConflict(ModelRef ref);

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

		public static ConflictResolution merge(JsonObject mergedData) {
			return new ConflictResolution(ConflictResolutionType.MERGE, mergedData);
		}

	}

	public enum ConflictResolutionType {
		OVERWRITE, KEEP, MERGE;
	}

}

package org.openlca.git.actions;

import org.openlca.git.model.Reference;

import com.google.gson.JsonObject;

public interface ConflictResolver {

	boolean isConflict(Reference ref);

	ConflictResolution resolveConflict(Reference ref, JsonObject fromHistory);

	public class ConflictResolution {

		public final ConflictResolutionType type;
		public final JsonObject data;

		private ConflictResolution(ConflictResolutionType type, JsonObject mergedData) {
			this.type = type;
			this.data = mergedData;
		}

		public static ConflictResolution overwriteLocal() {
			return new ConflictResolution(ConflictResolutionType.OVERWRITE_LOCAL, null);
		}

		public static ConflictResolution keepLocal() {
			return new ConflictResolution(ConflictResolutionType.KEEP_LOCAL, null);
		}

		public static ConflictResolution merge(JsonObject mergedData) {
			return new ConflictResolution(ConflictResolutionType.MERGE, mergedData);
		}

	}

	public enum ConflictResolutionType {
		OVERWRITE_LOCAL, KEEP_LOCAL, MERGE;
	}

}

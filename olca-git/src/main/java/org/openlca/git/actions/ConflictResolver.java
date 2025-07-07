package org.openlca.git.actions;

import org.openlca.core.model.TypedRefId;

import com.google.gson.JsonObject;

public interface ConflictResolver {

	static final ConflictResolver NULL = new ConflictResolver() {
		@Override
		public boolean isConflict(TypedRefId ref) {
			return false;
		}

		@Override
		public ConflictResolutionInfo peekConflictResolution(TypedRefId ref) {
			return null;
		}

		@Override
		public ConflictResolutionInfo peekConflictResolutionWithWorkspace(TypedRefId ref) {
			return null;
		}

		@Override
		public ConflictResolution resolveConflict(TypedRefId ref, JsonObject fromHistory) {
			return null;
		}

		@Override
		public ConflictResolution resolveConflictWithWorkspace(TypedRefId ref, JsonObject other) {
			return null;
		}
	};

	/**
	 * Return if a conflict exists for the given ref
	 */
	boolean isConflict(TypedRefId ref);

	/**
	 * Return either local or workspace conflict resolution info for the given
	 * ref, this way - during merge - it can be determined if data sets need to
	 * be loaded for merging.
	 */
	ConflictResolutionInfo peekConflictResolution(TypedRefId ref);

	/**
	 * Return workspace conflict resolution info for the given ref, this way -
	 * during merge - it can be determined if data sets need to be loaded for
	 * merging.
	 */
	ConflictResolutionInfo peekConflictResolutionWithWorkspace(TypedRefId ref);

	/**
	 * Return the conflict resolution for the given ref, if conflicts exist with
	 * both LOCAL and WORKSPACE, return LOCAL conflict
	 */
	ConflictResolution resolveConflict(TypedRefId ref, JsonObject other);

	/**
	 * In case that conflicts with both LOCAL and WORKSPACE exist, this method
	 * is used to retrieve the WORKSPACE conflict
	 */
	ConflictResolution resolveConflictWithWorkspace(TypedRefId ref, JsonObject other);

	public class ConflictResolution extends ConflictResolutionInfo {

		public final JsonObject data;

		private ConflictResolution(ConflictResolutionType type, GitContext conflictWith, JsonObject mergedData) {
			super(type, conflictWith);
			this.data = mergedData;
		}

		public static ConflictResolution isEqual(GitContext context) {
			return new ConflictResolution(ConflictResolutionType.IS_EQUAL, context, null);
		}

		public static ConflictResolution keep(GitContext context) {
			return new ConflictResolution(ConflictResolutionType.KEEP, context, null);
		}

		public static ConflictResolution overwrite(GitContext context) {
			return new ConflictResolution(ConflictResolutionType.OVERWRITE, context, null);
		}

		public static ConflictResolution merge(GitContext context, JsonObject mergedData) {
			return new ConflictResolution(ConflictResolutionType.MERGE, context, mergedData);
		}

	}

	public enum ConflictResolutionType {

		IS_EQUAL, KEEP, OVERWRITE, MERGE;

	}

	public class ConflictResolutionInfo {

		public final ConflictResolutionType type;
		public final GitContext context;

		private ConflictResolutionInfo(ConflictResolutionType type, GitContext context) {
			this.type = type;
			this.context = context;
		}

	}

	public enum GitContext {

		LOCAL, WORKSPACE;

	}

}

package org.openlca.git.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.ObjectId;
import org.openlca.git.model.ModelRef;
import org.openlca.git.model.Reference;

class ImportResults {

	private final List<ImportResult> list = new ArrayList<>();

	void add(Reference ref, ImportState state) {
		list.add(new ImportResult(ref.path, state, ref.objectId));
	}

	void add(ModelRef ref, ImportState state) {
		list.add(new ImportResult(ref.path, state, null));
	}

	List<ImportResult> get(ImportState state) {
		return list.stream()
				.filter(ref -> ref.state == state)
				.collect(Collectors.toList());
	}

	int size() {
		return list.size();
	}

	static class ImportResult extends ModelRef {

		final ImportState state;
		final ObjectId objectId;

		private ImportResult(String path, ImportState state, ObjectId objectId) {
			super(path);
			this.state = state;
			this.objectId = objectId;
		}

		@Override
		protected String fieldsToString() {
			var s = super.fieldsToString();
			return s + ", state=" + state + ", objectId=" + ObjectId.toString(objectId);
		}

	}

	static enum ImportState {

		UPDATED, DELETED, MERGED, KEPT, KEPT_DELETED;

	}

}
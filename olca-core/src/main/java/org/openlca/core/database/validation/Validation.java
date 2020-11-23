package org.openlca.core.database.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;

public class Validation {

	final IDatabase db;
	final List<Issue> issues = new ArrayList<>();
	int maxIssues = -1;

	/**
	 * The maximum type for the validation. Everything below and including this
	 * type is validated. This is related to our usage hierarchy: if maxType is
	 * Process, then typically all flows are validated.
	 */
	private ModelType maxType;

	private HashMap<ModelType, TLongHashSet> filter;

	private Validation(IDatabase db) {
		this.db = db;
	}

	public static Validation withConfig(IDatabase db) {
		return new Validation(db);
	}

	public void maxIssues(int maxIssues) {
		this.maxIssues = maxIssues;
	}

	/**
	 * Validates only the things that are reachable from the given models
	 * including them. Lower types may are also validated even when they are
	 * not referenced by the given models (e.g. we typically check all unit
	 * groups when a list of processes is given even when they are not used in
	 * these processes).
	 */
	public void subSetOf(List<Descriptor> descriptors) {

	}

	public static List<Issue> of(IDatabase db) {
		if (db == null)
			return Collections.emptyList();
		return new Validation(db).run();
	}

	public List<Issue> run() {
		issues.clear();

		return Collections.emptyList();
	}

}

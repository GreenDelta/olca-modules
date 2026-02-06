package org.openlca.util;

import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.DQSystem;

/// We have some predefined IDs of some common data quality systems. We may
/// link them in some imports when these are available in the respective
/// database.
public class DQSystems {

	private static final String EI_DQS = "e7ac7cf6-5457-453e-99f9-d889826fffe8";
	private static final String ILCD_DQS = "506e2c14-4d09-49fd-9460-97e8085b2b55";

	/// Returns the ILCD data quality system from the database or `null` if the
	/// database does not contain it.
	public static DQSystem ilcd(IDatabase db) {
		return db != null
			? new DQSystemDao(db).getForRefId(ILCD_DQS)
			: null;
	}

	/// Returns the ecoinvent data quality system from the database or `null` if
	/// the database does not contain it.
	public static DQSystem ecoinvent(IDatabase db) {
		return db != null
			? new DQSystemDao(db).getForRefId(EI_DQS)
			: null;
	}
}

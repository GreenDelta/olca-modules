package org.openlca.util;

import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;

public class DQSystems {

	private static final String EI_DQS = "e7ac7cf6-5457-453e-99f9-d889826fffe8";

	/**
	 * Returns the ecoinvent data quality system from the database. It creates a
	 * new instance of the system and inserts it in the database if it does not
	 * exist yet.
	 */
	public static DQSystem ecoinvent(IDatabase db) {
		if (db == null)
			return null;
		DQSystemDao dao = new DQSystemDao(db);
		DQSystem dqs = dao.getForRefId(EI_DQS);
		if (dqs == null) {
			dqs = dao.insert(ecoinvent());
		}
		return dqs;
	}

	/** Creates an instance of the ecoinvent data quality system. */
	public static DQSystem ecoinvent() {
		DQSystem system = new DQSystem();
		system.setName("ecoinvent data quality system");
		system.setRefId(EI_DQS);
		system.hasUncertainties = true;
		String[] indicators = eiIndicators();
		String[][] scores = eiScores();
		double[][] uncertainties = eiUncertainties();
		for (int i = 0; i < indicators.length; i++) {
			DQIndicator indicator = new DQIndicator();
			indicator.name = indicators[i];
			indicator.position = i + 1;
			for (int j = 0; j < scores[i].length; j++) {
				DQScore score = new DQScore();
				score.description = scores[i][j];
				score.position = j + 1;
				score.uncertainty = uncertainties[i][j];
				indicator.scores.add(score);
			}
			system.indicators.add(indicator);
		}
		return system;
	}

	private static String[] eiIndicators() {
		return new String[] {
				"Reliability",
				"Completeness",
				"Temporal correlation",
				"Geographical correlation",
				"Further technological correlation" };
	}

	private static String[][] eiScores() {
		return new String[][] {
				{
						"Verified data based on measurements",
						"Verified data partly based on assumptions or non-verified data based on measurements",
						"Non-verified data partly based on qualified estimates",
						"Qualified estimate (e.g. by industrial expert)",
						"Non-qualified estimates"
				}, {
						"Representative data from all sites relevant for the market considered, over and adequate period to even out normal fluctuations",
						"Representative data from > 50% of the sites relevant for the market considered, over an adequate period to even out normal fluctuations",
						"Representative data from only some sites (<< 50%) relevant for the market considered or > 50% of sites but from shorter periods",
						"Representative data from only one site relevant for the market considered or some sites but from shorter periods",
						"Representativeness unknown or data from a small number of sites and from shorter periods"
				}, {
						"Less than 3 years of difference to the time period of the data set",
						"Less than 6 years of difference to the time period of the data set",
						"Less than 10 years of difference to the time period of the data set",
						"Less than 15 years of difference to the time period of the data set",
						"Age of data unknown or more than 15 years of difference to the time period of the data set"
				}, {
						"Data from area under study",
						"Average data from larger area in which the area under study is included",
						"Data from area with similar production conditions",
						"Data from area with slightly similar production conditions",
						"Data from unknown or distinctly different area (North America instead of Middle East, OECD-Europe instead of Russia)"
				}, {
						"Data from enterprises, processes and materials under study",
						"Data from processes and materials under study (i.e. identical technology) but from different enterprises",
						"Data from processes and materials under study but from different technology",
						"Data on related processes or materials",
						"Data on related processes on laboratory scale or from different technology"
				}
		};
	}

	private static double[][] eiUncertainties() {
		return new double[][] {
				{ 1, 1.05, 1.1, 1.2, 1.5 },
				{ 1, 1.02, 1.05, 1.1, 1.2 },
				{ 1, 1.03, 1.1, 1.2, 1.5 },
				{ 1, 1.01, 1.02, 1.05, 1.1 },
				{ 1, 1.05, 1.2, 1.5, 2.0 },
		};
	}
}
package org.openlca.io.ecospold2.input;

import org.openlca.core.database.DQSystemDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;

class PedigreeDQSystem {

	private final static String REF_ID = "e7ac7cf6-5457-453e-99f9-d889826fffe8";
	private final static String[] INDICATORS = {
		"Reliability",
		"Completeness",
		"Temporal correlation",
		"Geographical correlation",
		"Further technological correlation"
	};
	private final static String[][] SCORES = {
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
	private final static double[][] UNCERTAINTIES = {
		{1, 1.05, 1.1, 1.2, 1.5},
		{1, 1.02, 1.05, 1.1, 1.2},
		{1, 1.03, 1.1, 1.2, 1.5},
		{1, 1.01, 1.02, 1.05, 1.1},
		{1, 1.05, 1.2, 1.5, 2.0},		
	};

	
	private final IDatabase db;
	private DQSystem system;

	PedigreeDQSystem(IDatabase db) {
		this.db = db;
		system = new DQSystemDao(db).getForRefId(REF_ID);
	}

	DQSystem get() {
		if (system == null)
			create();
		return system;
	}

	private void create() {		
		system = new DQSystem();
		system.setName("Pedigree");
		system.setRefId(REF_ID);
		system.hasUncertainties = true;
		for (int i = 0; i < INDICATORS.length; i++) {
			DQIndicator indicator = new DQIndicator();
			indicator.name = INDICATORS[i];
			indicator.position = i + 1;
			for (int j = 0; j < SCORES[i].length; j++) {
				DQScore score = new DQScore();
				score.description = SCORES[i][j];
				score.position = j + 1;
				score.uncertainty = UNCERTAINTIES[i][j];
			}
			system.indicators.add(indicator);
		}
		system = new DQSystemDao(db).insert(system);
	}
	
}
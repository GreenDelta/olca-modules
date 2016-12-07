package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ecospold2.master.ActivityIndexEntry;
import org.openlca.ecospold2.master.ActivityName;
import org.openlca.ecospold2.master.Company;
import org.openlca.ecospold2.master.Person;
import org.openlca.ecospold2.master.Source;
import org.openlca.ecospold2.master.Unit;

public class UserMasterData {

	public final List<Unit> units = new ArrayList<>();
	public final List<ActivityName> activityNames = new ArrayList<>();
	public final List<Company> companies = new ArrayList<>();
	public final List<Person> persons = new ArrayList<>();
	public final List<Source> sources = new ArrayList<>();
	public final List<IntermediateExchange> intermediateExchanges = new ArrayList<>();
	public final List<ElementaryExchange> elementaryExchanges = new ArrayList<>();
	public final List<Geography> geographies = new ArrayList<>();
	public final List<ActivityIndexEntry> activityIndexEntries = new ArrayList<>();
	public final List<Parameter> parameters = new ArrayList<>();

}

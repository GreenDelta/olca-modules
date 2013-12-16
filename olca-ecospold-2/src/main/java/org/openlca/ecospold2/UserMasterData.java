package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

public class UserMasterData {

	private List<Unit> units = new ArrayList<>();
	private List<ActivityName> activityNames = new ArrayList<>();
	private List<IntermediateExchange> intermediateExchanges = new ArrayList<>();
	private List<Geography> geographies = new ArrayList<>();
	private List<ActivityIndexEntry> activityIndexEntries = new ArrayList<>();

	public List<ActivityName> getActivityNames() {
		return activityNames;
	}

	public List<IntermediateExchange> getIntermediateExchanges() {
		return intermediateExchanges;
	}

	public List<Unit> getUnits() {
		return units;
	}

	public List<ActivityIndexEntry> getActivityIndexEntries() {
		return activityIndexEntries;
	}

	public List<Geography> getGeographies() {
		return geographies;
	}

	public Element toXml() {
		Element e = new Element("usedUserMasterData", IO.MD_NS);
		for (Unit unit : units)
			e.addContent(unit.toXml(IO.MD_NS));
		for (ActivityName activityName : activityNames)
			e.addContent(activityName.toXml(IO.MD_NS));
		for (Geography geography : geographies)
			e.addContent(geography.toXml(IO.MD_NS));
		for (IntermediateExchange techFlow : intermediateExchanges)
			e.addContent(techFlow.toXml(IO.MD_NS));
		for (ActivityIndexEntry indexEntry : activityIndexEntries)
			e.addContent(indexEntry.toXml(IO.MD_NS));
		return e;
	}

}

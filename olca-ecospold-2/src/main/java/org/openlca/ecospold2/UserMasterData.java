package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

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

	public Element toXml() {
		Element e = new Element("usedUserMasterData", IO.MD_NS);
		for (Unit unit : units)
			e.addContent(unit.toXml(IO.MD_NS));
		for (ActivityName activityName : activityNames)
			e.addContent(activityName.toXml(IO.MD_NS));
		for (Company company : companies)
			e.addContent(company.toXml(IO.MD_NS));
		for (Person person : persons)
			e.addContent(person.toXml(IO.MD_NS));
		for (Source source : sources)
			e.addContent(source.toXml(IO.MD_NS));
		for (ElementaryExchange elemFlow : elementaryExchanges)
			e.addContent(elemFlow.toXml(IO.MD_NS));
		for (IntermediateExchange techFlow : intermediateExchanges)
			e.addContent(techFlow.toXml(IO.MD_NS));
		for (ActivityIndexEntry indexEntry : activityIndexEntries)
			e.addContent(indexEntry.toXml(IO.MD_NS));
		return e;
	}

}

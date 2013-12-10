package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

public class UserMasterData {

	private List<ActivityName> activityNames = new ArrayList<>();

	public List<ActivityName> getActivityNames() {
		return activityNames;
	}

	public Element toXml() {
		Element e = new Element("usedUserMasterData", IO.MD_NS);
		for (ActivityName activityName : activityNames)
			e.addContent(activityName.toXml(IO.MD_NS));
		return e;
	}

}

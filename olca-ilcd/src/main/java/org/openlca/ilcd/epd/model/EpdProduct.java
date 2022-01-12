package org.openlca.ilcd.epd.model;

import java.util.ArrayList;

import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.flows.Flow;

public class EpdProduct {

	public Flow flow;
	public Ref genericFlow;
	public boolean vendorSpecific;
	public Ref vendor;
	public Ref documentation;
	public final ArrayList<MaterialPropertyValue> properties = new ArrayList<>();

}

package org.openlca.ilcd.io;

import org.openlca.ilcd.contacts.Contact;
import org.openlca.ilcd.descriptors.ContactDescriptor;
import org.openlca.ilcd.descriptors.FlowDescriptor;
import org.openlca.ilcd.descriptors.FlowPropertyDescriptor;
import org.openlca.ilcd.descriptors.MethodDescriptor;
import org.openlca.ilcd.descriptors.ProcessDescriptor;
import org.openlca.ilcd.descriptors.SourceDescriptor;
import org.openlca.ilcd.descriptors.UnitGroupDescriptor;
import org.openlca.ilcd.flowproperties.FlowProperty;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.models.Model;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

class Dir {

	private Dir() {
	}

	public static String get(Class<?> c) {
		if (c == null)
			return "null";
		if (c.equals(Model.class))
			return "lifecyclemodels";
		if (c.equals(Process.class) || c.equals(ProcessDescriptor.class))
			return "processes";
		if (c.equals(Flow.class) || c.equals(FlowDescriptor.class))
			return "flows";
		if (c.equals(FlowProperty.class) || c.equals(FlowPropertyDescriptor.class))
			return "flowproperties";
		if (c.equals(UnitGroup.class) || c.equals(UnitGroupDescriptor.class))
			return "unitgroups";
		if (c.equals(LCIAMethod.class) || c.equals(MethodDescriptor.class))
			return "lciamethods";
		if (c.equals(Source.class) || c.equals(SourceDescriptor.class))
			return "sources";
		if (c.equals(Contact.class) || c.equals(ContactDescriptor.class))
			return "contacts";
		return "unknown";
	}

}

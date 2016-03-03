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
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.sources.Source;
import org.openlca.ilcd.units.UnitGroup;

class Dir {

	private Dir() {
	}

	public static String get(Class<?> clazz) {
		if (clazz == null) {
			return "null";
		} else if (clazz.equals(Process.class)
				|| clazz.equals(ProcessDescriptor.class)) {
			return "processes";
		} else if (clazz.equals(Flow.class)
				|| clazz.equals(FlowDescriptor.class)) {
			return "flows";
		} else if (clazz.equals(FlowProperty.class)
				|| clazz.equals(FlowPropertyDescriptor.class)) {
			return "flowproperties";
		} else if (clazz.equals(UnitGroup.class)
				|| clazz.equals(UnitGroupDescriptor.class)) {
			return "unitgroups";
		} else if (clazz.equals(LCIAMethod.class)
				|| clazz.equals(MethodDescriptor.class)) {
			return "lciamethods";
		} else if (clazz.equals(Source.class)
				|| clazz.equals(SourceDescriptor.class)) {
			return "sources";
		} else if (clazz.equals(Contact.class)
				|| clazz.equals(ContactDescriptor.class)) {
			return "contacts";
		} else {
			return "unknown";
		}
	}

}

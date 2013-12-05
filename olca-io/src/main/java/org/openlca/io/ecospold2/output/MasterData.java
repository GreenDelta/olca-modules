package org.openlca.io.ecospold2.output;

import java.util.HashSet;

import org.openlca.core.model.Category;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;

/** The collected master data from an export. */
class MasterData {

	HashSet<Location> locations = new HashSet<>();
	HashSet<Category> classifications = new HashSet<>();
	HashSet<Category> compartments = new HashSet<>();
	HashSet<FlowDescriptor> elementaryFlows = new HashSet<>();
	HashSet<FlowDescriptor> technosphereFlows = new HashSet<>();
	HashSet<ActorDescriptor> contacts = new HashSet<>();
	HashSet<SourceDescriptor> sources = new HashSet<>();

}

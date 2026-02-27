package org.openlca.sd.model;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.Descriptor;

/// Describes a reference to an openLCA managed entity. We only store references
/// to these entities in our system dynamics model. They are then resolved in
/// the simulator etc.
public record EntityRef(String name, String refId, ModelType type) {

	public static EntityRef of(Descriptor d) {
		return new EntityRef(d.name, d.refId, d.type);
	}

	public static EntityRef of(RootEntity e) {
		return new EntityRef(e.name, e.refId, ModelType.of(e));
	}

}

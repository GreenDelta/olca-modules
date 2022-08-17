
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.UnitGroup;
import org.openlca.proto.ProtoUnitGroup;

public record UnitGroupReader(EntityResolver resolver)
	implements EntityReader<UnitGroup, ProtoUnitGroup> {

	@Override
	public UnitGroup read(ProtoUnitGroup proto) {
		var group = new UnitGroup();
		update(group, proto);
		return group;
	}

	@Override
	public void update(UnitGroup group, ProtoUnitGroup proto) {
		Util.mapBase(group, ProtoWrap.of(proto), resolver);

	}
}

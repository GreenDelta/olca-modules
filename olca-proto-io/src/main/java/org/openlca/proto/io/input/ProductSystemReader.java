
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.ProductSystem;
import org.openlca.proto.ProtoProductSystem;

public record ProductSystemReader(EntityResolver resolver)
	implements EntityReader<ProductSystem, ProtoProductSystem> {

	@Override
	public ProductSystem read(ProtoProductSystem proto) {
		var system = new ProductSystem();
		update(system, proto);
		return system;
	}

	@Override
	public void update(ProductSystem system, ProtoProductSystem proto) {
		Util.mapBase(system, ProtoWrap.of(proto), resolver);

	}
}

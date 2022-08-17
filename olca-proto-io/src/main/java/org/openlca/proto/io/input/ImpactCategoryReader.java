
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.ImpactCategory;
import org.openlca.proto.ProtoImpactCategory;

public record ImpactCategoryReader(EntityResolver resolver)
	implements EntityReader<ImpactCategory, ProtoImpactCategory> {

	@Override
	public ImpactCategory read(ProtoImpactCategory proto) {
		var impact = new ImpactCategory();
		update(impact, proto);
		return impact;
	}

	@Override
	public void update(ImpactCategory impact, ProtoImpactCategory proto) {
		Util.mapBase(impact, ProtoWrap.of(proto), resolver);

	}
}

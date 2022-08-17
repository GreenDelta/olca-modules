
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.SocialIndicator;
import org.openlca.proto.ProtoSocialIndicator;

public record SocialIndicatorReader(EntityResolver resolver)
	implements EntityReader<SocialIndicator, ProtoSocialIndicator> {

	@Override
	public SocialIndicator read(ProtoSocialIndicator proto) {
		var indicator = new SocialIndicator();
		update(indicator, proto);
		return indicator;
	}

	@Override
	public void update(SocialIndicator indicator, ProtoSocialIndicator proto) {
		Util.mapBase(indicator, ProtoWrap.of(proto), resolver);

	}
}

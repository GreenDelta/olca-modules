package org.openlca.proto.io.input;

import com.google.protobuf.Message;
import org.openlca.core.model.RootEntity;

interface EntityReader<E extends RootEntity, P extends Message> {

	E read(P proto);

	void update(E entity, P proto);

}

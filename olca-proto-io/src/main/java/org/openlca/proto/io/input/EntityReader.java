package org.openlca.proto.io.input;

import org.openlca.core.model.RootEntity;

import com.google.protobuf.Message;

interface EntityReader<E extends RootEntity, P extends Message> {

	E read(P proto);

	void update(E entity, P proto);

}

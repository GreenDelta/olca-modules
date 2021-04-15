package org.openlca.proto.input;

import org.openlca.core.model.CategorizedEntity;

public interface Import<T extends CategorizedEntity> {

  ImportStatus<T> of(String id);

}

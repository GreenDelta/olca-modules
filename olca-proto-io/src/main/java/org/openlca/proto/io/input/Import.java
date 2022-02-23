package org.openlca.proto.io.input;

import org.openlca.core.model.RootEntity;

/**
 * An import of a categorized entity. Such an import should be stateless so that
 * it could be used for several entities.
 *
 * @param <T> the type of entities that can be imported
 */
public interface Import<T extends RootEntity> {

  ImportStatus<T> of(String id);

}

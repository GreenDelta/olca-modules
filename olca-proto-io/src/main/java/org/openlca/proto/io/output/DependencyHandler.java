package org.openlca.proto.io.output;

import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.RootDescriptor;

public interface DependencyHandler {

  void push(RootEntity entity);

  void push(RootDescriptor descriptor);
}

package org.openlca.proto.io.output;

import org.openlca.core.model.RefEntity;
import org.openlca.core.model.descriptors.Descriptor;

public interface DependencyHandler {

  void push(RefEntity entity);

  void push(Descriptor descriptor);
}

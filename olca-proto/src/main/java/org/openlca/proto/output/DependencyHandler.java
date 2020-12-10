package org.openlca.proto.output;

import org.openlca.core.model.RootEntity;

public interface DependencyHandler {

  void push(RootEntity dependency);

}

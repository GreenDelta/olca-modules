package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.Process;
import org.openlca.proto.ProtoParameterRedef;
import org.openlca.util.Strings;

public class ParameterRedefReader {

  private final EntityResolver resolver;

  public ParameterRedefReader(EntityResolver resolver) {
    this.resolver = resolver;
  }

  public static ParameterRedef read(
    EntityResolver resolver, ProtoParameterRedef proto) {
    return new ParameterRedefReader(resolver).read(proto);
  }

  public ParameterRedef read(ProtoParameterRedef proto) {
    if (proto == null)
      return null;
    var redef = new ParameterRedef();

    redef.name = proto.getName();
    redef.value = proto.getValue();
    redef.uncertainty = In.uncertainty(proto.getUncertainty());
    redef.description = proto.getDescription();

    // context
    if (!proto.hasContext())
      return redef;
    var context = proto.getContext().getId();
    if (Strings.nullOrEmpty(context))
      return redef;

    // we could check the context type but do we know that
    // this is correctly entered? thus, we first try to
    // find a process with that ID (the usual case) and
    // then an impact category
    var process = resolver.getDescriptor(Process.class, context);
    if (process != null) {
      redef.contextType = ModelType.PROCESS;
      redef.contextId = process.id;
      return redef;
    }

    var impact = resolver.getDescriptor(ImpactCategory.class, context);
    if (impact == null)
      return redef;
    redef.contextType = ModelType.IMPACT_CATEGORY;
    redef.contextId = impact.id;
    return redef;
  }
}

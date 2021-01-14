package org.openlca.proto;

import com.google.protobuf.MessageOrBuilder;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.proto.output.ActorWriter;
import org.openlca.proto.output.CategoryWriter;
import org.openlca.proto.output.CurrencyWriter;
import org.openlca.proto.output.DQSystemWriter;
import org.openlca.proto.output.FlowPropertyWriter;
import org.openlca.proto.output.FlowWriter;
import org.openlca.proto.output.ImpactCategoryWriter;
import org.openlca.proto.output.ImpactMethodWriter;
import org.openlca.proto.output.LocationWriter;
import org.openlca.proto.output.ParameterWriter;
import org.openlca.proto.output.ProcessWriter;
import org.openlca.proto.output.ProductSystemWriter;
import org.openlca.proto.output.ProjectWriter;
import org.openlca.proto.output.SocialIndicatorWriter;
import org.openlca.proto.output.SourceWriter;
import org.openlca.proto.output.UnitGroupWriter;
import org.openlca.proto.output.WriterConfig;

public final class Messages {

  private Messages() {
  }

  public static byte[] toBinary(IDatabase db, RootEntity e) {
    if (e == null)
      return null;
    var conf = WriterConfig.of(db);

    if (e instanceof Actor) {
      var proto = new ActorWriter(conf)
        .write((Actor) e);
      return proto.toByteArray();
    }

    if (e instanceof Category) {
      var proto = new CategoryWriter(conf)
        .write((Category) e);
      return proto.toByteArray();
    }

    if (e instanceof Currency) {
      var proto = new CurrencyWriter(conf)
        .write((Currency) e);
      return proto.toByteArray();
    }

    if (e instanceof DQSystem) {
      var proto = new DQSystemWriter(conf)
        .write((DQSystem) e);
      return proto.toByteArray();
    }

    if (e instanceof Flow) {
      var proto = new FlowWriter(conf)
        .write((Flow) e);
      return proto.toByteArray();
    }

    if (e instanceof FlowProperty) {
      var proto = new FlowPropertyWriter(conf)
        .write((FlowProperty) e);
      return proto.toByteArray();
    }

    if (e instanceof ImpactCategory) {
      var proto = new ImpactCategoryWriter(conf)
        .write((ImpactCategory) e);
      return proto.toByteArray();
    }

    if (e instanceof ImpactMethod) {
      var proto = new ImpactMethodWriter(conf)
        .write((ImpactMethod) e);
      return proto.toByteArray();
    }

    if (e instanceof Location) {
      var proto = new LocationWriter(conf)
        .write((Location) e);
      return proto.toByteArray();
    }

    if (e instanceof Parameter) {
      var proto = new ParameterWriter(conf)
        .write((Parameter) e);
      return proto.toByteArray();
    }

    if (e instanceof Process) {
      var proto = new ProcessWriter(conf)
        .write((Process) e);
      return proto.toByteArray();
    }

    if (e instanceof ProductSystem) {
      var proto = new ProductSystemWriter(conf)
        .write((ProductSystem) e);
      return proto.toByteArray();
    }

    if (e instanceof Project) {
      var proto = new ProjectWriter(conf)
        .write((Project) e);
      return proto.toByteArray();
    }

    if (e instanceof SocialIndicator) {
      var proto = new SocialIndicatorWriter(conf)
        .write((SocialIndicator) e);
      return proto.toByteArray();
    }

    if (e instanceof Source) {
      var proto = new SourceWriter(conf)
        .write((Source) e);
      return proto.toByteArray();
    }

    if (e instanceof UnitGroup) {
      var proto = new UnitGroupWriter(conf)
        .write((UnitGroup) e);
      return proto.toByteArray();
    }

    throw new RuntimeException("Unsupported entity type" +
      " for binary translation: " + e.getClass());
  }

  public static boolean isEmpty(MessageOrBuilder message) {
    if (message == null)
      return true;
    var fields = message.getDescriptorForType().getFields();
    for (var field : fields) {
      if (field.isRepeated()) {
        int count = message.getRepeatedFieldCount(field);
        if (count > 0)
          return false;
        continue;
      }
      if (message.hasField(field))
        return false;
    }
    return true;
  }

  public static boolean isNotEmpty(MessageOrBuilder message) {
    return !isEmpty(message);
  }

}

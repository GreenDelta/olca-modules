package org.openlca.proto.server;

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
import org.openlca.proto.generated.data.DataSet;
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

class DataUtil {

  static DataSet.Builder toDataSet(IDatabase db, RootEntity e) {
    var ds = DataSet.newBuilder();
    var conf = WriterConfig.of(db);

    if (e instanceof Actor)
      return ds.setActor(new ActorWriter(conf)
        .write((Actor) e));

    if (e instanceof Category)
      return ds.setCategory(new CategoryWriter(conf)
        .write((Category) e));

    if (e instanceof Currency)
      return ds.setCurrency(new CurrencyWriter(conf)
        .write((Currency) e));

    if (e instanceof DQSystem)
      return ds.setDqSystem(new DQSystemWriter(conf)
        .write((DQSystem) e));

    if (e instanceof Flow)
      return ds.setFlow(new FlowWriter(conf)
        .write((Flow) e));

    if (e instanceof FlowProperty)
      return ds.setFlowProperty(new FlowPropertyWriter(conf)
        .write((FlowProperty) e));

    if (e instanceof ImpactCategory)
      return ds.setImpactCategory(new ImpactCategoryWriter(conf)
        .write((ImpactCategory) e));

    if (e instanceof ImpactMethod)
      return ds.setImpactMethod(new ImpactMethodWriter(conf)
        .write((ImpactMethod) e));

    if (e instanceof Location)
      return ds.setLocation(new LocationWriter(conf)
        .write((Location) e));

    if (e instanceof Parameter)
      return ds.setParameter(new ParameterWriter(conf)
        .write((Parameter) e));

    if (e instanceof Process)
      return ds.setProcess(new ProcessWriter(conf)
        .write((Process) e));

    if (e instanceof ProductSystem)
      return ds.setProductSystem(new ProductSystemWriter(conf)
        .write((ProductSystem) e));

    if (e instanceof Project)
      return ds.setProject(new ProjectWriter(conf)
        .write((Project) e));

    if (e instanceof SocialIndicator)
      return ds.setSocialIndicator(new SocialIndicatorWriter(conf)
        .write((SocialIndicator) e));

    if (e instanceof Source)
      return ds.setSource(new SourceWriter(conf)
        .write((Source) e));

    if (e instanceof UnitGroup)
      return ds.setUnitGroup(new UnitGroupWriter(conf)
        .write((UnitGroup) e));

    return ds;
  }

}

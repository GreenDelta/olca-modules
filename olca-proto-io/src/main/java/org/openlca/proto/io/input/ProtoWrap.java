package org.openlca.proto.io.input;

import com.google.protobuf.Message;
import com.google.protobuf.ProtocolStringList;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Currency;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Project;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Source;
import org.openlca.core.model.UnitGroup;
import org.openlca.proto.ProtoActor;
import org.openlca.proto.ProtoCurrency;
import org.openlca.proto.ProtoDQSystem;
import org.openlca.proto.ProtoEpd;
import org.openlca.proto.ProtoFlow;
import org.openlca.proto.ProtoFlowProperty;
import org.openlca.proto.ProtoImpactCategory;
import org.openlca.proto.ProtoImpactMethod;
import org.openlca.proto.ProtoLocation;
import org.openlca.proto.ProtoParameter;
import org.openlca.proto.ProtoProcess;
import org.openlca.proto.ProtoProductSystem;
import org.openlca.proto.ProtoProject;
import org.openlca.proto.ProtoResult;
import org.openlca.proto.ProtoSocialIndicator;
import org.openlca.proto.ProtoSource;
import org.openlca.proto.ProtoUnitGroup;

abstract class ProtoWrap<T extends Message> {

	private final T proto;

	ProtoWrap(T proto) {
		this.proto = proto;
	}

	T proto() {
		return proto;
	}

	abstract String id();

	abstract String name();

	abstract String description();

	abstract String version();

	abstract String lastChange();

	abstract String category();

	abstract ProtocolStringList tags();

	abstract <T extends RootEntity> T read(EntityResolver resolver);

	abstract <T extends RootEntity> void update(
		T entity, EntityResolver resolver);

	static ProtoWrap<ProtoActor> of(ProtoActor proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<Actor, ProtoActor> reader(EntityResolver resolver) {
				return new ActorReader(resolver);
			}
		};
	}

	static ProtoWrap<ProtoSource> of(ProtoSource proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<Source, ProtoSource> reader(EntityResolver resolver) {
				return new SourceReader(resolver);
			}
		};
	}

	static ProtoWrap<ProtoCurrency> of(ProtoCurrency proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<Currency, ProtoCurrency> reader(EntityResolver resolver) {
				return new CurrencyReader(resolver);
			}
		};
	}

	static ProtoWrap<ProtoUnitGroup> of(ProtoUnitGroup proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<UnitGroup, ProtoUnitGroup> reader(EntityResolver resolver) {
				return new UnitGroupReader(resolver);
			}

		};
	}

	static ProtoWrap<ProtoFlowProperty> of(ProtoFlowProperty proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<FlowProperty, ProtoFlowProperty> reader(
				EntityResolver resolver) {
				return new FlowPropertyReader(resolver);
			}

		};
	}

	static ProtoWrap<ProtoDQSystem> of(ProtoDQSystem proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<DQSystem, ProtoDQSystem> reader(EntityResolver resolver) {
				return new DQSystemReader(resolver);
			}
		};
	}

	static ProtoWrap<ProtoFlow> of(ProtoFlow proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<Flow, ProtoFlow> reader(EntityResolver resolver) {
				return new FlowReader(resolver);
			}

		};
	}

	static ProtoWrap<ProtoImpactMethod> of(ProtoImpactMethod proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<ImpactMethod, ProtoImpactMethod> reader(
				EntityResolver resolver) {
				return new ImpactMethodReader(resolver);
			}

		};
	}

	static ProtoWrap<ProtoLocation> of(ProtoLocation proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<Location, ProtoLocation> reader(EntityResolver resolver) {
				return new LocationReader(resolver);
			}

		};
	}

	static ProtoWrap<ProtoParameter> of(ProtoParameter proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<Parameter, ProtoParameter> reader(EntityResolver resolver) {
				return new ParameterReader(resolver);
			}

		};
	}

	static ProtoWrap<ProtoImpactCategory> of(ProtoImpactCategory proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<ImpactCategory, ProtoImpactCategory> reader(
				EntityResolver resolver) {
				return new ImpactCategoryReader(resolver);
			}

		};
	}

	static ProtoWrap<ProtoProcess> of(ProtoProcess proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<Process, ProtoProcess> reader(EntityResolver resolver) {
				return new ProcessReader(resolver);
			}

		};
	}

	static ProtoWrap<ProtoProject> of(ProtoProject proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<Project, ProtoProject> reader(EntityResolver resolver) {
				return new ProjectReader(resolver);
			}

		};
	}

	static ProtoWrap<ProtoSocialIndicator> of(ProtoSocialIndicator proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<SocialIndicator, ProtoSocialIndicator> reader(
				EntityResolver resolver) {
				return new SocialIndicatorReader(resolver);
			}

		};
	}

	static ProtoWrap<ProtoProductSystem> of(ProtoProductSystem proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<ProductSystem, ProtoProductSystem> reader(
				EntityResolver resolver) {
				return new ProductSystemReader(resolver);
			}

		};
	}

	static ProtoWrap<ProtoEpd> of(ProtoEpd proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<Epd, ProtoEpd> reader(EntityResolver resolver) {
				return new EpdReader(resolver);
			}

		};
	}

	static ProtoWrap<ProtoResult> of(ProtoResult proto) {
		return new ProtoWrap<>(proto) {
			@Override
			String id() {
				return proto.getId();
			}

			@Override
			String name() {
				return proto.getName();
			}

			@Override
			String description() {
				return proto.getDescription();
			}

			@Override
			String version() {
				return proto.getVersion();
			}

			@Override
			String lastChange() {
				return proto.getLastChange();
			}

			@Override
			String category() {
				return proto.getCategory();
			}

			@Override
			ProtocolStringList tags() {
				return proto.getTagsList();
			}

			@Override
			EntityReader<Result, ProtoResult> reader(EntityResolver resolver) {
				return new ResultReader(resolver);
			}
		};
	}
}

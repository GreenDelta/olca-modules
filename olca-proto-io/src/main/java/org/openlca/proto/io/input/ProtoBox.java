package org.openlca.proto.io.input;

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

import com.google.protobuf.Message;
import com.google.protobuf.ProtocolStringList;

abstract class ProtoBox<M extends Message, E extends RootEntity> {

	private final M message;

	ProtoBox(M message) {
		this.message = message;
	}

	M message() {
		return message;
	}

	abstract String id();

	abstract String name();

	abstract String description();

	abstract String version();

	abstract String lastChange();

	abstract String category();

	abstract ProtocolStringList tags();

	abstract E read(EntityResolver resolver);

	abstract void update(E entity, EntityResolver resolver);

	static ProtoBox<ProtoActor, Actor> of(ProtoActor proto) {
		return new ProtoBox<>(proto) {
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
			Actor read(EntityResolver resolver) {
				return new ActorReader(resolver).read(message());
			}

			@Override
			void update(Actor actor, EntityResolver resolver) {
				new ActorReader(resolver).update(actor, message());
			}
		};
	}

	static ProtoBox<ProtoSource, Source> of(ProtoSource proto) {
		return new ProtoBox<>(proto) {
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
			Source read(EntityResolver resolver) {
				return new SourceReader(resolver).read(message());
			}

			@Override
			void update(Source source, EntityResolver resolver) {
				new SourceReader(resolver).update(source, message());
			}
		};
	}

	static ProtoBox<ProtoCurrency, Currency> of(ProtoCurrency proto) {
		return new ProtoBox<>(proto) {
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
			Currency read(EntityResolver resolver) {
				return new CurrencyReader(resolver).read(message());
			}

			@Override
			void update(Currency currency, EntityResolver resolver) {
				new CurrencyReader(resolver).update(currency, message());
			}
		};
	}

	static ProtoBox<ProtoUnitGroup, UnitGroup> of(ProtoUnitGroup proto) {
		return new ProtoBox<>(proto) {
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
			UnitGroup read(EntityResolver resolver) {
				return new UnitGroupReader(resolver).read(message());
			}

			@Override
			void update(UnitGroup group, EntityResolver resolver) {
				new UnitGroupReader(resolver).update(group, message());
			}
		};
	}

	static ProtoBox<ProtoFlowProperty, FlowProperty> of(ProtoFlowProperty proto) {
		return new ProtoBox<>(proto) {
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
			FlowProperty read(EntityResolver resolver) {
				return new FlowPropertyReader(resolver).read(message());
			}

			@Override
			void update(FlowProperty property, EntityResolver resolver) {
				new FlowPropertyReader(resolver).update(property, message());
			}
		};
	}

	static ProtoBox<ProtoDQSystem, DQSystem> of(ProtoDQSystem proto) {
		return new ProtoBox<>(proto) {
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
			DQSystem read(EntityResolver resolver) {
				return new DQSystemReader(resolver).read(message());
			}

			@Override
			void update(DQSystem entity, EntityResolver resolver) {
				new DQSystemReader(resolver).update(entity, message());
			}
		};
	}

	static ProtoBox<ProtoFlow, Flow> of(ProtoFlow proto) {
		return new ProtoBox<>(proto) {
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
			Flow read(EntityResolver resolver) {
				return new FlowReader(resolver).read(message());
			}

			@Override
			void update(Flow entity, EntityResolver resolver) {
				new FlowReader(resolver).update(entity, message());
			}
		};
	}

	static ProtoBox<ProtoImpactMethod, ImpactMethod> of(ProtoImpactMethod proto) {
		return new ProtoBox<>(proto) {
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
			ImpactMethod read(EntityResolver resolver) {
				return new ImpactMethodReader(resolver).read(message());
			}

			@Override
			void update(ImpactMethod entity, EntityResolver resolver) {
				new ImpactMethodReader(resolver).update(entity, message());
			}
		};
	}

	static ProtoBox<ProtoLocation, Location> of(ProtoLocation proto) {
		return new ProtoBox<>(proto) {
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
			Location read(EntityResolver resolver) {
				return new LocationReader(resolver).read(message());
			}

			@Override
			void update(Location entity, EntityResolver resolver) {
				new LocationReader(resolver).update(entity, message());
			}
		};
	}

	static ProtoBox<ProtoParameter, Parameter> of(ProtoParameter proto) {
		return new ProtoBox<>(proto) {
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
			Parameter read(EntityResolver resolver) {
				return new ParameterReader(resolver).read(message());
			}

			@Override
			void update(Parameter entity, EntityResolver resolver) {
				new ParameterReader(resolver).update(entity, message());
			}
		};
	}

	static ProtoBox<ProtoImpactCategory, ImpactCategory> of(ProtoImpactCategory proto) {
		return new ProtoBox<>(proto) {
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
			ImpactCategory read(EntityResolver resolver) {
				return new ImpactCategoryReader(resolver).read(message());
			}

			@Override
			void update(ImpactCategory entity, EntityResolver resolver) {
				new ImpactCategoryReader(resolver).update(entity, message());
			}
		};
	}

	static ProtoBox<ProtoProcess, Process> of(ProtoProcess proto) {
		return new ProtoBox<>(proto) {
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
			Process read(EntityResolver resolver) {
				return new ProcessReader(resolver).read(message());
			}

			@Override
			void update(Process entity, EntityResolver resolver) {
				new ProcessReader(resolver).update(entity, message());
			}
		};
	}

	static ProtoBox<ProtoProject, Project> of(ProtoProject proto) {
		return new ProtoBox<>(proto) {
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
			Project read(EntityResolver resolver) {
				return new ProjectReader(resolver).read(message());
			}

			@Override
			void update(Project entity, EntityResolver resolver) {
				new ProjectReader(resolver).update(entity, message());
			}
		};
	}

	static ProtoBox<ProtoSocialIndicator, SocialIndicator> of(ProtoSocialIndicator proto) {
		return new ProtoBox<>(proto) {
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
			SocialIndicator read(EntityResolver resolver) {
				return new SocialIndicatorReader(resolver).read(message());
			}

			@Override
			void update(SocialIndicator entity, EntityResolver resolver) {
				new SocialIndicatorReader(resolver).update(entity, message());
			}
		};
	}

	static ProtoBox<ProtoProductSystem, ProductSystem> of(ProtoProductSystem proto) {
		return new ProtoBox<>(proto) {
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
			ProductSystem read(EntityResolver resolver) {
				return new ProductSystemReader(resolver).read(message());
			}

			@Override
			void update(ProductSystem entity, EntityResolver resolver) {
				new ProductSystemReader(resolver).update(entity, message());
			}
		};
	}

	static ProtoBox<ProtoEpd, Epd> of(ProtoEpd proto) {
		return new ProtoBox<>(proto) {
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
			Epd read(EntityResolver resolver) {
				return new EpdReader(resolver).read(message());
			}

			@Override
			void update(Epd entity, EntityResolver resolver) {
				new EpdReader(resolver).update(entity, message());
			}
		};
	}

	static ProtoBox<ProtoResult, Result> of(ProtoResult proto) {
		return new ProtoBox<>(proto) {
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
			Result read(EntityResolver resolver) {
				return new ResultReader(resolver).read(message());
			}

			@Override
			void update(Result entity, EntityResolver resolver) {
				new ResultReader(resolver).update(entity, message());
			}
		};
	}
}

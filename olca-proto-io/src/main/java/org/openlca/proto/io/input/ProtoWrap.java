package org.openlca.proto.io.input;

import com.google.protobuf.Message;
import com.google.protobuf.ProtocolStringList;
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
		};
	}
}

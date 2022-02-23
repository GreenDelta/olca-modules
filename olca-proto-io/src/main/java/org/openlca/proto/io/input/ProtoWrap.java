package org.openlca.proto.io.input;

import com.google.protobuf.ProtocolStringList;
import org.openlca.core.model.RootEntity;
import org.openlca.proto.ProtoActor;
import org.openlca.proto.ProtoCategory;
import org.openlca.proto.ProtoCurrency;
import org.openlca.proto.ProtoDQSystem;
import org.openlca.proto.ProtoFlow;
import org.openlca.proto.ProtoFlowProperty;
import org.openlca.proto.ProtoImpactCategory;
import org.openlca.proto.ProtoImpactMethod;
import org.openlca.proto.ProtoLocation;
import org.openlca.proto.ProtoParameter;
import org.openlca.proto.ProtoProcess;
import org.openlca.proto.ProtoProductSystem;
import org.openlca.proto.ProtoProject;
import org.openlca.proto.ProtoRef;
import org.openlca.proto.ProtoSocialIndicator;
import org.openlca.proto.ProtoSource;
import org.openlca.proto.ProtoUnitGroup;
import org.openlca.util.Strings;

abstract class ProtoWrap {

  abstract String id();

  abstract String name();

  abstract String description();

  abstract String version();

  abstract String lastChange();

  abstract ProtoRef category();

  abstract ProtocolStringList tags();

  abstract String library();

  void mapTo(RootEntity e, ProtoImport config) {
    if (e == null)
      return;

    // root entity fields
    e.refId = id();
    e.name = name();
    e.description = description();
    e.version = In.versionOf(version());
    e.lastChange = In.timeOf(lastChange());

    // categorized entity fields
    var catID = category().getId();
    if (Strings.notEmpty(catID)) {
      e.category = new CategoryImport(config)
        .of(catID)
        .model();
    }
    e.tags = tags()
      .stream()
      .reduce((tag, tags) -> tags + "," + tag)
      .orElse(null);
    e.library = Strings.notEmpty(library())
      ? library()
      : null;

  }

  static ProtoWrap of(ProtoCategory proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoActor proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoSource proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoCurrency proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoUnitGroup proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoFlowProperty proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoDQSystem proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoFlow proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoImpactMethod proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoLocation proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoParameter proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoImpactCategory proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoProcess proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoProject proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoSocialIndicator proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }

  static ProtoWrap of(ProtoProductSystem proto) {
    return new ProtoWrap() {
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
      ProtoRef category() {
        return proto.getCategory();
      }

      @Override
      ProtocolStringList tags() {
        return proto.getTagsList();
      }

      @Override
      String library() {
        return proto.getLibrary();
      }
    };
  }
}

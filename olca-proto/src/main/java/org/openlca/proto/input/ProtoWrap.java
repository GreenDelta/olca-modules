package org.openlca.proto.input;

import com.google.protobuf.ProtocolStringList;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.proto.Proto;
import org.openlca.util.Strings;

abstract class ProtoWrap {

  abstract String id();

  abstract String name();

  abstract String description();

  abstract String version();

  abstract String lastChange();

  abstract Proto.Ref category();

  abstract ProtocolStringList tags();

  abstract String library();

  void mapTo(CategorizedEntity e, ProtoImport config) {
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
      e.category = new CategoryImport(config).of(catID);
    }
    e.tags = tags()
      .stream()
      .reduce((tag, tags) -> tags + "," + tag)
      .orElse(null);
    e.library = Strings.notEmpty(library())
      ? library()
      : null;

  }

  static ProtoWrap of(Proto.Category proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.Actor proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.Source proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.Currency proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.UnitGroup proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.FlowProperty proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.DQSystem proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.Flow proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.ImpactMethod proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.Location proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.Parameter proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.ImpactCategory proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.Process proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.Project proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.SocialIndicator proto) {
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
      Proto.Ref category() {
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

  static ProtoWrap of(Proto.ProductSystem proto) {
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
      Proto.Ref category() {
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

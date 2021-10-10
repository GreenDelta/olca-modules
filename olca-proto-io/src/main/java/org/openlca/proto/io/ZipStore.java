package org.openlca.proto.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.util.JsonFormat;
import org.openlca.core.model.ModelType;
import org.openlca.geo.geojson.GeoJSON;
import org.openlca.jsonld.Json;
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
import org.openlca.proto.ProtoSocialIndicator;
import org.openlca.proto.ProtoSource;
import org.openlca.proto.ProtoUnitGroup;
import org.slf4j.LoggerFactory;

public class ZipStore implements ProtoReader, AutoCloseable {

  private final FileSystem zip;

  public static ZipStore open(File zipFile) throws IOException {
    return new ZipStore(zipFile);
  }

  private ZipStore(File zipFile) throws IOException {
    var uriStr = zipFile.toURI().toASCIIString();
    var uri = URI.create("jar:" + uriStr);
    var options = new HashMap<String, String>();
    if (!zipFile.exists()) {
      options.put("create", "true");
    }
    zip = FileSystems.newFileSystem(uri, options);
  }

  @Override
  public void close() throws IOException {
    zip.close();
  }

  @Override
  public Set<String> getIds(ModelType modelType) {
    if (modelType == null)
      return Collections.emptySet();
    var path = pathOf(modelType);
    var dir = zip.getPath(path);
    if (!Files.exists(dir))
      return Collections.emptySet();

    try {
      var ids = new HashSet<String>();
      Files.walkFileTree(dir, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
          var name = file.getFileName().toString();
          ids.add(name.split("\\.")[0]);
          return FileVisitResult.CONTINUE;
        }
      });
      return ids;
    } catch (Exception e) {
      var log = LoggerFactory.getLogger(getClass());
      log.error("failed to collect IDs from folder in zip: " + path, e);
      return Collections.emptySet();
    }
  }

  @Override
  public ProtoCategory getCategory(String id) {
    var proto = readBin("categories", id, ProtoCategory.parser());
    if (proto != null)
      return proto;
    var builder = ProtoCategory.newBuilder();
    if (readJson("categories", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoProject getProject(String id) {
    var proto = readBin("projects", id, ProtoProject.parser());
    if (proto != null)
      return proto;
    var builder = ProtoProject.newBuilder();
    if (readJson("projects", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoImpactMethod getImpactMethod(String id) {
    var proto = readBin("lcia_methods", id, ProtoImpactMethod.parser());
    if (proto != null)
      return proto;
    var builder = ProtoImpactMethod.newBuilder();
    if (readJson("lcia_methods", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoImpactCategory getImpactCategory(String id) {
    var proto = readBin("lcia_categories", id, ProtoImpactCategory.parser());
    if (proto != null)
      return proto;
    var builder = ProtoImpactCategory.newBuilder();
    if (readJson("lcia_categories", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoProductSystem getProductSystem(String id) {
    var proto = readBin("product_systems", id, ProtoProductSystem.parser());
    if (proto != null)
      return proto;
    var builder = ProtoProductSystem.newBuilder();
    if (readJson("product_systems", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoProcess getProcess(String id) {
    var proto = readBin("processes", id, ProtoProcess.parser());
    if (proto != null)
      return proto;
    var builder = ProtoProcess.newBuilder();
    if (readJson("processes", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoFlow getFlow(String id) {
    var proto = readBin("flows", id, ProtoFlow.parser());
    if (proto != null)
      return proto;
    var builder = ProtoFlow.newBuilder();
    if (readJson("flows", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoFlowProperty getFlowProperty(String id) {
    var proto = readBin("flow_properties", id, ProtoFlowProperty.parser());
    if (proto != null)
      return proto;
    var builder = ProtoFlowProperty.newBuilder();
    if (readJson("flow_properties", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoUnitGroup getUnitGroup(String id) {
    var proto = readBin("unit_groups", id, ProtoUnitGroup.parser());
    if (proto != null)
      return proto;
    var builder = ProtoUnitGroup.newBuilder();
    if (readJson("unit_groups", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoActor getActor(String id) {
    var proto = readBin("actors", id, ProtoActor.parser());
    if (proto != null)
      return proto;
    var builder = ProtoActor.newBuilder();
    if (readJson("actors", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoSource getSource(String id) {
    var proto = readBin("sources", id, ProtoSource.parser());
    if (proto != null)
      return proto;
    var builder = ProtoSource.newBuilder();
    if (readJson("sources", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoLocation getLocation(String id) {
    var proto = readBin("locations", id, ProtoLocation.parser());
    if (proto != null)
      return proto;

    // locations in the JSON format can contain GeoJSON objects
    // that we have to handle a bit differently as they cannot
    // be mapped to Proto messages
    var path = "locations/" + id + ".json";
    try {

      // we do the same as in readJson here to avoid fetching
      // the JSON data twice
      var data = get(path);
      if (data == null)
        return null;
      var json = new String(data, StandardCharsets.UTF_8);
      var builder = ProtoLocation.newBuilder();
      JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(json, builder);

      if (!builder.getGeometryBytes().isEmpty())
        return builder.build(); // `geometryBytes` was filled

      // check if there is a GeoJSON object
      var obj = new Gson().fromJson(json, JsonObject.class);
      var geo = Json.getObject(obj, "geometry");
      if (geo == null)
        return builder.build();
      var coll = GeoJSON.read(geo);
      if (coll == null ||coll.features.isEmpty())
        return builder.build();
      var geoBytes = GeoJSON.pack(coll);
      if (geoBytes == null || geoBytes.length == 0)
        return  builder.build();
      builder.setGeometryBytes(
        ByteString.copyFrom(geoBytes));
      return builder.build();
    } catch (Exception e) {
      var log = LoggerFactory.getLogger(getClass());
      log.error("failed to parse " + path, e);
      return null;
    }
  }

  @Override
  public ProtoSocialIndicator getSocialIndicator(String id) {
    var proto = readBin("social_indicators", id, ProtoSocialIndicator.parser());
    if (proto != null)
      return proto;
    var builder = ProtoSocialIndicator.newBuilder();
    if (readJson("social_indicators", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoCurrency getCurrency(String id) {
    var proto = readBin("currencies", id, ProtoCurrency.parser());
    if (proto != null)
      return proto;
    var builder = ProtoCurrency.newBuilder();
    if (readJson("currencies", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoParameter getParameter(String id) {
    var proto = readBin("parameters", id, ProtoParameter.parser());
    if (proto != null)
      return proto;
    var builder = ProtoParameter.newBuilder();
    if (readJson("parameters", id, builder)) {
      return builder.build();
    }
    return null;
  }

  @Override
  public ProtoDQSystem getDQSystem(String id) {
    var proto = readBin("dq_systems", id, ProtoDQSystem.parser());
    if (proto != null)
      return proto;
    var builder = ProtoDQSystem.newBuilder();
    if (readJson("dq_systems", id, builder)) {
      return builder.build();
    }
    return null;
  }

  private <T> T readBin(String folder, String id, Parser<T> binParser) {
    var path = folder + "/" + id + ".bin";
    try {
      var data = get(path);
      return data != null
        ? binParser.parseFrom(data)
        : null;
    } catch (Exception e) {
      var log = LoggerFactory.getLogger(getClass());
      log.error("failed to parse " + path, e);
      return null;
    }
  }

  private boolean readJson(String folder, String id, Message.Builder builder) {
    var path = folder + "/" + id + ".json";
    try {
      var data = get(path);
      if (data == null)
        return false;
      var json = new String(data, StandardCharsets.UTF_8);
      JsonFormat.parser()
        .ignoringUnknownFields()
        .merge(json, builder);
      return true;
    } catch (Exception e) {
      var log = LoggerFactory.getLogger(getClass());
      log.error("failed to parse " + path, e);
      return false;
    }
  }

  private byte[] get(String path) {
    try {
      Path file = zip.getPath(path);
      if (!Files.exists(file))
        return null;
      return Files.readAllBytes(file);
    } catch (Exception e) {
      var log = LoggerFactory.getLogger(getClass());
      log.error("failed to get file " + path, e);
      return null;
    }
  }

  private String pathOf(ModelType type) {
    if (type == null)
      return "unknown";
    return switch (type) {
      case ACTOR -> "actors";
      case CATEGORY -> "categories";
      case CURRENCY -> "currencies";
      case DQ_SYSTEM -> "dq_systems";
      case FLOW -> "flows";
      case FLOW_PROPERTY -> "flow_properties";
      case IMPACT_CATEGORY -> "lcia_categories";
      case IMPACT_METHOD -> "lcia_methods";
      case LOCATION -> "locations";
      case PARAMETER -> "parameters";
      case PROCESS -> "processes";
      case PRODUCT_SYSTEM -> "product_systems";
      case PROJECT -> "projects";
      case SOCIAL_INDICATOR -> "social_indicators";
      case SOURCE -> "sources";
      case UNIT_GROUP -> "unit_groups";
      default -> "unknown";
    };
  }
}

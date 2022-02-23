package org.openlca.proto.io.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.proto.grpc.SearchRequest;
import org.openlca.proto.io.input.In;
import org.openlca.util.Pair;

class Search {

  private final IDatabase database;

  private ModelType typeFilter;
  private final String[] terms;

  static Search of(IDatabase db, SearchRequest req) {
    if (req == null)
      return new Search(db, "");
    var search = new Search(db, req.getQuery());
    var type = In.modelTypeOf(req.getType());
    if (type != null && type.getModelClass() != null) {
      search.typeFilter = type;
    }
    return search;
  }

  private Search(IDatabase database, String term) {
    this.database = database;
    var rawTerm = term == null
      ? ""
      : term.toLowerCase().trim();
    terms = Arrays.stream(rawTerm.split(" "))
      .filter(s -> !s.isBlank())
      .toArray(String[]::new);
  }

  Stream<Descriptor> run() {
    return terms == null || terms.length == 0
      ? Stream.empty()
      : Arrays.stream(types())
      .flatMap(type -> allOf(type).stream())
      .map(d -> Pair.of(d, match(d)))
      .filter(pair -> pair.second != null)
      .sorted((p1, p2) -> compare(p1.second, p2.second))
      .map(p -> p.first);
  }

  private ModelType[] types() {
    if (typeFilter != null)
      return new ModelType[]{typeFilter};
    return new ModelType[]{
      ModelType.CATEGORY,
      ModelType.PROJECT,
      ModelType.PRODUCT_SYSTEM,
      ModelType.IMPACT_METHOD,
      ModelType.IMPACT_CATEGORY,
      ModelType.PROCESS,
      ModelType.FLOW,
      ModelType.SOCIAL_INDICATOR,
      ModelType.PARAMETER,
      ModelType.FLOW_PROPERTY,
      ModelType.UNIT_GROUP,
      ModelType.CURRENCY,
      ModelType.ACTOR,
      ModelType.SOURCE,
      ModelType.LOCATION,
      ModelType.DQ_SYSTEM
    };
  }

  private List<? extends Descriptor> allOf(ModelType type) {
    if (type == ModelType.PARAMETER)
      return new ParameterDao(database).getGlobalDescriptors();
    var dao = Daos.refDao(database, type);
    return dao == null
      ? Collections.emptyList()
      : dao.getDescriptors();
  }

  private int[] match(Descriptor d) {
    if (terms.length == 1
      && d.refId != null
      && d.refId.equalsIgnoreCase(terms[0])) {
      return new int[0];
    }
    var name = d.name;
    if (name == null)
      return null;
    var feed = name.toLowerCase();
    var match = new int[terms.length];
    var hasMatch = false;
    for (int i = 0; i < terms.length; i++) {
      int pos = feed.indexOf(terms[i]);
      if (pos >= 0) {
        hasMatch = true;
      }
      match[i] = pos;
    }
    return hasMatch ? match : null;
  }

  private int compare(int[] match1, int[] match2) {
    if (match1 == null || match2 == null)
      return 0;
    int n = Math.min(match1.length, match2.length);
    for (int i = 0; i < n; i++) {
      int pos1 = match1[i];
      int pos2 = match2[i];
      if (pos1 < 0 && pos2 < 0)
        continue;
      if (pos2 < 0)
        return -1;
      if (pos1 < 0)
        return 1;
      int diff = pos1 - pos2;
      if (diff != 0)
        return diff;
    }
    return 0;
  }
}

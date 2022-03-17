package org.openlca.proto.io;

import java.util.Arrays;

import org.openlca.core.model.RefEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.util.Strings;

public class Gen {

  public static void main(String[] args) {
    var root = SocialIndicator.class;
    var varName = "indicator";
    var protoName = "proto";

    Arrays.stream(root.getDeclaredFields())
      .sorted((f1, f2) -> Strings.compare(f1.getName(), f2.getName()))
      .forEach(f -> {
        var field = f.getName();
        var setter = protoName + ".set"
          + field.substring(0, 1).toUpperCase()
          + field.substring(1);

        var type = f.getType();

        if (type.equals(String.class)) {
          System.out.printf(
            "%s(Strings.orEmpty(%s.%s));%n",
            setter, varName, field);

        } else if (type.equals(boolean.class)
          || type.equals(double.class)
          || type.equals(int.class)) {
          System.out.printf(
            "%s(%s.%s);%n",
            setter, varName, field);

        } else if (type.equals(Double.class)) {
          System.out.printf(
            "if (%s.%s != null) {%n  %s(%s.%s);%n}%n",
            varName, field, setter, varName, field);

        } else if (RefEntity.class.isAssignableFrom(type)) {
          System.out.printf(
            "if (%s.%s != null) {%n  %s(Refs.toRef(%s.%s, config));%n}%n",
            varName, field, setter, varName, field);
        } else {
          System.out.println(
            "// ? proto." + setter + "(flow." + field + ")");
        }
      });
  }

}

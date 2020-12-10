package org.openlca.proto;

import com.google.protobuf.MessageOrBuilder;

public final class Messages {

  private Messages() {
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

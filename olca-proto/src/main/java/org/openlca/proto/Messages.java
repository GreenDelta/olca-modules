package org.openlca.proto;

import com.google.protobuf.MessageOrBuilder;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.RootEntity;
import org.openlca.proto.output.Out;

public final class Messages {

  private Messages() {
  }

  public static byte[] toBinary(IDatabase db, RootEntity e) {
    var proto = Out.toProto(db, e);
    return proto == null
      ? null
      : proto.toByteArray();
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

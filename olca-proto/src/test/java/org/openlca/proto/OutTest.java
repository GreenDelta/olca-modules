package org.openlca.proto;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openlca.core.model.Process;
import org.openlca.core.model.Version;
import org.openlca.proto.output.Refs;

public class OutTest {

  @Test
  public void testToProcessRef() {
    var process = new Process();
    process.name = "process name";
    process.description = "process description";
    process.version = Version.valueOf(1,0,0);
    var ref = Refs.refOf(process);
    assertEquals("process name", ref.getName());
    assertEquals("process description", ref.getDescription());
    assertEquals("01.00.000", ref.getVersion());
  }

}

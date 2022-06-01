package org.openlca.core.libraries;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.ToIntFunction;

import org.junit.Test;
import org.openlca.core.library.Libraries;
import org.openlca.core.library.LibraryDir;
import org.openlca.util.Dirs;

public class LibrariesTest {

	@Test
	public void testDependencyOrder() throws IOException {
		var dir = Files.createTempDirectory("_olca_tests").toFile();
		var libDir = LibraryDir.of(dir);
		var a = libDir.create("A");
		var b = libDir.create("B");
		b.addDependency(a);
		var c = libDir.create("C");
		c.addDependency(b);
		var d = libDir.create("D");
		d.addDependency(a);
		var e = libDir.create("E");
		e.addDependency(c);
		e.addDependency(d);

		var sorted = Libraries.dependencyOrderOf(e);
		ToIntFunction<String> pos = s -> {
			for (int i = 0; i < sorted.size(); i++) {
				if (sorted.get(i).name().equals(s))
					return i;
			}
			return -1;
		};

		assertEquals(5, sorted.size());
		assertEquals(0, pos.applyAsInt("A"));
		assertEquals(4, pos.applyAsInt("E"));
		assertTrue(pos.applyAsInt("B") < pos.applyAsInt("C"));

		Dirs.delete(libDir.folder());
	}

}

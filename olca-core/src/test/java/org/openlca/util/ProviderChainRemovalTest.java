package org.openlca.util;

import org.junit.Test;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;

import static org.junit.Assert.*;

/**
 * The test graphs here are written in DOT format, you can easily visualize
 * them with Graphviz, e.g. <a href="http://www.webgraphviz.com">Web-Graphviz</a>.
 * {@code 1} is always the reference process of the example.
 */
public class ProviderChainRemovalTest {

	@Test
	public void testSimpleChain() {
		var sys = parse("""
				digraph g {
				  2 -> 1;
				  3 -> 2;
				  4 -> 3;
				  5 -> 4;
				}
				""");

		// delete 5->4
		assertEquals(1, rem(sys, 5, 4));
		checkPresent(sys, 2, 1);
		checkPresent(sys, 3, 2);
		checkPresent(sys, 4, 3);
		checkAbsent(sys, 5, 4);
		checkProcs(sys, 1, 2, 3, 4);

		// delete 4->3
		assertEquals(1, rem(sys, 4, 3));
		checkPresent(sys, 2, 1);
		checkPresent(sys, 3, 2);
		checkAbsent(sys, 4, 3);
		checkAbsent(sys, 5, 4);
		checkProcs(sys, 1, 2, 3);

		// delete 3->2
		assertEquals(1, rem(sys, 3, 2));
		checkPresent(sys, 2, 1);
		checkAbsent(sys, 3, 2);
		checkAbsent(sys, 4, 3);
		checkAbsent(sys, 5, 4);
		checkProcs(sys, 1, 2);

		// delete 2->1
		assertEquals(1, rem(sys, 2, 1));
		checkAbsent(sys, 2, 1);
		checkAbsent(sys, 3, 2);
		checkAbsent(sys, 4, 3);
		checkAbsent(sys, 5, 4);
		checkProcs(sys, 1);
	}

	@Test
	public void testCutChain() {
		var sys = parse("""
				digraph g {
				  2 -> 1;
				  3 -> 2;
				  4 -> 3;
				  5 -> 4;
				}
				""");

		// delete 2->1
		assertEquals(4, rem(sys, 2, 1));
		checkAbsent(sys, 2, 1);
		checkAbsent(sys, 3, 2);
		checkAbsent(sys, 4, 3);
		checkAbsent(sys, 5, 4);
		assertTrue(sys.processLinks.isEmpty());
		checkProcs(sys, 1);
	}

	@Test
	public void testCutRefLoop() {
		var sys = parse("""
				digraph g {
				  1 -> 2;
				  2 -> 1;
				  3 -> 2;
				}
				""");

		// delete 1->2
		assertEquals(0, rem(sys, 1, 2));
		checkAbsent(sys, 1, 2);
		checkPresent(sys, 2, 1);
		checkPresent(sys, 3, 2);
		checkProcs(sys, 1, 2, 3);
	}

	@Test
	public void testCutLoop() {
		var sys = parse("""
				digraph g {
				  2 -> 1;
				  3 -> 2;
				  2 -> 3;
				}
				""");

		// delete 2->1
		assertEquals(2, rem(sys, 2, 1));
		checkAbsent(sys, 2, 1);
		checkAbsent(sys, 3, 2);
		checkAbsent(sys, 2, 3);
		checkProcs(sys, 1);
	}

	@Test
	public void testKeepLoop() {
		var sys = parse("""
				digraph g {
				  2 -> 1;
				  3 -> 2;
				  2 -> 3;
				  3 -> 1;
				}
				""");

		// delete 2->1
		assertEquals(0, rem(sys, 2, 1));
		checkAbsent(sys, 2, 1);
		checkPresent(sys, 3, 2);
		checkPresent(sys, 2, 3);
		checkPresent(sys, 3, 1);
		checkProcs(sys, 1, 2, 3);
	}

	private ProductSystem parse(String g) {
		var sys = new ProductSystem();
		sys.processes.add(1L);
		sys.referenceProcess = new Process();
		sys.referenceProcess.id = 1;
		for (var line : g.split("\n")) {
			var parts = line.split("->");
			if (parts.length < 2)
				continue;
			long p = Long.parseLong(parts[0].strip());
			long q = Long.parseLong(parts[1].split(";")[0].strip());
			sys.processes.add(p);
			sys.processes.add(q);
			var link = new ProcessLink();
			link.providerId = p;
			link.processId = q;
			sys.processLinks.add(link);
		}
		return sys;
	}

	private int rem(ProductSystem sys, long p, long q) {
		var r = ProviderChainRemoval.on(sys);
		return r.remove(link(sys, p, q));
	}

	private void checkProcs(ProductSystem sys, long... procs) {
		assertEquals(procs.length, sys.processes.size());
		for (long p : procs) {
			assertTrue(sys.processes.contains(p));
		}
	}

	private void checkAbsent(ProductSystem sys, long p, long q) {
		var msg = "link " + p + "->" + q + " must be absent";
		assertNull(msg, link(sys, p, q));
	}

	private void checkPresent(ProductSystem sys, long p, long q) {
		var msg = "link " + p + "->" + q + " must be present";
		assertNotNull(msg, link(sys, p, q));
	}

	private ProcessLink link(ProductSystem sys, long p, long q) {
		for (var link : sys.processLinks) {
			if (link.providerId == p && link.processId == q)
				return link;
		}
		return null;
	}
}

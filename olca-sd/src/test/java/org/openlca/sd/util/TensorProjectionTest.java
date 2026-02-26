package org.openlca.sd.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.openlca.sd.model.Dimension;
import org.openlca.sd.model.Subscript;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.eqn.TensorProjection;
import org.openlca.sd.model.cells.Cell;

public class TensorProjectionTest {

	// Dimensions used in tests
	private final Dimension product = Dimension.of("Product", "A", "B");
	private final Dimension region = Dimension.of("Region", "North", "South");

	/// Test: Same dimensions - tensor should be returned unchanged.
	///
	/// Source: [Product] with A=10, B=20
	/// Target: [Product]
	/// Expected: same tensor instance (A=10, B=20)
	@Test
	public void testSameDimensions() {
		var source = Tensor.of(product);
		source.set(Subscript.of("A"), Cell.of(10));
		source.set(Subscript.of("B"), Cell.of(20));

		var res = TensorProjection.of(source, source.dimensions());
		assertTrue(res.isOk());
		assertSame(source, res.value());
	}

	/// Test: Same shape, different dimension names - values copied to target.
	///
	/// Source: [Product] with A=10, B=20 (2 elements)
	/// Target: [Region] with North, South (2 elements)
	/// Expected: new tensor [Region] with North=10, South=20
	@Test
	public void testSameShapeDifferentDimensions() {
		var source = Tensor.of(product);
		source.set(Subscript.of("A"), Cell.of(10));
		source.set(Subscript.of("B"), Cell.of(20));

		var res = TensorProjection.of(source, List.of(region));
		assertTrue(res.isOk());

		var result = res.value();
		assertEquals(1, result.dimensions().size());
		assertEquals(region, result.dimensions().get(0));
		assertEquals(10.0, result.get(Subscript.of("North")).asNum(), 0.001);
		assertEquals(20.0, result.get(Subscript.of("South")).asNum(), 0.001);
	}

	/// Test: 2D tensor summed to 1D - sum over extra dimension.
	///
	/// Source: [Product, Region] - a 2x2 tensor
	///   A,North = 1    A,South = 2
	///   B,North = 3    B,South = 4
	///
	/// Target: [Product] - sum over Region
	///
	/// Expected: [Product]
	///   A = 1 + 2 = 3
	///   B = 3 + 4 = 7
	@Test
	public void testSumOverExtraDimension() {
		var source = Tensor.of(product, region);
		source.set(List.of(Subscript.of("A"), Subscript.of("North")), Cell.of(1));
		source.set(List.of(Subscript.of("A"), Subscript.of("South")), Cell.of(2));
		source.set(List.of(Subscript.of("B"), Subscript.of("North")), Cell.of(3));
		source.set(List.of(Subscript.of("B"), Subscript.of("South")), Cell.of(4));

		var res = TensorProjection.of(source, List.of(product));
		assertTrue(res.isOk());

		var result = res.value();
		assertEquals(1, result.dimensions().size());
		assertEquals(product, result.dimensions().get(0));
		assertEquals(3.0, result.get(Subscript.of("A")).asNum(), 0.001);  // 1 + 2
		assertEquals(7.0, result.get(Subscript.of("B")).asNum(), 0.001);  // 3 + 4
	}

	/// Test: Simulates the plastic-subs.stmx scenario.
	///
	/// Stock has dimension [Product] with 2 elements
	/// Flow has dimensions [Product, Waste] with 1 element in Waste
	///
	/// Source: [Product, Waste] where Waste has 1 element
	///   A,W1 = 5
	///   B,W1 = 8
	///
	/// Target: [Product] - sum over Waste
	///
	/// Expected: [Product]
	///   A = 5
	///   B = 8
	@Test
	public void testPlasticSubsScenario() {
		var waste = Dimension.of("Waste", "W1");

		var source = Tensor.of(product, waste);
		source.set(List.of(Subscript.of("A"), Subscript.of("W1")), Cell.of(5));
		source.set(List.of(Subscript.of("B"), Subscript.of("W1")), Cell.of(8));

		var res = TensorProjection.of(source, List.of(product));
		assertTrue(res.isOk());

		var result = res.value();
		assertEquals(1, result.dimensions().size());
		assertEquals(product, result.dimensions().get(0));
		assertEquals(5.0, result.get(Subscript.of("A")).asNum(), 0.001);
		assertEquals(8.0, result.get(Subscript.of("B")).asNum(), 0.001);
	}

	/// Test: 1D tensor projected to same 1D - unchanged.
	@Test
	public void testSimple1DUnchanged() {
		var source = Tensor.of(region);
		source.set(Subscript.of("North"), Cell.of(100));
		source.set(Subscript.of("South"), Cell.of(200));

		var res = TensorProjection.of(source, List.of(region));
		assertTrue(res.isOk());
		assertSame(source, res.value());
	}

	/// Test: Error when target dimension not in source (and different shape).
	@Test
	public void testErrorWhenTargetDimNotInSource() {
		// Source has 2 elements, target has 3 - different shapes, can't copy
		var threeProduct = Dimension.of("ThreeProduct", "X", "Y", "Z");
		var source = Tensor.of(region);
		source.set(Subscript.of("North"), Cell.of(100));
		source.set(Subscript.of("South"), Cell.of(200));

		// Try to project to a dimension with different size
		var res = TensorProjection.of(source, List.of(threeProduct));
		assertTrue(res.isError());
	}

	/// Test: Error when projecting to multiple dimensions (not supported).
	@Test
	public void testErrorWhenMultipleTargetDims() {
		var source = Tensor.of(product);
		source.set(Subscript.of("A"), Cell.of(10));
		source.set(Subscript.of("B"), Cell.of(20));

		// Source is 1D, target is 2D with different shape - not supported
		var threeRegion = Dimension.of("ThreeRegion", "X", "Y", "Z");
		var res = TensorProjection.of(source, List.of(product, threeRegion));
		assertTrue(res.isError());
	}
}

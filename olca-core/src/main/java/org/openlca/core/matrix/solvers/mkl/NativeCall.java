package org.openlca.core.matrix.solvers.mkl;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

/// The off-heap arguments of a BLAS or LAPACK call. Java arrays are copied
/// into a confined arena that is released when the call is closed. Arrays
/// that are registered as outputs are copied back into the Java arrays with
/// [#writeBack()] after a successful call. An instance must only be used in
/// the thread that created it.
final class NativeCall implements AutoCloseable {

	private final Arena arena = Arena.ofConfined();
	private final List<Runnable> writeBacks = new ArrayList<>();

	/// Copies the given values into an input segment.
	MemorySegment in(double[] values) {
		return arena.allocateFrom(JAVA_DOUBLE, values);
	}

	/// Copies the given values into an input segment.
	MemorySegment in(long[] values) {
		return arena.allocateFrom(JAVA_LONG, values);
	}

	/// Copies the given values into a segment that is also written back into
	/// the array.
	MemorySegment inOut(double[] values) {
		var seg = in(values);
		writeBacks.add(() -> MemorySegment.copy(
			seg, JAVA_DOUBLE, 0, values, 0, values.length));
		return seg;
	}

	/// Allocates an output segment of the size of the given array, without
	/// copying the values of the array into it. The segment is written back
	/// into the array.
	MemorySegment out(double[] values) {
		var seg = arena.allocate(JAVA_DOUBLE, values.length);
		writeBacks.add(() -> MemorySegment.copy(
			seg, JAVA_DOUBLE, 0, values, 0, values.length));
		return seg;
	}

	/// Allocates an output segment of the size of the given array, without
	/// copying the values of the array into it. The segment is written back
	/// into the array.
	MemorySegment out(long[] values) {
		var seg = arena.allocate(JAVA_LONG, values.length);
		writeBacks.add(() -> MemorySegment.copy(
			seg, JAVA_LONG, 0, values, 0, values.length));
		return seg;
	}

	/// Allocates a work array of the given size.
	MemorySegment work(long size) {
		return arena.allocate(JAVA_DOUBLE, size);
	}

	/// A scalar passed by reference.
	MemorySegment of(long value) {
		return arena.allocateFrom(JAVA_LONG, value);
	}

	/// A scalar passed by reference.
	MemorySegment of(double value) {
		return arena.allocateFrom(JAVA_DOUBLE, value);
	}

	/// A character passed by reference, e.g. `'N'` for no transposition.
	MemorySegment of(char value) {
		return arena.allocateFrom(JAVA_BYTE, (byte) value);
	}

	/// Allocates the `INFO` parameter of a LAPACK function.
	MemorySegment info() {
		return arena.allocate(JAVA_LONG);
	}

	/// Copies the output segments back into their Java arrays.
	void writeBack() {
		for (var w : writeBacks) {
			w.run();
		}
	}

	@Override
	public void close() {
		arena.close();
	}
}

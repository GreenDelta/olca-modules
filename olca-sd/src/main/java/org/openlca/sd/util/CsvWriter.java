package org.openlca.sd.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.openlca.commons.Res;
import org.openlca.commons.Strings;
import org.openlca.sd.eqn.SimulationState;
import org.openlca.sd.eqn.Simulator;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.BoolCell;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.NumCell;
import org.openlca.sd.model.cells.TensorCell;

public class CsvWriter {

	private final Simulator sim;
	private final File file;
	private final Map<String, Integer> columns;

	private CsvWriter(Simulator sim, File file) {
		this.sim = Objects.requireNonNull(sim);
		this.file = Objects.requireNonNull(file);
		columns = new HashMap<>();
	}

	public static CsvWriter of(Simulator sim, File file) {
		return new CsvWriter(sim, file);
	}

	public Res<Void> run() {
		try (var fw = new FileWriter(file, StandardCharsets.UTF_8);
				var writer = new BufferedWriter(fw)) {
			boolean first = true;
			for (var res : sim) {
				if (res.isError())
					return res.castError();
				var state = res.value();

				if (first) {
					var headers = initColumns(state);
					writer.write("\"Iteration\",\"Time\"");
					for (var h : headers) {
						writer.write(",\"");
						writer.write(h);
						writer.write("\"");
					}
					writer.newLine();
					first = false;
				}

				writer.write(state.iteration() + "," + state.time());
				for (var v : valuesOf(state)) {
					writer.write(',');
					writer.write(v != null ? v : "");
				}
				writer.newLine();
			}
			return Res.ok();
		} catch (Exception e) {
			return Res.error("Failed to write results to file: " + file, e);
		}
	}

	private String[] initColumns(SimulationState state) {
		var vars = new ArrayList<>(state.vars().values());
		vars.sort((vi, vj) -> Strings.compareIgnoreCase(
				vi.name().value(), vj.name().value()));
		int col = 0;
		var headers = new ArrayList<String>();
		for (var v : vars) {
			var val = v.value();
			if (val instanceof TensorCell(Tensor t)) {
				var ax = Tensors.addressesOf(t);
				for (var a : ax) {
					var key = Tensors.addressKeyOf(v, a);
					headers.add(key);
					columns.put(key, col++);
				}
				continue;
			}
			var key = v.name().value();
			headers.add(key);
			columns.put(key, col++);
		}
		return headers.toArray(String[]::new);
	}

	private String[] valuesOf(SimulationState state) {

		var xs = new String[columns.size()];
		BiConsumer<String, Cell> push = (key, cell) -> {
			var col = columns.get(key);
			if (col == null)
				return;
			int c = col;
			if (c < 0 || c >= xs.length)
				return;
			xs[c] = strOf(cell);
		};

		for (var v : state.vars().values()) {
			var val = v.value();
			if (val instanceof TensorCell(Tensor t)) {
				for (var address : Tensors.addressesOf(t)) {
					var key = Tensors.addressKeyOf(v, address);
					push.accept(key, t.get(address));
				}
				continue;
			}
			push.accept(v.name().value(), val);
		}

		return xs;
	}

	private String strOf(Cell cell) {
		return switch (cell) {
			case NumCell(double num) -> Double.toString(num);
			case BoolCell(boolean bool) -> Boolean.toString(bool);
			case null, default -> "";
		};
	}
}

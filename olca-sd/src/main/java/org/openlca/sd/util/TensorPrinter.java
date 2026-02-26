package org.openlca.sd.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.openlca.sd.model.Dimension;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.Subscript;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.cells.TensorCell;

public class TensorPrinter {

	private final PrintStream o;

	public TensorPrinter() {
		o = System.out;
	}

	public void print(Cell cell) {
		if (cell == null) {
			o.println("No tensor");
			o.println("'()\n");
			return;
		}

		if (cell instanceof TensorCell(Tensor tensor)) {
			print(tensor);
			return;
		}

		o.println("Cell:\n" + cell + "\n");
	}

	public void print(Tensor tensor) {
		if (tensor == null) {
			o.println("No tensor");
			o.println("'()\n");
			return;
		}

		var dims = tensor.dimensions();
		if (dims.size() == 1) {
			printDim1(dims.getFirst(), tensor);
			return;
		}

		if (dims.size() == 2) {
			printDim2(dims.getFirst(), dims.getLast(), tensor);
			return;
		}

		printDimN(dims, tensor);

	}

	private void printDim1(Dimension dim, Tensor tensor) {
		o.println("Tensor of dimension: " + dim.name().label());
		for (var elem : dim.elements()) {
			o.printf("%s\t%s%n", elem.label(), tensor.get(Subscript.of(elem)));
		}
		o.println();
	}

	private void printDim2(Dimension rows, Dimension cols, Tensor tensor) {
		o.println("Tensor of dimension: " + rows.name().label()
			+ " x " + cols.name().label());
		for (var colElem : cols.elements()) {
			o.print("\t" + colElem.label());
		}
		o.println();

		for (var rowElem : rows.elements()) {
			o.print(rowElem.label());
			for (var colElem : cols.elements()) {
				var cell = tensor.get(Subscript.of(rowElem), Subscript.of(colElem));
				o.print("\t"+ cell);
			}
			o.println();
		}

		o.println();
	}

	private void printDimN(List<Dimension> dims, Tensor tensor) {
		var elems = new ArrayList<List<Id>>();
		for (var dim : dims) {
			if (elems.isEmpty()) {
				for (var elem : dim.elements()) {
					elems.add(List.of(elem));
				}
				continue;
			}

			var nextElems = new ArrayList<List<Id>>();
			for (var seq : elems) {
				for (var elem : dim.elements()) {
					var nextSeq = new ArrayList<>(seq);
					nextSeq.add(elem);
					nextElems.add(nextSeq);
				}
			}
			elems = nextElems;
		}

		for (var seq : elems) {
			var subs = new ArrayList<Subscript>();
			for (var elem : seq) {
				subs.add(Subscript.of(elem));
				o.print(elem.label() + "\t");
			}
			o.println(tensor.get(subs));
		}
	}
}

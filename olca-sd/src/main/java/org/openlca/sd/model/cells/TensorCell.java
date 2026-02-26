package org.openlca.sd.model.cells;

import java.util.Objects;
import java.util.stream.Collectors;

import org.openlca.commons.Res;
import org.openlca.sd.eqn.Interpreter;
import org.openlca.sd.model.Tensor;

public record TensorCell(Tensor value) implements Cell {

	public TensorCell {
		Objects.requireNonNull(value);
	}

	@Override
	public Res<Cell> eval(Interpreter interpreter) {
		var t = Tensor.of(value.dimensions());
		for (int i = 0; i < value.size(); i++) {
			var res = value.get(i).eval(interpreter);
			if (res.isError())
				return res;
			t.set(i, res.value());
		}
		return Res.ok(new TensorCell(t));
	}

	@Override
	public String toString() {
		var dims = value.dimensions()
			.stream()
			.map(d -> d.name().label())
			.collect(Collectors.joining(" × "));
		return "tensor{" + dims + "}";
	}

}

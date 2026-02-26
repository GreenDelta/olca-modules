package org.openlca.sd.eqn;

import java.util.Map;
import java.util.Optional;

import org.openlca.sd.model.cells.Cell;
import org.openlca.sd.model.Id;
import org.openlca.sd.model.Var;

public record SimulationState(
	int iteration,
	double time,

	Map<Id, Var> vars
) {

	public Optional<Cell> valueOf(Id id) {
		if (vars == null || id == null)
			return Optional.empty();
		var v = vars.get(id);
		return v != null && v.values() != null && !v.values().isEmpty()
			? Optional.of(v.value())
			: Optional.empty();
	}

}

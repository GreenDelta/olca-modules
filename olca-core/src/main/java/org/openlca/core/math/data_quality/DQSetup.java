package org.openlca.core.math.data_quality;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.CalculationSetup;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProductSystem;

public class DQSetup {

	public AggregationType aggregationType;

	/**
	 * Indicates whether values should be rounded in `ceiling` mode (e.g.
	 * 2.1 is rounded to 3 in this case). If this field is set to false, normal
	 * `half-up` rounding is used.
	 */
	public boolean ceiling;

	public NAHandling naHandling;
	public DQSystem processSystem;
	public DQSystem exchangeSystem;

	public DQSetup() {
		aggregationType = AggregationType.WEIGHTED_AVERAGE;
		ceiling = false;
		naHandling = NAHandling.EXCLUDE;
	}

	/**
	 * Initializes the setup with default settings. The data
	 * quality systems are initialized with the respective
	 * values from the reference process of the system (which
	 * may be null).
	 */
	public static DQSetup of(ProductSystem system) {
		return system == null
				? new DQSetup()
				: of(system.referenceProcess);
	}

	public static DQSetup of(CalculationSetup setup) {
		return setup == null
				? new DQSetup()
				: of(setup.process());
	}

	public static DQSetup of(Process process) {
		var setup = new DQSetup();
		if (process != null) {
			setup.exchangeSystem = process.exchangeDqSystem;
			setup.processSystem = process.dqSystem;
		}
		return setup;
	}

	/**
	 * Returns a setup for the given database if all processes in the database
	 * are linked to the same data quality system. If this is not the case, an
	 * empty option is returned.
	 */
	public static Optional<DQSetup> consistentOf(IDatabase db) {
		if (db == null)
			return Optional.empty();
		var systems = db.getAll(DQSystem.class)
				.stream()
				.collect(Collectors.toMap(system -> system.id, system -> system));
		var sql = "select f_dq_system, f_exchange_dq_system from tbl_processes";
		var procRef = new SysRef();
		var exchRef = new SysRef();
		NativeSql.on(db).query(sql, r -> {
			var procSys = systems.get(r.getLong(1));
			var exchSys = systems.get(r.getLong(2));
			return procRef.apply(procSys) && exchRef.apply(exchSys);
		});

		if (procRef.error || exchRef.error)
			return Optional.empty();
		if (procRef.system == null && exchRef.system == null)
			return Optional.empty();

		var setup = new DQSetup();
		setup.processSystem = procRef.system;
		setup.exchangeSystem = exchRef.system;
		return Optional.of(setup);
	}

	private static class SysRef {
		DQSystem system;
		boolean error;

		boolean apply(DQSystem next) {
			if (error)
				return false;
			if (next == null)
				return true;
			if (system == null) {
				system = next;
				return true;
			}
			if (Objects.equals(system, next))
				return true;
			error = true;
			system = null;
			return false;
		}
	}
}

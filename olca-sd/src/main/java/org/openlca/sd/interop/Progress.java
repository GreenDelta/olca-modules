package org.openlca.sd.interop;

/// A callback interface for tracking the progress of a coupled simulation.
/// Implementations can use this to update a progress bar or to signal
/// cancellation of the simulation.
public interface Progress {

	/// Called after each simulation iteration has been completed, including
	/// the SD step and all coupled LCA calculations for that step.
	void tick();

	/// Returns `true` if the user or system has requested cancellation of the
	/// simulation. The simulator checks this between iterations and between
	/// system calculations.
	boolean isCanceled();
}

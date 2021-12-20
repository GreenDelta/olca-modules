package org.openlca.core.io;

/**
 * Describes a process that can be cancelled.
 */
public interface Cancelable extends Runnable {

	void cancel();

	boolean isCanceled();

}

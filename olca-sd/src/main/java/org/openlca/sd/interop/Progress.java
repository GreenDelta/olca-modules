package org.openlca.sd.interop;

public interface Progress {
	void worked(int work);

	boolean isCanceled();
}

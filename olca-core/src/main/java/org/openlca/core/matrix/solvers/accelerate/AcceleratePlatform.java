package org.openlca.core.matrix.solvers.accelerate;

/**
 * Platform detection utility for Accelerate framework support.
 */
public final class AcceleratePlatform {

	private AcceleratePlatform() {
	}

	/**
	 * Returns true if the current platform is ARM64 macOS, where Accelerate
	 * framework is available as a system library.
	 */
	public static boolean isArm64MacOS() {
		String os = System.getProperty("os.name", "").toLowerCase();
		String arch = System.getProperty("os.arch", "").toLowerCase();
		return os.contains("mac") && (arch.contains("aarch64") || arch.contains("arm64"));
	}
}


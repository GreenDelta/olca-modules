package org.openlca.core.matrix.solvers.accelerate;

public final class AcceleratePlatform {

	private AcceleratePlatform() {
	}

	public static boolean isArm64MacOS() {
		String os = System.getProperty("os.name", "").toLowerCase();
		String arch = System.getProperty("os.arch", "").toLowerCase();
		return os.contains("mac") && (arch.contains("aarch64") || arch.contains("arm64"));
	}
}


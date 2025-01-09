package org.openlca.io.smartepd;

import java.util.Optional;

public enum SmartModule {

	A1A2A3(SmartStage.PRODUCTION),
	A1(SmartStage.PRODUCTION),
	A2(SmartStage.PRODUCTION),
	A3(SmartStage.PRODUCTION),
	A4(SmartStage.CONSTRUCTION),
	A5(SmartStage.CONSTRUCTION),
	B1(SmartStage.USE),
	B2(SmartStage.USE),
	B3(SmartStage.USE),
	B4(SmartStage.USE),
	B5(SmartStage.USE),
	B6(SmartStage.USE),
	B7(SmartStage.USE),
	C1(SmartStage.END_OF_LIFE),
	C2(SmartStage.END_OF_LIFE),
	C3(SmartStage.END_OF_LIFE),
	C4(SmartStage.END_OF_LIFE),
	D(SmartStage.BENEFITS);

	private final SmartStage stage;

	SmartModule(SmartStage stage) {
		this.stage = stage;
	}

	public SmartStage stage() {
		return stage;
	}

	public static Optional<SmartModule> of(String name) {
		if (name == null)
			return Optional.empty();
		var s = name.strip().toUpperCase();
		for (var m : values()) {
			if (m.name().equals(s))
				return Optional.of(m);
		}
		if (s.equals("A1-A3"))
			return Optional.of(A1A2A3);
		return Optional.empty();
	}
}

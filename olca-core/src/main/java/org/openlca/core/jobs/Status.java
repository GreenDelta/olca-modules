package org.openlca.core.jobs;

public final class Status {

	public static final int FAILED = 0;
	public static final int OK = 1;
	public static final int CANCELLED = 2;
	public static final int RUNNING = 4;
	public static final int WAITING = 8;

	private int flag;
	private String message;

	public Status(int flag) {
		this.flag = flag;
	}

	public Status(int flag, String message) {
		this.flag = flag;
		this.message = message;
	}

	public int getFlag() {
		return flag;
	}

	public String getMessage() {
		return message;
	}

}

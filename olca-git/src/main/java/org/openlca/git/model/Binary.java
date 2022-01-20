package org.openlca.git.model;

public class Binary {

	public final String filename;
	public final byte[] data;

	public Binary(String filename, byte[] data) {
		this.filename = filename;
		this.data = data;
	}

}
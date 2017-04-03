package org.openlca.cloud.model.data;

public class BinaryFile {

	public final String path;
	public final byte[] data;

	public BinaryFile(String path, byte[] data) {
		this.path = path;
		this.data = data;
	}

}
package org.openlca.git.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openlca.git.model.Diff;

public interface BinaryResolver {

	BinaryResolver NULL = new BinaryResolver() {

		@Override
		public byte[] resolve(Diff change, String relativePath) throws IOException {
			return null;
		}

		@Override
		public List<String> list(Diff change, String relativePath) {
			return new ArrayList<>();
		}

		@Override
		public boolean isDirectory(Diff change, String relativePath) {
			return false;
		}
	};

	List<String> list(Diff change, String relativePath);

	boolean isDirectory(Diff change, String relativePath);

	byte[] resolve(Diff change, String relativePath) throws IOException;

}
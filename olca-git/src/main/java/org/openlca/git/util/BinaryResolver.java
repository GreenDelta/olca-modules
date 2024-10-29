package org.openlca.git.util;

import java.io.IOException;
import java.util.List;

import org.openlca.git.model.Diff;

public interface BinaryResolver {

	List<String> list(Diff change, String relativePath);

	boolean isDirectory(Diff change, String relativePath);

	byte[] resolve(Diff change, String relativePath) throws IOException;

}
package org.openlca.git.util;

import java.io.IOException;
import java.util.List;

import org.openlca.git.model.Change;

public interface BinaryResolver {

	List<String> list(Change change, String relativePath);

	boolean isDirectory(Change change, String relativePath);

	byte[] resolve(Change change, String relativePath) throws IOException;

}
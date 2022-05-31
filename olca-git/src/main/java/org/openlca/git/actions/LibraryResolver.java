package org.openlca.git.actions;

import org.openlca.core.library.Library;

public interface LibraryResolver {

	Library resolve(String libraryId);

}

package org.openlca.core.database;

public record DataPackage(DataPackageType type, String name, String version, String url) {

	public static DataPackage library(String name, String url) {
		return new DataPackage(DataPackageType.LIBRARY, name, null, url);
	}

	public static DataPackage repository(String name, String version, String url) {
		return new DataPackage(DataPackageType.REPOSITORY, name, version, url);
	}

	public boolean isLibrary() {
		return type == DataPackageType.LIBRARY;
	}
	
	public boolean isRepository() {
		return type == DataPackageType.REPOSITORY;
	}
	
	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof DataPackage p))
			return false;
		return name.equals(p.name);
	}

	@Override
	public final int hashCode() {
		return name.hashCode();
	}
	
	public static enum DataPackageType {
		
		LIBRARY, REPOSITORY;
		
	}

}
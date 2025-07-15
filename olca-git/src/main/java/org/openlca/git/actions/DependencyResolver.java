package org.openlca.git.actions;

import org.openlca.core.database.DataPackage;
import org.openlca.core.library.Library;
import org.openlca.git.repo.ClientRepository;

public interface DependencyResolver {

	IResolvedDependency<?> resolve(DataPackage dataPackage);

	public interface IResolvedDependency<T> {

		DataPackage dataPackage();

		T dependency();

		public static IResolvedDependency<Library> library(DataPackage dataPackage, Library library) {
			return new IResolvedDependency<Library>() {
				@Override
				public DataPackage dataPackage() {
					return dataPackage;
				}

				@Override
				public Library dependency() {
					return library;
				}
			};
		}

		public static IResolvedDependency<ClientRepository> repository(DataPackage dataPackage,
				ClientRepository repository) {
			return new IResolvedDependency<ClientRepository>() {
				@Override
				public DataPackage dataPackage() {
					return dataPackage;
				}

				@Override
				public ClientRepository dependency() {
					return repository;
				}
			};
		}

	}

}

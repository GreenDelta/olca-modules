package examples;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.jpa.PersistenceProvider;

import com.google.common.io.Files;

// This example creates a database schema from the openLCA model using the DDL
// generation feature from EclipseLink. In openLCA  we do not create the 
// database schema in this way but have the current schema as a separate file (
// current_schema_<database name>.sql in org.openlca.core.database.internal).
// This allows us to track the schema changes via version control (see also the
// upgrades package), optimize the schema for the special needs in openLCA, or
// add additional information to the database (like the openlca_version table)
// which are not managed by JPA.
// However, automatically generating the database schema might be useful when
// developing new features are using the openLCA model in other applications.
public class DatabaseSchemaGeneration {

	public static void main(String[] args) {

		try {

			Map<Object, Object> config = new HashMap<>();

			// set the connection data to the database
			// you should already have an existing database (in this case
			// olca_test_db)
			String url = "jdbc:mysql://localhost:3306/olca_test_db";
			String user = "root";
			String pw = "";
			config.put("javax.persistence.jdbc.url", url);
			config.put("javax.persistence.jdbc.user", user);
			config.put("javax.persistence.jdbc.password", pw);

			// change the driver and database type if you want to create the
			// database schema for another database type than MySQL
			config.put("javax.persistence.jdbc.driver", "com.mysql.jdbc.Driver");
			config.put("eclipselink.target-database", "MySQL");

			// specify the location where the schema files should be written
			String folder = Files.createTempDir().getAbsolutePath();
			String createDllFile = "olca_schema_create.sql";
			String dropDllFile = "olca_schema_drop.sql";

			// see
			// http://eclipse.org/eclipselink/documentation/2.4/jpa/extensions/p_ddl_generation_output_mode.htm
			// for more configuration details
			config.put("eclipselink.ddl-generation", "drop-and-create-tables");
			config.put("eclipselink.ddl-generation.output-mode", "both");
			config.put("eclipselink.create-ddl-jdbc-file-name", createDllFile);
			config.put("eclipselink.drop-ddl-jdbc-file-name", dropDllFile);
			config.put("eclipselink.application-location", folder);

			// now we create an entity manager which will generate the database
			// schema
			new PersistenceProvider()
					.createEntityManagerFactory("openLCA", config)
					.createEntityManager().close();

			System.out.println("DDL files should be created now in: " + folder);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

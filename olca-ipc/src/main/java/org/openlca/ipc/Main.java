package org.openlca.ipc;

import java.io.File;
import java.io.IOException;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args == null || args.length == 0 || args[0] == null || args[0].trim().isEmpty())
            throw new IllegalArgumentException("Missing database path as argument");
        File dir = new File(args[0]);
        if (!dir.exists() || !dir.isDirectory())
            throw new IllegalArgumentException("Database directory does not exist");
        int port = 8080;
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }
        IDatabase database = new DerbyDatabase(dir);
        Server server = new Server(port, database);
        System.out.println("Started openLCA IPC Server at localhost:" + server.getListeningPort());
    }

}
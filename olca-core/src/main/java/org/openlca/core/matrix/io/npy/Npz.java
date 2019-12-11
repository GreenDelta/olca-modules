package org.openlca.core.matrix.io.npy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Npz {

	private Npz() {
	}

	/**
	 * Returns a list with the entries of the given NPZ file. As an NPZ file
	 * is just a zip file of NPY files this gives the names of the NPY files
	 * in the zip.
	 */
	public static List<String> entries(File file) {
		try (ZipFile zip = new ZipFile(file)){
			List<String> names = new ArrayList<>();
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry e = entries.nextElement();
				names.add(e.getName());

				// TODO: this is currently just for testing
				System.out.println(e.getName());
				try (InputStream in = zip.getInputStream(e);
					 BufferedInputStream buf = new BufferedInputStream(in)) {
					System.out.println(Header.read(buf));
				}
			}
			return names;
		} catch (IOException e) {
			throw new RuntimeException("Failed to read NPZ file " + file, e);
		}
	}

	public static void main(String[] args) {
		String path = "/Users/ms/Downloads/csr.npz";
		System.out.println(Npz.entries(new File(path)));
	}

}

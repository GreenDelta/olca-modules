package org.openlca.core.library;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.util.Exceptions;

public final class LibTechIndex {

	private LibTechIndex() {
	}

	public static void write(Library library, IDatabase db, TechIndex index) {
		var context = WriterContext.create(db, library);
		write(context, index);
	}

	public static void write(WriterContext context, TechIndex index) {
		new Writer(context, index).write();
	}

	private record Writer(WriterContext context, TechIndex index) {

		void write() {
			var file = new File(context.library().folder, "index_A.bin");
			var index = Proto.ProductIndex.newBuilder();
			this.index.each((i, product) -> {
				var entry = Proto.ProductEntry.newBuilder();
				entry.setIndex(i);
				entry.setProcess(context.toProtoProcess(product.provider()));
				entry.setProduct(context.toProtoFlow(product.flow()));
				index.addProduct(entry.build());
			});

			try (var stream = new FileOutputStream(file);
					var buffer = new BufferedOutputStream(stream)) {
				index.build().writeTo(buffer);
			} catch (Exception e) {
				Exceptions.unchecked(
					"failed to write tech-index to " + file, e);
			}
		}
	}
}

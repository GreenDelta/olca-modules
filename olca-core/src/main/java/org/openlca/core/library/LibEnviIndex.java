package org.openlca.core.library;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.openlca.core.database.IDatabase;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.util.Exceptions;

public final class LibEnviIndex {

	private LibEnviIndex() {
	}

	public static void write(Library library, IDatabase db, EnviIndex index) {
		var context = WriterContext.create(db, library);
		write(context, index);
	}

	public static void write(WriterContext context, EnviIndex index) {
		new Writer(context, index).write();
	}

	private record Writer(WriterContext context, EnviIndex index) {

		void write() {
			var file = new File(context.library().folder, "index_B.bin");
			var index = Proto.ElemFlowIndex.newBuilder();
			this.index.each((i, f) -> {
				var entry = Proto.ElemFlowEntry.newBuilder();
				entry.setIndex(i);
				entry.setFlow(context.toProtoFlow(f.flow()));
				if (f.location() != null) {
					entry.setLocation(context.toProtoLocation(f.location()));
				}
				index.addFlow(entry);
			});

			try (var stream = new FileOutputStream(file);
					 var buffer = new BufferedOutputStream(stream)) {
				index.build().writeTo(buffer);
			} catch (Exception e) {
				Exceptions.unchecked(
					"failed to write envi-index to " + file, e);
			}
		}
	}

}

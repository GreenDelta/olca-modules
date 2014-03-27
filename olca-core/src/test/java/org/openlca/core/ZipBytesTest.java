package org.openlca.core;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.util.BinUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipBytesTest {

	private Logger log = LoggerFactory.getLogger(getClass());

	private final String TEXT = "Sed ut perspiciatis unde omnis iste natus " +
			"error sit voluptatem accusantium doloremque laudantium, totam rem " +
			"aperiam, eaque ipsa quae ab illo inventore veritatis et quasi " +
			"architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam " +
			"voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed " +
			"quia consequuntur magni dolores eos qui ratione voluptatem sequi " +
			"nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor " +
			"sit amet, consectetur, adipisci velit, sed quia non numquam eius " +
			"modi tempora incidunt ut labore et dolore magnam aliquam quaerat " +
			"voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem " +
			"ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi " +
			"consequatur? Quis autem vel eum iure reprehenderit qui in ea " +
			"voluptate velit esse quam nihil molestiae consequatur, vel illum " +
			"qui dolorem eum fugiat quo voluptas nulla pariatur?";

	@Test
	public void testGzip() throws Exception {
		log.trace("test gzip");
		byte[] bytes = TEXT.getBytes("utf-8");
		byte[] compressed = BinUtils.gzip(bytes);
		log.trace("compressed from {} to {}", bytes.length, compressed.length);
		bytes = BinUtils.gunzip(compressed);
		String t = new String(bytes, "utf-8");
		Assert.assertEquals(TEXT, t);
	}

	@Test
	public void testZip() throws Exception {
		log.trace("test zip / deflate");
		byte[] bytes = TEXT.getBytes("utf-8");
		byte[] compressed = BinUtils.zip(bytes);
		log.trace("compressed from {} to {}", bytes.length, compressed.length);
		bytes = BinUtils.unzip(compressed);
		String t = new String(bytes, "utf-8");
		Assert.assertEquals(TEXT, t);
	}

}

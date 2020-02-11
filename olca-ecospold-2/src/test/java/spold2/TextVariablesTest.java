package spold2;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

public class TextVariablesTest {

	@Test
	public void testTextVariables() throws Exception {
		InputStream is = getClass().getResourceAsStream("text_variables_test.spold");
		DataSet ds = EcoSpold2.read(is).activity();
		String comment = RichText.join(ds.description.activity.generalComment);
		Assert.assertFalse(comment.contains("test {{var1}} and {{var2}}."));
		Assert.assertTrue(comment.contains("test val1 and val2."));
	}

}

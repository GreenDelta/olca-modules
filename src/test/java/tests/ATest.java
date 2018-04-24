package tests;

import org.junit.Assert;
import org.junit.Test;

public class ATest {

    @Test
    public void test() {
        System.out.println("running a test ...");
        Assert.assertTrue(4 == 2 << 1);
    }
}

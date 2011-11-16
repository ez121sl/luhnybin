package ez;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ez.CreditCardFilter.CreditCardFilterWriter2;
import ez.CreditCardFilter.Util;


public class CreditCardFilterTest {
	
	@Test
	public void testLuhn() throws Exception {
		List<Integer> l1 = Arrays.asList(5, 6, 7, 8);
		
		Assert.assertTrue(Util.luhn(l1, 0, l1.size()));
		
		List<Integer> l2 = Arrays.asList(6, 7, 8, 9);
		
		Assert.assertTrue(!Util.luhn(l2, 0, l2.size()));
	}
	
	@Test
	public void testPump() throws Exception {
		String test = "Hello, 5523 1900 2865 6920 World!";
		/*
		String test = "" +
		"LF only ->\n\n<- LF only\n" +
		"56613959932537\n" +
		"508733740140655\n" +
		"6853371389452376\n" +
		"49536290423965\n" +
		"306903975081421\n" +
		"6045055735309820\n" +
		"5872120460121\n" +
		"99929316122852072\n" +
		"0003813474535310\n" +
		"0114762758182750\n" +
		"9875610591081018250321\n";
		*/
		
		StringWriter out = new StringWriter();
		CreditCardFilter.pump(new StringReader(test), new CreditCardFilterWriter2(out));
		out.close();
		Assert.assertEquals(test.replaceAll("\\d", "X"), out.toString());
	}

}

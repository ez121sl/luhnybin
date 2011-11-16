package ez;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class CreditCardFilter {

	public static void main(String[] args) throws IOException {
		Writer out = new CreditCardFilterWriter2(new OutputStreamWriter(System.out, "UTF-8")); 
		pump(new InputStreamReader(System.in, "UTF-8"), out);
		out.close();
	}
	
	static void pump(Reader in, Writer out) throws IOException {
		int c;
		while ((c = in.read()) >=0) {
			out.write(c);
			out.flush(); //System.out is buffering. Must flush every char or test hangs
			//since it is waiting for the complete output
		}
	}
	
	public static class CreditCardFilterWriter2 extends FilterWriter {
		
		private StringBuilder buf;
		private List<Integer> digits;
		private List<Boolean> mask;
		
		private static enum CharType {
			DIGIT, SEPARATOR, OTHER
		}
		
		public CreditCardFilterWriter2(Writer out) {
			super(out);
			reset();
		}
		
		@Override
		public void close() throws IOException {
			maskNumbers();
			flushBuffer();
			super.close();
		}

		@Override
		public void write(int c) throws IOException {
			CharType ct = charType(c);
			if (digits.isEmpty()) {
				if (ct == CharType.DIGIT) {
					handleDigit(c);
				} else {
					super.write(c);
				}
			} else {
				switch (ct) {
				case OTHER:
					handleOther(c);
					break;
				case SEPARATOR:
					handleSeparator(c);
					break;
				case DIGIT:
					handleDigit(c);
					break;
				}
			}
		}

		private void reset() {
			buf = new StringBuilder();
			digits = new ArrayList<Integer>(16);
			mask = new ArrayList<Boolean>(16);
		}
		
		private CharType charType(int c) {
			if (Character.isDigit(c)) {
				return CharType.DIGIT;
			}
			if (' ' == (char)c || '-' == (char)c) {
				return CharType.SEPARATOR;
			}
			return CharType.OTHER;
		}
		
		private void addDigit(int c) {
			buf.append((char)c);
			digits.add(c - (int)'0');
			mask.add(false);
		}
		
		private void handleSeparator(int c) throws IOException {
			buf.append((char)c);				
		}
		
		private void handleDigit(int c) throws IOException {
			addDigit(c);
			if (digits.size() >= 16) {
				maskNumbers();
				flushOneDigit();
			}
		}
		
		private void handleOther(int c) throws IOException {
			maskNumbers();
			flushBuffer();
			super.write(c);
		}

		private void flushOneDigit() throws IOException {
			super.write(mask.get(0) ? 'X' : buf.charAt(0));
			buf.delete(0, 1);
			digits.remove(0);
			mask.remove(0);
			//Flush any separators following it too
			while (buf.length() > 0 && charType(buf.charAt(0)) == CharType.SEPARATOR) {
				super.write(buf.charAt(0));
				buf.delete(0, 1);		
			}
		}
		
		private void flushBuffer() throws IOException {
//			while (!digits.isEmpty()) {
//				flushOneDigit();
//			}
			int d = 0;
			for (int i = 0; i < buf.length(); i++) {
				char c = buf.charAt(i);
				if (charType(c) == CharType.SEPARATOR) {
					super.write(c);
				} else {
					super.write(mask.get(d++) ? 'X' : c);
				}
			}
			reset();
		}
		
		private void maskNumbers() {
			int ds = digits.size();
			if (ds < 14) {
				return;
			}
			if (ds > 15) {
				if (Util.luhn(digits, 0, ds - 2)) {
					Util.fill(mask, true, 0, ds - 2);
				}
				if (Util.luhn(digits, 1, ds - 1)) {
					Util.fill(mask, true, 1, ds - 1);
				}
				if (Util.luhn(digits, 2, ds)) {
					Util.fill(mask, true, 2, ds);
				}
			}
			if (ds > 14) {
				if (Util.luhn(digits, 0, ds - 1)) {
					Util.fill(mask, true, 0, ds - 1);
				}
				if (Util.luhn(digits, 1, ds)) {
					Util.fill(mask, true, 1, ds);
				}
			}
			if (Util.luhn(digits, 0, ds)) {
				Util.fill(mask, true, 0, ds);
			}
		}		
	}
	
	static class Util {
		
		private static final int [] SUMS = new int [] { 0, 2, 4, 6, 8, 1, 3, 5, 7, 9 };  

		public static boolean luhn(List<Integer> dd, int from, int to) {
			int sum = 0;
			boolean mult = false;
			for (int i = to - 1; i >= from; i--) {
//				if (mult) {
//					int d = dd.get(i) * 2;
//					sum += d % 10;
//					sum += d / 10;
//				} else {
//					sum += dd.get(i);
//				}
//				mult = !mult;
				sum += mult ? SUMS[dd.get(i)] : dd.get(i); 
				mult = !mult;
			}
			return sum % 10 == 0;
		}

		public static <T> void fill(List<? super T> list, T val, int from, int to) {
			for (int i = from; i < to; i++) {
				list.set(i, val);
			}
		}
		
	}
	
}

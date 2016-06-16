
public class test {

	public static void main(String[] args) {
		double from = 7.5;
		double to = 8;
		int a = (int) (from * 2 - 2);
		int b = (int) (to * 2);
		String data = "00090002600233Na".toUpperCase();
		data = data.substring(a,b);
		System.out.println(data);
	}
}

package dcop.test;

public class Temp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for (int i=5;i<=20;i+=5) {
			for (int k=1;k<=10;k++) {
				double f = 1;
				if (k<i)
					f = ((double)k-1) / (k + 1);
				System.out.print(f + " ");
			}
			System.out.println();
		}
	}

}

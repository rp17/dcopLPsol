package dcop.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ExtractData {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int cycle = 999;
		String prefix = "normalized\\n10_full_";
		String[] algo = { "1_3true2_N\\", "2_3true2_N\\", "3_3true2_N\\", "4_3true2_N\\", "5_3true2_N\\" };
		File dir = new File(prefix + algo[0]);
		String[] children = dir.list();
		double[][] matrix = new double[algo.length][children.length];
		if (children != null) {
			for (int j = 0; j < children.length; j++) {
				for (int i = 0; i < algo.length; i++) {
					BufferedReader r = new BufferedReader(new FileReader(prefix
							+ algo[i] + children[j]));
					String line = "";
					while ((line = r.readLine()) != null) {
						String[] data = line.split(" ");
						int c = Integer.parseInt(data[0]);
						if (c == cycle) {
							matrix[i][j] = Double.parseDouble(data[1]);
							break;
						}
					}
				}
			}

			for (int j = 0; j < children.length; j++) {
				for (int i = 0; i < algo.length; i++) {
					System.out.print(matrix[i][j] + "\t");
				}
				System.out.println();
			}
		}

	}
}

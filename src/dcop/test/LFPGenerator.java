package dcop.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import dcop.data.Graph;
import dcop.data.GraphSettings;
import dcop.data.GraphStructure;
import dcop.data.Helper;

public class LFPGenerator {

	/**
	 * @param args
	 *            - <settings file name> <output file name>
	 * @throws IOException
	 * 
	 */
	public static void main(String[] args) throws IOException {
		BufferedWriter wr = new BufferedWriter(new FileWriter(args[0]));
		int[] agents = { 5 };
		//int[] agents = { 5, 10, 15, 20 };
		double[] density = { 2 };
		for (int l = 0; l < 1; l++) {
			for (int i = 0; i < agents.length; i++) {
				wr.write("AGENTS: " + agents[i] + Helper.newline);
				for (int j = 0; j < density.length; j++) {

					System.out.print(agents[i] + "N\t" + density[j] + "D\t");
					GraphSettings configuration = new GraphSettings(0,
							GraphStructure.RANDOMCONNECT, agents[i],
							density[j]);
					configuration.output("settings");
					int n = 30;
					for (int t = 2; t < 10; t++) {
						double data = 0;
						if (t < agents[i]) {
							for (int k = 0; k < n; k++) {
								double val = LFP(t, false, "settings");
								if (val == -1) {
									k--;
									continue;
								}
								data += val;
								// System.out.print(".");
							}
						} else
							data = n;
						wr.write(data / n + " ");

						System.out.print(data / n + " ");
						wr.flush();
					}
					wr.write(Helper.newline);
					System.out.println();
				}
			}
			wr.write(Helper.newline);
		}
		wr.close();
	}

	private static double LFP(int t, boolean isT, String settings) {
		Graph g = Graph.genGraph(settings);
		if (isT) {
			int status = g.generateGLPSolInputT(t, "lfp.mod");
			if (status == 1)
				return 1;
			if (status == -1)
				return -1;
		} else
			g.generateGLPSolInputK(t, "lfp.mod");

		File f = new File("lfp.mod");
		if (!f.exists() || f.length() > 50000000)
			return -1;

		String glpsol = "D:\\wspacesEcl42\\advSEworkspace\\dcopLPsol\\lib\\glpsol";
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(
					glpsol + " --model lfp.mod --output tmp");
			BufferedReader r;
			String line = "";
			r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = r.readLine()) != null) {

			}
			Thread.sleep(150);
			if (p.exitValue() == 0) {
				r = new BufferedReader(new FileReader("tmp"));
				while ((line = r.readLine()) != null) {
					if (line.startsWith("Objective")) {
						// System.out.println(line);
						String s = line.replaceAll("[^.^0-9]", "");
						// s = s.replaceAll("\\(MINimum\\)", "");
						double val = Double.parseDouble(s);
						// System.out.println(val);
						return val;
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

}

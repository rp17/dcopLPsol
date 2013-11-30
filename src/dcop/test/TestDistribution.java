package dcop.test;

import dcop.data.Graph;
import dcop.data.GraphSettings;
import dcop.data.GraphStructure;
import dcop.data.Variable;

public class TestDistribution {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int n = 100;
		int samples = 10000;
		double d = 2;
		GraphSettings configuration = new GraphSettings(0,
				GraphStructure.SCALEFREE, n, d);
		double[] freq = new double[n];
		for (int i = 0; i < freq.length; i++)
			freq[i] = 0;
		int k = samples;
		while (k > 0) {
			configuration.output("settings");
			Graph g = Graph.genGraph("settings");
			for (Variable v : g.varList) {
				freq[v.getDegree()]++;
			}
			k--;
		}
		for (int i = 0; i < freq.length; i++) {
			freq[i] /= samples;
			freq[i] /= n;
			System.out.println(freq[i]);
		}
		
	}

}

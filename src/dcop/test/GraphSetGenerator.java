package dcop.test;

import java.util.Random;

import dcop.data.Graph;
import dcop.data.GraphSettings;

public class GraphSetGenerator {

	/**
	 * @param args
	 *            <settings file name> <#samples>
	 */
	public static void main(String[] args) {
		GraphSettings s = new GraphSettings(args[0]);
		Random r = new Random(s.seed);
		int samples = Integer.parseInt(args[1]);
		while (samples > 0) {
			samples--;
			s.seed = r.nextLong();
			s.updateRandom();
			new Graph(s).uniformOutput();
		}

	}

}

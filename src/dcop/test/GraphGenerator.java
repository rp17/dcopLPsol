package dcop.test;

import dcop.data.Graph;

public class GraphGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Graph.genGraph(args[0]).output(args[1]);
	}

}

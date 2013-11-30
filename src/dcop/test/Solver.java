package dcop.test;

import dcop.data.Graph;

public class Solver {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Graph g = Graph.genGraph(args[0]);
		g._branchAndBound();
	}

}

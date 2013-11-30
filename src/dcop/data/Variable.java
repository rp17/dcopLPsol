package dcop.data;

import java.util.Vector;

public class Variable {

	Graph graph;
	public int id;
	public int domain;
	public int value;
	private int _value;
	public Vector<Constraint> neighbors;

	public Variable(int i, int d, Graph g) {
		id = i;
		graph = g;
		domain = d;
		neighbors = new Vector<Constraint>();
	}

	public Variable(String s, Graph g) {
		String[] ss = s.split(" ");
		assert ss.length >= 5;
		id = Integer.parseInt(ss[1]);
		domain = Integer.parseInt(ss[3]);
		neighbors = new Vector<Constraint>();
		value = -1;
		graph = g;
	}

	
	public void backupValue() {
		_value = value;
	}

	public void recoverValue() {
		value = _value;
	}

	public boolean addConstraint(Constraint c) {
		return neighbors.add(c);
	}

	public String toString() {
		return "VARIABLE " + id + " 1 " + domain + Helper.newline;
	}

	public int getDegree() {
		return neighbors.size();
	}
	
	public boolean hasNeighbor(int id) {
		for (Constraint c : neighbors) {
			if (c.getNeighbor(this).id == id)
				return true;
		}
		return false;
	}

	public int calculateFReward() {
		if (value == -1) {
			return -1;
		}
		int reward = 0;
		for (Constraint c : neighbors) {
			int v = c.getFReward();
			if (v == -1)
				return -1;
			reward += v;
		}
		return reward;
	}

}

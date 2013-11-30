package dcop.data;

public class Constraint {

	Graph graph;

	Variable first;
	Variable second;

	int d1;
	int d2;

	int[][] f;

	public Constraint(Variable a, Variable b) {
		first = a;
		second = b;
		graph = a.graph;
		d1 = a.domain;
		d2 = b.domain;
		f = new int[d1][d2];
		first.addConstraint(this);
		second.addConstraint(this);
	}

	public Variable getNeighbor(Variable v) {
		if (v == first)
			return second;
		if (v == second)
			return first;
		return null;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("CONSTRAINT ");
		buffer.append(first.id);
		buffer.append(" ");
		buffer.append(second.id);
		buffer.append(Helper.newline);
		for (int i = 0; i < d1; i++) {
			for (int j = 0; j < d2; j++) {
				buffer.append("F ");
				buffer.append(i);
				buffer.append(" ");
				buffer.append(j);
				buffer.append(" ");
				buffer.append(f[i][j]);
				buffer.append(Helper.newline);
			}
		}
		return buffer.toString();
	}

	public int getFReward() {
		if (first.value == -1 || second.value == -1)
			return -1;
		return f[first.value][second.value];
	}
}

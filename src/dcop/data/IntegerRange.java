package dcop.data;

public class IntegerRange {
	public int min;
	public int max;
	public int range;
	public double[] prob;

	public IntegerRange(int a, int b) {
		if (a > b) {
			min = b;
			max = a;
		} else {
			min = a;
			max = b;
		}
		range = max - min;
		prob = new double[range];
		double sum = 0;
		for (int i = min; i < max; i++) {
			double p = Math.pow(i + 2, -3);
			sum += p;
			prob[i - min] = sum;
		}
		for (int i = min; i < max; i++) {
			prob[i - min] /= sum;
		}
	}

	public int powerDistribution(double rand) {
		if (rand < prob[0])
			return min;
		if (rand >= 1)
			return max;
		for (int i = 1; i < range; i++) {
			if (rand < prob[i])
				return min + i;
		}
		return max;
	}

	public String toString() {
		return "" + min + " " + max;
	}

}

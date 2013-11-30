package dcop.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class Graph {
	private GraphSettings settings;

	public Variable[] varList;
	public Vector<Constraint> conList;

	private int _counter;
	private HashSet<Long> _set;
	private static final int constraintLimit = 45000;

	public static Graph genGraph(String settingfile) {
		GraphSettings settings = new GraphSettings(settingfile);
		return new Graph(settings);
	}

	public Graph(GraphSettings s) {
		settings = s;
		varList = new Variable[settings.agents];
		conList = new Vector<Constraint>();
		for (int i = 0; i < settings.agents; i++)
			varList[i] = new Variable(i, settings.domain, this);
		if (settings.type == GraphStructure.RANDOMCONNECT)
			randomConnect();
		else if (settings.type == GraphStructure.RANDOMEDGE)
			randomConnect();
		else if (settings.type == GraphStructure.SMALLWORLD)
			wattsStrogatz();
		else if (settings.type == GraphStructure.SCALEFREE)
			scalefree(1.7);
		else if (settings.type == GraphStructure.STAR)
			createStar();
		else if (settings.type == GraphStructure.FULLYCONNECTED)
			fullyconnected();
		else if (settings.type == GraphStructure.SCALEFREEMS)
			scalefree_MichaelSmall(3, settings.density);
		else if (settings.type == GraphStructure.SIMONMODEL)
			createSimonModel(3, settings.density);
		generateRandomF();
	}

	private void fullyconnected() {
		// TODO Auto-generated method stub
		for (int i = 0; i < varList.length; i++)
			for (int j = i + 1; j < varList.length; j++) {
				conList.add(new Constraint(varList[i], varList[j]));
			}
	}

	public Graph(String inFilename) {
		// We assume in the input file, there is at most one link between two
		// variables
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					inFilename));
			ArrayList<Variable> var = new ArrayList<Variable>();
			conList = new Vector<Constraint>();
			String line;
			Constraint c = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("VARIABLE")) {
					var.add(new Variable(line, this));
				} else if (line.startsWith("CONSTRAINT")) {
					String[] ss = line.split(" ");
					assert ss.length >= 3;
					int first = Integer.parseInt(ss[1]);
					int second = Integer.parseInt(ss[2]);
					c = new Constraint(var.get(first), var.get(second));
					conList.add(c);
				} else if (line.startsWith("F")) {
					assert c != null;
					String[] ss = line.split(" ");
					assert ss.length >= 4;
					int x = Integer.parseInt(ss[1]);
					int y = Integer.parseInt(ss[2]);
					int v = Integer.parseInt(ss[3]);
					c.f[x][y] = v;
				}
			}
			varList = new Variable[var.size()];
			var.toArray(varList);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createStar() {
		assert settings != null;
		int center = settings.random.nextInt(settings.agents);
		Variable c = varList[center];
		for (int i = 0; i < center; i++)
			conList.add(new Constraint(varList[i], c));
		for (int i = center + 1; i < varList.length; i++)
			conList.add(new Constraint(c, varList[i]));
	}

	private void scalefree(double power) {
		assert settings != null;
		int m = (int) settings.density;
		int m0 = m + 1;
		Vector<Integer> agentOrder = new Vector<Integer>();
		for (int i = 0; i < settings.agents; i++)
			agentOrder.add(i);
		Collections.shuffle(agentOrder, settings.random);
		for (int i = 0; i < m0; i++)
			for (int j = i + 1; j < m0; j++)
				conList.add(new Constraint(varList[agentOrder.get(i)],
						varList[agentOrder.get(j)]));
		for (int i = m0; i < settings.agents; i++) {
			HashSet<Integer> set = new HashSet<Integer>();
			for (int j = 0; j < m; j++) {
				double p = settings.random.nextDouble();
				double sum = 0;
				for (int k = 0; k < i; k++) {
					if (!set.contains(k))
						sum += Math.pow(varList[agentOrder.get(k)].getDegree(),
								power);
				}
				double c = 0;
				for (int k = 0; k < i; k++) {
					if (!set.contains(k)) {
						c += Math
								.pow(varList[agentOrder.get(k)].getDegree(), power)
								/ sum;
						if (c > p) {
							set.add(k);
							int first = agentOrder.get(i);
							int second = agentOrder.get(k);
							if (first < second)
								conList.add(new Constraint(varList[first],
										varList[second]));
							else
								conList.add(new Constraint(varList[second],
										varList[first]));
							break;
						}
					}
				}
			}
		}
	}

	private void createSimonModel(int m0, double alpha) {
		assert settings != null;
		Vector<Integer> agentOrder = new Vector<Integer>();
		for (int i = 0; i < settings.agents; i++)
			agentOrder.add(i);
		Collections.shuffle(agentOrder, settings.random);
		for (int i = 0; i < m0; i++)
			for (int j = i + 1; j < m0; j++) {
				int first = agentOrder.get(i);
				int second = agentOrder.get(j);
				if (first < second)
					conList
							.add(new Constraint(varList[first], varList[second]));
				else
					conList
							.add(new Constraint(varList[second], varList[first]));
			}
		int k = m0;
		while (k < settings.agents) {
			double p = settings.random.nextDouble();
			if (p < alpha) {
				int first = agentOrder.get(settings.random.nextInt(k));
				int second = agentOrder.get(k);
				if (first < second)
					conList
							.add(new Constraint(varList[first], varList[second]));
				else
					conList
							.add(new Constraint(varList[second], varList[first]));
				k++;
			} else {
				int first = agentOrder.get(settings.random.nextInt(k));
				double pp = settings.random.nextDouble();
				double sum = 0;
				for (int i = 0; i < k; i++)
					sum += varList[agentOrder.get(i)].getDegree();
				double c = 0;
				for (int i = 0; i < k; i++) {
					c += (double) varList[agentOrder.get(i)].getDegree() / sum;
					if (c > pp) {
						int second = agentOrder.get(i);
						if (first != second
								&& !varList[first].hasNeighbor(second)) {
							if (first < second)
								conList.add(new Constraint(varList[first],
										varList[second]));
							else
								conList.add(new Constraint(varList[second],
										varList[first]));
							break;
						}
					}
				}
			}
		}
	}

	private void scalefree_MichaelSmall(int m0, double gamma) {
		assert settings != null;
		int N = settings.agents;
		Vector<Integer> agentOrder = new Vector<Integer>();
		for (int i = 0; i < N; i++)
			agentOrder.add(i);
		Collections.shuffle(agentOrder, settings.random);
		for (int i = 0; i < m0; i++)
			for (int j = i + 1; j < m0; j++)
				conList.add(new Constraint(varList[agentOrder.get(i)],
						varList[agentOrder.get(j)]));
		double[] p = new double[N - 1];
		double sum = 0;
		for (int i = 0; i < p.length; i++) {
			p[i] = Math.pow((i + 1), -gamma);
			sum += p[i];
		}
		int kmax = -1;
		for (int i = 0; i < p.length; i++) {
			p[i] /= sum;
			if (p[i] * N >= 1 && kmax == -1)
				kmax = i + 1;
		}
		double[] degD = new double[N - 1];
		degD[m0 - 1] = m0;

		for (int t = 1; t <= N - m0; t++) {
			int dmax = m0 + t - 1;
			if (dmax > kmax)
				dmax = kmax;
			while (true) {
				int pick = settings.random.nextInt(dmax);
				if (degD[pick] < p[pick] * N) {
					HashSet<Integer> set = new HashSet<Integer>();
					degD[pick]++;
					while (pick >= 0) {
						pick--;
						double prob = settings.random.nextDouble();
						sum = 0;
						for (int k = 0; k < m0 + t; k++) {
							if (!set.contains(k))
								sum += Math.pow(varList[agentOrder.get(k)]
										.getDegree(), 1);
						}
						double c = 0;
						for (int k = 0; k < m0 + t; k++) {
							if (!set.contains(k)) {
								c += Math.pow(varList[agentOrder.get(k)]
										.getDegree(), 1)
										/ sum;
								if (c > prob) {
									set.add(k);
									int first = agentOrder.get(m0 + t);
									int second = agentOrder.get(k);
									int d = varList[second].getDegree();
									degD[d - 1]--;
									degD[d]++;
									if (first < second)
										conList
												.add(new Constraint(
														varList[first],
														varList[second]));
									else
										conList
												.add(new Constraint(
														varList[second],
														varList[first]));
									break;
								}
							}
						}
					}
				}
			}

		}

	}

	private void wattsStrogatz() {
		assert settings != null;
		int N = settings.agents;
		int K = (int) settings.density;

		boolean[][] connected = new boolean[N][N];
		for (int i = 0; i < settings.agents; i++) {
			for (int j = 0; j < settings.agents; j++) {
				connected[i][j] = false;
				if (i == j)
					connected[i][j] = true;
			}
		}
		int left = K / 2;
		int right = K - left;

		for (int i = 0; i < N; ++i) {
			for (int j = 1; j <= left; ++j) {
				int next = (i - j + N) % N;
				connected[i][next] = true;
				connected[next][i] = true;
			}
			for (int j = 1; j <= right; ++j) {
				int next = (i + j) % N;
				connected[i][next] = true;
				connected[next][i] = true;
			}
		}
		double prob = 0.3;
		for (int i = 0; i < N; ++i) {
			for (int j = i + 1; j < N; ++j) {
				if (!connected[i][j]) {
					continue;
				} else {
					if (settings.random.nextFloat() < prob) {
						int next = -1;
						boolean done = false;
						while (!done) {
							next = settings.random.nextInt(N);
							if (next != i && next != j && !connected[i][next]) {
								done = true;
							}
						}
						connected[i][j] = false;
						connected[j][i] = false;

						connected[i][next] = true;
						connected[next][i] = true;
					}
				}
			}
		}
		for (int i = 0; i < N; ++i) {
			for (int j = i + 1; j < N; ++j) {
				if (connected[i][j]) {
					int first = i;
					int second = j;

					if (first < second)
						conList.add(new Constraint(varList[first],
								varList[second]));
					else
						conList.add(new Constraint(varList[second],
								varList[first]));
				}
			}
		}
	}

	/**
	 * This is one approach to generate a connected random graph -- starting
	 * with an empty set, adding one node at a time, and randomly connecting it
	 * to a node in the set with certain probability. In this way, the first n-1
	 * edges are generated to give out a tree. To meet the average density
	 * requirement, simply add more edges randomly.
	 */
	private void randomConnect() {
		assert settings != null;
		boolean[][] connected = new boolean[settings.agents][settings.agents];
		for (int i = 0; i < settings.agents; i++)
			for (int j = 0; j < settings.agents; j++) {
				connected[i][j] = false;
				if (i == j)
					connected[i][j] = true;
			}
		int edges = (int) Math.ceil((settings.density * settings.agents / 2));
		ArrayList<Integer> agentOrder = new ArrayList<Integer>();
		for (int i = 0; i < settings.agents; i++)
			agentOrder.add(i);
		Collections.shuffle(agentOrder, settings.random);
		for (int i = 1; i < settings.agents; i++) {
			int first = agentOrder.get(i);
			int pick = -1;
			int k = 0;
			double prob = 0.5;
			while (k < i - 1) {
				if (settings.random.nextFloat() < prob)
					pick = k;
				k++;
			}
			if (pick == -1)
				pick = i - 1;
			int second = agentOrder.get(pick);
			connected[first][second] = true;
			connected[second][first] = true;
			if (first < second)
				conList.add(new Constraint(varList[first], varList[second]));
			else
				conList.add(new Constraint(varList[second], varList[first]));
			edges--;
		}

		for (int i = 0; i < edges; i++) {
			int first = 0;
			int second = 0;
			do {
				first = settings.random.nextInt(settings.agents);
				second = settings.random.nextInt(settings.agents);
			} while (connected[first][second]);
			if (first < second)
				conList.add(new Constraint(varList[first], varList[second]));
			else
				conList.add(new Constraint(varList[second], varList[first]));
			connected[first][second] = true;
			connected[second][first] = true;
		}
	}

	private void generateRandomF() {
		for (Constraint c : conList) {
			for (int i = 0; i < c.d1; i++)
				for (int j = 0; j < c.d2; j++)
					c.f[i][j] = settings.randomF();
		}
	}

	private void generateHardF() {
		for (Constraint c : conList) {
			ArrayList<Integer> l1 = new ArrayList<Integer>();
			ArrayList<Integer> l2 = new ArrayList<Integer>();
			for (int i = 0; i < c.d1; i++)
				l1.add(i);
			for (int i = 0; i < c.d2; i++)
				l2.add(i);
			Collections.shuffle(l1, settings.random);
			Collections.shuffle(l2, settings.random);
			for (int i = 0; i < c.d1; i++)
				for (int j = 0; j < c.d2; j++)
					c.f[i][j] = 0;
			int v = settings.fRange.max;
			for (int i = 0; i < l1.size(); i++) {
				if (l2.size() > i) {
					c.f[l1.get(i)][l2.get(i)] = v;
					v /= 10;
				}
			}
		}
	}

	public void output(String name) {
		try {
			BufferedWriter wr = new BufferedWriter(new FileWriter(name));
			wr.write("AGENT 1" + Helper.newline);
			for (int i = 0; i < varList.length; i++)
				wr.write(varList[i].toString());
			for (Constraint c : conList) {
				wr.write(c.toString());
			}
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void uniformOutput() {
		assert settings != null;
		File f = new File(settings.folder);
		if (f.exists() && !f.isDirectory()) {
			System.out.println("Can't output graph, folder already exists!");
			return;
		}
		if (!f.exists())
			f.mkdirs();
		String name = settings.folder + File.separatorChar + settings.agents
				+ "V" + settings.density + "D" + settings.seed;
		name = name.replaceAll("\\.", "_");
		output(name + ".dcop");
		settings.output(name + ".settings");
	}

	public void _branchAndBound() {
		Vector<Variable> order = new Vector<Variable>();
		for (Variable v : varList)
			order.add(v);
		Collections.sort(order, new Comparator<Variable>() {
			public int compare(Variable v1, Variable v2) {
				if (v1.neighbors.size() > v2.neighbors.size()
						|| (v1.neighbors.size() == v2.neighbors.size() && v1.id <= v2.id))
					return -1;
				return 1;
			}
		});
		for (Variable v : varList)
			v.value = -1;
		for (Variable v : order)
			System.out.println(v.id + "\t" + v.neighbors.size());
	}

	public int generateGLPSolInputT(int t, String name) {
		try {
			BufferedWriter wr = new BufferedWriter(new FileWriter(name));
			wr.write("set I;" + Helper.newline);
			wr.write("var x00{i in I}, >= 0, <=1;" + Helper.newline);
			wr.write("var x11{i in I}, >= 0, <=1;" + Helper.newline);
			wr.write("minimize d: sum{i in I}x00[i];" + Helper.newline);
			wr.write("s.t. total: sum{i in I}x11[i] = 1;" + Helper.newline);
			_counter = 0;
			ArrayList<HashSet<Integer>> clusterList = new ArrayList<HashSet<Integer>>();
			for (Variable v : varList)
				if (generateSTforTCluser(t, v, clusterList, wr)) {
					return 1;
				}
			_set = new HashSet<Long>();
			for (HashSet<Integer> cluster : clusterList) {
				if (cluster == null)
					continue;
				for (Variable v : varList)
					v.value = 0;
				Vector<Integer> list = new Vector<Integer>();
				for (Integer i : cluster)
					list.add(i);
//				System.err.println(cluster.size());

				enumerate(list, cluster, wr, 0);
				if (_set.size() > Graph.constraintLimit)
					return -1;
			}

			wr.write("data;" + Helper.newline);
			wr.write("set I := ");
			for (int i = 0; i < conList.size(); i++)
				wr.write(i + " ");
			wr.write(";" + Helper.newline);
			wr.write("end;" + Helper.newline);
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void generateGLPSolInputK(int k, String name) {
		try {
			BufferedWriter wr = new BufferedWriter(new FileWriter(name));
			wr.write("set I;" + Helper.newline);
			wr.write("var x00{i in I}, >= 0, <=1;" + Helper.newline);
			wr.write("var x11{i in I}, >= 0, <=1;" + Helper.newline);
			wr.write("minimize d: sum{i in I}x00[i];" + Helper.newline);
			wr.write("s.t. total: sum{i in I}x11[i] = 1;" + Helper.newline);
			_counter = 0;
			generateSTforKgroups(k, wr);
			wr.write("data;" + Helper.newline);
			wr.write("set I := ");
			for (int i = 0; i < conList.size(); i++)
				wr.write(i + " ");
			wr.write(";" + Helper.newline);
			wr.write("end;" + Helper.newline);
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void generateSTforKgroups(int k, BufferedWriter wr)
			throws IOException {

		HashSet<Integer> pSet = new HashSet<Integer>();
		this.enumerateKGroups(pSet, 0, wr, k);

	}

	private boolean _increment(Integer[] kgroup) {
		int bit = 0;
		while (bit < kgroup.length) {
			if (varList[kgroup[bit]].value == 0) {
				varList[kgroup[bit]].value = 1;
				for (int j = 0; j < bit; j++)
					varList[kgroup[j]].value = 0;
				return true;
			} else {
				bit++;
			}
		}
		for (int j = 0; j < kgroup.length; j++)
			varList[kgroup[j]].value = 0;
		return false;
	}

	private boolean testConnect(HashSet<Integer> pSet) {
		for (Integer i : pSet) {
			Variable v = varList[i];
			boolean f = false;
			for (Constraint c : v.neighbors) {
				if (pSet.contains(c.getNeighbor(v).id)) {
					f = true;
					break;
				}
			}
			if (!f)
				return false;
		}
		return true;
	}

	private void enumerateKGroups(HashSet<Integer> pSet, int max,
			BufferedWriter wr, int k) throws IOException {
		if (pSet.size() == k) {
			if (!testConnect(pSet))
				return;
			Integer[] kgroup = new Integer[pSet.size()];
			pSet.toArray(kgroup);
			for (int j = 0; j < kgroup.length; j++)
				varList[kgroup[j]].value = 0;
			while (_increment(kgroup)) {
				StringBuffer s = new StringBuffer("s.t. l" + _counter + ": ");
				boolean first = true;
				boolean something = false;
				for (int j = 0; j < conList.size(); j++) {
					Constraint c = conList.get(j);
					if (!pSet.contains(c.first.id)
							&& !pSet.contains(c.second.id))
						continue;
					if (c.first.value == 0 && c.second.value == 0)
						continue;
					if (!first) {
						s.append(" + ");
					} else
						first = false;
					if (c.first.value == 1 && c.second.value == 1)
						something = true;
					s.append("x00" + "[" + j + "]");
				}
				if (something)
					wr.write(s.toString());
				else
					continue;
				wr.write(" >= ");
				first = true;
				for (int j = 0; j < conList.size(); j++) {
					Constraint c = conList.get(j);
					if (!pSet.contains(c.first.id)
							&& !pSet.contains(c.second.id))
						continue;
					if (c.first.value == 1 && c.second.value == 1) {
						if (!first) {
							wr.write(" + ");
						} else
							first = false;
						wr.write("x" + c.first.value + c.second.value + "[" + j
								+ "]");
					}
				}
				_counter++;
				wr.write(";" + Helper.newline);
			}
		} else {
			for (int i = max; i < varList.length; i++) {
				pSet.add(i);
				this.enumerateKGroups(pSet, i + 1, wr, k);
				pSet.remove(i);
			}
		}
	}

	private boolean generateSTforTCluser(int t, Variable top,
			ArrayList<HashSet<Integer>> clusterList, BufferedWriter wr)
			throws IOException {

		HashMap<Integer, Integer> minDis = new HashMap<Integer, Integer>();
		ArrayList<Variable> queue = new ArrayList<Variable>();
		queue.add(top);
		minDis.put(top.id, 0);
		int size = 1;
		while (!queue.isEmpty()) {
			Variable v = queue.remove(0);
			int depth = minDis.get(v.id);
			for (Constraint c : v.neighbors) {
				Variable n = c.getNeighbor(v);
				if (!minDis.containsKey(n.id)) {
					minDis.put(n.id, depth + 1);
					if (depth <= t)
						size++;
					queue.add(n);
				}
			}
		}

		HashSet<Integer> cluster = new HashSet<Integer>();
		for (Integer i : minDis.keySet()) {
			if (minDis.get(i) <= t)
				cluster.add(i);
		}

		if (cluster.size() == varList.length)
			return true;

		boolean add = true;
		for (int i = 0; i < clusterList.size(); i++) {
			HashSet<Integer> set = clusterList.get(i);
			if (set == null)
				continue;
			if (set.containsAll(cluster))
				return false;
			if (cluster.containsAll(set)) {
				if (add) {
					clusterList.set(i, cluster);
					add = false;
				} else
					clusterList.set(i, null);
			}
		}
		if (add) {
			for (int i = 0; i < clusterList.size(); i++) {
				HashSet<Integer> set = clusterList.get(i);
				if (set == null) {
					clusterList.set(i, cluster);
					add = false;
					break;
				}
			}
			if (add)
				clusterList.add(cluster);
		}

		return false;
	}

	private boolean _testT() {
		if (_set.size() > Graph.constraintLimit)
			return false;
		
		HashSet<Integer> set = new HashSet<Integer>();
		ArrayList<Variable> queue = new ArrayList<Variable>();
		int count = 0;
		long gId = 0;
		for (Variable v : varList) {
			if (v.value == 1) {
				count++;
				if (queue.isEmpty())
					queue.add(v);
				gId += 1 << v.id;
			}
		}
		
		while (!queue.isEmpty()) {
			Variable v = queue.remove(0);
			set.add(v.id);
			for (Constraint c : v.neighbors) {
				Variable n = c.getNeighbor(v);
				if (n.value == 1 && !set.contains(n.id)) {
					queue.add(n);
				}
			}
		}
		
		if (count == set.size()) {
			if (!_set.contains(gId)) {
				_set.add(gId);
				return true;
			} else
				return false;
		} else 
			return false;
	}

	private void enumerate(Vector<Integer> list, HashSet<Integer> set,
			BufferedWriter wr, int i) throws IOException {
		if (i >= list.size()) {
//			if (!_testT())
//				return;
			StringBuffer s = new StringBuffer("s.t. l" + _counter + ": ");
			boolean first = true;
			boolean something = false;
			for (int j = 0; j < conList.size(); j++) {
				Constraint c = conList.get(j);
				if (!set.contains(c.first.id) && !set.contains(c.second.id))
					continue;
				if (c.first.value == 0 && c.second.value == 0)
					continue;
				if (!first) {
					s.append(" + ");
				} else
					first = false;
				if (c.first.value == 1 && c.second.value == 1)
					something = true;
				s.append("x00" + "[" + j + "]");
			}
			if (something)
				wr.write(s.toString());
			else
				return;
			wr.write(" >= ");
			first = true;
			for (int j = 0; j < conList.size(); j++) {
				Constraint c = conList.get(j);
				if (!set.contains(c.first.id) && !set.contains(c.second.id))
					continue;
				if (c.first.value == 1 && c.second.value == 1) {
					if (!first) {
						wr.write(" + ");
					} else
						first = false;
					wr.write("x" + c.first.value + c.second.value + "[" + j
							+ "]");
				}
			}
			_counter++;
			wr.write(";" + Helper.newline);
			return;
		}
		int x = list.get(i);
		varList[x].value = 0;
		enumerate(list, set, wr, i + 1);
		varList[x].value = 1;
		enumerate(list, set, wr, i + 1);
	}
}

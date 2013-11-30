package dcop.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

public class GraphSettings {
	Random random;
	public long seed;

	public GraphStructure type;
	public int agents;
	public double density;
	public int domain;
	public IntegerRange fRange;
	public String folder;

	public void output(String filename) {
		try {
			BufferedWriter wr = new BufferedWriter(new FileWriter(filename));
			wr.write("" + seed);
			wr.newLine();
			switch (type) {
			case RANDOMEDGE:
				wr.write("randomedge");
				break;
			case RANDOMCONNECT:
				wr.write("randomconnect");
				break;
			case SCALEFREE:
				wr.write("scalefree");
				break;
			case STAR:
				wr.write("star");
				break;
			case FULLYCONNECTED:
				wr.write("fullyconnected");
				break;
			case SMALLWORLD:
				wr.write("smallworld");
				break;
			case SCALEFREEMS:
				wr.write("scalefreems");
				break;
			case SIMONMODEL:
				wr.write("simonmodel");
				break;
			}
			wr.newLine();
			wr.write("" + agents);
			wr.newLine();
			wr.write("" + density);
			wr.newLine();
			wr.write("" + domain);
			wr.newLine();
			wr.write(fRange.toString());
			wr.newLine();
			wr.write(folder);
			wr.newLine();
			wr.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public GraphSettings(long s, GraphStructure gs, int a, double d) {
		seed = s;
		random = new Random(seed);
		type = gs;
		agents = a;
		density = d;
		domain = 3;
		fRange = new IntegerRange(1, 10);
		folder = ".";
	}

	public GraphSettings(String filename) {
		try {
			// Random seed
			// Graph Structure
			// #Agents
			// Density
			// Domain Size
			// f-Range
			// folder
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line = reader.readLine();
			seed = Long.parseLong(line);
			if (seed == 0) {
				seed = new Random(Calendar.getInstance().getTimeInMillis())
						.nextLong();
			}
			random = new Random(seed);
			line = reader.readLine();
			type = GraphStructure.RANDOMEDGE;
			if (line.equalsIgnoreCase("randomedge"))
				type = GraphStructure.RANDOMEDGE;
			else if (line.equalsIgnoreCase("randomconnect"))
				type = GraphStructure.RANDOMCONNECT;
			else if (line.equalsIgnoreCase("scalefree"))
				type = GraphStructure.SCALEFREE;
			else if (line.equalsIgnoreCase("star"))
				type = GraphStructure.STAR;
			else if (line.equalsIgnoreCase("fullyconnected"))
				type = GraphStructure.FULLYCONNECTED;
			else if (line.equalsIgnoreCase("smallworld"))
				type = GraphStructure.SMALLWORLD;
			else if (line.equalsIgnoreCase("scalefreems"))
				type = GraphStructure.SCALEFREEMS;
			else if (line.equalsIgnoreCase("simonmodel"))
				type = GraphStructure.SIMONMODEL;
			line = reader.readLine();
			agents = Integer.parseInt(line);
			line = reader.readLine();
			density = Double.parseDouble(line);
			line = reader.readLine();
			domain = Integer.parseInt(line);
			line = reader.readLine();
			String[] sarr = line.split(" ");
			assert sarr.length > 1;
			fRange = new IntegerRange(Integer.parseInt(sarr[0]), Integer
					.parseInt(sarr[1]));
			folder = reader.readLine();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateRandom() {
		random = new Random(seed);
	}

	public int randomF() {
		return fRange.min + random.nextInt(fRange.range);
	}
	
	// zeta distribution
	public int powerF() {
		return fRange.powerDistribution(random.nextDouble());
	}
}

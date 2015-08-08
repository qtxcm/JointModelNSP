package microblog;

import java.util.HashMap;

public class Feature {
	public String name = ""; // feature name = template name + value.
	public double weight = 0; // current weight
	public double sum = 0; // The total of weight
	public int lastUpdateIndex = 0; // lasting index of update
	public double aveWeight = 0; // average weight

	public enum featureName {
		CharUnigram, CharBigram;
	}

	public Feature() {
	}

	public Feature(String name, double weight, double sum, int index,
			double aveWeight) {
		this.name = name;
		this.weight = weight;
		this.sum = sum;
		this.lastUpdateIndex = index;
		this.aveWeight = aveWeight;
	}

	public void AveWeight(int curUpdateIndex) {
		this.sum += (curUpdateIndex - this.lastUpdateIndex) * this.weight;
		this.aveWeight = this.sum / curUpdateIndex;
		this.lastUpdateIndex = curUpdateIndex;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap<String, Feature> hm = new HashMap<String, Feature>();
		hm.put("aa", new Feature());
		Feature f = hm.get("aa");
		f.sum = 100;
		System.out.println(hm.get("aa").sum);
		featureName a = featureName.CharUnigram;
		System.out.println(a.toString());
	}

}

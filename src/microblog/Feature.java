package microblog;

import java.util.HashMap;

public class Feature {

	public String name="";   //特征名  特征模型+特征值  例： "CharUnigram=中"
	public double weight=0;  //当前权重
	public double sum=0;     //历史总权重
	public int lastUpdateIndex=0; //最后一次更新序号
	public double aveWeight=0; //标准化权重

	 public enum featureName{
	    	CharUnigram,CharBigram;}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap<String, Feature>  hm = new HashMap<String, Feature>();
		hm.put("aa", new Feature());
		Feature f = hm.get("aa");
		f.sum=100;
		System.out.println(hm.get("aa").sum);
		featureName a=featureName.CharUnigram;
		System.out.println(a.toString());
		
	}
	public  Feature(){	
	}
	public  Feature(String name, double weight, double sum, int index, double aveWeight){	
		this.name=name;
		this.weight=weight;
		this.sum=sum;
		this.lastUpdateIndex=index;
		this.aveWeight = aveWeight;
	}
	
	public void AveWeight(int curUpdateIndex){
		this.sum += (curUpdateIndex-this.lastUpdateIndex)*this.weight;
		this.aveWeight=this.sum/curUpdateIndex;
		this.lastUpdateIndex = curUpdateIndex;
	}

}

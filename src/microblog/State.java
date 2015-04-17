package microblog;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class State {
	public int lastAction=0;  // 0-999:"S"  or 1000:"A" or -1:"invalid" or 2000:"finish"
	public List<Integer> hisActions = new ArrayList<Integer>();
	public String[] arrWord; //单词序列
	public String[] arrTag; //标注序列
	public String[] arrNormal; //对应单词的词义或标准化单词
	
	public static  int MAXNUM=1000;//最大长度
	public int size=0;
    public double score=0;    //分数
    
    public boolean  bIsGold = true;
    public boolean bStart = true;
    //public ArrayList<String> arrFeature=new ArrayList<String>();
    
	public static String[] arrPOS = { "VA", "VC", "VE", "VV", "NR", "NT", "NN",
		"LC", "PN", "DT", "CD", "OD", "M", "AD", "P", "CC", "CS", "DEC",
		"DEG", "DER", "DEV", "AS", "SP", "ETC", "MSP", "IJ", "ON", "LB",
		"SB", "BA", "JJ", "FW", "PU" };
	//public static String[] arrPOS = {"Ag","a","ad","an","b","c","Dg","d","e","f",
	//	"g","h","i","j","k","l","m","Ng","n","nr","ns","nt","nz","o","p","q","r",
	//	"s","Tg","t","u","Vg","v","vd","vn","w","x","y","z"};
	
   
    public State(){
    	this.lastAction=-1;
    	this.arrWord = new String[MAXNUM]; 
    	this.arrTag = new String[MAXNUM];
    	this.arrNormal = new String[MAXNUM];
    	this.score=0;	
    	this.size=0;
    	this.bIsGold=true;
    	hisActions = new ArrayList<Integer>();
    	bStart = true;
    	//arrFeature=new ArrayList<String>();
    	//arrFeatureTemplate=new ArrayList<String>();
    }
    public State(State newState){
    	this.lastAction=newState.lastAction;
    	this.arrWord = new String[MAXNUM]; 
    	this.arrTag = new String[MAXNUM];
    	this.arrNormal = new String[MAXNUM];
    	this.size=newState.size; 
    	this.score=newState.score; 
    	this.bIsGold = newState.bIsGold;
    	for(int i=0;i<size;i++){
    		this.arrWord[i]=newState.arrWord[i];
    		this.arrTag[i]=newState.arrTag[i];
    		this.arrNormal[i]=newState.arrNormal[i];
    	}
    	hisActions = new ArrayList<Integer>();
    	for(int act : newState.hisActions)
    	{
    		hisActions.add(act);
    	}
    	bStart = newState.bStart;
    	//for(String f:newState.arrFeature){
    		//arrFeature.add(f); 
    	//}
    }
    
 /*   
    public State(String[] content, int action){
    	this.lastAction=action;
    	this.arrWord = new String[MAXNUM]; 
    	this.arrTag = new String[MAXNUM];
    	this.size=content.length;
    	//arrFeature=new ArrayList<String>();
    }  
*/    
    public void Sep(String curChar, int POSID, String preWordSense){
    	String POS = arrPOS[POSID];
    	arrWord[size]= curChar;
    	arrTag[size]= POS;   
    	if(preWordSense !=null && preWordSense.length()>0 && size>=1){
    		arrNormal[size-1] = preWordSense;
    	}
    	this.lastAction = POSID;
    	this.hisActions.add(lastAction);
    	size++;
    	bStart = false;
    }
    
    public void Finish(String preWordSense){   	
    	this.lastAction = 2000;
    	this.hisActions.add(lastAction);
    	if(preWordSense !=null && size>=1){
    		arrNormal[size-1] = preWordSense;
    	}
    	bStart = false;
    }
    
    public void Add(String curChar) {
    	if(size>0){
    		arrWord[size-1] = arrWord[size-1]+curChar;	    	
	    	this.lastAction=1000;
	    	this.hisActions.add(lastAction);
	    	bStart = false;
    	}
    	else {
    		System.out.println("初始不能做合并操作");
    	}    	
    }
 
    
    public String toString(){
    	String str="";
		for(int i=0;i<size;i++){
			if(arrNormal[i] != null && arrNormal[i].length()>0){
				str+=arrWord[i] + "|"+arrNormal[i] +"_" + arrTag[i] + " ";
			}else{
				str+=arrWord[i] +"_" + arrTag[i] + " ";
			}
			
		}
		return str.trim();		
    } 
    
    public String GetSentence(){
    	String str="";
		for(int i=0;i<size;i++){		
			   str+=arrWord[i];

		}
		return str.trim();		
    } 
}

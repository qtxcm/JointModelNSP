package microblog;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class State {
	public int lastAction=0;  // 0:"S"  or 1:"A"
	public String[] arrWord; //单词序列
	public String[] arrTag; //标注序列
	
	public static  int MAXNUM=400;//最大长度
	public int size=0;
    public double score=0;    //分数
    
    public boolean  bIsGold = true;
    //public ArrayList<String> arrFeature=new ArrayList<String>();
   
    public State(){
    	this.lastAction=0;
    	this.arrWord = new String[MAXNUM]; 
    	this.arrTag = new String[MAXNUM];
    	this.score=0;	
    	this.size=0;
    	this.bIsGold=true;
    	//arrFeature=new ArrayList<String>();
    	//arrFeatureTemplate=new ArrayList<String>();
    }
    public State(State newState){
    	this.lastAction=newState.lastAction;
    	this.arrWord = new String[MAXNUM]; 
    	this.arrTag = new String[MAXNUM];
    	this.size=newState.size; 
    	this.score=newState.score; 
    	this.bIsGold = newState.bIsGold;
    	for(int i=0;i<size;i++){
    		this.arrWord[i]=newState.arrWord[i];
    		this.arrTag[i]=newState.arrTag[i];
    	}     	
    	//for(String f:newState.arrFeature){
    		//arrFeature.add(f); 
    	//}
    }
    public State(String[] content, int action){
    	this.lastAction=action;
    	this.arrWord = new String[MAXNUM]; 
    	this.arrTag = new String[MAXNUM];
    	this.size=content.length;
    	//arrFeature=new ArrayList<String>();
    }  
    
    public void Sep(String curChar, String POS){
    	arrWord[size]= curChar;
    	arrTag[size]= POS;    	
    	this.lastAction = 0;
    	size++;
    }
    
    public void Add(String curChar) throws Exception {
    	if(size>0){
    		arrWord[size-1] = arrWord[size-1]+curChar;	    	
	    	this.lastAction=1;
    	}else {
    		throw new Exception("初始不能做合并操作");
    	}    	
    }
    public void AddScore(double score){
    	this.score +=score;
    }
    
    public String toString(){
    	String str="";
		for(int i=0;i<size;i++)
			str+=arrWord[i] +"_" + arrTag[i] + " ";
		return str.trim();		
    } 
    
    public String GetSentence(){
    	String str="";
		for(int i=0;i<size;i++){		
			   str+=arrWord[i];

		}
		return str.trim();		
    } 
    
    /**
	 * 已标注序列转化为State对象
	 * @param tagSequence
	 * @return
	 */
	public  State TagSeConvertState(String tagSequence){
		State newState= new State();		
		StringTokenizer token=new StringTokenizer(tagSequence," "); 
		int i=0;
		while ( token.hasMoreElements() ){
			String tempStr = token.nextToken();
			int index = tempStr.indexOf("_");
			newState.arrWord[i] = tempStr.substring(0, index);
			newState.arrTag[i] = tempStr.substring(index+1, tempStr.length());	
			i++;
			}		
		newState.size=i;		
		return newState;	
	}	
    
}

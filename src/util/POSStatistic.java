package util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import microblog.Feature;

//统计训练集的所有的字符及词的词性
public class POSStatistic {
    public HashMap<String, String> hsChar = new HashMap<String, String>();
    public HashMap<String, String> hsWord = new HashMap<String, String>();

    public HashMap<String, Integer> hmCharCount = new HashMap<String, Integer>();
    public HashMap<String, Integer> hmWordCount = new HashMap<String, Integer>();
	
	public String train_file = "f:\\train.ctb70-yiou.pos";
	
	public String CharPos_file = "f:\\char.pos";
	public String WordPos_file = "f:\\word.pos";
    public POSStatistic(String CharPos_file, String WordPos_file){
    	this.CharPos_file=CharPos_file;
    	this.WordPos_file = WordPos_file;
    }	
    public POSStatistic(){    	
    }	
     
    public void StatisticsTrainFile(){
    	File file = new File(train_file);
		BufferedInputStream fis;
		try {
			fis = new BufferedInputStream(new FileInputStream(file));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis, "utf-8"), 5 * 1024 * 1024);// 用50M的缓冲读取文本文件
			String line = "";			
			while ((line = reader.readLine()) != null) {
				if(line.trim().length()>0){
					String[] temstrs=line.trim().split(" ");					
						for(int i=0;i< temstrs.length;i++){
							String f=temstrs[i];
							String[] strs=f.trim().split("_");
							String word = strs[0];
							String tag= strs[1];
							
							
							
							if(hsWord.containsKey(word)){
								String poss= hsWord.get(word);
								Integer count = hmWordCount.get(word);
								hmWordCount.put(word, count+1);
								if(poss.indexOf(tag+"|")<0){
									poss+=tag+"|";
									hsWord.put(word, poss);
								}								
							}else {
								hsWord.put(word, tag+"|");
								hmWordCount.put(word, 1);
							}
							for(int j=0;j<word.length();j++ ){
								String ch= word.substring(j,j+1);
								String poss= hsChar.get(ch);
								if(hmCharCount.containsKey(ch)){
									Integer count = hmCharCount.get(ch);
									hmCharCount.put(ch,count+1);
								}else{
									hmCharCount.put(ch,1);
								}								
								if(poss ==null){
									hsChar.put(ch, tag+"|");									
								}else if(poss.indexOf(tag+"|")<0){
									poss +=tag+"|";
									hsChar.put(ch, poss);									
								}							
							}							
						}
				}								
			}
			
			//保存
			FileWriter fw = new FileWriter(WordPos_file);
			BufferedWriter bw = new BufferedWriter(fw); // 将缓冲对文件的输出			
		    Iterator iter = hsWord.entrySet().iterator();
		      while (iter.hasNext()) {
		         Map.Entry<String, String> entry = (Map.Entry<String, String> ) iter.next();
		         String key = entry.getKey();
		         String value = entry.getValue();
		         Integer count = hmWordCount.get(key);
		         if(count > 3){
		        	  bw.write(key+" " + value + "\r\n");
		         }
		         //value = value.substring(0,value.length()-1);
		       
		   }
		      bw.close();
		      fw.close();
			
		       fw = new FileWriter(CharPos_file);
				 bw = new BufferedWriter(fw); // 将缓冲对文件的输出			
			     iter = hsChar.entrySet().iterator();
			      while (iter.hasNext()) {
			         Map.Entry<String, String> entry = (Map.Entry<String, String> ) iter.next();
			         String key = entry.getKey();
			         String value = entry.getValue();
			         Integer count = hmCharCount.get(key);
			         if(count > 5){
			        	 bw.write(key+" " + value + "\r\n");
			         }
			   }
			      bw.close();
			      fw.close();	
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
    }
    /**
     * 加载字符和单词词性map
     * @param hsChar
     * @param hsWord
     */
    public void loadCharAndWordPos(HashMap<String, String>hsChar, HashMap<String, String>hsWord){
    	  hsChar = new HashMap<String, String>();
    	  hsWord = new HashMap<String, String>();
    	
		try {
			File file = new File(WordPos_file);
			BufferedInputStream fis;
			fis = new BufferedInputStream(new FileInputStream(file));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis, "utf-8"), 5 * 1024 * 1024);// 用50M的缓冲读取文本文件
			String line = "";			
			while ((line = reader.readLine()) != null) {
				if(line.trim().length()>0){
					String[] temstrs=line.trim().split(" ");					
					hsWord.put(temstrs[0].trim(), temstrs[1].trim());	
				}
			}
			reader.close();
			fis.close();
			
			
			file = new File(CharPos_file);
			fis = new BufferedInputStream(new FileInputStream(file));
			reader = new BufferedReader(new InputStreamReader(
					fis, "utf-8"), 5 * 1024 * 1024);// 用50M的缓冲读取文本文件
			line = "";			
			while ((line = reader.readLine()) != null) {
				if(line.trim().length()>0){
					String[] temstrs=line.trim().split(" ");					
					hsChar.put(temstrs[0].trim(), temstrs[1].trim());	
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   	
    	
    }
    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		POSStatistic ob= new  POSStatistic();
		ob.StatisticsTrainFile();
	}
}

package microblog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import microblog.Feature.featureName;

public class Model {	
	public  HashMap<String, Set<String>> m_posCloseSet = new HashMap<String, Set<String>>();
	public  HashMap<String, Map<String, Integer>> m_wordPOSSets = new HashMap<String, Map<String, Integer>>();
	public  HashMap<String, Integer> m_wordFreq = new HashMap<String, Integer>();
	public  HashMap<String, Map<String, Integer>> m_startCharPOSSets = new HashMap<String, Map<String, Integer>>();
	public  HashMap<String, Integer> m_startCharFreq = new HashMap<String, Integer>();
	
	
	// feature templates abstd::cout characters
	public HashMap<String, Feature> m_mapOrgCharUnigram=new HashMap<String, Feature>();   //C0
	public HashMap<String, Feature> m_mapOrgCharBigram=new HashMap<String, Feature>();    //C-1C0
	public HashMap<String, Feature> m_mapOrgCharTrigram=new HashMap<String, Feature>();   //C-2C-1C0
	
	// feature templates abstd::cout words	  
	public HashMap<String, Feature> m_mapOrgSeenWords=new HashMap<String, Feature>();     //w_1
	public HashMap<String, Feature> m_mapOrgLastWordByWord=new HashMap<String, Feature>(); //w-2w-1
	public HashMap<String, Feature> m_mapOrgCurrentWordLastChar=new HashMap<String, Feature>();//w_1_end_w_2
	public HashMap<String, Feature> m_mapOrgLastWordFirstChar=new HashMap<String, Feature>();//w_1_c_0
	public HashMap<String, Feature> m_mapOrgFirstCharLastWordByWord=new HashMap<String, Feature>();//start_w_1_C_0
	public HashMap<String, Feature> m_mapOrgLastWordByLastChar=new HashMap<String, Feature>();//w_1_c_0_t_1
	public HashMap<String, Feature> m_mapOrgSeparateChars=new HashMap<String, Feature>();//end_w_1_c_0
	public HashMap<String, Feature> m_mapOrgConsecutiveChars=new HashMap<String, Feature>();//char_bigram  for app
	public HashMap<String, Feature> m_mapOrgFirstAndLastChars=new HashMap<String, Feature>(); //start_w_1end_w_1
	public HashMap<String, Feature> m_mapOrgOneCharWord=new HashMap<String, Feature>();//w-1 if(len_w-1==1)
	public HashMap<String, Feature> m_mapOrgLengthByFirstChar = new HashMap<String, Feature>();//start_w_1_len_w_1
	public HashMap<String, Feature> m_mapOrgLengthByLastChar = new HashMap<String, Feature>();//end_w_1_len_w_1
	public HashMap<String, Feature> m_mapOrgLengthByLastWord = new HashMap<String, Feature>();//w_2_len_w_1
	public HashMap<String, Feature> m_mapOrgLastLengthByWord = new HashMap<String, Feature>();//w_1_len_w_2
	
	// feature templates tag	
	public HashMap<String, Feature> m_mapOrgCurrentTag=new HashMap<String, Feature>(); //w_1_t_1
	public HashMap<String, Feature> m_mapOrgLastTagByTag=new HashMap<String, Feature>(); //t-1t0
	public HashMap<String, Feature> m_mapOrgLastTwoTagsByTag=new HashMap<String, Feature>(); //t-2t-1t0
	public HashMap<String, Feature> m_mapOrgTagByLastWord=new HashMap<String, Feature>(); //w-1t0
	public HashMap<String, Feature> m_mapOrgLastTagByWord=new HashMap<String, Feature>(); //w-1t-2
	public HashMap<String, Feature> m_mapOrgTagByFirstChar=new HashMap<String, Feature>();//first_char_0, tag_0
	public HashMap<String, Feature> m_mapOrgTagByLastChar=new HashMap<String, Feature>();//end_w_1_t_1
	public HashMap<String, Feature> m_mapOrgTagByChar=new HashMap<String, Feature>();//(first_char_0, tag_0  for two action
	public HashMap<String, Feature> m_mapOrgTagOfOneCharWord=new HashMap<String, Feature>();//end_w_2_w_1_c_0 if len_w_1=1 
	public HashMap<String, Feature> m_mapOrgRepeatedCharByTag=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapOrgTagByWordAndPrevChar=new HashMap<String, Feature>();//w_1_end_w_2_t_1
	public HashMap<String, Feature> m_mapOrgTagByWordAndNextChar=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapOrgTaggedCharByFirstChar=new HashMap<String, Feature>();// first_char char_unigram, tag_0 for app
	public HashMap<String, Feature> m_mapOrgTaggedCharByLastChar=new HashMap<String, Feature>();//w_1的char与last_char
	
	// extra features
	public HashMap<String, Feature> m_mapOrgTaggedSeparateChars=new HashMap<String, Feature>();//last_char_1, tag_1, first_char_0, tag_0
	public HashMap<String, Feature> m_mapOrgTaggedConsecutiveChars=new HashMap<String, Feature>();//char_bigram, tag_0 for app
	
	public HashMap<String, Feature> m_mapOrgWordTagTag=new HashMap<String, Feature>();//word_2, tag_0_tag_1
	public HashMap<String, Feature> m_mapOrgTagWordTag=new HashMap<String, Feature>();//word_1, tag_0_tag_2
	public HashMap<String, Feature> m_mapOrgFirstCharBy2Tags=new HashMap<String, Feature>();//first_char_0, tag_0_tag_1
	public HashMap<String, Feature> m_mapOrgFirstCharBy3Tags=new HashMap<String, Feature>();//first_char_0, tag_0_tag_1_tag_2
	public HashMap<String, Feature> m_mapOrgFirstCharAndChar=new HashMap<String, Feature>();
	
	public HashMap<String, Feature> m_mapOrgSepCharAndNextChar=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapOrgAppCharAndNextChar=new HashMap<String, Feature>();
	
	public HashMap<String, Feature> m_mapOrgPartialWord=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapOrgPartialLengthByFirstChar=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapOrgLengthByTagAndFirstChar=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapOrgLengthByTagAndLastChar=new HashMap<String, Feature>();
	
	public HashMap<String, Feature> m_mapOrgTag0Tag1Size1=new HashMap<String, Feature>();//t_1_t_0_len(w_1)
	public HashMap<String, Feature> m_mapOrgTag1Tag2Size1=new HashMap<String, Feature>();//t_2_t_1_len(w_1)
	public HashMap<String, Feature> m_mapOrgTag0Tag1Tag2Size1=new HashMap<String, Feature>();//t_1_t_0_len(w_1)
	
	 // feature templates knowledge
	public HashMap<String, Feature> m_mapOrgTagByFirstCharCat=new HashMap<String, Feature>();//first_char_cat_0, tag_0
	public HashMap<String, Feature> m_mapOrgTagByLastCharCat=new HashMap<String, Feature>();//last_char_cat_1, tag_1
	public HashMap<String, Feature> m_mapOrgSeparateCharCat=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapOrgConsecutiveCharCat=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapOrgConsecutiveCharTagCat=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapOrgSeparateWordCat=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapOrgTagByCurWordCat=new HashMap<String, Feature>();
	
	// statistical information
	public HashMap<String, Integer> m_mapWordFrequency = new HashMap<String, Integer>();
	public HashMap<String, String> m_mapTagDictionary = new HashMap<String, String>();
	public HashMap<String, String> m_mapCharTagDictionary = new HashMap<String, String>();
	public HashMap<String, String> m_mapCanStart = new HashMap<String, String>();
	//normalize features
	public HashMap<String, Feature> m_mapNorCharUnigram=new HashMap<String, Feature>();   //C0
	public HashMap<String, Feature> m_mapNorCharBigram=new HashMap<String, Feature>();    //C-1C0
	public HashMap<String, Feature> m_mapNorCharTrigram=new HashMap<String, Feature>();   //C-2C-1C0
	
	// feature templates abstd::cout words	  
	public HashMap<String, Feature> m_mapNorSeenWords=new HashMap<String, Feature>();     //w_1
	public HashMap<String, Feature> m_mapNorLastWordByWord=new HashMap<String, Feature>(); //w-2w-1
	public HashMap<String, Feature> m_mapNorCurrentWordLastChar=new HashMap<String, Feature>();//w_1_end_w_2
	public HashMap<String, Feature> m_mapNorLastWordFirstChar=new HashMap<String, Feature>();//w_1_c_0
	public HashMap<String, Feature> m_mapNorFirstCharLastWordByWord=new HashMap<String, Feature>();//start_w_1_C_0
	public HashMap<String, Feature> m_mapNorLastWordByLastChar=new HashMap<String, Feature>();//w_1_c_0_t_1
	public HashMap<String, Feature> m_mapNorSeparateChars=new HashMap<String, Feature>();//end_w_1_c_0
	public HashMap<String, Feature> m_mapNorConsecutiveChars=new HashMap<String, Feature>();//char_bigram  for app
	public HashMap<String, Feature> m_mapNorFirstAndLastChars=new HashMap<String, Feature>(); //start_w_1end_w_1
	public HashMap<String, Feature> m_mapNorOneCharWord=new HashMap<String, Feature>();//w-1 if(len_w-1==1)
	public HashMap<String, Feature> m_mapNorLengthByFirstChar = new HashMap<String, Feature>();//start_w_1_len_w_1
	public HashMap<String, Feature> m_mapNorLengthByLastChar = new HashMap<String, Feature>();//end_w_1_len_w_1
	public HashMap<String, Feature> m_mapNorLengthByLastWord = new HashMap<String, Feature>();//w_2_len_w_1
	public HashMap<String, Feature> m_mapNorLastLengthByWord = new HashMap<String, Feature>();//w_1_len_w_2
	
	// feature templates tag	
	public HashMap<String, Feature> m_mapNorCurrentTag=new HashMap<String, Feature>(); //w_1_t_1
	public HashMap<String, Feature> m_mapNorLastTagByTag=new HashMap<String, Feature>(); //t-1t0
	public HashMap<String, Feature> m_mapNorLastTwoTagsByTag=new HashMap<String, Feature>(); //t-2t-1t0
	public HashMap<String, Feature> m_mapNorTagByLastWord=new HashMap<String, Feature>(); //w-1t0
	public HashMap<String, Feature> m_mapNorLastTagByWord=new HashMap<String, Feature>(); //w-1t-2
	public HashMap<String, Feature> m_mapNorTagByFirstChar=new HashMap<String, Feature>();//first_char_0, tag_0
	public HashMap<String, Feature> m_mapNorTagByLastChar=new HashMap<String, Feature>();//end_w_1_t_1
	public HashMap<String, Feature> m_mapNorTagByChar=new HashMap<String, Feature>();//(first_char_0, tag_0  for two action
	public HashMap<String, Feature> m_mapNorTagOfOneCharWord=new HashMap<String, Feature>();//end_w_2_w_1_c_0 if len_w_1=1 
	public HashMap<String, Feature> m_mapNorRepeatedCharByTag=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapNorTagByWordAndPrevChar=new HashMap<String, Feature>();//w_1_end_w_2_t_1
	public HashMap<String, Feature> m_mapNorTagByWordAndNextChar=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapNorTaggedCharByFirstChar=new HashMap<String, Feature>();// first_char char_unigram, tag_0 for app
	public HashMap<String, Feature> m_mapNorTaggedCharByLastChar=new HashMap<String, Feature>();//w_1的char与last_char
	
	// extra features
	public HashMap<String, Feature> m_mapNorTaggedSeparateChars=new HashMap<String, Feature>();//last_char_1, tag_1, first_char_0, tag_0
	public HashMap<String, Feature> m_mapNorTaggedConsecutiveChars=new HashMap<String, Feature>();//char_bigram, tag_0 for app
	
	public HashMap<String, Feature> m_mapNorWordTagTag=new HashMap<String, Feature>();//word_2, tag_0_tag_1
	public HashMap<String, Feature> m_mapNorTagWordTag=new HashMap<String, Feature>();//word_1, tag_0_tag_2
	public HashMap<String, Feature> m_mapNorFirstCharBy2Tags=new HashMap<String, Feature>();//first_char_0, tag_0_tag_1
	public HashMap<String, Feature> m_mapNorFirstCharBy3Tags=new HashMap<String, Feature>();//first_char_0, tag_0_tag_1_tag_2
	public HashMap<String, Feature> m_mapNorFirstCharAndChar=new HashMap<String, Feature>();
	
	public HashMap<String, Feature> m_mapNorSepCharAndNextChar=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapNorAppCharAndNextChar=new HashMap<String, Feature>();
	
	public HashMap<String, Feature> m_mapNorPartialWord=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapNorPartialLengthByFirstChar=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapNorLengthByTagAndFirstChar=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapNorLengthByTagAndLastChar=new HashMap<String, Feature>();
	
	public HashMap<String, Feature> m_mapNorTag0Tag1Size1=new HashMap<String, Feature>();//t_1_t_0_len(w_1)
	public HashMap<String, Feature> m_mapNorTag1Tag2Size1=new HashMap<String, Feature>();//t_2_t_1_len(w_1)
	public HashMap<String, Feature> m_mapNorTag0Tag1Tag2Size1=new HashMap<String, Feature>();//t_1_t_0_len(w_1)
	
	 // feature templates knowledge
	public HashMap<String, Feature> m_mapNorTagByFirstCharCat=new HashMap<String, Feature>();//first_char_cat_0, tag_0
	public HashMap<String, Feature> m_mapNorTagByLastCharCat=new HashMap<String, Feature>();//last_char_cat_1, tag_1
	public HashMap<String, Feature> m_mapNorSeparateCharCat=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapNorConsecutiveCharCat=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapNorConsecutiveCharTagCat=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapNorSeparateWordCat=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapNorTagByCurWordCat=new HashMap<String, Feature>();	
	
	//词义 feature templates 
	public HashMap<String, Feature> m_mapWordSense = new HashMap<String, Feature>(); //w1s1
	public HashMap<String, Feature> m_mapLastWordAndWordSense = new HashMap<String, Feature>(); //w1s1w0
	public HashMap<String, Feature> m_mapPreWordAndWordSense = new HashMap<String, Feature>(); //w1s1w2
	public HashMap<String, Feature> m_mapStartPreAndWordSense = new HashMap<String, Feature>(); //w1s1start_w2 del
	public HashMap<String, Feature> m_mapEndPreAndWordSense = new HashMap<String, Feature>(); //w1s1end_w2
	public HashMap<String, Feature> m_mapStartLastAndWordSense = new HashMap<String, Feature>(); //w1s1start_w0  del
	public HashMap<String, Feature> m_mapLastCharAndWordSense = new HashMap<String, Feature>(); //w1s1C0
	public HashMap<String, Feature> m_mapTagWordSense = new HashMap<String, Feature>(); //w1s1t1
	public HashMap<String, Feature> m_mapPreTagAndWordSense = new HashMap<String, Feature>(); //w1s1t2
	public HashMap<String, Feature> m_mapLastTagAndWordSense = new HashMap<String, Feature>(); //w1s1t0
	public HashMap<String, Feature> m_mapThreeWordAndSense = new HashMap<String, Feature>(); //w1s1w0w2
	public HashMap<String, Feature> m_mapPreLastTagAndWordSense = new HashMap<String, Feature>(); //w1s1t0t2
	public HashMap<String, Feature> m_mapTwoWordSense = new HashMap<String, Feature>(); //w1s1w2s2
	public HashMap<String, Feature> m_mapLastTagAndTwoWordSense = new HashMap<String, Feature>(); //w1s1w2s2t0
	public HashMap<String, Feature> m_mapLastWordAndTwoWordSense = new HashMap<String, Feature>(); //w1s1w2s2w0
	public HashMap<String, Feature> m_mapLastTagAndPreWordSense = new HashMap<String, Feature>(); //w2s2t0
	public HashMap<String, Feature> m_mapLastWordAndPreWordSense = new HashMap<String, Feature>(); //w2s2w0
	public HashMap<String, Feature> m_mapLastCharAndPreWordSense = new HashMap<String, Feature>(); //w3s2c0
	
	//语言模型
	public HashMap<String, Feature> m_mapGram2=new HashMap<String, Feature>();//
	public HashMap<String, Feature> m_mapGram3=new HashMap<String, Feature>();//
	public HashMap<String, Feature> m_mapGram4=new HashMap<String, Feature>();//	
	
	public Model(){
		//loadPosCloseSet();
	}
	
	
	
	
/*	public void loadPosCloseSet(){
		PosCloseSet = new HashMap<String, HashSet<String>>();
		
		HashSet<String> newSet = new HashSet<String>();
		newSet.add("了");newSet.add("着");newSet.add("过");newSet.add("的");
		PosCloseSet.put("AS", newSet);
		
		newSet = new HashSet<String>();
		newSet.add("把");newSet.add("将");
		PosCloseSet.put("BA", newSet);
		
		//PosCloseSet.put("CS", "如果|")
		newSet = new HashSet<String>();
		newSet.add("如果");newSet.add("如");newSet.add("若");newSet.add("假如");newSet.add("即使");newSet.add("不管");
		newSet.add("不论");newSet.add("无论");newSet.add("不但");newSet.add("尽管");newSet.add("虽然");newSet.add("虽");
		newSet.add("只要");newSet.add("只有");newSet.add("一旦");
		PosCloseSet.put("CS", newSet);
		
		newSet = new HashSet<String>();
		newSet.add("的"); newSet.add("之");
		PosCloseSet.put("DEC", newSet);
		
		newSet = new HashSet<String>();
		newSet.add("的"); newSet.add("之");
		PosCloseSet.put("DEG", newSet);
		
		newSet = new HashSet<String>();
		newSet.add("得"); 
		PosCloseSet.put("DER", newSet);
		
		newSet = new HashSet<String>();
		newSet.add("地"); 
		PosCloseSet.put("DEV", newSet);		
		
		newSet = new HashSet<String>();
		newSet.add("等"); newSet.add("等等");
		PosCloseSet.put("ETC", newSet);
		
		newSet = new HashSet<String>();
		newSet.add("被"); newSet.add("叫");newSet.add("给"); newSet.add("为");
		PosCloseSet.put("LB", newSet);
		
		newSet = new HashSet<String>();
		newSet.add("被"); newSet.add("给");
		PosCloseSet.put("SB", newSet);
		
		newSet = new HashSet<String>();
		newSet.add("是"); newSet.add("为");newSet.add("非");
		PosCloseSet.put("VC", newSet);
		
		newSet = new HashSet<String>();
		newSet.add("有"); newSet.add("没");newSet.add("没有");newSet.add("无");
		PosCloseSet.put("VE", newSet);
		
	}
*/	
	
	
	public void init(String filename, boolean bNewTrain) {
		if(bNewTrain == true){
			m_posCloseSet = new HashMap<String, Set<String>>();
			m_posCloseSet.put("AS", new HashSet<String>());
			m_posCloseSet.put("BA", new HashSet<String>());
			m_posCloseSet.put("CS", new HashSet<String>());
			m_posCloseSet.put("CC", new HashSet<String>());
			m_posCloseSet.put("DEC", new HashSet<String>());
			m_posCloseSet.put("DEG", new HashSet<String>());
			m_posCloseSet.put("DEV", new HashSet<String>());
			m_posCloseSet.put("DER", new HashSet<String>());
			m_posCloseSet.put("DT", new HashSet<String>());
			m_posCloseSet.put("ETC", new HashSet<String>());
			m_posCloseSet.put("IJ", new HashSet<String>());
			m_posCloseSet.put("LB", new HashSet<String>());
			m_posCloseSet.put("LC", new HashSet<String>());
			m_posCloseSet.put("P", new HashSet<String>());
			m_posCloseSet.put("PN", new HashSet<String>());
			m_posCloseSet.put("PU", new HashSet<String>());
			m_posCloseSet.put("SB", new HashSet<String>());
			m_posCloseSet.put("SP", new HashSet<String>());
			m_posCloseSet.put("VC", new HashSet<String>());
			m_posCloseSet.put("VE", new HashSet<String>());
			m_wordPOSSets = new HashMap<String, Map<String, Integer>>();
			m_wordFreq = new HashMap<String, Integer>();
			m_startCharPOSSets = new HashMap<String, Map<String, Integer>>();
			m_startCharFreq = new HashMap<String, Integer>();
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));
			String line = "";
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.isEmpty())continue;
				String[] temstrs=line.trim().split("\\s+");	
				for(String tempStr : temstrs)
				{
					int index = tempStr.indexOf("_");
					if(index == -1)
					{
						System.out.println("error input line: " + line);
						continue;
					}
					String[] theWordSense = tempStr.substring(0, index).split("\\|");
					String theWord = theWordSense[0];
					String theSense = "";
					if(theWordSense.length==2){
						theSense = theWordSense[1];
					}
					//System.out.println(theWord + "   " + line);
					String theFirstChar = theWord.substring(0,1);
					String thePOS = tempStr.substring(index+1, tempStr.length());					
					

					if(!m_wordFreq.containsKey(theWord))
					{
						m_wordFreq.put(theWord, 0);
						m_wordPOSSets.put(theWord, new HashMap<String, Integer>());
					}					
					m_wordFreq.put(theWord, m_wordFreq.get(theWord)+1);
					
					if(!m_wordPOSSets.get(theWord).containsKey(thePOS))
					{
						m_wordPOSSets.get(theWord).put(thePOS, 0);
					}
					m_wordPOSSets.get(theWord).put(thePOS, m_wordPOSSets.get(theWord).get(thePOS)+1);					
					
					if(!m_startCharFreq.containsKey(theFirstChar))
					{
						m_startCharFreq.put(theFirstChar, 0);
						m_startCharPOSSets.put(theFirstChar, new HashMap<String, Integer>());
					}
					m_startCharFreq.put(theFirstChar, m_startCharFreq.get(theFirstChar)+1);
					if(!m_startCharPOSSets.get(theFirstChar).containsKey(thePOS))
					{
						m_startCharPOSSets.get(theFirstChar).put(thePOS, 0);
					}
					m_startCharPOSSets.get(theFirstChar).put(thePOS, m_startCharPOSSets.get(theFirstChar).get(thePOS)+1);
					
					if(m_posCloseSet.containsKey(thePOS))
					{
						m_posCloseSet.get(thePOS).add(theWord);
					}
					
					if(theSense.length()>0){
						String theFirstCharSense = theSense.substring(0,1);
						if(!m_wordFreq.containsKey(theSense))
						{
							m_wordFreq.put(theSense, 0);
							m_wordPOSSets.put(theSense, new HashMap<String, Integer>());
						}					
						m_wordFreq.put(theSense, m_wordFreq.get(theWord)+1);
						
						if(!m_wordPOSSets.get(theSense).containsKey(thePOS))
						{
							m_wordPOSSets.get(theSense).put(thePOS, 0);
						}
						m_wordPOSSets.get(theSense).put(theSense, m_wordPOSSets.get(theSense).get(thePOS)+1);
						
						if(!m_startCharFreq.containsKey(theFirstCharSense))
						{
							m_startCharFreq.put(theFirstCharSense, 0);
							m_startCharPOSSets.put(theFirstCharSense, new HashMap<String, Integer>());
						}
						m_startCharFreq.put(theFirstCharSense, m_startCharFreq.get(theFirstCharSense)+1);
						if(!m_startCharPOSSets.get(theFirstCharSense).containsKey(thePOS))
						{
							m_startCharPOSSets.get(theFirstCharSense).put(thePOS, 0);
						}
						m_startCharPOSSets.get(theFirstCharSense).put(thePOS, m_startCharPOSSets.get(theFirstCharSense).get(thePOS)+1);
						
						if(m_posCloseSet.containsKey(thePOS))
						{
							m_posCloseSet.get(thePOS).add(theSense);
						}					
					}				
				}				
			}			
			reader.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//从文件中载入
	public int load(String filename){
		int preRoundIndexForTrain = 0;				
		newFeatureTemplates();
		m_posCloseSet = new HashMap<String, Set<String>>();
		m_wordPOSSets = new HashMap<String, Map<String, Integer>>();
		m_wordFreq = new HashMap<String, Integer>();
		m_startCharPOSSets = new HashMap<String, Map<String, Integer>>();
		m_startCharFreq = new HashMap<String, Integer>();
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "UTF-8"));
			String line = "";
			//String context = "";
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if(line.isEmpty())continue;
				String[] temstrs=line.trim().split("\\s+");	
				if(temstrs.length == 1) 
				{
					System.out.println("error line: " + line);
					continue;
				}
				if(temstrs[0].equals("weight") && temstrs.length == 6)
				{
					String name=temstrs[1].trim();
					double dweigth= Double.parseDouble(temstrs[2]);
					double dsum= Double.parseDouble(temstrs[3]);
					int iindex = (int)(Double.parseDouble(temstrs[4]));
					double aveWeight = Double.parseDouble(temstrs[5]);
					if(preRoundIndexForTrain==0)  preRoundIndexForTrain=iindex;
					
					
					String[] names = name.split("=");								
					HashMap<String, Feature> hm= GetFeatureTemplate(names[0]);
					hm.put(name, new Feature(name, dweigth,dsum, iindex,aveWeight));	
				}
				else if(temstrs[0].equals("worddict") && temstrs.length % 2 == 1)
				{
					String theWord = temstrs[1];
					int wordfreq = Integer.parseInt(temstrs[2]);
					if(m_wordFreq.containsKey(theWord))
					{
						System.out.println("model word dict word duplication: " + theWord);
					}
					m_wordFreq.put(theWord, wordfreq);
					m_wordPOSSets.put(theWord, new HashMap<String, Integer>());
					int sumfreq = 0;
					for(int idx = 3; idx < temstrs.length - 1; idx++)
					{
						String thePOS = temstrs[idx];
						idx++;
						int curPOSFreq = Integer.parseInt(temstrs[idx]);			
						sumfreq += curPOSFreq;
						if(m_wordPOSSets.get(theWord).containsKey(thePOS))
						{
							System.out.println("model word dict pos duplication: " + theWord + " " + thePOS);
						}
						m_wordPOSSets.get(theWord).put(thePOS, curPOSFreq);
					}
					if(sumfreq != wordfreq)
					{
						System.out.println("model word dict freq does not match: " + theWord );
					}
				}
				else if(temstrs[0].equals("schardict") && temstrs.length % 2 == 1 && temstrs[1].length() == 1)
				{
					String theWord = temstrs[1];
					int wordfreq = Integer.parseInt(temstrs[2]);
					if(m_startCharFreq.containsKey(theWord))
					{
						System.out.println("model start char dict char duplication: " + theWord);
					}
					m_startCharFreq.put(theWord, wordfreq);
					m_startCharPOSSets.put(theWord, new HashMap<String, Integer>());
					int sumfreq = 0;
					for(int idx = 3; idx < temstrs.length - 1; idx++)
					{
						String thePOS = temstrs[idx];
						idx++;
						int curPOSFreq = Integer.parseInt(temstrs[idx]);			
						sumfreq += curPOSFreq;
						if(m_startCharPOSSets.get(theWord).containsKey(thePOS))
						{
							System.out.println("model start char dict pos duplication: " + theWord + " " + thePOS);
						}
						m_startCharPOSSets.get(theWord).put(thePOS, curPOSFreq);
					}
					if(sumfreq != wordfreq)
					{
						System.out.println("model start char dict freq does not match: " + theWord );
					}
				}
				else if(temstrs[0].equals("closetag"))
				{
					String thePOS = temstrs[1];
					if(m_posCloseSet.containsKey(thePOS))
					{
						System.out.println("model close tag dict POS duplication: " + thePOS);
					}
					m_posCloseSet.put(thePOS, new HashSet<String>());
					for(int idx = 2; idx < temstrs.length; idx++)
					{
						if(m_posCloseSet.get(thePOS).contains(temstrs[idx]))
						{
							System.out.println("model close tag dict POS word duplication: " + thePOS + " " + temstrs[idx]);
						}
						m_posCloseSet.get(thePOS).add(temstrs[idx]);
					}
				}
				else
				{
					System.out.println("error line: " + line);
				}

			}								
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return preRoundIndexForTrain;
	}
	
	//写到文件中
    public void save(String filename){
		try {
			PrintWriter bw = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF-8"));
			for(featureName f: featureName.values()){
				HashMap<String, Feature> hm= GetFeatureTemplate(f.toString());				
				for(String theKey : hm.keySet())
				{
					Feature tempf = hm.get(theKey);
					bw.println("weight " + tempf.name+" "+ Double.toString(tempf.weight)+" "+ Double.toString(tempf.sum) +" "+ Double.toString(tempf.lastUpdateIndex)+" "+Double.toString(tempf.aveWeight));;
				}			  
			}
			
			for(String theKey : m_wordFreq.keySet())
			{
				String outline = "worddict\t" + theKey + " "+ Integer.toString(m_wordFreq.get(theKey));
				for(String thePOSKey : m_wordPOSSets.get(theKey).keySet())
				{
					outline = outline + " " + thePOSKey + " " + Integer.toString(m_wordPOSSets.get(theKey).get(thePOSKey));
				}
				bw.println(outline);
			}
			
			for(String theKey : m_startCharFreq.keySet())
			{
				String outline = "schardict\t" + theKey + " "+ Integer.toString(m_startCharFreq.get(theKey));
				for(String thePOSKey : m_startCharPOSSets.get(theKey).keySet())
				{
					outline = outline + " " + thePOSKey + " " + Integer.toString(m_startCharPOSSets.get(theKey).get(thePOSKey));
				}
				bw.println(outline);
			}
			
			for(String theKey : m_posCloseSet.keySet())
			{
				String outline = "closetag\t" + theKey;
				for(String theWordKey : m_posCloseSet.get(theKey))
				{
					outline = outline + " " + theWordKey;
				}
				bw.println(outline);
			}
			
			
			bw.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// 创建FileWriter对象，用来写入字符流
		
	}
    /**
     * 更新权重,总和更新次上次训练次数
     * @param arrFeature  特征
     * @param iType   负：减    正：加
     * @param index   训练的次数  
     */
    
    public void UpdateWeighth(List<String> oprFeatures, int iType, int updateIndex){
    	double  tempiType = iType;
    	double dType =iType;
    	for(String curFeature : oprFeatures){
    		//System.out.println(arrFeature[i].toString());
    		//if(arrFeature[i] == null)  return;    		
    		int _index = curFeature.indexOf("=");     	
    		
    		String sTemplateName = curFeature.substring(0,_index);
    		HashMap<String, Feature>  hm = GetFeatureTemplate(sTemplateName); 		
    		Feature temp= hm.get(curFeature);    		

    		if(temp!=null){ 
    			
	    			if(temp.lastUpdateIndex<updateIndex){
	    			   // temp.sum += (updateIndex-temp.lastUpdateIndex-1)*temp.weight;   
	    				temp.sum += (updateIndex-temp.lastUpdateIndex)*temp.weight;  
	    			}
	    			if(curFeature.equals("SeenWords=事")){
	    				System.out.println("UpdateWeighth before:" + temp.name + temp.weight+"   " +temp.sum+"  "+ temp.lastUpdateIndex+"  "+ temp.aveWeight);
	    			}
	    			temp.weight += iType;    				
	    			//temp.sum += temp.weight;
	    			temp.sum += iType;  
	    			temp.lastUpdateIndex = updateIndex;
	    			//if(curFeature.equals("SeenWords=事")){
	    				//System.out.println("UpdateWeighth after:" + temp.name + temp.weight+"   " +temp.sum+"  "+ temp.lastUpdateIndex+"  "+ temp.aveWeight);
	    			//}
	    			hm.put(curFeature, temp);    			
    		}else{   			
    			hm.put(curFeature, new Feature(curFeature, (double)iType, (double)iType, updateIndex, 0.0));   			
    		}    		
    	}    	
    }   
    
    /**
     * 获取平均化权重
     * @param curRoundIndexForTrain   权重更新总次数
     */
    public void AveWeight( int curRoundIndexForTrain){
    	for(featureName f: featureName.values()){
    		HashMap<String, Feature> hm= GetFeatureTemplate(f.toString());
    		Iterator iter = hm.entrySet().iterator();
    		while (iter.hasNext()) {
    		   Map.Entry<String, Feature> entry = (Map.Entry<String, Feature> ) iter.next();
    		   String key = entry.getKey();
    		   Feature tempf = entry.getValue();    		  
    		   if(tempf.lastUpdateIndex<curRoundIndexForTrain){
    			   tempf.sum+=(curRoundIndexForTrain-tempf.lastUpdateIndex)*tempf.weight;
    			   tempf.lastUpdateIndex=curRoundIndexForTrain;    			   
    		   }
    			tempf.aveWeight= tempf.sum/(curRoundIndexForTrain);  
    			
    			if(tempf.name.equals("SeenWords=事"))
    				System.out.println("AveWeight SeenWords=事weight:" + tempf.weight +   "ave:" + tempf.aveWeight);
    			
    			hm.put(key, tempf);

    		}   	
    	}
    } 
    
    
    public HashMap<String, Feature> GetFeatureTemplate(String featureTemplate){    
    	//System.out.println(featureTemplate);
    	featureName aa = featureName.valueOf(featureName.class, featureTemplate);   
    	
    	switch(aa){    	
    		case OrgCharUnigram : return m_mapOrgCharUnigram; 
    		case OrgCharBigram : return m_mapOrgCharBigram; 
    		case OrgCharTrigram : return m_mapOrgCharTrigram; 
    		case OrgSeenWords : return m_mapOrgSeenWords; 
    		case OrgLastWordByWord : return m_mapOrgLastWordByWord;   
    		case OrgCurrentWordLastChar : return m_mapOrgCurrentWordLastChar; 
    		case OrgLastWordFirstChar : return m_mapOrgLastWordFirstChar; 
    		
    		case OrgFirstCharLastWordByWord : return m_mapOrgFirstCharLastWordByWord; 
    		case OrgLastWordByLastChar : return m_mapOrgLastWordByLastChar; 
    		case OrgSeparateChars : return m_mapOrgSeparateChars; 
    		case OrgConsecutiveChars : return m_mapOrgConsecutiveChars; 
    		case OrgFirstAndLastChars : return m_mapOrgFirstAndLastChars; 
    		case OrgOneCharWord : return m_mapOrgOneCharWord; 
    		case OrgLengthByFirstChar : return m_mapOrgLengthByFirstChar; 
    		case OrgLengthByLastChar : return m_mapOrgLengthByLastChar; 
    		case OrgLengthByLastWord : return m_mapOrgLengthByLastWord; 
    		case OrgLastLengthByWord : return m_mapOrgLastLengthByWord; 
    		case OrgCurrentTag : return m_mapOrgCurrentTag; 
    		
    		case OrgLastTagByTag : return m_mapOrgLastTagByTag; 
    		case OrgLastTwoTagsByTag : return m_mapOrgLastTwoTagsByTag; 
    		case OrgTagByLastWord : return m_mapOrgTagByLastWord; 
    		case OrgLastTagByWord : return m_mapOrgLastTagByWord; 
    		case OrgTagByFirstChar : return m_mapOrgTagByFirstChar; 
    		case OrgTagByLastChar : return m_mapOrgTagByLastChar; 
    		case OrgTagByChar : return m_mapOrgTagByChar; 
    		case OrgTagOfOneCharWord : return m_mapOrgTagOfOneCharWord; 
    		case OrgRepeatedCharByTag : return m_mapOrgRepeatedCharByTag; 
    		case OrgTagByWordAndPrevChar : return m_mapOrgTagByWordAndPrevChar; 
    		case OrgTagByWordAndNextChar : return m_mapOrgTagByWordAndNextChar; 
    		case OrgTaggedCharByFirstChar : return m_mapOrgTaggedCharByFirstChar; 
    		case OrgTaggedCharByLastChar : return m_mapOrgTaggedCharByLastChar; 
    		case OrgTaggedSeparateChars : return m_mapOrgTaggedSeparateChars; 
    		case OrgTaggedConsecutiveChars : return m_mapOrgTaggedConsecutiveChars; 
    		case OrgWordTagTag : return m_mapOrgWordTagTag; 
    		case OrgTagWordTag : return m_mapOrgTagWordTag;
    		
    		case OrgFirstCharBy2Tags : return m_mapOrgFirstCharBy2Tags; 
    		case OrgFirstCharBy3Tags : return m_mapOrgFirstCharBy3Tags; 
    		case OrgFirstCharAndChar : return m_mapOrgFirstCharAndChar; 
    		case OrgSepCharAndNextChar : return m_mapOrgSepCharAndNextChar; 
    		case OrgAppCharAndNextChar : return m_mapOrgAppCharAndNextChar; 
    		case OrgPartialLengthByFirstChar : return m_mapOrgPartialLengthByFirstChar; 
    		case OrgLengthByTagAndFirstChar : return m_mapOrgLengthByTagAndFirstChar; 
    		case OrgLengthByTagAndLastChar : return m_mapOrgLengthByTagAndLastChar; 
    		case OrgTag0Tag1Size1 : return m_mapOrgTag0Tag1Size1; 
    		case OrgTag1Tag2Size1 : return m_mapOrgTag1Tag2Size1; 
    		case OrgTag0Tag1Tag2Size1 : return m_mapOrgTag0Tag1Tag2Size1; 
    		case OrgTagByFirstCharCat : return m_mapOrgTagByFirstCharCat; 
    		case OrgTagByLastCharCat : return m_mapOrgTagByLastCharCat; 
    		case OrgSeparateCharCat : return m_mapOrgSeparateCharCat; 
    		case OrgConsecutiveCharCat : return m_mapOrgConsecutiveCharCat;  
    		case OrgPartialWord: return m_mapOrgPartialWord;
    		case OrgConsecutiveCharTagCat: return m_mapOrgConsecutiveCharTagCat;
    		case OrgSeparateWordCat: return m_mapOrgSeparateWordCat;
    		case OrgTagByCurWordCat: return m_mapOrgTagByCurWordCat;       		
    		//normalization
    		case NorCharUnigram : return m_mapNorCharUnigram; 
    		case NorCharBigram : return m_mapNorCharBigram; 
    		case NorCharTrigram : return m_mapNorCharTrigram; 
    		case NorSeenWords : return m_mapNorSeenWords; 
    		case NorLastWordByWord : return m_mapNorLastWordByWord;   
    		case NorCurrentWordLastChar : return m_mapNorCurrentWordLastChar; 
    		case NorLastWordFirstChar : return m_mapNorLastWordFirstChar; 
    		
    		case NorFirstCharLastWordByWord : return m_mapNorFirstCharLastWordByWord; 
    		case NorLastWordByLastChar : return m_mapNorLastWordByLastChar; 
    		case NorSeparateChars : return m_mapNorSeparateChars; 
    		case NorConsecutiveChars : return m_mapNorConsecutiveChars; 
    		case NorFirstAndLastChars : return m_mapNorFirstAndLastChars; 
    		case NorOneCharWord : return m_mapNorOneCharWord; 
    		case NorLengthByFirstChar : return m_mapNorLengthByFirstChar; 
    		case NorLengthByLastChar : return m_mapNorLengthByLastChar; 
    		case NorLengthByLastWord : return m_mapNorLengthByLastWord; 
    		case NorLastLengthByWord : return m_mapNorLastLengthByWord; 
    		case NorCurrentTag : return m_mapNorCurrentTag; 
    		
    		case NorLastTagByTag : return m_mapNorLastTagByTag; 
    		case NorLastTwoTagsByTag : return m_mapNorLastTwoTagsByTag; 
    		case NorTagByLastWord : return m_mapNorTagByLastWord; 
    		case NorLastTagByWord : return m_mapNorLastTagByWord; 
    		case NorTagByFirstChar : return m_mapNorTagByFirstChar; 
    		case NorTagByLastChar : return m_mapNorTagByLastChar; 
    		case NorTagByChar : return m_mapNorTagByChar; 
    		case NorTagOfOneCharWord : return m_mapNorTagOfOneCharWord; 
    		case NorRepeatedCharByTag : return m_mapNorRepeatedCharByTag; 
    		case NorTagByWordAndPrevChar : return m_mapNorTagByWordAndPrevChar; 
    		case NorTagByWordAndNextChar : return m_mapNorTagByWordAndNextChar; 
    		case NorTaggedCharByFirstChar : return m_mapNorTaggedCharByFirstChar; 
    		case NorTaggedCharByLastChar : return m_mapNorTaggedCharByLastChar; 
    		case NorTaggedSeparateChars : return m_mapNorTaggedSeparateChars; 
    		case NorTaggedConsecutiveChars : return m_mapNorTaggedConsecutiveChars; 
    		case NorWordTagTag : return m_mapNorWordTagTag; 
    		case NorTagWordTag : return m_mapNorTagWordTag;
    		
    		case NorFirstCharBy2Tags : return m_mapNorFirstCharBy2Tags; 
    		case NorFirstCharBy3Tags : return m_mapNorFirstCharBy3Tags; 
    		case NorFirstCharAndChar : return m_mapNorFirstCharAndChar; 
    		case NorSepCharAndNextChar : return m_mapNorSepCharAndNextChar; 
    		case NorAppCharAndNextChar : return m_mapNorAppCharAndNextChar; 
    		case NorPartialLengthByFirstChar : return m_mapNorPartialLengthByFirstChar; 
    		case NorLengthByTagAndFirstChar : return m_mapNorLengthByTagAndFirstChar; 
    		case NorLengthByTagAndLastChar : return m_mapNorLengthByTagAndLastChar; 
    		case NorTag0Tag1Size1 : return m_mapNorTag0Tag1Size1; 
    		case NorTag1Tag2Size1 : return m_mapNorTag1Tag2Size1; 
    		case NorTag0Tag1Tag2Size1 : return m_mapNorTag0Tag1Tag2Size1; 
    		case NorTagByFirstCharCat : return m_mapNorTagByFirstCharCat; 
    		case NorTagByLastCharCat : return m_mapNorTagByLastCharCat; 
    		case NorSeparateCharCat : return m_mapNorSeparateCharCat; 
    		case NorConsecutiveCharCat : return m_mapNorConsecutiveCharCat;  
    		case NorPartialWord: return m_mapNorPartialWord;
    		case NorConsecutiveCharTagCat: return m_mapNorConsecutiveCharTagCat;
    		case NorSeparateWordCat: return m_mapNorSeparateWordCat;
    		case NorTagByCurWordCat: return m_mapNorTagByCurWordCat; 
    		
    		
    		//词义 feature templates 
    		case WordSense: return m_mapWordSense  ; //w1s1
    		case LastWordAndWordSense: return m_mapLastWordAndWordSense  ; //w1s1w0
    		case PreWordAndWordSense: return m_mapPreWordAndWordSense  ; //w1s1w2
    		case StartPreAndWordSense:  return m_mapStartPreAndWordSense  ; //w1s1statr_w2
    		case EndPreAndWordSense:  return m_mapEndPreAndWordSense  ; //w1s1end_w2
    		case StartLastAndWordSense:  return m_mapStartLastAndWordSense  ; //w1s1start_w0
    		case LastCharAndWordSense:  return m_mapLastCharAndWordSense  ; //w1s1C0
    		case TagWordSense:  return m_mapTagWordSense  ; //w1s1t1
    		case PreTagAndWordSense:  return m_mapPreTagAndWordSense  ; //w1s1t2
    		case LastTagAndWordSense:  return m_mapLastTagAndWordSense  ; //w1s1t0
    		case ThreeWordAndSense:  return m_mapThreeWordAndSense  ; //w1s1w0w2
    		case PreLastTagAndWordSense:  return m_mapPreLastTagAndWordSense  ; //w1s1t0t2
    		case TwoWordSense:  return m_mapTwoWordSense  ; //w1s1w2s2
    		case LastTagAndTwoWordSense:  return m_mapLastTagAndTwoWordSense  ; //w1s1w2s2t0
    		case LastWordAndTwoWordSense:  return m_mapLastWordAndTwoWordSense  ; //w1s1w2s2w0
    		case LastTagAndPreWordSense:  return m_mapLastTagAndPreWordSense  ; //w2s2t0
    		case LastWordAndPreWordSense:  return m_mapLastWordAndPreWordSense  ; //w2s2w0
    		case LastCharAndPreWordSense:  return m_mapLastCharAndPreWordSense  ; //w3s2c0
    		
    		case Gram2: return m_mapGram2;
    		case Gram3: return m_mapGram3;
    		case Gram4: return m_mapGram4;
    	}
    	return null;    	
    }
    public enum featureName{
    	OrgCharUnigram,OrgCharBigram,OrgCharTrigram,OrgSeenWords,OrgLastWordByWord,OrgCurrentWordLastChar,OrgLastWordFirstChar,
    	OrgFirstCharLastWordByWord,OrgLastWordByLastChar,OrgSeparateChars,OrgConsecutiveChars,OrgFirstAndLastChars,
    	OrgOneCharWord,OrgLengthByFirstChar,OrgLengthByLastChar,OrgLengthByLastWord,OrgLastLengthByWord,OrgCurrentTag,  
    	OrgLastTagByTag,  OrgLastTwoTagsByTag,OrgTagByLastWord,OrgLastTagByWord,OrgTagByFirstChar,OrgTagByLastChar,OrgTagByChar,
    	OrgTagOfOneCharWord,OrgRepeatedCharByTag, OrgTagByWordAndPrevChar,OrgTagByWordAndNextChar,OrgTagWordTag,
    	OrgTaggedCharByFirstChar,OrgTaggedCharByLastChar,OrgTaggedSeparateChars,OrgTaggedConsecutiveChars,OrgWordTagTag,
    	OrgFirstCharBy2Tags,OrgFirstCharBy3Tags,OrgFirstCharAndChar,OrgSepCharAndNextChar,OrgAppCharAndNextChar, OrgPartialWord,
    	OrgPartialLengthByFirstChar,OrgLengthByTagAndFirstChar,OrgLengthByTagAndLastChar,OrgTag0Tag1Size1,
    	OrgTag1Tag2Size1,OrgTag0Tag1Tag2Size1,OrgTagByFirstCharCat,OrgTagByLastCharCat,OrgSeparateCharCat,OrgConsecutiveCharCat,
    	OrgConsecutiveCharTagCat, OrgSeparateWordCat,OrgTagByCurWordCat,
    	NorCharUnigram,NorCharBigram,NorCharTrigram,NorSeenWords,NorLastWordByWord,NorCurrentWordLastChar,NorLastWordFirstChar,
    	NorFirstCharLastWordByWord,NorLastWordByLastChar,NorSeparateChars,NorConsecutiveChars,NorFirstAndLastChars,
    	NorOneCharWord,NorLengthByFirstChar,NorLengthByLastChar,NorLengthByLastWord,NorLastLengthByWord,NorCurrentTag,  
    	NorLastTagByTag,  NorLastTwoTagsByTag,NorTagByLastWord,NorLastTagByWord,NorTagByFirstChar,NorTagByLastChar,NorTagByChar,
    	NorTagOfOneCharWord,NorRepeatedCharByTag, NorTagByWordAndPrevChar,NorTagByWordAndNextChar,NorTagWordTag,
    	NorTaggedCharByFirstChar,NorTaggedCharByLastChar,NorTaggedSeparateChars,NorTaggedConsecutiveChars,NorWordTagTag,
    	NorFirstCharBy2Tags,NorFirstCharBy3Tags,NorFirstCharAndChar,NorSepCharAndNextChar,NorAppCharAndNextChar, NorPartialWord,
    	NorPartialLengthByFirstChar,NorLengthByTagAndFirstChar,NorLengthByTagAndLastChar,NorTag0Tag1Size1,
    	NorTag1Tag2Size1,NorTag0Tag1Tag2Size1,NorTagByFirstCharCat,NorTagByLastCharCat,NorSeparateCharCat,NorConsecutiveCharCat,
    	NorConsecutiveCharTagCat, NorSeparateWordCat,NorTagByCurWordCat,
    	WordSense,LastWordAndWordSense,PreWordAndWordSense,StartPreAndWordSense,EndPreAndWordSense,StartLastAndWordSense,
    	LastCharAndWordSense,TagWordSense,PreTagAndWordSense,LastTagAndWordSense,ThreeWordAndSense,PreLastTagAndWordSense,
    	TwoWordSense,LastTagAndTwoWordSense,LastWordAndTwoWordSense,LastTagAndPreWordSense,LastWordAndPreWordSense,
    	LastCharAndPreWordSense,Gram2,Gram3,Gram4;
    }
    
  //instantiation features
	public void newFeatureTemplates(){
		// feature templates abstd::cout characters
		m_mapNorCharUnigram=new HashMap<String, Feature>();   //C0
		m_mapOrgCharBigram=new HashMap<String, Feature>();    //C-1C0
		m_mapOrgCharTrigram=new HashMap<String, Feature>();   //C-2C-1C0
		
		// feature templates abstd::cout words	  
		m_mapOrgSeenWords=new HashMap<String, Feature>();     //w_1
		m_mapOrgLastWordByWord=new HashMap<String, Feature>(); //w-2w-1
		m_mapOrgCurrentWordLastChar=new HashMap<String, Feature>();//w_1_end_w_2
		m_mapOrgLastWordFirstChar=new HashMap<String, Feature>();//w_1_c_0
		m_mapOrgFirstCharLastWordByWord=new HashMap<String, Feature>();//start_w_1_C_0
		m_mapOrgLastWordByLastChar=new HashMap<String, Feature>();//w_1_c_0_t_1
		m_mapOrgSeparateChars=new HashMap<String, Feature>();//end_w_1_c_0
		m_mapOrgConsecutiveChars=new HashMap<String, Feature>();//char_bigram  for app
		m_mapOrgFirstAndLastChars=new HashMap<String, Feature>(); //start_w_1end_w_1
		m_mapOrgOneCharWord=new HashMap<String, Feature>();//w-1 if(len_w-1==1)
		m_mapOrgLengthByFirstChar = new HashMap<String, Feature>();//start_w_1_len_w_1
		m_mapOrgLengthByLastChar = new HashMap<String, Feature>();//end_w_1_len_w_1
		m_mapOrgLengthByLastWord = new HashMap<String, Feature>();//w_2_len_w_1
		m_mapOrgLastLengthByWord = new HashMap<String, Feature>();//w_1_len_w_2
		
		// feature templates tag	
		m_mapOrgCurrentTag=new HashMap<String, Feature>(); //w_1_t_1
		m_mapOrgLastTagByTag=new HashMap<String, Feature>(); //t-1t0
		m_mapOrgLastTwoTagsByTag=new HashMap<String, Feature>(); //t-2t-1t0
		m_mapOrgTagByLastWord=new HashMap<String, Feature>(); //w-1t0
		m_mapOrgLastTagByWord=new HashMap<String, Feature>(); //w-1t-2
		m_mapOrgTagByFirstChar=new HashMap<String, Feature>();//first_char_0, tag_0
		m_mapOrgTagByLastChar=new HashMap<String, Feature>();//end_w_1_t_1
		m_mapOrgTagByChar=new HashMap<String, Feature>();//(first_char_0, tag_0  for two action
		m_mapOrgTagOfOneCharWord=new HashMap<String, Feature>();//end_w_2_w_1_c_0 if len_w_1=1 
		m_mapOrgRepeatedCharByTag=new HashMap<String, Feature>();
		m_mapOrgTagByWordAndPrevChar=new HashMap<String, Feature>();//w_1_end_w_2_t_1
		m_mapOrgTagByWordAndNextChar=new HashMap<String, Feature>();
		m_mapOrgTaggedCharByFirstChar=new HashMap<String, Feature>();// first_char char_unigram, tag_0 for app
		m_mapOrgTaggedCharByLastChar=new HashMap<String, Feature>();//w_1的char与last_char
		
		// extra features
		m_mapOrgTaggedSeparateChars=new HashMap<String, Feature>();//last_char_1, tag_1, first_char_0, tag_0
		m_mapOrgTaggedConsecutiveChars=new HashMap<String, Feature>();//char_bigram, tag_0 for app
		
		m_mapOrgWordTagTag=new HashMap<String, Feature>();//word_2, tag_0_tag_1
		m_mapOrgTagWordTag=new HashMap<String, Feature>();//word_1, tag_0_tag_2
		m_mapOrgFirstCharBy2Tags=new HashMap<String, Feature>();//first_char_0, tag_0_tag_1
		m_mapOrgFirstCharBy3Tags=new HashMap<String, Feature>();//first_char_0, tag_0_tag_1_tag_2
		m_mapOrgFirstCharAndChar=new HashMap<String, Feature>();
		
		m_mapOrgSepCharAndNextChar=new HashMap<String, Feature>();
		m_mapOrgAppCharAndNextChar=new HashMap<String, Feature>();
		
		m_mapOrgPartialWord=new HashMap<String, Feature>();
		m_mapOrgPartialLengthByFirstChar=new HashMap<String, Feature>();
		m_mapOrgLengthByTagAndFirstChar=new HashMap<String, Feature>();
		m_mapOrgLengthByTagAndLastChar=new HashMap<String, Feature>();
		
		m_mapOrgTag0Tag1Size1=new HashMap<String, Feature>();//t_1_t_0_len(w_1)
		m_mapOrgTag1Tag2Size1=new HashMap<String, Feature>();//t_2_t_1_len(w_1)
		m_mapOrgTag0Tag1Tag2Size1=new HashMap<String, Feature>();//t_1_t_0_len(w_1)
		
		 // feature templates knowledge
		m_mapOrgTagByFirstCharCat=new HashMap<String, Feature>();//first_char_cat_0, tag_0
		m_mapOrgTagByLastCharCat=new HashMap<String, Feature>();//last_char_cat_1, tag_1
		m_mapOrgSeparateCharCat=new HashMap<String, Feature>();
		m_mapOrgConsecutiveCharCat=new HashMap<String, Feature>();
		
		m_mapOrgConsecutiveCharTagCat=new HashMap<String, Feature>();
		m_mapOrgSeparateWordCat=new HashMap<String, Feature>();
		m_mapOrgTagByCurWordCat=new HashMap<String, Feature>(); 
		
		// normalization features
		m_mapNorCharUnigram=new HashMap<String, Feature>();   //C0
		m_mapNorCharBigram=new HashMap<String, Feature>();    //C-1C0
		m_mapNorCharTrigram=new HashMap<String, Feature>();   //C-2C-1C0
		
		// feature templates abstd::cout words	  
		m_mapNorSeenWords=new HashMap<String, Feature>();     //w_1
		m_mapNorLastWordByWord=new HashMap<String, Feature>(); //w-2w-1
		m_mapNorCurrentWordLastChar=new HashMap<String, Feature>();//w_1_end_w_2
		m_mapNorLastWordFirstChar=new HashMap<String, Feature>();//w_1_c_0
		m_mapNorFirstCharLastWordByWord=new HashMap<String, Feature>();//start_w_1_C_0
		m_mapNorLastWordByLastChar=new HashMap<String, Feature>();//w_1_c_0_t_1
		m_mapNorSeparateChars=new HashMap<String, Feature>();//end_w_1_c_0
		m_mapNorConsecutiveChars=new HashMap<String, Feature>();//char_bigram  for app
		m_mapNorFirstAndLastChars=new HashMap<String, Feature>(); //start_w_1end_w_1
		m_mapNorOneCharWord=new HashMap<String, Feature>();//w-1 if(len_w-1==1)
		m_mapNorLengthByFirstChar = new HashMap<String, Feature>();//start_w_1_len_w_1
		m_mapNorLengthByLastChar = new HashMap<String, Feature>();//end_w_1_len_w_1
		m_mapNorLengthByLastWord = new HashMap<String, Feature>();//w_2_len_w_1
		m_mapNorLastLengthByWord = new HashMap<String, Feature>();//w_1_len_w_2
		
		// feature templates tag	
		m_mapNorCurrentTag=new HashMap<String, Feature>(); //w_1_t_1
		m_mapNorLastTagByTag=new HashMap<String, Feature>(); //t-1t0
		m_mapNorLastTwoTagsByTag=new HashMap<String, Feature>(); //t-2t-1t0
		m_mapNorTagByLastWord=new HashMap<String, Feature>(); //w-1t0
		m_mapNorLastTagByWord=new HashMap<String, Feature>(); //w-1t-2
		m_mapNorTagByFirstChar=new HashMap<String, Feature>();//first_char_0, tag_0
		m_mapNorTagByLastChar=new HashMap<String, Feature>();//end_w_1_t_1
		m_mapNorTagByChar=new HashMap<String, Feature>();//(first_char_0, tag_0  for two action
		m_mapNorTagOfOneCharWord=new HashMap<String, Feature>();//end_w_2_w_1_c_0 if len_w_1=1 
		m_mapNorRepeatedCharByTag=new HashMap<String, Feature>();
		m_mapNorTagByWordAndPrevChar=new HashMap<String, Feature>();//w_1_end_w_2_t_1
		m_mapNorTagByWordAndNextChar=new HashMap<String, Feature>();
		m_mapNorTaggedCharByFirstChar=new HashMap<String, Feature>();// first_char char_unigram, tag_0 for app
		m_mapNorTaggedCharByLastChar=new HashMap<String, Feature>();//w_1的char与last_char
		
		// extra features
		m_mapNorTaggedSeparateChars=new HashMap<String, Feature>();//last_char_1, tag_1, first_char_0, tag_0
		m_mapNorTaggedConsecutiveChars=new HashMap<String, Feature>();//char_bigram, tag_0 for app
		
		m_mapNorWordTagTag=new HashMap<String, Feature>();//word_2, tag_0_tag_1
		m_mapNorTagWordTag=new HashMap<String, Feature>();//word_1, tag_0_tag_2
		m_mapNorFirstCharBy2Tags=new HashMap<String, Feature>();//first_char_0, tag_0_tag_1
		m_mapNorFirstCharBy3Tags=new HashMap<String, Feature>();//first_char_0, tag_0_tag_1_tag_2
		m_mapNorFirstCharAndChar=new HashMap<String, Feature>();
		
		m_mapNorSepCharAndNextChar=new HashMap<String, Feature>();
		m_mapNorAppCharAndNextChar=new HashMap<String, Feature>();
		
		m_mapNorPartialWord=new HashMap<String, Feature>();
		m_mapNorPartialLengthByFirstChar=new HashMap<String, Feature>();
		m_mapNorLengthByTagAndFirstChar=new HashMap<String, Feature>();
		m_mapNorLengthByTagAndLastChar=new HashMap<String, Feature>();
		
		m_mapNorTag0Tag1Size1=new HashMap<String, Feature>();//t_1_t_0_len(w_1)
		m_mapNorTag1Tag2Size1=new HashMap<String, Feature>();//t_2_t_1_len(w_1)
		m_mapNorTag0Tag1Tag2Size1=new HashMap<String, Feature>();//t_1_t_0_len(w_1)
		
		 // feature templates knowledge
		m_mapNorTagByFirstCharCat=new HashMap<String, Feature>();//first_char_cat_0, tag_0
		m_mapNorTagByLastCharCat=new HashMap<String, Feature>();//last_char_cat_1, tag_1
		m_mapNorSeparateCharCat=new HashMap<String, Feature>();
		m_mapNorConsecutiveCharCat=new HashMap<String, Feature>();
		
		m_mapNorConsecutiveCharTagCat=new HashMap<String, Feature>();
		m_mapNorSeparateWordCat=new HashMap<String, Feature>();
		m_mapNorTagByCurWordCat=new HashMap<String, Feature>(); 
		
		
		//词义 feature templates 
		m_mapWordSense  =new HashMap<String, Feature>(); //w1s1
		m_mapLastWordAndWordSense  =new HashMap<String, Feature>(); //w1s1w0
		m_mapPreWordAndWordSense  =new HashMap<String, Feature>(); //w1s1w2
		m_mapStartPreAndWordSense  =new HashMap<String, Feature>(); //w1s1statr_w2
		m_mapEndPreAndWordSense  =new HashMap<String, Feature>(); //w1s1end_w2
		m_mapStartLastAndWordSense  =new HashMap<String, Feature>(); //w1s1start_w0
		m_mapLastCharAndWordSense  =new HashMap<String, Feature>(); //w1s1C0
		m_mapTagWordSense  =new HashMap<String, Feature>(); //w1s1t1
		m_mapPreTagAndWordSense  =new HashMap<String, Feature>(); //w1s1t2
		m_mapLastTagAndWordSense  =new HashMap<String, Feature>(); //w1s1t0
		m_mapThreeWordAndSense  =new HashMap<String, Feature>(); //w1s1w0w2
		m_mapPreLastTagAndWordSense  =new HashMap<String, Feature>(); //w1s1t0t2
		m_mapTwoWordSense  =new HashMap<String, Feature>(); //w1s1w2s2
		m_mapLastTagAndTwoWordSense  =new HashMap<String, Feature>(); //w1s1w2s2t0
		m_mapLastWordAndTwoWordSense  =new HashMap<String, Feature>(); //w1s1w2s2w0
		m_mapLastTagAndPreWordSense  =new HashMap<String, Feature>(); //w2s2t0
		m_mapLastWordAndPreWordSense  =new HashMap<String, Feature>(); //w2s2w0
		m_mapLastCharAndPreWordSense  =new HashMap<String, Feature>(); //w3s2c0
		
		m_mapGram2 = new HashMap<String, Feature>();
		m_mapGram3 = new HashMap<String, Feature>();
		m_mapGram4 = new HashMap<String, Feature>();
	}
	
	
	public void printWeight(BufferedWriter bw){
		try {
			//OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename, false),"UTF-8");
			 bw.write("begin" +"\r\n");	
			String strFea="";
			for(featureName f: featureName.values()){
				HashMap<String, Feature> hm= GetFeatureTemplate(f.toString());
				Iterator it= hm.keySet().iterator();
				  int n=0;
				  strFea="";
				  while (it.hasNext())
				  {
					   Object key=it.next();
					   if((n+1)%5 == 0){
						   n=0;
						   bw.write(strFea+"\r\n");
						   strFea="";		   
					   }
					   Feature tempf = hm.get(key);
					   strFea +=tempf.name+"#"+ tempf.weight+"#"+ tempf.sum +"#"+ tempf.lastUpdateIndex+"#"+tempf.aveWeight+" ";
					   n++;
				  }				  
			}			
			  bw.write(strFea.trim()+"\r\n");	
			  bw.write("end" + "\r\n");
			  //bw.close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
    
    
}

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import microblog.Feature.featureName;

public class Model {	
	public  HashMap<String, Set<String>> m_posCloseSet = new HashMap<String, Set<String>>();
	public  HashMap<String, Map<String, Integer>> m_wordPOSSets = new HashMap<String, Map<String, Integer>>();
	public  HashMap<String, Integer> m_wordFreq = new HashMap<String, Integer>();
	public  HashMap<String, Map<String, Integer>> m_startCharPOSSets = new HashMap<String, Map<String, Integer>>();
	public  HashMap<String, Integer> m_startCharFreq = new HashMap<String, Integer>();
	
	
	// feature templates abstd::cout characters
	public HashMap<String, Feature> m_mapCharUnigram=new HashMap<String, Feature>();   //C0
	public HashMap<String, Feature> m_mapCharBigram=new HashMap<String, Feature>();    //C-1C0
	public HashMap<String, Feature> m_mapCharTrigram=new HashMap<String, Feature>();   //C-2C-1C0
	
	// feature templates abstd::cout words	  
	public HashMap<String, Feature> m_mapSeenWords=new HashMap<String, Feature>();     //w_1
	public HashMap<String, Feature> m_mapLastWordByWord=new HashMap<String, Feature>(); //w-2w-1
	public HashMap<String, Feature> m_mapCurrentWordLastChar=new HashMap<String, Feature>();//w_1_end_w_2
	public HashMap<String, Feature> m_mapLastWordFirstChar=new HashMap<String, Feature>();//w_1_c_0
	public HashMap<String, Feature> m_mapFirstCharLastWordByWord=new HashMap<String, Feature>();//start_w_1_C_0
	public HashMap<String, Feature> m_mapLastWordByLastChar=new HashMap<String, Feature>();//w_1_c_0_t_1
	public HashMap<String, Feature> m_mapSeparateChars=new HashMap<String, Feature>();//end_w_1_c_0
	public HashMap<String, Feature> m_mapConsecutiveChars=new HashMap<String, Feature>();//char_bigram  for app
	public HashMap<String, Feature> m_mapFirstAndLastChars=new HashMap<String, Feature>(); //start_w_1end_w_1
	public HashMap<String, Feature> m_mapOneCharWord=new HashMap<String, Feature>();//w-1 if(len_w-1==1)
	public HashMap<String, Feature> m_mapLengthByFirstChar = new HashMap<String, Feature>();//start_w_1_len_w_1
	public HashMap<String, Feature> m_mapLengthByLastChar = new HashMap<String, Feature>();//end_w_1_len_w_1
	public HashMap<String, Feature> m_mapLengthByLastWord = new HashMap<String, Feature>();//w_2_len_w_1
	public HashMap<String, Feature> m_mapLastLengthByWord = new HashMap<String, Feature>();//w_1_len_w_2
	
	// feature templates tag	
	public HashMap<String, Feature> m_mapCurrentTag=new HashMap<String, Feature>(); //w_1_t_1
	public HashMap<String, Feature> m_mapLastTagByTag=new HashMap<String, Feature>(); //t-1t0
	public HashMap<String, Feature> m_mapLastTwoTagsByTag=new HashMap<String, Feature>(); //t-2t-1t0
	public HashMap<String, Feature> m_mapTagByLastWord=new HashMap<String, Feature>(); //w-1t0
	public HashMap<String, Feature> m_mapLastTagByWord=new HashMap<String, Feature>(); //w-1t-2
	public HashMap<String, Feature> m_mapTagByFirstChar=new HashMap<String, Feature>();//first_char_0, tag_0
	public HashMap<String, Feature> m_mapTagByLastChar=new HashMap<String, Feature>();//end_w_1_t_1
	public HashMap<String, Feature> m_mapTagByChar=new HashMap<String, Feature>();//(first_char_0, tag_0  for two action
	public HashMap<String, Feature> m_mapTagOfOneCharWord=new HashMap<String, Feature>();//end_w_2_w_1_c_0 if len_w_1=1 
	public HashMap<String, Feature> m_mapRepeatedCharByTag=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapTagByWordAndPrevChar=new HashMap<String, Feature>();//w_1_end_w_2_t_1
	public HashMap<String, Feature> m_mapTagByWordAndNextChar=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapTaggedCharByFirstChar=new HashMap<String, Feature>();// first_char char_unigram, tag_0 for app
	public HashMap<String, Feature> m_mapTaggedCharByLastChar=new HashMap<String, Feature>();//w_1的char与last_char
	
	// extra features
	public HashMap<String, Feature> m_mapTaggedSeparateChars=new HashMap<String, Feature>();//last_char_1, tag_1, first_char_0, tag_0
	public HashMap<String, Feature> m_mapTaggedConsecutiveChars=new HashMap<String, Feature>();//char_bigram, tag_0 for app
	
	public HashMap<String, Feature> m_mapWordTagTag=new HashMap<String, Feature>();//word_2, tag_0_tag_1
	public HashMap<String, Feature> m_mapTagWordTag=new HashMap<String, Feature>();//word_1, tag_0_tag_2
	public HashMap<String, Feature> m_mapFirstCharBy2Tags=new HashMap<String, Feature>();//first_char_0, tag_0_tag_1
	public HashMap<String, Feature> m_mapFirstCharBy3Tags=new HashMap<String, Feature>();//first_char_0, tag_0_tag_1_tag_2
	public HashMap<String, Feature> m_mapFirstCharAndChar=new HashMap<String, Feature>();
	
	public HashMap<String, Feature> m_mapSepCharAndNextChar=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapAppCharAndNextChar=new HashMap<String, Feature>();
	
	public HashMap<String, Feature> m_mapPartialWord=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapPartialLengthByFirstChar=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapLengthByTagAndFirstChar=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapLengthByTagAndLastChar=new HashMap<String, Feature>();
	
	public HashMap<String, Feature> m_mapTag0Tag1Size1=new HashMap<String, Feature>();//t_1_t_0_len(w_1)
	public HashMap<String, Feature> m_mapTag1Tag2Size1=new HashMap<String, Feature>();//t_2_t_1_len(w_1)
	public HashMap<String, Feature> m_mapTag0Tag1Tag2Size1=new HashMap<String, Feature>();//t_1_t_0_len(w_1)
	
	 // feature templates knowledge
	public HashMap<String, Feature> m_mapTagByFirstCharCat=new HashMap<String, Feature>();//first_char_cat_0, tag_0
	public HashMap<String, Feature> m_mapTagByLastCharCat=new HashMap<String, Feature>();//last_char_cat_1, tag_1
	public HashMap<String, Feature> m_mapSeparateCharCat=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapConsecutiveCharCat=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapConsecutiveCharTagCat=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapSeparateWordCat=new HashMap<String, Feature>();
	public HashMap<String, Feature> m_mapTagByCurWordCat=new HashMap<String, Feature>();
	
	// statistical information
	public HashMap<String, Integer> m_mapWordFrequency = new HashMap<String, Integer>();
	public HashMap<String, String> m_mapTagDictionary = new HashMap<String, String>();
	public HashMap<String, String> m_mapCharTagDictionary = new HashMap<String, String>();
	public HashMap<String, String> m_mapCanStart = new HashMap<String, String>();
	
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
	
	public void init(String filename){
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
					String theWord = tempStr.substring(0, index);
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
				}
			}
			
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//从文件中载入
	public void load(String filename){
		//features=new HashMap<String, Double>();
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
				if(temstrs[0].equals("weight") && temstrs.length == 5)
				{
					String name=temstrs[1].trim();
					double dweigth= Double.parseDouble(temstrs[2]);
					double dsum= Double.parseDouble(temstrs[3]);
					int iindex = Integer.parseInt(temstrs[4]);
					
					String[] names = name.split("=");								
					HashMap<String, Feature> hm= GetFeatureTemplate(names[0]);
					hm.put(name, new Feature(name, dweigth,dsum, iindex));	
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
    	for(String curFeature : oprFeatures){
    		//System.out.println(arrFeature[i].toString());
    		//if(arrFeature[i] == null)  return;    		
    		int _index = curFeature.indexOf("="); 
    	
    		String sTemplateName = curFeature.substring(0,_index);
    		HashMap<String, Feature>  hm = GetFeatureTemplate(sTemplateName); 		
    		Feature temp= hm.get(curFeature);
    		if(temp!=null){
    			if(temp.lastUpdateIndex<updateIndex){
    			    temp.sum += (updateIndex-temp.lastUpdateIndex-1)*temp.weight;    			    
    			}
    			temp.weight += iType;    				
    			temp.sum +=temp.weight;
    			temp.lastUpdateIndex = updateIndex;
    			hm.put(curFeature, temp);
    		}else{    			
    			hm.put(curFeature, new Feature(curFeature, (double)iType, (double)iType, updateIndex)); 			
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
    			   //tempf.aveWeight= tempf.sum/curRoundIndexForTrain;  
    			   //tempf.weight=tempf.aveWeight;
    		   }
    			tempf.aveWeight= tempf.sum/(curRoundIndexForTrain+1);  
    			
    			hm.put(key, tempf);

    		}   	
    	}
    }
    		
    	
    
    
    
    public HashMap<String, Feature> GetFeatureTemplate(String featureTemplate){    	
    	featureName aa = featureName.valueOf(featureName.class, featureTemplate);    	
    	switch(aa){    	
    		case CharUnigram : return m_mapCharUnigram; 
    		case CharBigram : return m_mapCharBigram; 
    		case CharTrigram : return m_mapCharTrigram; 
    		case SeenWords : return m_mapSeenWords; 
    		case LastWordByWord : return m_mapLastWordByWord;   
    		case CurrentWordLastChar : return m_mapCurrentWordLastChar; 
    		case LastWordFirstChar : return m_mapLastWordFirstChar; 
    		
    		case FirstCharLastWordByWord : return m_mapFirstCharLastWordByWord; 
    		case LastWordByLastChar : return m_mapLastWordByLastChar; 
    		case SeparateChars : return m_mapSeparateChars; 
    		case ConsecutiveChars : return m_mapConsecutiveChars; 
    		case FirstAndLastChars : return m_mapFirstAndLastChars; 
    		case OneCharWord : return m_mapOneCharWord; 
    		case LengthByFirstChar : return m_mapLengthByFirstChar; 
    		case LengthByLastChar : return m_mapLengthByLastChar; 
    		case LengthByLastWord : return m_mapLengthByLastWord; 
    		case LastLengthByWord : return m_mapLastLengthByWord; 
    		case CurrentTag : return m_mapCurrentTag; 
    		
    		case LastTagByTag : return m_mapLastTagByTag; 
    		case LastTwoTagsByTag : return m_mapLastTwoTagsByTag; 
    		case TagByLastWord : return m_mapTagByLastWord; 
    		case LastTagByWord : return m_mapLastTagByWord; 
    		case TagByFirstChar : return m_mapTagByFirstChar; 
    		case TagByLastChar : return m_mapTagByLastChar; 
    		case TagByChar : return m_mapTagByChar; 
    		case TagOfOneCharWord : return m_mapTagOfOneCharWord; 
    		case RepeatedCharByTag : return m_mapRepeatedCharByTag; 
    		case TagByWordAndPrevChar : return m_mapTagByWordAndPrevChar; 
    		case TagByWordAndNextChar : return m_mapTagByWordAndNextChar; 
    		case TaggedCharByFirstChar : return m_mapTaggedCharByFirstChar; 
    		case TaggedCharByLastChar : return m_mapTaggedCharByLastChar; 
    		case TaggedSeparateChars : return m_mapTaggedSeparateChars; 
    		case TaggedConsecutiveChars : return m_mapTaggedConsecutiveChars; 
    		case WordTagTag : return m_mapWordTagTag; 
    		case TagWordTag : return m_mapTagWordTag;
    		
    		case FirstCharBy2Tags : return m_mapFirstCharBy2Tags; 
    		case FirstCharBy3Tags : return m_mapFirstCharBy3Tags; 
    		case FirstCharAndChar : return m_mapFirstCharAndChar; 
    		case SepCharAndNextChar : return m_mapSepCharAndNextChar; 
    		case AppCharAndNextChar : return m_mapAppCharAndNextChar; 
    		case PartialLengthByFirstChar : return m_mapPartialLengthByFirstChar; 
    		case LengthByTagAndFirstChar : return m_mapLengthByTagAndFirstChar; 
    		case LengthByTagAndLastChar : return m_mapLengthByTagAndLastChar; 
    		case Tag0Tag1Size1 : return m_mapTag0Tag1Size1; 
    		case Tag1Tag2Size1 : return m_mapTag1Tag2Size1; 
    		case Tag0Tag1Tag2Size1 : return m_mapTag0Tag1Tag2Size1; 
    		case TagByFirstCharCat : return m_mapTagByFirstCharCat; 
    		case TagByLastCharCat : return m_mapTagByLastCharCat; 
    		case SeparateCharCat : return m_mapSeparateCharCat; 
    		case ConsecutiveCharCat : return m_mapConsecutiveCharCat;  
    		case PartialWord: return m_mapPartialWord;
    		case ConsecutiveCharTagCat: return m_mapConsecutiveCharTagCat;
    		case SeparateWordCat: return m_mapSeparateWordCat;
    		case TagByCurWordCat: return m_mapTagByCurWordCat;    	
    	}
    	return null;
    	
    }
    public enum featureName{
    	CharUnigram,CharBigram,CharTrigram,SeenWords,LastWordByWord,CurrentWordLastChar,LastWordFirstChar,
    	FirstCharLastWordByWord,LastWordByLastChar,SeparateChars,ConsecutiveChars,FirstAndLastChars,
    	OneCharWord,LengthByFirstChar,LengthByLastChar,LengthByLastWord,LastLengthByWord,CurrentTag,  
    	LastTagByTag,  LastTwoTagsByTag,TagByLastWord,LastTagByWord,TagByFirstChar,TagByLastChar,TagByChar,
    	TagOfOneCharWord,RepeatedCharByTag, TagByWordAndPrevChar,TagByWordAndNextChar,TagWordTag,
    	TaggedCharByFirstChar,TaggedCharByLastChar,TaggedSeparateChars,TaggedConsecutiveChars,WordTagTag,
    	FirstCharBy2Tags,FirstCharBy3Tags,FirstCharAndChar,SepCharAndNextChar,AppCharAndNextChar, PartialWord,
    	PartialLengthByFirstChar,LengthByTagAndFirstChar,LengthByTagAndLastChar,Tag0Tag1Size1,
    	Tag1Tag2Size1,Tag0Tag1Tag2Size1,TagByFirstCharCat,TagByLastCharCat,SeparateCharCat,ConsecutiveCharCat,
    	ConsecutiveCharTagCat, SeparateWordCat,TagByCurWordCat;
    }
    
  //instantiation features
	public void newFeatureTemplates(){
		// feature templates abstd::cout characters
		m_mapCharUnigram=new HashMap<String, Feature>();   //C0
		m_mapCharBigram=new HashMap<String, Feature>();    //C-1C0
		m_mapCharTrigram=new HashMap<String, Feature>();   //C-2C-1C0
		
		// feature templates abstd::cout words	  
		m_mapSeenWords=new HashMap<String, Feature>();     //w_1
		m_mapLastWordByWord=new HashMap<String, Feature>(); //w-2w-1
		m_mapCurrentWordLastChar=new HashMap<String, Feature>();//w_1_end_w_2
		m_mapLastWordFirstChar=new HashMap<String, Feature>();//w_1_c_0
		m_mapFirstCharLastWordByWord=new HashMap<String, Feature>();//start_w_1_C_0
		m_mapLastWordByLastChar=new HashMap<String, Feature>();//w_1_c_0_t_1
		m_mapSeparateChars=new HashMap<String, Feature>();//end_w_1_c_0
		m_mapConsecutiveChars=new HashMap<String, Feature>();//char_bigram  for app
		m_mapFirstAndLastChars=new HashMap<String, Feature>(); //start_w_1end_w_1
		m_mapOneCharWord=new HashMap<String, Feature>();//w-1 if(len_w-1==1)
		m_mapLengthByFirstChar = new HashMap<String, Feature>();//start_w_1_len_w_1
		m_mapLengthByLastChar = new HashMap<String, Feature>();//end_w_1_len_w_1
		m_mapLengthByLastWord = new HashMap<String, Feature>();//w_2_len_w_1
		m_mapLastLengthByWord = new HashMap<String, Feature>();//w_1_len_w_2
		
		// feature templates tag	
		m_mapCurrentTag=new HashMap<String, Feature>(); //w_1_t_1
		m_mapLastTagByTag=new HashMap<String, Feature>(); //t-1t0
		m_mapLastTwoTagsByTag=new HashMap<String, Feature>(); //t-2t-1t0
		m_mapTagByLastWord=new HashMap<String, Feature>(); //w-1t0
		m_mapLastTagByWord=new HashMap<String, Feature>(); //w-1t-2
		m_mapTagByFirstChar=new HashMap<String, Feature>();//first_char_0, tag_0
		m_mapTagByLastChar=new HashMap<String, Feature>();//end_w_1_t_1
		m_mapTagByChar=new HashMap<String, Feature>();//(first_char_0, tag_0  for two action
		m_mapTagOfOneCharWord=new HashMap<String, Feature>();//end_w_2_w_1_c_0 if len_w_1=1 
		m_mapRepeatedCharByTag=new HashMap<String, Feature>();
		m_mapTagByWordAndPrevChar=new HashMap<String, Feature>();//w_1_end_w_2_t_1
		m_mapTagByWordAndNextChar=new HashMap<String, Feature>();
		m_mapTaggedCharByFirstChar=new HashMap<String, Feature>();// first_char char_unigram, tag_0 for app
		m_mapTaggedCharByLastChar=new HashMap<String, Feature>();//w_1的char与last_char
		
		// extra features
		m_mapTaggedSeparateChars=new HashMap<String, Feature>();//last_char_1, tag_1, first_char_0, tag_0
		m_mapTaggedConsecutiveChars=new HashMap<String, Feature>();//char_bigram, tag_0 for app
		
		m_mapWordTagTag=new HashMap<String, Feature>();//word_2, tag_0_tag_1
		m_mapTagWordTag=new HashMap<String, Feature>();//word_1, tag_0_tag_2
		m_mapFirstCharBy2Tags=new HashMap<String, Feature>();//first_char_0, tag_0_tag_1
		m_mapFirstCharBy3Tags=new HashMap<String, Feature>();//first_char_0, tag_0_tag_1_tag_2
		m_mapFirstCharAndChar=new HashMap<String, Feature>();
		
		m_mapSepCharAndNextChar=new HashMap<String, Feature>();
		m_mapAppCharAndNextChar=new HashMap<String, Feature>();
		
		m_mapPartialWord=new HashMap<String, Feature>();
		m_mapPartialLengthByFirstChar=new HashMap<String, Feature>();
		m_mapLengthByTagAndFirstChar=new HashMap<String, Feature>();
		m_mapLengthByTagAndLastChar=new HashMap<String, Feature>();
		
		m_mapTag0Tag1Size1=new HashMap<String, Feature>();//t_1_t_0_len(w_1)
		m_mapTag1Tag2Size1=new HashMap<String, Feature>();//t_2_t_1_len(w_1)
		m_mapTag0Tag1Tag2Size1=new HashMap<String, Feature>();//t_1_t_0_len(w_1)
		
		 // feature templates knowledge
		m_mapTagByFirstCharCat=new HashMap<String, Feature>();//first_char_cat_0, tag_0
		m_mapTagByLastCharCat=new HashMap<String, Feature>();//last_char_cat_1, tag_1
		m_mapSeparateCharCat=new HashMap<String, Feature>();
		m_mapConsecutiveCharCat=new HashMap<String, Feature>();
		
		m_mapConsecutiveCharTagCat=new HashMap<String, Feature>();
		m_mapSeparateWordCat=new HashMap<String, Feature>();
		m_mapTagByCurWordCat=new HashMap<String, Feature>(); 	
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

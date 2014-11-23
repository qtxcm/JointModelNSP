package microblog;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


public class Evaluator {
	 public BufferedWriter bw ;
	 public BufferedWriter bwlog;
	 public String error_file;
     public List<String> arrTestResult;
     public List<String> arrTestStand;    
     int iSegCorrect=0, iSegPred=0, iSegGold=0;
     int iTagCorrect=0, iTagPred=0, iTagGold=0;
     
 	private static class Sentence {
		String[] words;
		String[] poss;
		String chars;
	}
     
     public Evaluator(){    	 
     }
     public Evaluator(List<String> arrTestResult, List<String> arrTestStand, BufferedWriter bwlog, String error_file){
    	 this.arrTestResult=arrTestResult;
    	 this.arrTestStand=arrTestStand;   
    	 this.error_file = error_file;
    	 this.bwlog = bwlog;
    	 
    	FileWriter fw;
  		try {
  			fw = new FileWriter(error_file);
  			bw = new BufferedWriter(fw); // 将缓冲对文件的输出  			
  		} catch (IOException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}// 创建FileWriter对象，用来写入字符流
  		
     }
     
     public void Computer(){
    	 for(int i=0; i<arrTestStand.size();i++){
    		 String sStand = arrTestStand.get(i);
    		 String sResult= arrTestResult.get(i);    		 
    		 compareTwoSequence(sStand, sResult);   		 
    	 }
    	 
    	 Save();
    	 try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     }
     
     public void Save(){
    	 float segP=(float) (iSegCorrect*1.0/iSegPred);
    	 float segR=(float) (iSegCorrect*1.0/iSegGold);
     	 float segF=(float) (2.0*iSegCorrect/(iSegPred+iSegGold));
     	 float tagP=(float) (iTagCorrect*1.0/iTagPred);
   	     float tagR=(float) (iTagCorrect*1.0/iTagGold);
    	 float tagF=(float) (2.0*iTagCorrect/(iTagPred+iTagGold));
    	 try {
			bwlog.write("segmentation result: precise="+ segP + "     recall rate="+ segR + "   F="+ segF + "\r\n");
			bwlog.write("segmentation result: precise="+ tagP + "     recall rate="+ tagR + "   F="+ tagF + "\r\n");
		    	
    	 } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 
    	
 		
     }
     
     public void compareTwoSequence(String sStand, String sResult){
    	 if(sStand.equals(sResult) == false){
    		 try {
    			 bw.write("stand :" + sStand +"\r\n" );    		
    			 bw.write("result:" + sResult +"\r\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	 }

		 
		 Sentence goldSentence = TagSentConvertSentence(sStand);
		 Sentence predSentence = TagSentConvertSentence(sResult);
		 
		 
		 int[] evalRes = reco(goldSentence, predSentence);
		 
	    iSegCorrect += evalRes[2];
	    iTagCorrect += evalRes[3];
	    iSegPred += evalRes[0];
	    iSegGold += evalRes[1];
	    iTagPred += evalRes[0];
	    iTagGold += evalRes[1];

		return;   	 
     }
     
     
	public static int[] reco(Sentence goldSentence, Sentence predSentence) {
		// seg: 0 goldWords 1 predWords
		// seg: 2 recoWords 
		// tag: 3 recoPos 
		int[] predRes = new int[4];

		for (int i = 0; i < 4; i++) {
			predRes[i] = 0;
		}

		String[] goldWords = goldSentence.words;
		String[] goldLabels = goldSentence.poss;
		String[] predWords = predSentence.words;
		String[] predLabels = predSentence.poss;

		int m = 0, n = 0;
		for (int i = 0; i < goldWords.length; i++) {
			predRes[0]++;
		}

		for (int i = 0; i < predWords.length; i++) {
			predRes[1]++;
		}

		while (m < predWords.length && n < goldWords.length) {
			if (predWords[m].equals(goldWords[n])) {
				predRes[2]++;
				boolean bTagMatch = false;
				if (predLabels[m].equals(goldLabels[n])) {
					bTagMatch = true;
					predRes[3]++;
				}
				m++;
				n++;
			} else {
				int lgold = goldWords[n].length();
				int lpred = predWords[m].length();
				int lm = m + 1;
				int ln = n + 1;
				int sm = m;
				int sn = n;

				while (lm < predWords.length || ln < goldWords.length) {
					if (lgold > lpred && lm < predWords.length) {
						lpred = lpred + predWords[lm].length();
						sm = lm;
						lm++;
					} else if (lgold < lpred && ln < goldWords.length) {
						lgold = lgold + goldWords[ln].length();
						sn = ln;
						ln++;
					} else {
						break;
					}
				}

				m = sm + 1;
				n = sn + 1;
			}
		}
		return predRes;
	}

	
    // no usage except for valuation
    
    public  static Sentence TagSentConvertSentence(String tagSequence){
    	Sentence sent= new Sentence();		
		String[] wordposses = tagSequence.split("\\s+");
		sent.poss = new String[wordposses.length];
		sent.words = new String[wordposses.length];
		sent.chars = "";
		for(int idx = 0; idx < wordposses.length; idx++)
		{
			int index = wordposses[idx].indexOf("_");
			sent.words[idx] = wordposses[idx].substring(0, index);
			sent.poss[idx] = wordposses[idx].substring(index+1);
			sent.chars = sent.chars + sent.words[idx];

		}
		
		return sent;	
	}

}

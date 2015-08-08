package microblog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

public class Evaluator {
	public BufferedWriter bw;
	public BufferedWriter bwlog;
	public String error_file;
	public List<String> arrTestResult;
	public List<String> arrTestStand;
	int iSegCorrect = 0, iSegPred = 0, iSegGold = 0;
	int iTagCorrect = 0, iTagPred = 0, iTagGold = 0;
	int iNormalCorrect = 0, iNormalPred = 0, iNormalGold = 0;

	private static class Sentence {
		String[] words;
		String[] poss;
		String[] senses;
		String chars;
	}

	public Evaluator() {
	}

	public Evaluator(List<String> arrTestResult, List<String> arrTestStand,
			BufferedWriter bwlog, String error_file) {
		this.arrTestResult = arrTestResult;
		this.arrTestStand = arrTestStand;
		this.error_file = error_file;
		this.bwlog = bwlog;

		FileWriter fw;
		try {
			fw = new FileWriter(error_file);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void Computer() {
		for (int i = 0; i < arrTestStand.size(); i++) {
			String sStand = arrTestStand.get(i);
			String sResult = arrTestResult.get(i);
			// if(sStand.length()>0 && sResult.length()>0){
			compareTwoSequence(sStand, sResult);
			// }
		}
		Save();
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * save result.
	 */
	public void Save() {
		float segP = (float) (iSegCorrect * 1.0 / iSegPred);
		float segR = (float) (iSegCorrect * 1.0 / iSegGold);
		float segF = (float) (2.0 * iSegCorrect / (iSegPred + iSegGold));
		float tagP = (float) (iTagCorrect * 1.0 / iTagPred);
		float tagR = (float) (iTagCorrect * 1.0 / iTagGold);
		float tagF = (float) (2.0 * iTagCorrect / (iTagPred + iTagGold));
		float normalP = (float) (iNormalCorrect * 1.0 / iNormalPred);
		float normalR = (float) (iNormalCorrect * 1.0 / iNormalGold);
		float normalF = (float) (2.0 * iNormalCorrect / (iNormalPred + iNormalGold));

		try {
			bwlog.write("segmentation result: precise=" + segP
					+ "     recall rate=" + segR + "   F=" + segF + "\r\n");
			bwlog.write("pos          result: precise=" + tagP
					+ "     recall rate=" + tagR + "   F=" + tagF + "\r\n");
			bwlog.write("Normalization result: precise=" + normalP
					+ "     recall rate=" + normalR + "   F=" + normalF
					+ "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * compared gold standard with segmented sentence.
	 * 
	 * @param sStand
	 * @param sResult
	 */
	public void compareTwoSequence(String sStand, String sResult) {
		Sentence goldSentence = TagSentConvertSentence(sStand);
		Sentence predSentence = TagSentConvertSentence(sResult);
		int[] evalRes = reco(goldSentence, predSentence);
		iSegCorrect += evalRes[2];
		iTagCorrect += evalRes[3];
		iSegPred += evalRes[1];
		iSegGold += evalRes[0];
		iTagPred += evalRes[1];
		iTagGold += evalRes[0];
		iNormalCorrect += evalRes[6];
		iNormalPred += evalRes[5];
		iNormalGold += evalRes[4];
		try {
			String temresult = resultProcess(sResult);
			if (sStand.equals(temresult) == false) {
				bw.write("stand :" + sStand + "\r\n");
				bw.write("result:" + temresult + "\r\n");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	public static int[] reco(Sentence goldSentence, Sentence predSentence) {
		// seg: 0 goldWords 1 predWords
		// seg: 2 recoWords
		// tag: 3 recoPos
		// tag: 4 goldSenses 5 predWords 6 recoSenses
		// 7:是否相等， 0：相等，1：不等
		int[] predRes = new int[8];

		for (int i = 0; i < 8; i++) {
			predRes[i] = 0;
		}

		String[] goldWords = goldSentence.words;
		String[] goldLabels = goldSentence.poss;
		String[] predWords = predSentence.words;
		String[] predLabels = predSentence.poss;
		String[] goldSenses = goldSentence.senses;
		String[] predSenses = predSentence.senses;
		int m = 0, n = 0;
		for (int i = 0; i < goldWords.length; i++) {
			predRes[0]++;
			if (goldSenses[i] != null && goldSenses[i].length() > 0) {
				predRes[4]++;
			}
		}

		for (int i = 0; i < predWords.length; i++) {
			predRes[1]++;
			if (predSenses[i] != null && predSenses[i].length() > 0) {
				predRes[5]++;
			}
		}
		boolean bequal = true;
		while (m < predWords.length && n < goldWords.length) {
			if (predWords[m].equals(goldWords[n])) {
				predRes[2]++;
				if (predSenses[m].length() > 0
						&& predSenses[m].equals(goldSenses[n])) {
					predRes[6]++;
				} else {
					if (bequal == true)
						bequal = false;
				}
				boolean bTagMatch = false;
				if (predLabels[m].equals(goldLabels[n])) {
					bTagMatch = true;
					predRes[3]++;
				} else {
					if (bequal == true)
						bequal = false;
				}
				m++;
				n++;
			} else {
				if (bequal == true)
					bequal = false;
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
		if (bequal == true)
			predRes[7] = 0;
		else
			predRes[7] = 1;
		return predRes;
	}

	public static Sentence TagSentConvertSentence(String tagSequence) {
		Sentence sent = new Sentence();
		if (tagSequence.trim().length() < 1)
			return sent;
		String[] wordposses = tagSequence.split("\\s+");
		// StringTokenizer st=new StringTokenizer(tagSequence," ");
		sent.poss = new String[wordposses.length];
		sent.words = new String[wordposses.length];
		sent.senses = new String[wordposses.length];
		sent.chars = "";
		for (int idx = 0; idx < wordposses.length; idx++) {
			int index = wordposses[idx].indexOf("_");
			String wordSense = wordposses[idx].substring(0, index);
			int wordIndex = wordSense.indexOf("|");
			if (wordIndex >= 0) {
				String temword = wordSense.substring(0, wordIndex);
				String temsense = wordSense.substring(wordIndex + 1);
				if (temword.equals(temsense)) {
					sent.words[idx] = temword;
					sent.senses[idx] = "";
				} else {
					sent.words[idx] = temword;
					sent.senses[idx] = temsense;
				}
			} else {
				sent.words[idx] = wordSense;
				sent.senses[idx] = "";
			}
			sent.poss[idx] = wordposses[idx].substring(index + 1);
			sent.chars = sent.chars + sent.words[idx];
		}

		return sent;
	}

	public static Sentence TagSentConvertSentenceSelf(String tagSequence) {
		Sentence sent = new Sentence();
		if (tagSequence.trim().length() < 1)
			return sent;
		String[] wordposses = tagSequence.split("\\s+");
		sent.poss = new String[wordposses.length];
		sent.words = new String[wordposses.length];
		sent.senses = new String[wordposses.length];
		sent.chars = "";
		for (int idx = 0; idx < wordposses.length; idx++) {
			int index = wordposses[idx].indexOf("_");
			String wordSense = wordposses[idx].substring(0, index);
			int wordIndex = wordSense.indexOf("|");
			if (wordIndex >= 0) {
				sent.words[idx] = wordSense.substring(0, wordIndex);
				sent.senses[idx] = wordSense.substring(wordIndex + 1);
			} else {
				sent.words[idx] = wordSense;
				sent.senses[idx] = wordSense;
			}
			sent.poss[idx] = wordposses[idx].substring(index + 1);
			sent.chars = sent.chars + sent.words[idx];
		}

		return sent;
	}

	public static void main(String[] args) {
		String standfile = "F:\\0test\\corpus\\712CTB\\CTBtest.pos";
		String resultfile = "F:\\0test\\temp\\out33Poszpar";
		BufferedWriter bwlog;
		String errorfile = "F:\\0test\\temp\\out33Poszparerror";
		List<String> arrTestSource = new ArrayList<String>();
		List<String> arrTestResult = new ArrayList<String>();
		File infile = new File(standfile);
		BufferedInputStream infis;
		File outfile = new File(resultfile);
		BufferedInputStream outfis;
		try {
			bwlog = new BufferedWriter(new FileWriter(
					"F:\\0test\\temp\\out33Poszparlog"));
			infis = new BufferedInputStream(new FileInputStream(standfile));
			BufferedReader inreader = new BufferedReader(new InputStreamReader(
					infis, "UTF8"));
			String line = "";
			while ((line = inreader.readLine()) != null) {
				if (line.trim().length() > 0) {
					arrTestSource.add(line.trim());
				}
			}
			outfis = new BufferedInputStream(new FileInputStream(resultfile));
			BufferedReader outReader = new BufferedReader(
					new InputStreamReader(outfis, "UTF8"));
			line = "";
			while ((line = outReader.readLine()) != null) {
				if (line.trim().length() > 0) {
					arrTestResult.add(line.trim());
				}
			}
			Evaluator ob = new Evaluator(arrTestResult, arrTestSource, bwlog,
					errorfile);
			ob.Computer();
			bwlog.close();
			infis.close();
			outfis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String resultProcess(String result) {
		String rent = "";
		StringTokenizer st = new StringTokenizer(result, " ");
		while (st.hasMoreElements()) {
			String wordposses = st.nextToken();
			int index = wordposses.indexOf("_");
			String wordSense = wordposses.substring(0, index);
			int wordIndex = wordSense.indexOf("|");

			if (wordIndex >= 0) {
				String temword = wordSense.substring(0, wordIndex);
				String temsense = wordSense.substring(wordIndex + 1);
				if (temword.equals(temsense)) {
					rent += temword;
				} else {
					rent += temword + "|" + temsense;
				}
			} else {
				rent += wordSense;
			}
			rent += "_" + wordposses.substring(index + 1) + " ";
		}
		return rent.trim();
	}
}

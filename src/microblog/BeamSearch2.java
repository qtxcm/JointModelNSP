package microblog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import util.POSStatistic;

import microblog.State;

public class BeamSearch2 {
	// 传参文件
	public Double MINVALUE = -65555.0;
	public String train_file = "";
	public String test_file = ""; // 与训练文件格式相同，作为黄金标准
	public String output_file = "out";
	public String model_file = "";
	public String evaluationError_file = "erresult"; //作为
	public int number_of_iterations = 10;// training number;
	public boolean bNewTrain = true;
	public String output_path = "";
	public int number_of_test = 0;
	public int number_of_train = 0;
	public int search_width = 16; // 搜索宽度
	public String charPos_file = "";
	public String wordPos_file = "";
	
	//
	public String[] arrTrainSource; // train sentences
	public String[][] arrCurSentecFeature; // 当前句子特征
	public State[] bestStates;// 当前最好的K个状态。
	public State GoldState;// 标准状态

	public String[] arrTestSource; // test sentences

	int curRoundIndexForTrain = 0;
	public Model model = new Model(); // 特征model;存储特征及权重
	public String[] arrTestResult;

    public HashMap<String, String> hmChar = new HashMap<String, String>();//字符词性map
    public HashMap<String, String> hmWord = new HashMap<String, String>();//单词词性map
	
	private String CurSentence = "";// 待处理的句子
	// private int lenofSentence=0;//待处理的句子长度
	public static String[] arrPOS = { "VA", "VC", "VE", "VV", "NR", "NT", "NN",
			"LC", "PN", "DT", "CD", "OD", "M", "AD", "P", "CC", "CS", "DEC",
			"DEG", "DER", "DEV", "AS", "SP", "ETC", "MSP", "IJ", "ON", "LB",
			"SB", "BA", "JJ", "FW", "PU" };
	public State[] agenda;// 当前标注序列集

	public BufferedWriter bwlog;
	public SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss SSS ");// 设置日期格式
	HeapSort heapSort = new HeapSort();

	/**
	 * initial training
	 * 
	 * @param train_file
	 *            已标注文件
	 * @param model_file
	 * @param number_of_iterations
	 * @param bNewTrain
	 *            新的训练
	 */
	public BeamSearch2(String train_file, int number_of_train,
			String model_file, int number_of_iterations, boolean bNewTrain,
			int search_width, String outfile_path) {
		this.train_file = train_file;
		this.number_of_train = number_of_train;
		this.model_file = model_file;
		this.number_of_iterations = number_of_iterations;
		this.bNewTrain = bNewTrain;
		this.search_width = search_width;
		this.output_path = output_path;

		try {
			bwlog = new BufferedWriter(new FileWriter(output_path + "log.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BeamSearch2() {
	}

	/**
	 * initial training
	 * 
	 * @param test_file
	 *            已标注测试文件与训练文件格式相同
	 * @param model_file
	 * @param output_file
	 *            最终测试结果
	 */
	public BeamSearch2(String test_file, int number_of_test, String model_file,
			String output_file, String evaluationError_file, int search_width) {
		this.test_file = test_file;
		this.number_of_test = number_of_test;
		this.model_file = model_file;
		this.output_file = output_file;
		this.evaluationError_file = evaluationError_file;
		this.search_width = search_width;
		try {
			bwlog = new BufferedWriter(new FileWriter(output_path + "log.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BeamSearch2(String train_file, int number_of_train,
			String model_file, int number_of_iterations, boolean bNewTrain,
			int search_width, String test_file, int number_of_test,
			String output_path, String charPos_file, String wordPos_file) {
		this.train_file = train_file;
		this.number_of_train = number_of_train;
		this.model_file = model_file;
		this.number_of_iterations = number_of_iterations;
		this.bNewTrain = bNewTrain;
		this.search_width = search_width;
		this.test_file = test_file;
		this.number_of_test = number_of_test;
		this.output_path = output_path;
		this.charPos_file = charPos_file;
		this.wordPos_file = wordPos_file;

		try {
			bwlog = new BufferedWriter(new FileWriter(output_path + "log.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void trainTestProcess() throws Exception {

		//System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
		initialWordPOS();
		initialTrain();
		initialTest();
		curRoundIndexForTrain = 0;
		for (int n = 0; n < number_of_iterations; n++) {
			bwlog.write("train round begin:" + n + "  start " + df.format(new Date())+"\r\n");
			for (int i = 0; i < this.number_of_train; i++) {
				GoldState = TagSeConvertState(this.arrTrainSource[i]);
				this.arrCurSentecFeature = GetFeatureBySentence(this.arrTrainSource[i]);

				// bwlog.write("第"+n+"次循环第"+ i + "个句子 训练开始： "+
				// this.goldTagSquence.toString() + "\r\n");
				trainer(n, i);
				// bwlog.write("第"+n+"次循环第"+ i + "个句子 训练结束： "+
				// this.goldTagSquence.toString() + "\r\n");
			}
			model.AveWeight(curRoundIndexForTrain);
			//model.save(output_path + model_file + n);
			bwlog.write("train round end:"+ n+"   " +df.format(new Date())+"\r\n");
			bwlog.write("develop start:"+"\r\n");
			// 测试
			for (int i = 0; i < arrTestSource.length; i++) {

				this.CurSentence = TagSeConvertState(arrTestSource[i])
						.GetSentence();
				this.arrTestResult[i] = Decoder();
			}
			//save(this.arrTestResult, output_path + this.output_file + n);
			Evaluator eva = new Evaluator(this.arrTestResult, arrTestSource, bwlog, this.output_path+evaluationError_file+n);
			eva.Computer();
			// curRoundIndexForTrain=1;
			bwlog.write("develop end:"+"\r\n");
			bwlog.flush();
		}
		model.save(model_file);
		bwlog.flush();
		bwlog.close();
	}

	/**
	 * 对输入的语料作Train
	 * 
	 * @throws Exception
	 */
	public void trainProcess() throws Exception {
		initialTrain();
		for (int n = 0; n < number_of_iterations; n++) {
			for (int i = 0; i < arrTrainSource.length; i++) {
				GoldState = TagSeConvertState(this.arrTrainSource[i]);
				this.arrCurSentecFeature = GetFeatureBySentence(this.arrTrainSource[i]);
				trainer(n, i);
			}
			System.out.println("Iterator:" + n);
		}
		model.save(model_file);
	}

	/**
	 * 测试
	 */
	public void testProcess() {
		try {
			initialTest();
			for (int i = 0; i < arrTestSource.length; i++) {

				this.CurSentence = TagSeConvertState(arrTestSource[i])
						.GetSentence();
				this.arrTestResult[i] = Decoder();
			}
			save(this.arrTestResult, this.output_file);
			Evaluator eva = new Evaluator(this.arrTestResult, arrTestSource, bwlog, this.output_path+ this.evaluationError_file);
			eva.Computer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initialWordPOS(){
		POSStatistic ob = new POSStatistic(this.output_path+this.charPos_file, this.output_path+this.wordPos_file);
		ob.loadCharAndWordPos(hmChar, hmWord);
	}
	private void initialTest() {
		this.arrTestSource = new String[this.number_of_test];
		this.arrTestResult = new String[this.number_of_test];
		// this.model.load(model_file);
		File file = new File(this.test_file);
		BufferedInputStream fis;
		try {
			fis = new BufferedInputStream(new FileInputStream(file));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis, "utf-8"), 5 * 1024 * 1024);// 用50M的缓冲读取文本文件
			String line = "";
			int i = 0;			
			while ((line = reader.readLine()) != null) {
				if(i>=this.number_of_test) break;
				if (line.trim().length() > 0) {
					this.arrTestSource[i] = line.trim() + " #_PU";
					i++;
				}
			}
			reader.close();
			fis.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 训练初始化
	 */
	private void initialTrain() {
		this.arrTrainSource = new String[this.number_of_train];
		if (this.bNewTrain == true) {
			this.model.newFeatureTemplates();
		} else {
			this.model.load(model_file);
		}
		File file = new File(this.train_file);
		BufferedInputStream fis;
		try {
			fis = new BufferedInputStream(new FileInputStream(file));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis, "utf-8"), 5 * 1024 * 1024);// 用50M的缓冲读取文本文件
			String line = "";
			int i = 0;
			
			while ((line = reader.readLine()) != null) {
				if(i>=this.number_of_train) break;
				if (line.trim().length() > 0) {
					this.arrTrainSource[i] = line.trim() + " #_PU";
					i++;
				}
			}
			reader.close();
			fis.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			
		}
	}

	/**
	 * @throws Exception
	 * 
	 */
	public void trainer(int round, int sentenceIndex) throws Exception {
		this.agenda = new State[this.search_width];
		this.agenda[0] = new State();this.agenda[0].bIsGold=true;
		// curRoundIndexForTrain++;

		curRoundIndexForTrain  ++;
		System.out.println("setnece:" + sentenceIndex + " begin:");// +df.format(newDate())); //new Date()为获取当前系统时间
		long st1 = System.nanoTime();

		for (int i = 0; i < this.CurSentence.length(); i++) {
			String curSChar = String.valueOf(CurSentence.charAt(i));
			//bwlog.write("第 " + i + "数符处理:" + curSChar + "\r\n");
			int agendaLen = 1;
			if (i == 0)
				agendaLen = 1;
			else
				agendaLen = agenda.length;

			State[] temAgenda = new State[this.search_width];
			for (int o = 0; o < this.search_width; o++) {
				temAgenda[o] = new State();
				temAgenda[o].score = MINVALUE;
				temAgenda[o].bIsGold = false;
			}
			long st2 = System.nanoTime();
			for (int j = 0; j < agendaLen; j++) {
				State state = agenda[j];

				if (i > 0  && i< this.CurSentence.length()-1) {
					if(canAction(state, curSChar, state.arrTag[state.size-1], 1) == true){					
						State tempCands = Append(state, curSChar, true); // append action
						if (tempCands.score > temAgenda[0].score) {
							if (state.bIsGold == true
									&& tempCands.arrTag[tempCands.size - 1]
											.equals(this.GoldState.arrTag[tempCands.size-1])
									&& this.GoldState.arrWord[tempCands.size - 1]
											.indexOf(tempCands.arrWord[tempCands.size - 1]) == 0) {
								tempCands.bIsGold = true;
							} else
								tempCands.bIsGold = false;
							heapSort.BestAgendaSort(temAgenda, tempCands);
						}
					}
				
				}
				if(i== this.CurSentence.length()-1){//最后一个伪字符 #_PU
					State temCand = Sep(state, curSChar, "PU", true);// seperate action
					if (temCand.score >= temAgenda[0].score) {
						if (state.bIsGold == true
								&& temCand.arrTag[temCand.size - 1]
										.equals(this.GoldState.arrTag[temCand.size - 1])
								&& this.GoldState.arrWord[temCand.size - 1]
										.indexOf(temCand.arrWord[temCand.size - 1]) == 0) {
							temCand.bIsGold = true;
						} else
							temCand.bIsGold = false;
						heapSort.BestAgendaSort(temAgenda, temCand);
					}
				}else {
					for (int k = 0; k < arrPOS.length; k++) {
						if(canAction(state, curSChar,arrPOS[k], 0) == true){		
							State temCand = Sep(state, curSChar, arrPOS[k], true);// seperate action
							if (temCand.score >= temAgenda[0].score) {
								if (state.bIsGold == true
										&& temCand.arrTag[temCand.size - 1]
												.equals(this.GoldState.arrTag[temCand.size - 1])
										&& this.GoldState.arrWord[temCand.size - 1]
												.indexOf(temCand.arrWord[temCand.size - 1]) == 0) {
									temCand.bIsGold = true;
								} else
									temCand.bIsGold = false;
								heapSort.BestAgendaSort(temAgenda, temCand);
							}
						}
					}
				}
			}
			//System.out.println("1:" + (System.nanoTime() - st2));
			st2 = System.nanoTime();
			this.agenda = temAgenda;
			// 构造标准的部分序列
			boolean bEqual = false;
			for (int m = 0; m < this.agenda.length; m++) {
				if (this.agenda[m].bIsGold == true)
				{
					bEqual = true; break;
				}
			}

			if (bEqual == false)// 没有标注序列与黄金标准相同
			{
				for (int p = 0; p <= i; p++) {
					model.UpdateWeighth(this.arrCurSentecFeature[p], 1,	curRoundIndexForTrain);// 标准权重增加
				}
				//model.printWeight(bwlog);

				State temState = Best(this.agenda, 1)[0];
				String[][] bestFeatures = new String[i + 1][50];
				bestFeatures = this.GetFeatureBySentence(temState.toString());
				for (int p = 0; p <= i; p++) {
					model.UpdateWeighth(bestFeatures[p], -1,
							curRoundIndexForTrain);
				}
				//System.out.println("2:" + (System.nanoTime() - st2));

				return;				
			}			
		}
		

		//System.out.println("" + (System.nanoTime() - st1));
		long st2 = System.nanoTime();
		this.agenda = Best(this.agenda, 1);
		if (this.agenda[0].bIsGold == false) {// 最终的结果与标准不相符
			String[][] bestFeatures = new String[this.CurSentence.length()][50];
			bestFeatures = this.GetFeatureBySentence(this.agenda[0].toString());
			 for(int p=0; p<this.CurSentence.length(); p++){
			model.UpdateWeighth(
					this.arrCurSentecFeature[p], 1,
					curRoundIndexForTrain);// 标准权重增加
			model.UpdateWeighth(bestFeatures[p], -1,
					curRoundIndexForTrain);// 标准权重增加
			 }
		}
		//System.out.println("" + (System.nanoTime() - st2));
		// System.out.println("setnece:"+ curRoundIndexForTrain+" endupdate:"+
		// df.format(new Date()));// new Date()为获取当前系统时间

	}

	/**
	 * 已标注序列转化为State对象
	 * 
	 * @param tagSequence
	 * @return
	 */
	public State TagSeConvertState(String tagSequence) {
		State newState = new State();
		StringTokenizer token = new StringTokenizer(tagSequence, " ");
		int i = 0;
		while (token.hasMoreElements()) {
			String tempStr = token.nextToken();
			int index = tempStr.indexOf("_");
			newState.arrWord[i] = tempStr.substring(0, index);
			newState.arrTag[i] = tempStr.substring(index + 1, tempStr.length());
			i++;
		}
		newState.size = i;
		return newState;
	}

	/**
	 * 解码器
	 * 
	 * @return
	 * @throws Exception
	 */
	public String Decoder() throws Exception {
		this.agenda = new State[this.search_width];
		this.agenda[0] = new State();
		for (int i = 0; i < this.CurSentence.length(); i++) {
			String curSChar = String.valueOf(CurSentence.charAt(i));
			//bwlog.write("第 " + i + "数符处理:" + curSChar + "\r\n");
			int agendaLen = 1;
			if (i == 0)
				agendaLen = 1;
			else
				agendaLen = agenda.length;

			State[] temAgenda = new State[this.search_width];			
			for (int o = 0; o < this.search_width; o++) {
				temAgenda[o] = new State();
				temAgenda[o].score = MINVALUE;				
			}
			
			for (int j = 0; j < agendaLen; j++) {// 遍历状态序列，生成新的状态
				State state = agenda[j];
				if (i > 0 && i< this.CurSentence.length()-1) {//最后一个伪结束符只能sep	
					if(canAction(state, curSChar, state.arrTag[state.size-1], 1) == true){
						State tempCands = Append(state, curSChar, false); // append
						if (tempCands.score > temAgenda[0].score) {													// action
							heapSort.BestAgendaSort(temAgenda, tempCands);
						}
					}
				}
				if(i== this.CurSentence.length()-1){//最后一个伪字符 #_PU
					State temCand = Sep(state, curSChar, "PU", false);// seperate
					if (temCand.score > temAgenda[0].score) {															
						heapSort.BestAgendaSort(temAgenda, temCand);
					}
				}else{
					for (int k = 0; k < arrPOS.length; k++) {
						if(canAction(state, curSChar, arrPOS[k], 0) == true){					
							State temCand = Sep(state, curSChar, arrPOS[k], false);// seperate
							if (temCand.score > temAgenda[0].score) {															
								heapSort.BestAgendaSort(temAgenda, temCand);
							}
						}
					}
				}
			}

			this.agenda = temAgenda; // 求前K个分数最高的序列
		}
		this.agenda = Best(this.agenda, 1);

		return this.agenda[0].toString();
	}
	
	/**
	 * 
	 * @param state
	 * @param curChar
	 * @param action
	 * @return
	 */
	public boolean canAction(State state, String curChar, String pos, int action){
		
		//return true;
		boolean bRet = false;
		
		String word = curChar;
		if(action==1)//app actin
		{
			word = state.arrWord[state.size-1]+"curChar";
		}
		
		HashSet<String> closeset = model.PosCloseSet.get(pos);
		if(closeset != null){
			if(closeset.contains(word)){
				bRet = true;
			}			
		}else{
			bRet = true;
		}		
		return bRet;
		
	}

	/**
	 * 计算前k个最高的标注序列
	 * 
	 * @param n
	 * @return
	 */
	public State[] Best(State[] temAgenda, int k) {
		State[] retAgenda = new State[k];
		HeapSort heapSort = new HeapSort();
		if (search_width > temAgenda.length)
			return temAgenda;
		retAgenda = heapSort.heapSortK(temAgenda, temAgenda.length, k);
		return retAgenda;
	}

	public double GetLocalFeaturesScore(State state) {
		double dScore = 0.0;
		String c_0 = "", c_1 = "", c_2 = "";
		String w_0 = "", w_1 = "", w_2 = "", t_0 = "", t_1 = "", t_2 = "";
		String start_w_1 = "", end_w_1 = "", end_w_2 = "";
		int len_w_1 = 0, len_w_2 = 0;
		int size = state.size;
		if (size > 0) {
			w_0 = state.arrWord[size - 1];
			t_0 = state.arrTag[size - 1];
			w_1 = "S1";
			t_1 = "T1";
			w_2 = "S2";
			t_2 = "T2";
			if (size > 1) {
				w_1 = state.arrWord[size - 2];
				t_1 = state.arrTag[size - 2];
			}
			if (size > 2) {
				w_2 = state.arrWord[size - 3];
				t_2 = state.arrTag[size - 3];
			}

			c_0 = String.valueOf(w_0.charAt(w_0.length() - 1));
			if (w_1.equals("S1")) {
				len_w_1 = 0;
			} else {
				len_w_1 = w_1.length();
				if(len_w_1>5) len_w_1 = 5;
			}
			if (w_2.equals("S2")) {
				len_w_2 = 0;
			} else {
				len_w_2 = w_2.length();
				if(len_w_2>5) len_w_2 = 5;
			}

			if (w_0.length() == 1) {
				if (len_w_1 == 1) {
					c_1 = String.valueOf(w_1.charAt(0));
					if (len_w_2 == 0) {
						c_2 = String.valueOf(w_2.charAt(w_2.length() - 1));
					} else {
						c_2 = "S2";
					}
				} else if (len_w_1 > 1) {
					c_1 = String.valueOf(w_1.charAt(w_1.length() - 1));
					c_2 = String.valueOf(w_1.charAt(w_1.length() - 2));
				} else {
					c_1 = "S1";
					c_2 = "S2";
				}
			} else {
				if (w_0.length() == 2) {
					c_1 = String.valueOf(w_0.charAt(0));
					if (len_w_1 > 0) {
						c_2 = String.valueOf(w_1.charAt(w_1.length() - 1));
					} else {
						c_2 = "S2";
					}
				} else {
					c_1 = String.valueOf(w_0.charAt(0));
					c_2 = String.valueOf(w_0.charAt(1));
				}
			}
			if (len_w_1 > 0) {
				start_w_1 = w_1.substring(0, 1);
				end_w_1 = w_1.substring(w_1.length() - 1, w_1.length());
			} else {
				start_w_1 = "S1";
				end_w_1 = "S1";
			}
			if (len_w_2 > 0) {
				end_w_2 = w_2.substring(w_2.length() - 1, w_2.length());
			} else {
				end_w_2 = "S2";
			}

			// 构造特征
			Feature fe = null;
			if (state.lastAction == 1) {// app
				fe = model.m_mapConsecutiveChars.get("ConsecutiveChars=" + c_1
						+ c_0);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapTagByChar.get("TagByChar=" + t_0 + c_0);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapTaggedCharByFirstChar
						.get("TaggedCharByFirstChar=" + c_0 + t_0 + c_1);
				if (fe != null)
					dScore += fe.weight;
				
//				String wordCat = this.hmWord.get(w_0);
//				if(wordCat !=null && wordCat.indexOf(t_0+"|")>-1){
//					fe = model.m_mapConsecutiveCharCat
//							.get("ConsecutiveCharCat=" + t_0 );
//			        if (fe != null)
//				       dScore += fe.weight;
//				}
//				
//				if(w_0.length()>1){
//					String charCat0 = this.hmChar.get(c_0);
//					String charCat1 = this.hmChar.get(c_1);
//					if(charCat0 !=null && charCat0.indexOf(t_0+"|")>-1 && charCat1 !=null && charCat1.indexOf(t_0+"|")>-1){
//						fe = model.m_mapConsecutiveCharCat
//						.get("ConsecutiveCharTagCat=" + t_0 );
//						if (fe != null)
//							dScore += fe.weight;
//						}
//				}

			} else {
				fe = model.m_mapSeenWords.get("SeenWords=" + w_1);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapLastWordByWord.get("LastWordByWord=" + w_1
						+ "_" + w_2);
				if (fe != null)
					dScore += fe.weight;
				if(len_w_1 == 1){
					fe = model.m_mapOneCharWord.get("OneCharWord=" + w_1);
					if (fe != null)
						dScore += fe.weight;
					
					fe = model.m_mapTagOfOneCharWord.get("TagOfOneCharWord=" + c_2
							+ c_1 + c_0 + t_1);
					if (fe != null)
						dScore += fe.weight;				
				}
				//else {
					fe = model.m_mapFirstAndLastChars.get("FirstAndLastChars="
							+ start_w_1 + end_w_1);
					if (fe != null)
						dScore += fe.weight;
					
					fe = model.m_mapLengthByLastChar.get("LengthByLastChar="
							+ end_w_1 + len_w_1);
					if (fe != null)
						dScore += fe.weight;
					
					fe = model.m_mapLengthByFirstChar.get("LengthByFirstChar="
							+ start_w_1 + len_w_1);
					if (fe != null)
						dScore += fe.weight;					
				//}
				fe = model.m_mapCurrentWordLastChar.get("CurrentWordLastChar="
						+ end_w_2 + "_" + w_1);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapLastWordByLastChar.get("LastWordByLastChar="
						+ end_w_2 + end_w_1);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapLengthByLastWord.get("LengthByLastWord=" + w_2
						+ len_w_1);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapLastLengthByWord.get("LastLengthByWord="
						+ len_w_2 + w_1);
				if (fe != null)
					dScore += fe.weight;
							
				fe = model.m_mapCurrentTag.get("CurrentTag=" + w_1 + t_1);
				if (fe != null)
					dScore += fe.weight;

				//if(len_w_1<=2){
					fe = model.m_mapTagByWordAndPrevChar
							.get("TagByWordAndPrevChar=" + w_1 + t_1 + end_w_2);
					if (fe != null)
						dScore += fe.weight;
					
					fe = model.m_mapTagByLastWord.get("TagByLastWord=" + w_1 + t_0);
					if (fe != null)
						dScore += fe.weight;
					
					fe = model.m_mapTagByWordAndNextChar
					.get("TagByWordAndNextChar=" + w_1 + t_1 + c_0);
					if (fe != null)
						dScore += fe.weight;					
				//}
				
				fe = model.m_mapLastTagByWord.get("LastTagByWord=" + w_1 + t_2);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapTagByLastChar.get("TagByLastChar=" + end_w_1
						+ t_1);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapLastTagByTag.get("LastTagByTag=" + t_1 + t_0);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapTag0Tag1Size1.get("Tag0Tag1Size1=" + t_1 + t_0
						+ len_w_1);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapTag1Tag2Size1.get("Tag1Tag2Size1=" + t_2 + t_1
						+ len_w_1);
				if (fe != null)
					dScore += fe.weight;
				fe = model.m_mapTag0Tag1Tag2Size1.get("Tag0Tag1Tag2Size1="
						+ t_2 + t_1 + t_0 + len_w_1);
				if (fe != null)
					dScore += fe.weight;
				fe = model.m_mapLastTwoTagsByTag.get("LastTwoTagsByTag=" + t_2
						+ t_1 + t_0);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapTagByFirstChar.get("TagByChar=" + t_0
						+ c_0);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapFirstCharBy2Tags.get("FirstCharBy2Tags=" + t_0+t_1
						+ c_0);
				if (fe != null)
					dScore += fe.weight;				

				fe = model.m_mapSeparateChars.get("SeparateChars=" + c_1 + c_0);
				if (fe != null)
					dScore += fe.weight;			
				
				fe = model.m_mapLastWordFirstChar.get("LastWordFirstChar="
						+ w_1 + "_" + c_0);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapFirstCharLastWordByWord
				.get("FirstCharLastWordByWord=" + start_w_1 + c_0);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapTaggedSeparateChars.get("TaggedSeparateChars="
						+ end_w_1 + t_1 + c_0 + t_0);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapTagWordTag.get("TagWordTag=" + t_2 + w_1 + t_0);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapWordTagTag.get("WordTagTag="
						+ w_2 + t_1 + t_0);
				if (fe != null)
					dScore += fe.weight;				

				fe = model.m_mapTagByFirstChar.get("TagByFirstChar="
						+ start_w_1 + t_1);
				if (fe != null)
					dScore += fe.weight;				
				
				String charCat = this.hmChar.get(start_w_1);
				if(charCat !=null && charCat.indexOf(t_0+"|")>-1){
					//fe = model.m_mapSeparateCharCat.get("SeparateCharCat="+ t_0);
					//if (fe != null)
						//dScore += fe.weight;
					fe = model.m_mapTagByFirstCharCat.get("TagByFirstCharCat="+c_0 + t_0);
					if (fe != null)
						dScore += fe.weight;				
				}
				
				String wordCat = this.hmWord.get(w_1);
				if(wordCat !=null && wordCat.indexOf(t_1+"|")>-1){
					//fe = model.m_mapSeparateWordCat.get("SeparateWordCat="+ t_1);
					//if (fe != null)
						//dScore += fe.weight;
					fe = model.m_mapTagByCurWordCat.get("TagByCurWordCat="+t_1+ t_0);
					if (fe != null)
						dScore += fe.weight;							
				}
				
				String charCat1 = this.hmChar.get(end_w_1);
				if(charCat1 !=null && charCat1.indexOf(t_1+"|")>-1){
					fe = model.m_mapTagByLastCharCat.get("TagByLastCharCat="+ start_w_1 + t_1);
					if (fe != null)
						dScore += fe.weight;						
				}
				
				for (int j = 0; j < len_w_1 - 1; ++j) {
					fe = model.m_mapTaggedCharByLastChar
							.get("TaggedCharByLastChar="
									+ w_1.substring(j, j + 1) + t_1 + end_w_1);
					if (fe != null)
						dScore += fe.weight;
				}
				
				
			}
		}
		state.score += dScore;
		return dScore;
	}

	public double GetLocalFeaturesScoreForTest(State state) {
		double dScore = 0.0;
		String c_0 = "", c_1 = "", c_2 = "";
		String w_0 = "", w_1 = "", w_2 = "", t_0 = "", t_1 = "", t_2 = "";
		String start_w_1 = "", end_w_1 = "", end_w_2 = "";
		int len_w_1 = 0, len_w_2 = 0;
		int size = state.size;
		if (size > 0) {
			w_0 = state.arrWord[size - 1];
			t_0 = state.arrTag[size - 1];
			w_1 = "S1";
			t_1 = "T1";
			w_2 = "S2";
			t_2 = "T2";
			if (size > 1) {
				w_1 = state.arrWord[size - 2];
				t_1 = state.arrTag[size - 2];
			}
			if (size > 2) {
				w_2 = state.arrWord[size - 3];
				t_2 = state.arrTag[size - 3];
			}

			c_0 = String.valueOf(w_0.charAt(w_0.length() - 1));
			if (w_1.equals("S1")) {
				len_w_1 = 0;
			} else {
				len_w_1 = w_1.length();
			}
			if (w_2.equals("S2")) {
				len_w_2 = 0;
			} else {
				len_w_2 = w_2.length();
			}

			if (w_0.length() == 1) {
				if (len_w_1 == 1) {
					c_1 = String.valueOf(w_1.charAt(0));
					if (len_w_2 == 0) {
						c_2 = String.valueOf(w_2.charAt(w_2.length() - 1));
					} else {
						c_2 = "S2";
					}
				} else if (len_w_1 > 1) {
					c_1 = String.valueOf(w_1.charAt(w_1.length() - 1));
					c_2 = String.valueOf(w_1.charAt(w_1.length() - 2));
				} else {
					c_1 = "S1";
					c_2 = "S2";
				}
			} else {
				if (w_0.length() == 2) {
					c_1 = String.valueOf(w_0.charAt(0));
					if (len_w_1 > 0) {
						c_2 = String.valueOf(w_1.charAt(w_1.length() - 1));
					} else {
						c_2 = "S2";
					}
				} else {
					c_1 = String.valueOf(w_0.charAt(0));
					c_2 = String.valueOf(w_0.charAt(1));
				}
			}
			if (len_w_1 > 0) {
				start_w_1 = w_1.substring(0, 1);
				end_w_1 = w_1.substring(w_1.length() - 1, w_1.length());
			} else {
				start_w_1 = "S1";
				end_w_1 = "S1";
			}
			if (len_w_2 > 0) {
				end_w_2 = w_2.substring(w_2.length() - 1, w_2.length());
			} else {
				end_w_2 = "S2";
			}

			// 构造特征
			Feature fe = null;
			if (state.lastAction == 1) {// app
				fe = model.m_mapConsecutiveChars.get("ConsecutiveChars=" + c_1
						+ c_0);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapTagByChar.get("TagByChar=" + t_0 + c_0);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapTaggedCharByFirstChar
						.get("TaggedCharByFirstChar=" + c_0 + t_0 + c_1);
				if (fe != null)
					dScore += fe.weight;
				
//				String wordCat = this.hmWord.get(w_0);
//				if(wordCat !=null && wordCat.indexOf(t_0+"|")>-1){
//					fe = model.m_mapConsecutiveCharCat
//							.get("ConsecutiveCharCat=" + t_0 );
//			        if (fe != null)
//				       dScore += fe.weight;
//				}
//				
//				if(w_0.length()>1){
//					String charCat0 = this.hmChar.get(c_0);
//					String charCat1 = this.hmChar.get(c_1);
//					if(charCat0 !=null && charCat0.indexOf(t_0+"|")>-1 && charCat1 !=null && charCat1.indexOf(t_0+"|")>-1){
//						fe = model.m_mapConsecutiveCharCat
//						.get("ConsecutiveCharTagCat=" + t_0 );
//						if (fe != null)
//							dScore += fe.weight;
//						}
//				}

			} else {
				fe = model.m_mapSeenWords.get("SeenWords=" + w_1);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapLastWordByWord.get("LastWordByWord=" + w_1
						+ "_" + w_2);
				if (fe != null)
					dScore += fe.weight;
				if(len_w_1 == 1){
					fe = model.m_mapOneCharWord.get("OneCharWord=" + w_1);
					if (fe != null)
						dScore += fe.weight;
					
					fe = model.m_mapTagOfOneCharWord.get("TagOfOneCharWord=" + c_2
							+ c_1 + c_0 + t_1);
					if (fe != null)
						dScore += fe.weight;				
				}
				//else {
					fe = model.m_mapFirstAndLastChars.get("FirstAndLastChars="
							+ start_w_1 + end_w_1);
					if (fe != null)
						dScore += fe.weight;
					
					fe = model.m_mapLengthByLastChar.get("LengthByLastChar="
							+ end_w_1 + len_w_1);
					if (fe != null)
						dScore += fe.weight;
					
					fe = model.m_mapLengthByFirstChar.get("LengthByFirstChar="
							+ start_w_1 + len_w_1);
					if (fe != null)
						dScore += fe.weight;					
				//}
				fe = model.m_mapCurrentWordLastChar.get("CurrentWordLastChar="
						+ end_w_2 + "_" + w_1);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapLastWordByLastChar.get("LastWordByLastChar="
						+ end_w_2 + end_w_1);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapLengthByLastWord.get("LengthByLastWord=" + w_2
						+ len_w_1);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapLastLengthByWord.get("LastLengthByWord="
						+ len_w_2 + w_1);
				if (fe != null)
					dScore += fe.weight;
							
				fe = model.m_mapCurrentTag.get("CurrentTag=" + w_1 + t_1);
				if (fe != null)
					dScore += fe.weight;

				//if(len_w_1<=2){
					fe = model.m_mapTagByWordAndPrevChar
							.get("TagByWordAndPrevChar=" + w_1 + t_1 + end_w_2);
					if (fe != null)
						dScore += fe.weight;
					
					fe = model.m_mapTagByLastWord.get("TagByLastWord=" + w_1 + t_0);
					if (fe != null)
						dScore += fe.weight;
					
					fe = model.m_mapTagByWordAndNextChar
					.get("TagByWordAndNextChar=" + w_1 + t_1 + c_0);
					if (fe != null)
						dScore += fe.weight;					
				//}
				
				fe = model.m_mapLastTagByWord.get("LastTagByWord=" + w_1 + t_2);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapTagByLastChar.get("TagByLastChar=" + end_w_1
						+ t_1);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapLastTagByTag.get("LastTagByTag=" + t_1 + t_0);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapTag0Tag1Size1.get("Tag0Tag1Size1=" + t_1 + t_0
						+ len_w_1);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapTag1Tag2Size1.get("Tag1Tag2Size1=" + t_2 + t_1
						+ len_w_1);
				if (fe != null)
					dScore += fe.weight;
				fe = model.m_mapTag0Tag1Tag2Size1.get("Tag0Tag1Tag2Size1="
						+ t_2 + t_1 + t_0 + len_w_1);
				if (fe != null)
					dScore += fe.weight;
				fe = model.m_mapLastTwoTagsByTag.get("LastTwoTagsByTag=" + t_2
						+ t_1 + t_0);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapTagByFirstChar.get("TagByChar=" + t_0
						+ c_0);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapFirstCharBy2Tags.get("FirstCharBy2Tags=" + t_0+t_1
						+ c_0);
				if (fe != null)
					dScore += fe.weight;				

				fe = model.m_mapSeparateChars.get("SeparateChars=" + c_1 + c_0);
				if (fe != null)
					dScore += fe.weight;			
				
				fe = model.m_mapLastWordFirstChar.get("LastWordFirstChar="
						+ w_1 + "_" + c_0);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapFirstCharLastWordByWord
				.get("FirstCharLastWordByWord=" + start_w_1 + c_0);
				if (fe != null)
					dScore += fe.weight;

				fe = model.m_mapTaggedSeparateChars.get("TaggedSeparateChars="
						+ end_w_1 + t_1 + c_0 + t_0);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapTagWordTag.get("TagWordTag=" + t_2 + w_1 + t_0);
				if (fe != null)
					dScore += fe.weight;
				
				fe = model.m_mapWordTagTag.get("WordTagTag="
						+ w_2 + t_1 + t_0);
				if (fe != null)
					dScore += fe.weight;				

				fe = model.m_mapTagByFirstChar.get("TagByFirstChar="
						+ start_w_1 + t_1);
				if (fe != null)
					dScore += fe.weight;				
				
				String charCat = this.hmChar.get(start_w_1);
				if(charCat !=null && charCat.indexOf(t_0+"|")>-1){
					//fe = model.m_mapSeparateCharCat.get("SeparateCharCat="+ t_0);
					//if (fe != null)
						//dScore += fe.weight;
					fe = model.m_mapTagByFirstCharCat.get("TagByFirstCharCat="+c_0 + t_0);
					if (fe != null)
						dScore += fe.weight;				
				}
				
				String wordCat = this.hmWord.get(w_1);
				if(wordCat !=null && wordCat.indexOf(t_1+"|")>-1){
					//fe = model.m_mapSeparateWordCat.get("SeparateWordCat="+ t_1);
					//if (fe != null)
						//dScore += fe.weight;
					fe = model.m_mapTagByCurWordCat.get("TagByCurWordCat="+t_1+ t_0);
					if (fe != null)
						dScore += fe.weight;							
				}
				
				String charCat1 = this.hmChar.get(end_w_1);
				if(charCat1 !=null && charCat1.indexOf(t_1+"|")>-1){
					fe = model.m_mapTagByLastCharCat.get("TagByLastCharCat="+ start_w_1 + t_1);
					if (fe != null)
						dScore += fe.weight;						
				}
				
				for (int j = 0; j < len_w_1 - 1; ++j) {
					fe = model.m_mapTaggedCharByLastChar
							.get("TaggedCharByLastChar="
									+ w_1.substring(j, j + 1) + t_1 + end_w_1);
					if (fe != null)
						dScore += fe.weight;
				}
				
				
			}
		}
		state.score += dScore;
		return dScore;
	}

	public String[] GetLocalFeatures(String w_0, String w_1, String w_2,
			String t_0, String t_1, String t_2, int action) {
		String[] arrFeature = new String[50];
		String c_0 = "", c_1 = "", c_2 = "";
		String start_w_1 = "", end_w_1 = "", end_w_2 = "";
		int len_w_1 = 0, len_w_2 = 0;

		c_0 = String.valueOf(w_0.charAt(w_0.length() - 1));
		if (w_1.equals("S1")) {
			len_w_1 = 0;
		} else {
			len_w_1 = w_1.length();
			if(len_w_1>5) len_w_1 = 5;
		}
		if (w_2.equals("S2")) {
			len_w_2 = 0;
		} else {
			len_w_2 = w_2.length();
			if(len_w_2>5) len_w_2 = 5;
		}
		if (w_0.length() == 1) {
			if (len_w_1 == 1) {
				c_1 = String.valueOf(w_1.charAt(0));
				if (len_w_2 == 0) {
					c_2 = String.valueOf(w_2.charAt(w_2.length() - 1));
				} else {
					c_2 = "S2";
				}
			} else if (len_w_1 > 1) {
				c_1 = String.valueOf(w_1.charAt(w_1.length() - 1));
				c_2 = String.valueOf(w_1.charAt(w_1.length() - 2));
			} else {
				c_1 = "S1";
				c_2 = "S2";
			}
		} else {
			if (w_0.length() == 2) {
				c_1 = String.valueOf(w_0.charAt(0));
				if (len_w_1 > 0) {
					c_2 = String.valueOf(w_1.charAt(w_1.length() - 1));
				} else {
					c_2 = "S2";
				}
			} else {
				c_1 = String.valueOf(w_0.charAt(0));
				c_2 = String.valueOf(w_0.charAt(1));
			}
		}
		if (len_w_1 > 0) {
			start_w_1 = w_1.substring(0, 1);
			end_w_1 = w_1.substring(w_1.length() - 1, w_1.length());
		} else {
			start_w_1 = "S1";
			end_w_1 = "S1";
		}
		if (len_w_2 > 0) {
			end_w_2 = w_2.substring(w_2.length() - 1, w_2.length());
		} else {
			end_w_2 = "S2";
		}
		// 构造特征
		if (action == 1) {
			arrFeature[0] = "ConsecutiveChars=" + c_1 + c_0;
			arrFeature[1] = "TagByChar=" + t_0 + c_0;
			arrFeature[2] = "TaggedCharByFirstChar=" + c_0 + t_0 + c_1;
			
			int k=3;
			String wordCat = this.hmWord.get(w_0);
			if(wordCat !=null && wordCat.indexOf(t_0+"|")>-1){
				//arrFeature[k] = "ConsecutiveCharCat="+ w_0;
				//arrFeature[k] = "ConsecutiveCharCat="+t_0;
				//k++;
			}	
			
			if(w_0.length()>1){
				String charCat0 = this.hmChar.get(c_0);
				String charCat1 = this.hmChar.get(c_1);
				if(charCat0 !=null && charCat0.indexOf(t_0+"|")>-1 && charCat1 !=null && charCat1.indexOf(t_0+"|")>-1){
					//arrFeature[k] = "ConsecutiveCharTagCat="+ t_0;
					//k++;
				}
			}
			
			
		} else {
			int k=0;
			
			arrFeature[k++] = "SeenWords=" + w_1;
			arrFeature[k++] = "LastWordByWord=" + w_1 + "_" + w_2;
			if(len_w_1 == 1){
			  arrFeature[k++] = "OneCharWord=" + w_1;
			  arrFeature[k++] = "TagOfOneCharWord=" + c_2 + c_1 + c_0 + t_1;
			}
			//else {m_mapLengthByFirstChar
				arrFeature[k++] = "FirstAndLastChars=" + start_w_1 + end_w_1;
				arrFeature[k++] = "LengthByLastChar=" + end_w_1 + len_w_1;
				arrFeature[k++] = "LengthByFirstChar=" + start_w_1 + len_w_1;
			//}
			arrFeature[k++] = "CurrentWordLastChar=" + end_w_2 + "_" + w_1;
			arrFeature[k++] = "LastWordByLastChar=" + end_w_2 + end_w_1;
			arrFeature[k++] = "LengthByLastWord=" + w_2 + len_w_1;
			arrFeature[k++] = "LastLengthByWord=" + len_w_2 + w_1;
			arrFeature[k++] = "CurrentTag=" + w_1 + t_1;
			//if(len_w_1<=2){
				arrFeature[k++] = "TagByWordAndPrevChar=" + w_1 + t_1 + end_w_2;
				arrFeature[k++] = "TagByLastWord=" + w_1 + t_0;
				arrFeature[k++] = "TagByWordAndNextChar=" + w_1 + t_1 + c_0;
			//}				
				
			arrFeature[k++] = "LastTagByWord=" + w_1 + t_2;			
			arrFeature[k++] = "TagByLastChar=" + end_w_1 + t_1;
			arrFeature[k++] = "LastTagByTag=" + t_1 + t_0;
			arrFeature[k++] = "Tag0Tag1Size1=" + t_1 + t_0 + len_w_1;
			arrFeature[k++] = "Tag1Tag2Size1=" + t_2 + t_1 + len_w_1;
			arrFeature[k++] = "Tag0Tag1Tag2Size1=" + t_2 + t_1 + t_0 + len_w_1;
			arrFeature[k++] = "LastTwoTagsByTag=" + t_2 + t_1 + t_0;
			 
			arrFeature[k++] = "TagByChar=" + t_0 + c_0;					

			arrFeature[k++] = "FirstCharBy2Tags=" + c_0+t_0+t_1;			

			arrFeature[k++] = "SeparateChars=" + c_1 + c_0;
			arrFeature[k++] = "LastWordFirstChar=" + w_1 + "_" + c_0;
			arrFeature[k++] = "FirstCharLastWordByWord=" + start_w_1 + c_0;			
			arrFeature[k++] = "TaggedSeparateChars=" + end_w_1 + t_1 + c_0 + t_0;
			arrFeature[k++] = "TagWordTag=" + w_1 + t_0 + t_2;
			arrFeature[k++] = "WordTagTag=" + w_2 + t_0 + t_1;
			
			
			arrFeature[k++] = "TagByFirstChar=" + start_w_1 + t_1;	
			
			String charCat = this.hmChar.get(start_w_1);
			if(charCat !=null && charCat.indexOf(t_0+"|")>-1){
				//arrFeature[k++] = "SeparateCharCat="+ t_0;
				arrFeature[k++] = "TagByFirstCharCat="+c_0 + t_0;
				
			}
			
			String wordCat = this.hmWord.get(w_1);
			if(wordCat !=null && wordCat.indexOf(t_1+"|")>-1){
				//arrFeature[k] = "ConsecutiveCharCat="+ w_0;
				//arrFeature[k++] = "SeparateWordCat="+t_1;					
				arrFeature[k++] = "TagByCurWordCat="+t_1+t_0;					
			}
			
			String charCat1 = this.hmChar.get(end_w_1);
			if(charCat1 !=null && charCat1.indexOf(t_1+"|")>-1){
				arrFeature[k++] = "TagByLastCharCat="+ t_1+start_w_1;
				
			}			
			
			// System.out.println(w_1);
			int j = 0;
			for (j = 0; j < len_w_1 - 1 && j < 10; j++) {
				 arrFeature[k++] ="TaggedCharByLastChar="+ w_1.substring(j,j+1)+t_1+end_w_1;
				 
			}
			
		}
		return arrFeature;   
	}

	public String[][] GetFeatureBySentence(String tagSentence) {
		this.CurSentence ="";
		String[][] arrFeature;
		int action;
		StringTokenizer st = new StringTokenizer(tagSentence);
		String[] arrWord = new String[st.countTokens()];
		String[] arrTag = new String[st.countTokens()];
		int k = 0;

		while (st.hasMoreElements()) {
			String str = st.nextToken();
			int index = str.indexOf("_");
			arrWord[k] = str.substring(0, index).trim();
			arrTag[k] = str.substring(index + 1, str.length()).trim();
			this.CurSentence += arrWord[k];
			k++;
		}
		arrFeature = new String[this.CurSentence.length()][50];

		int iCharIndex = 0;
		int index = 0;
		String w_0, w_1, w_2, t_0, t_1, t_2;
		for (int i = 0; i < arrWord.length; i++) {

			for (int j = 0; j < arrWord[i].length(); j++) {
				t_0 = arrTag[i];
				w_0 = arrWord[i].substring(0, j + 1);
				if (i == 0) {
					t_1 = "T1";
					w_1 = "S1";
					t_2 = "T2";
					w_2 = "S2";
				} else if (i == 1) {
					t_1 = arrTag[i - 1];
					w_1 = arrWord[i - 1];
					t_2 = "T2";
					w_2 = "S2";
				} else {
					t_1 = arrTag[i - 1];
					w_1 = arrWord[i - 1];
					t_2 = arrTag[i - 2];
					w_2 = arrWord[i - 2];
				}
				if (j == 0)
					action = 0;
				else
					action = 1;
				arrFeature[iCharIndex] = GetLocalFeatures(w_0, w_1, w_2, t_0,
						t_1, t_2, action);

				iCharIndex++;
			}

		}
		return arrFeature;
	}

	/**
	 * 把当前字符作为一个新词加入已标注序列尾部
	 * 
	 * @param state
	 * @param character
	 * @param pos
	 * @return
	 */
	private State Sep(State state, String curChar, String pos, boolean bType) {
		State newState = new State(state);
		newState.Sep(curChar, pos);
		if(newState.score == this.MINVALUE)
			 newState.score=0;
		if (bType == true) {
			GetLocalFeaturesScore(newState);
		} else {
			GetLocalFeaturesScoreForTest(newState);
		}
		return newState;
	}

	/**
	 * append action: 把当前字符直接加入已标注最后一个词尾，词性与最后词相同
	 * 
	 * @param state
	 * @param character
	 * @return
	 * @throws Exception
	 */
	private State Append(State state, String curChar, boolean bType)
			throws Exception {
		State newState = new State(state);
		newState.Add(curChar);
		if(newState.score == this.MINVALUE)
			 newState.score=0;
		if (bType == true) {
			GetLocalFeaturesScore(newState);
		} else {
			GetLocalFeaturesScoreForTest(newState);
		}
		return newState;
	}

	/**
	 * 输出到文件
	 * 
	 * @param arrlist
	 */
	private void save(String[] arrlist, String filename) {
		FileWriter fw;
		try {
			fw = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(fw); // 将缓冲对文件的输出
			for (int i = 0; i < arrlist.length; i++) {
				bw.write(arrlist[i] + "\r\n");
			}
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// 创建FileWriter对象，用来写入字符流
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * String curGoldsequence="abc_AC"; int lastIndex=
		 * curGoldsequence.lastIndexOf("_"); curGoldsequence =
		 * curGoldsequence.substring
		 * (0,lastIndex)+"AA"+curGoldsequence.substring(
		 * lastIndex,curGoldsequence.length());
		 * System.out.println(curGoldsequence);
		 */
		// TODO Auto-generated method stub
		String s = "我是中国人!";
		String g = "我是_VE 中国人_NN !_PU";
		BeamSearch2 bs = new BeamSearch2();
		// bs.sentence=s;
		// bs.goldTagSquence = g;
		String r = "";
		try {
			for (int i = 0; i < 10; i++) {
				// bs.trainer(i);
			}
			// bs.model.load("e:\\a.txt");
			r = bs.Decoder();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(r);
	}
}

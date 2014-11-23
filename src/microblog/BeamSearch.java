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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;


import microblog.State;

public class BeamSearch {
	// 传参文件
	public Double MINVALUE = -Double.MAX_VALUE;
	public String train_file = "";
	public String dev_file = ""; // 与训练文件格式相同，作为黄金标准
	public String test_file = ""; // 与训练文件格式相同，作为黄金标准
	public String output_file = "out";
	public String model_file = "";
	public String evaluationError_file = "erresult"; //作为
	public int number_of_iterations = 10;// training number;
	public boolean bNewTrain = true;
	public String output_path = "";
	public int number_of_test = 0;
	public int number_of_dev = 0;
	public int number_of_train = 0;
	public int search_width = 16; // 搜索宽度
	//public String charPos_file = "";
	//public String wordPos_file = "";
	
	//
	public List<String> arrTrainSource; // train sentences
	public List<State> bestStates;// 当前最好的K个状态。
	public List<Integer> goldActions = null;// 标准状态

	public List<String> arrTestSource; // test sentences
	public List<String> arrTestResult;
	
	public List<String> arrDevSource; // test sentences
	public List<String> arrDevResult;

	int curRoundIndexForTrain = 0;
	public Model model = new Model(); // 特征model;存储特征及权重
	
	int curTrainIterCorrectInstance = 0;


	private String CurSentence = "";// 待处理的句子
	// private int lenofSentence=0;//待处理的句子长度
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
	public BeamSearch(String train_file, int number_of_train,
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

	public BeamSearch() {
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
	public BeamSearch(String test_file, int number_of_test, String model_file,
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

	public BeamSearch(String train_file, int number_of_train,
			String model_file, int number_of_iterations, boolean bNewTrain,
			int search_width, String test_file, int number_of_test,
			String dev_file, int number_of_dev,
			String output_path) {
		this.train_file = train_file;
		this.number_of_train = number_of_train;
		this.model_file = model_file;
		this.number_of_iterations = number_of_iterations;
		this.bNewTrain = bNewTrain;
		this.search_width = search_width;
		this.test_file = test_file;
		this.number_of_test = number_of_test;
		this.output_path = output_path;
		this.dev_file = dev_file;
		this.number_of_dev = number_of_dev;
		//this.charPos_file = charPos_file;
		//this.wordPos_file = wordPos_file;

		try {
			bwlog = new BufferedWriter(new FileWriter(output_path + "log.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void trainDevTestProcess() throws Exception {

		//System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
		//initialWordPOS();
		initialTrain();
		initialTest();
		initialDev();
		curRoundIndexForTrain = 0;
		for (int n = 0; n < number_of_iterations; n++) {
			bwlog.write("train round begin:" + n + "  start " + df.format(new Date())+"\r\n");
			curTrainIterCorrectInstance = 0;
			for (int i = 0; i < this.arrTrainSource.size(); i++) {
				//GoldState = TagSeConvertState(this.arrTrainSource[i]);
				//this.arrCurSentecFeature = GetFeatureBySentence(this.arrTrainSource[i]);
				this.CurSentence = UnTagSentence(this.arrTrainSource.get(i));
				this.goldActions = getGoldActions(this.arrTrainSource.get(i));
				if(CurSentence.length() != goldActions.size() -1)
				{
					System.out.println("error...");
				}
				// bwlog.write("第"+n+"次循环第"+ i + "个句子 训练开始： "+
				// this.goldTagSquence.toString() + "\r\n");
				trainer(n, i);
				// bwlog.write("第"+n+"次循环第"+ i + "个句子 训练结束： "+
				// this.goldTagSquence.toString() + "\r\n");
			}
			model.AveWeight(curRoundIndexForTrain);
			System.out.println(curRoundIndexForTrain);
			System.out.println("Correct Intance Num: " + Integer.toString(curTrainIterCorrectInstance));
			//model.save(output_path + model_file + n);
			bwlog.write("train round end:"+ n+"   " +df.format(new Date())+"\r\n");
			
			bwlog.write("develop start:"+"\r\n");
			// 测试
			this.arrDevResult.clear();
			for (int i = 0; i < arrDevSource.size(); i++) {

				this.CurSentence = UnTagSentence(this.arrDevSource.get(i));
				this.arrDevResult.add(Decoder());
			}
			//save(this.arrTestResult, output_path + this.output_file + n);
			Evaluator eva = new Evaluator(this.arrDevResult, arrDevSource, bwlog, this.output_path+evaluationError_file+n);
			eva.Computer();
			// curRoundIndexForTrain=1;
			bwlog.write("develop end:"+"\r\n");
			
			bwlog.write("test start:"+"\r\n");
			// 测试
			this.arrTestResult.clear();
			for (int i = 0; i < arrTestSource.size(); i++) {

				this.CurSentence = UnTagSentence(this.arrTestSource.get(i));
				this.arrTestResult.add(Decoder());
			}
			//save(this.arrTestResult, output_path + this.output_file + n);
			eva = new Evaluator(this.arrTestResult, arrTestSource, bwlog, this.output_path+evaluationError_file+n);
			eva.Computer();
			// curRoundIndexForTrain=1;
			bwlog.write("test end:"+"\r\n");
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
			curTrainIterCorrectInstance = 0;
			for (int i = 0; i < arrTrainSource.size(); i++) {
				//GoldState = TagSeConvertState(this.arrTrainSource[i]);
				//this.arrCurSentecFeature = GetFeatureBySentence(this.arrTrainSource[i]);
				this.CurSentence = UnTagSentence(this.arrTrainSource.get(i));
				this.goldActions = getGoldActions(this.arrTrainSource.get(i));
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
			this.arrTestResult.clear();
			for (int i = 0; i < arrTestSource.size(); i++) {
				this.CurSentence = UnTagSentence(this.arrTestSource.get(i));
				this.arrTestResult.add(Decoder());
			}
			save(this.arrTestResult, this.output_file);
			Evaluator eva = new Evaluator(this.arrTestResult, arrTestSource, bwlog, this.output_path+ this.evaluationError_file);
			eva.Computer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void initialTest() {
		//this.arrTestSource = new String[this.number_of_test];
		//this.arrTestResult = new String[this.number_of_test];
		// this.model.load(model_file);
		this.arrTestSource = new ArrayList<String>();
		this.arrTestResult = new ArrayList<String>();
		File file = new File(this.test_file);
		BufferedInputStream fis;
		try {
			fis = new BufferedInputStream(new FileInputStream(file));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis, "UTF8"));// 用50M的缓冲读取文本文件
			String line = "";
			int i = 0;			
			while ((line = reader.readLine()) != null) {
				if(this.number_of_test > 0 && i>=this.number_of_test) break;
				if (line.trim().length() > 0) {
					this.arrTestSource.add(line.trim());
					i++;
				}
			}
			reader.close();
			this.number_of_test = this.arrTestSource.size();
			fis.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void initialDev() {
		//this.arrTestSource = new String[this.number_of_test];
		//this.arrTestResult = new String[this.number_of_test];
		// this.model.load(model_file);
		this.arrDevSource = new ArrayList<String>();
		this.arrDevResult = new ArrayList<String>();
		File file = new File(this.dev_file);
		BufferedInputStream fis;
		try {
			fis = new BufferedInputStream(new FileInputStream(file));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis, "UTF8"));// 用50M的缓冲读取文本文件
			String line = "";
			int i = 0;			
			while ((line = reader.readLine()) != null) {
				if(this.number_of_dev > 0 && i>=this.number_of_dev) break;
				if (line.trim().length() > 0) {
					this.arrDevSource.add(line.trim());
					i++;
				}
			}
			reader.close();
			this.number_of_dev = this.arrDevSource.size();
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
		this.arrTrainSource = new ArrayList<String>();;
		if (this.bNewTrain == true) {
			this.model.newFeatureTemplates();
			this.model.init(this.train_file);
		} else {
			this.model.load(model_file);
		}
		File file = new File(this.train_file);
		BufferedInputStream fis;
		try {
			fis = new BufferedInputStream(new FileInputStream(file));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis, "UTF8"));// 用50M的缓冲读取文本文件
			String line = "";
			int i = 0;
			
			while ((line = reader.readLine()) != null) {
				if(this.number_of_train > 0 && i>=this.number_of_train) break;
				if (line.trim().length() > 0) {
					this.arrTrainSource.add(line.trim());
					i++;
				}
			}
			reader.close();
			fis.close();
			this.number_of_train = this.arrTrainSource.size();
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

		curRoundIndexForTrain++;
		System.out.print("setnece:" + sentenceIndex + " begin:");// +df.format(newDate())); //new Date()为获取当前系统时间
		System.out.flush();
		long st1 = System.nanoTime();

		for (int i = 0; i <= this.CurSentence.length(); i++) {
			String curSChar = "";
			if(i < this.CurSentence.length()) curSChar = String.valueOf(CurSentence.charAt(i));
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
				if(i > 0 && state.bStart) continue;

				if (i > 0  && i< this.CurSentence.length()) {
					//if(canAction(state, curSChar, state.arrTag[state.size-1], 1) == true){					
					State tempCands = Append(state, curSChar, null, false); // append action
					if (tempCands.score > temAgenda[0].score) {
						if (state.bIsGold == true
								&& this.goldActions.get(i) == 1000) {
							tempCands.bIsGold = true;
						} else
							tempCands.bIsGold = false;
						heapSort.BestAgendaSort(temAgenda, tempCands);
					}
					//}				
				}
				if(i== this.CurSentence.length()){//最后一个伪字符 #_PU
					State temCand = Finish(state, null, false);// end action
					if (temCand.score >= temAgenda[0].score) {
						if (state.bIsGold == true
								&& this.goldActions.get(i) == 2000) {
							temCand.bIsGold = true;
						} else
							temCand.bIsGold = false;
						heapSort.BestAgendaSort(temAgenda, temCand);
					}
				}else {
					for (int k = 0; k < State.arrPOS.length; k++) {
						if(CanSeperate(state, curSChar,State.arrPOS[k]) == true){		
							State temCand = Sep(state, curSChar, k, null, false);// seperate action
							if (temCand.score >= temAgenda[0].score) {
								if (state.bIsGold == true
										&& this.goldActions.get(i) == k) {
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
					bEqual = true; 
					break;
				}
			}

			if (bEqual == false)// 没有标注序列与黄金标准相同
			{
				//for (int p = 0; p <= i; p++) {
				//	model.UpdateWeighth(this.arrCurSentecFeature[p], 1,	curRoundIndexForTrain);// 标准权重增加
				//}
				//model.printWeight(bwlog);
				State bestState = Best(this.agenda, 1)[0];
				List<Integer> predActions = bestState.hisActions;			
				double bestScore = bestState.score;
				double[] scores = updateParameters(predActions);
				if(Math.abs(bestScore- scores[0]) > 0.00001)
				{
					System.out.println("score not match...");
				}
				
				if(scores[1]- scores[0] > 0.00001)
				{
					System.out.println("gold score larger...");
				}
				
				System.out.println(String.format("Update at %d/%d, best score %f, gold score %f", predActions.size(), goldActions.size(), scores[0], scores[1]));
				return;				
			}			
		}
		

		//System.out.println("" + (System.nanoTime() - st1));
		long st2 = System.nanoTime();
		this.agenda = Best(this.agenda, 1);
		if (this.agenda[0].bIsGold == false) {// 最终的结果与标准不相符
			List<Integer> predActions = this.agenda[0].hisActions;
			if(predActions.size() != goldActions.size())
			{
				System.out.println("action num do not match.....");
			}
			double bestScore = this.agenda[0].score;
			
			double[] scores = updateParameters(predActions);
			if(Math.abs(bestScore- scores[0]) > 0.00001)
			{
				System.out.println("score not match...");
			}
			if(scores[1]- scores[0] > 0.00001)
			{
				System.out.println("gold score larger...");
			}
			System.out.println(String.format("Update at end: %d/%d, best score %f, gold score %f", predActions.size(), goldActions.size(), scores[0], scores[1]));
		}
		else
		{
			this.curTrainIterCorrectInstance++;
			System.out.println(String.format("Corrected : %d", goldActions.size()));
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
/*	
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
*/
	/**
	 * 解码器
	 * 
	 * @return
	 * @throws Exception
	 */
	public String Decoder() throws Exception {
		this.agenda = new State[this.search_width];
		this.agenda[0] = new State();
		for (int i = 0; i <= this.CurSentence.length(); i++) {
			String curSChar = "";
			if(i < this.CurSentence.length())curSChar = String.valueOf(CurSentence.charAt(i));
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
				if(i > 0 && state.bStart)continue;
				if (i > 0 && i< this.CurSentence.length()) {//最后一个伪结束符只能sep	
					//if(canAction(state, curSChar, state.arrTag[state.size-1], 1) == true){
						State tempCands = Append(state, curSChar, null, true); // append
						if (tempCands.score > temAgenda[0].score) {													// action
							heapSort.BestAgendaSort(temAgenda, tempCands);
						}
					//}
				}
				if(i== this.CurSentence.length()){//最后一个伪字符 #_PU
					State temCand = Finish(state, null, true);// seperate
					if (temCand.score > temAgenda[0].score) {															
						heapSort.BestAgendaSort(temAgenda, temCand);
					}
				}else{
					for (int k = 0; k < State.arrPOS.length; k++) {
						if(CanSeperate(state, curSChar,State.arrPOS[k]) == true){				
							State temCand = Sep(state, curSChar, k, null, true);// seperate
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
/*
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
*/
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

	
	public double GetLocalFeaturesScoreByZMS(State state, List<String> fvs, boolean bAverage) {
		double dScore = 0.0;
		String c_0 = "", c_1 = "", c_2 = "";
		String w_0 = "", w_1 = "", w_2 = "", t_0 = "", t_1 = "", t_2 = "";
		String start_w_1 = "", end_w_1 = "", end_w_2 = "";
		int len_w_1 = 0, len_w_2 = 0;
		int size = state.size;
		if (size > 0) {
			
			if(state.lastAction != 2000)
			{
				w_0 = state.arrWord[size - 1];
				t_0 = state.arrTag[size - 1];
				w_1 = "#S#";
				t_1 = "#T#";
				w_2 = "#S#";
				t_2 = "#T#";
				if (size > 1) {
					w_1 = state.arrWord[size - 2];
					t_1 = state.arrTag[size - 2];
				}
				if (size > 2) {
					w_2 = state.arrWord[size - 3];
					t_2 = state.arrTag[size - 3];
				}
			}
			else
			{
				w_0 = "#S#";
				t_0 = "#T#";
				w_1 = state.arrWord[size - 1];
				t_1 = state.arrTag[size - 1];
				w_2 = "#S#";
				t_2 = "#T#";
				if (size > 1) {
					w_2 = state.arrWord[size - 2];
					t_2 = state.arrTag[size - 2];
				}

			}
			
			if (w_1.equals("#S#")) {
				len_w_1 = 0;
			} else {
				len_w_1 = w_1.length();
				if(len_w_1>5) len_w_1 = 5;
			}
			if (w_2.equals("#S#")) {
				len_w_2 = 0;
			} else {
				len_w_2 = w_2.length();
				if(len_w_2>5) len_w_2 = 5;
			}

			if (state.lastAction != 1000 && state.lastAction >= 0) {
				c_0 = w_0;
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
			} 
			else if(state.lastAction == 1000){
				c_0 = String.valueOf(w_0.charAt(w_0.length() - 1));
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
			String strfeat = null;
			if (state.lastAction == 1000) {// app
				strfeat = "ConsecutiveChars=" + c_1	+ c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapConsecutiveChars.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "TagByChar=" + t_0 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTagByChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				strfeat = "TaggedCharByFirstChar=" + c_0 + t_0 + c_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTaggedCharByFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight :fe.weight;
				
//				String wordCat = this.hmWord.get(w_0);
//				if(wordCat !=null && wordCat.indexOf(t_0+"|")>-1){
//					strfeat = "ConsecutiveCharCat=" + t_0;
//					if(fvs != null)fvs.add(strfeat);
//					fe = model.m_mapConsecutiveCharCat
//							.get(strfeat);
//			        if (fe != null)
//				       dScore += bAverage ? fe.aveWeight : fe.weight;
//				}
//				
//				if(w_0.length()>1){
//					String charCat0 = this.hmChar.get(c_0);
//					String charCat1 = this.hmChar.get(c_1);
//					if(charCat0 !=null && charCat0.indexOf(t_0+"|")>-1 && charCat1 !=null && charCat1.indexOf(t_0+"|")>-1){
//						strfeat = "ConsecutiveCharTagCat=" + t_0;
//						if(fvs != null)fvs.add(strfeat);
//						fe = model.m_mapConsecutiveCharCat
//						.get(strfeat);
//						if (fe != null)
//							dScore += bAverage ? fe.aveWeight : fe.weight;
//						}
//				}

			} else {
				strfeat = "SeenWords=" + w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapSeenWords.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				strfeat = "LastWordByWord=" + w_1 + "_" + w_2;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapLastWordByWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				
				if(len_w_1 == 1){
					strfeat = "OneCharWord=" + w_1;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapOneCharWord.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					
					strfeat = "TagOfOneCharWord=" + c_2 + c_1 + c_0 + t_1;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapTagOfOneCharWord.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;				
				}
				
				strfeat = "FirstAndLastChars=" + start_w_1 + end_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapFirstAndLastChars.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "LengthByLastChar=" + end_w_1 + len_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapLengthByLastChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "LengthByFirstChar=" + start_w_1 + len_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapLengthByFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;					
				
				strfeat = "CurrentWordLastChar=" + end_w_2 + "_" + w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapCurrentWordLastChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "LastWordByLastChar="	+ end_w_2 + end_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapLastWordByLastChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "LengthByLastWord=" + w_2 + len_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapLengthByLastWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				strfeat = "LastLengthByWord=" + len_w_2 + w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapLastLengthByWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
					
				strfeat = "CurrentTag=" + w_1 + t_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapCurrentTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				//if(len_w_1<=2){
				strfeat = "TagByWordAndPrevChar=" + w_1 + t_1 + end_w_2;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTagByWordAndPrevChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "TagByLastWord=" + w_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTagByLastWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "TagByWordAndNextChar=" + w_1 + t_1 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTagByWordAndNextChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;					
				//}
				
				strfeat = "LastTagByWord=" + w_1 + t_2;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapLastTagByWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				strfeat = "TagByLastChar=" + end_w_1 + t_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTagByLastChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				strfeat = "LastTagByTag=" + t_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapLastTagByTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "Tag0Tag1Size1=" + t_1 + t_0 + len_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTag0Tag1Size1.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "Tag1Tag2Size1=" + t_2 + t_1 + len_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTag1Tag2Size1.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "Tag0Tag1Tag2Size1=" + t_2 + t_1 + t_0 + len_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTag0Tag1Tag2Size1.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "LastTwoTagsByTag=" + t_2 + t_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapLastTwoTagsByTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
								
				strfeat = "TagByChar=" + t_0 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTagByFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
								
				strfeat = "FirstCharBy2Tags=" + t_0 + t_1 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapFirstCharBy2Tags.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;				

				strfeat = "SeparateChars=" + c_1 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapSeparateChars.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;			
				
				strfeat = "LastWordFirstChar=" + w_1 + "_" + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapLastWordFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "FirstCharLastWordByWord=" + start_w_1 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapFirstCharLastWordByWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				strfeat = "TaggedSeparateChars=" + end_w_1 + t_1 + c_0 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTaggedSeparateChars.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "TagWordTag=" + t_2 + w_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTagWordTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "WordTagTag="	+ w_2 + t_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapWordTagTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;				

				strfeat = "TagByFirstChar="	+ start_w_1 + t_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapTagByFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;		
				
				// feature name misunderstanding here
				/*
				{
					if(model.m_wordFreq.containsKey(w_1)
							&& model.m_wordFreq.get(w_1) > 5
							&& model.m_wordPOSSets.get(w_1).containsKey(t_1))
					{
						strfeat = "SeparateCharCat="+ t_1;
						if(fvs != null)fvs.add(strfeat);
						fe = model.m_mapSeparateCharCat.get(strfeat);
						if (fe != null)
							dScore += bAverage ? fe.aveWeight : fe.weight;
						
						strfeat = "TagByFirstCharCat="+len_w_1 + t_1;
						if(fvs != null)fvs.add(strfeat);
						fe = model.m_mapTagByFirstCharCat.get(strfeat);
						if (fe != null)
							dScore += bAverage ? fe.aveWeight : fe.weight;		
					}
					
					if(model.m_wordFreq.containsKey(w_1)
							&& model.m_wordFreq.get(w_1) > 5)
					{					
						strfeat = "SeparateWordCat="+len_w_1;
						if(fvs != null)fvs.add(strfeat);
						fe = model.m_mapSeparateWordCat.get(strfeat);
						if (fe != null)
							dScore += bAverage ? fe.aveWeight : fe.weight;	
					}
				}
				*/
				
				//String charCat = this.hmChar.get(start_w_1);
				//if(charCat !=null && charCat.indexOf(t_0+"|")>-1){
					//strfeat = "SeparateCharCat="+ t_0;
					//if(fvs != null)fvs.add(strfeat);
					//fe = model.m_mapSeparateCharCat.get(strfeat);
					//if (fe != null)
						//dScore += bAverage ? fe.aveWeight : fe.weight;
					//strfeat = "TagByFirstCharCat="+c_0 + t_0;
					//if(fvs != null)fvs.add(strfeat);
					//fe = model.m_mapTagByFirstCharCat.get(strfeat);
					//if (fe != null)
					//	dScore += bAverage ? fe.aveWeight : fe.weight;				
				//}
				
				
				//String wordCat = this.hmWord.get(w_1);
				//if(wordCat !=null && wordCat.indexOf(t_1+"|")>-1){
					//strfeat = "SeparateWordCat="+ t_1;
					//if(fvs != null)fvs.add(strfeat);
					//fe = model.m_mapSeparateWordCat.get(strfeat);
					//if (fe != null)
						//dScore += bAverage ? fe.aveWeight : fe.weight;
					//strfeat = "TagByCurWordCat="+t_1+ t_0;
					//if(fvs != null)fvs.add(strfeat);
					//fe = model.m_mapTagByCurWordCat.get(strfeat);
					//if (fe != null)
					//	dScore += bAverage ? fe.aveWeight : fe.weight;							
				//}
				
				//String charCat1 = this.hmChar.get(end_w_1);
				//if(charCat1 !=null && charCat1.indexOf(t_1+"|")>-1){
				//	strfeat = "TagByLastCharCat="+ start_w_1 + t_1;
				//	if(fvs != null)fvs.add(strfeat);
				//	fe = model.m_mapTagByLastCharCat.get(strfeat);
				//	if (fe != null)
				//		dScore += bAverage ? fe.aveWeight : fe.weight;						
				//}
				
				for (int j = 0; j < len_w_1 - 1; ++j) {
					strfeat = "TaggedCharByLastChar=" + w_1.substring(j, j + 1) + t_1 + end_w_1;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapTaggedCharByLastChar.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
				}
				
				
			}
		}
		state.score += dScore;
		return dScore;
	}


	/**
	 * 把当前字符作为一个新词加入已标注序列尾部
	 * 
	 * @param state
	 * @param character
	 * @param pos
	 * @return
	 */
	private State Sep(State state, String curChar, int POSID, List<String> fvs, boolean bAverage) {
		State newState = new State(state);
		newState.Sep(curChar, POSID);
		if(newState.score == this.MINVALUE)
			 newState.score=0;
		//if (bType == true) {
		//	GetLocalFeaturesScore(newState);
		//} else {
		//	GetLocalFeaturesScoreForTest(newState);
		//}
		GetLocalFeaturesScoreByZMS(newState, fvs, bAverage);
		return newState;
	}
	
	
	/**
	 * 把当前字符作为一个新词加入已标注序列尾部
	 * 
	 * @param state
	 * @param character
	 * @param pos
	 * @return
	 */
	private State Finish(State state, List<String> fvs, boolean bAverage) {
		State newState = new State(state);
		newState.Finish();
		if(newState.score == this.MINVALUE)
			 newState.score=0;
		GetLocalFeaturesScoreByZMS(newState, fvs, bAverage);
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
	private State Append(State state, String curChar, List<String> fvs, boolean bAverage)
			throws Exception {
		State newState = new State(state);
		newState.Add(curChar);
		if(newState.score == this.MINVALUE)
			 newState.score=0;
//		if (bType == true) {
//			GetLocalFeaturesScore(newState);
//		} else {
//			GetLocalFeaturesScoreForTest(newState);
//		}
		GetLocalFeaturesScoreByZMS(newState, fvs, bAverage);
		return newState;
	}

	/**
	 * 输出到文件
	 * 
	 * @param arrlist
	 */
	private void save(List<String> arrlist, String filename) {
		FileWriter fw;
		try {
			fw = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(fw); // 将缓冲对文件的输出
			for (int i = 0; i < arrlist.size(); i++) {
				bw.write(arrlist.get(i) + "\r\n");
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
		BeamSearch bs = new BeamSearch();
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
	
	public double[] updateParameters(List<Integer> predActions)
	{
		State tmpState = new State();
		State goldState = new State();
		double[] scores = new double[2]; 
		scores[0] = 0.0;
		scores[1] = 0.0;
		double currentScore;
		int p = 0;
		for(; p < predActions.size(); p++)
		{
			String curSChar = "";
			if(p < this.CurSentence.length()) curSChar = String.valueOf(CurSentence.charAt(p));

			int predAction = predActions.get(p);
			int goldAction = goldActions.get(p);
			if( predAction == goldAction)
			{
				if(predAction < 1000 && predAction >= 0)
				{
					tmpState.Sep(curSChar, predAction);
					goldState.Sep(curSChar, predAction);
				}						
				else if(predAction == 1000)
				{
					tmpState.Add(curSChar);
					goldState.Add(curSChar);
				}
				else if(predAction == 2000)
				{
					System.out.println("Impossible.....");
					tmpState.Finish();
					goldState.Finish();
				}
				else
				{
					System.out.println("error gold action: " + Integer.toString(predAction));
				}
				
				currentScore = GetLocalFeaturesScoreByZMS(tmpState, null, false);
				scores[0] += currentScore;
				scores[1] += currentScore;
			}
			else
			{
				break;
			}
		}
		if(p >= predActions.size())
		{
			System.out.println("Impossible.....");
		}
		List<String> goldFeatures = new ArrayList<String>();
		List<String> predFeatures = new ArrayList<String>();
		for(; p < predActions.size(); p++)
		{
			String curSChar = "";
			if(p < this.CurSentence.length()) curSChar = String.valueOf(CurSentence.charAt(p));
			int predAction = predActions.get(p);
			int goldAction = goldActions.get(p);
			
			if(predAction < 1000 && predAction >= 0)
			{
				tmpState.Sep(curSChar, predAction);
			}						
			else if(predAction == 1000)
			{
				tmpState.Add(curSChar);
			}
			else if(predAction == 2000)
			{
				tmpState.Finish();
			}
			else
			{
				System.out.println("error gold action: " + Integer.toString(predAction));
			}
			
			currentScore = GetLocalFeaturesScoreByZMS(tmpState, predFeatures, false);
			
			scores[0] += currentScore;
			
			
			if(goldAction < 1000 && goldAction >= 0)
			{
				goldState.Sep(curSChar, goldAction);
			}						
			else if(goldAction == 1000)
			{
				goldState.Add(curSChar);
			}
			else if(goldAction == 2000)
			{
				goldState.Finish();
			}
			else
			{
				System.out.println("error gold action: " + Integer.toString(goldAction));
			}
			
			currentScore = GetLocalFeaturesScoreByZMS(goldState, goldFeatures, false);
			scores[1] += currentScore;
			
		}
		
		model.UpdateWeighth(goldFeatures,  1, curRoundIndexForTrain);
		model.UpdateWeighth(predFeatures, -1, curRoundIndexForTrain);
		
		return scores;
	}
	
	
	public static String UnTagSentence(String tagSequence)
	{
		String sentence = "";
    	StringTokenizer token=new StringTokenizer(tagSequence," "); 
		while ( token.hasMoreElements() ){
			String tempStr = token.nextToken();
			int index = tempStr.indexOf("_");
			String theWord = tempStr.substring(0, index);
			//String thePOS = tempStr.substring(index+1, tempStr.length());
			sentence = sentence + theWord;			
		}
    	return sentence;
	}
	
    public static List<Integer> getGoldActions(String tagSequence)
    {
    	List<Integer> goldActions = new ArrayList<Integer>();
    	
    	StringTokenizer token=new StringTokenizer(tagSequence," "); 
		while ( token.hasMoreElements() ){
			String tempStr = token.nextToken();
			int index = tempStr.indexOf("_");
			String theWord = tempStr.substring(0, index);
			String thePOS = tempStr.substring(index+1, tempStr.length());
			int thePOSID = -1;
			for(int idx = 0; idx < State.arrPOS.length; idx++)
			{
				if(thePOS.equalsIgnoreCase(State.arrPOS[idx]))
				{
					thePOSID = idx;
					break;
				}
			}
			goldActions.add(thePOSID);
			for(int idx = 1; idx < theWord.length(); idx++)
			{
				goldActions.add(1000);
			}
			
		}
		goldActions.add(2000);
    	return goldActions;
    }
    
    
    public boolean CanSeperate(State state, String curSChar, String thePOS)
    {
    	//boolean bValid = true;
    	
    	if(model.m_startCharFreq.containsKey(curSChar) && 
    			model.m_startCharFreq.get(curSChar) > 10
    			&& !model.m_startCharPOSSets.get(curSChar).containsKey(thePOS))
    	{
    		return false;
    	}
    	int length = state.size;
    	if(length >= 1)
    	{
    		String theLastWord = state.arrWord[length-1];
    		String theLastPos = state.arrTag[length-1];
    		
        	if(model.m_wordFreq.containsKey(theLastWord) &&
        			model.m_wordFreq.get(theLastWord) > 10
        			&& !model.m_wordPOSSets.get(theLastWord).containsKey(theLastPos))
        	{
        		return false;
        	}
        	
        	if(model.m_posCloseSet.containsKey(theLastPos)
        			&& !model.m_posCloseSet.get(theLastPos).contains(theLastWord))
        	{
        		return false;
        	}
    	}
    	
    	
    	
    	
    	
    	return true;
    }
}

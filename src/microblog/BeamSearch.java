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
//Joint Model For WSD Segmentation POS 
public class BeamSearch {
	// 传参文件
	public Double MINVALUE = -Double.MAX_VALUE;
	public String train_file = "";
	public String dev_file = ""; // 与训练文件格式相同，作为黄金标准
	public String test_file = ""; // 与训练文件格式相同，作为黄金标准
	public String output_file = "out";
	public String model_file = "";
	public String sense_file =""; //WSD:表示每个单词的词性列表，normalization表示非标准化词的标准化单词
	public String lmChar_file = ""; //语言模型, fromat:	words 	prob	type	ngramNum
	public String lmWord_file = ""; //语言模型, fromat:	words 	prob	type	ngramNum
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
	//语言特征
	public HashMap<String, Integer> hsngramChar;  //char lm， 概率所属类别 （1，0-10%，2 10%-20%）
	public HashMap<String, Integer> hsngramWord;  //word lm， 概率所属类别 （1，0-10%，2 10%-20%）
	
	public List<String> arrTrainSource; // train sentences
	public List<State> bestStates;// 当前最好的K个状态。
	public List<Integer> goldActions = null;// 标准状态
	public State  goldSentenceState = null; //标准状态

	public List<String> arrTestSource; // test sentences
	public List<String> arrTestResult;
	
	public List<String> arrDevSource; // test sentences
	public List<String> arrDevResult;

	int curRoundIndexForTrain = 0;
	int preRoundIndexForTrain = 0; 
	public Model model = new Model(); // 特征model;存储特征及权重
	
	int curTrainIterCorrectInstance = 0;


	private String CurSentence = "";// 待处理的句子
	// private int lenofSentence=0;//待处理的句子长度
	public State[] agenda;// 当前标注序列集

	public BufferedWriter bwlog;
	public SimpleDateFormat df = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss SSS ");// 设置日期格式
	HeapSort heapSort = new HeapSort();
	
	public HashMap<String, String> hmWordSenseSet = new HashMap<String, String>();//单词词义集（在WSD表示词义集，在Normalization中表示非规范化词集)
    public boolean IsSPModel = true; //true:分词联合模型； false: WSD,Seg,Pos Model
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
			int search_width, String outfile_path, String sense_file, String out_path, String lmChar_file, String lmWord_file) {
		this.train_file = train_file;
		this.number_of_train = number_of_train;
		this.model_file = model_file;
		this.number_of_iterations = number_of_iterations;
		this.bNewTrain = bNewTrain;
		this.search_width = search_width;
		this.output_path = output_path;
		this.sense_file = sense_file;
		this.lmChar_file = lmChar_file;
		this.lmWord_file = lmWord_file;
		try {
			bwlog = new BufferedWriter(new FileWriter(this.output_path + "log.txt"));
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
	public BeamSearch(String test_file,  String model_file,
			String output_file, String evaluationError_file, int search_width, String sense_file,String out_path, String lm_file, String lmWord_file) {
		this.test_file = test_file;
		//this.number_of_test = number_of_test;
		this.model_file = model_file;
		this.output_file = output_file;
		this.evaluationError_file = evaluationError_file;
		this.search_width = search_width;
		this.sense_file = sense_file;
		this.output_path = out_path;
		this.lmChar_file = lmChar_file;
		this.lmWord_file = lmWord_file;
		try {
			bwlog = new BufferedWriter(new FileWriter(this.output_path + "log.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BeamSearch(String train_file, int number_of_train,
			String model_file, int number_of_iterations, boolean bNewTrain,
			int search_width, String test_file, int number_of_test,
			String dev_file, int number_of_dev,
			String output_path, String sense_file, String log_file, String lmChar_file, String lmWord_file) {
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
		this.sense_file = sense_file;
		this.lmChar_file = lmChar_file;
		this.lmWord_file = lmWord_file;
		//this.charPos_file = charPos_file;
		//this.wordPos_file = wordPos_file;

		try {
			bwlog = new BufferedWriter(new FileWriter(output_path + log_file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void trainDevTestProcess() throws Exception {

		//System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
		//initialWordPOS();
		initialSense();
		initialTrain();
		initialTest();
		initialDev();
		initialLM();//初始化语言模型
		
		curRoundIndexForTrain = preRoundIndexForTrain;
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
			//model.save(model_file+n);
			bwlog.write("train round end:"+ n+"   " +df.format(new Date())+"\r\n");
			
			bwlog.write("develop start:"+"\r\n");
			// 测试
			this.arrDevResult.clear();
			for (int i = 0; i < arrDevSource.size(); i++) {

				this.CurSentence = UnTagSentence(this.arrDevSource.get(i));
				this.arrDevResult.add(Decoder());
			}
			save(this.arrTestResult, output_path + this.output_file + n);
			
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
			save(this.arrTestResult, output_path + this.output_file + n);
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
		this.initialSense();
		curRoundIndexForTrain = preRoundIndexForTrain;
		for (int n = 0; n < number_of_iterations; n++) {
			curTrainIterCorrectInstance = 0;
			for (int i = 0; i < arrTrainSource.size(); i++) {
				//GoldState = TagSeConvertState(this.arrTrainSource[i]);
				//this.arrCurSentecFeature = GetFeatureBySentence(this.arrTrainSource[i]);
				this.CurSentence = UnTagSentence(this.arrTrainSource.get(i));
				this.goldActions = getGoldActions(this.arrTrainSource.get(i));
				trainer(n, i);
			}
			model.AveWeight(curRoundIndexForTrain);
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
			this.initialSense();
			this.arrTestResult.clear();
			this.model.load(model_file);
			for (int i = 0; i < arrTestSource.size(); i++) {
				this.CurSentence = UnTagSentence(this.arrTestSource.get(i));
				this.arrTestResult.add(Decoder());
			}
			save(this.arrTestResult, this.output_file);
			Evaluator eva = new Evaluator(this.arrTestResult, arrTestSource, bwlog, this.output_path+ this.evaluationError_file);
			eva.Computer();
			
			bwlog.flush();
			bwlog.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 测试
	 */
	public void testNoEvalProcess() {
		try {
			initialTest();
			this.initialSense();
			this.model.load(model_file);
			this.arrTestResult.clear();
			for (int i = 0; i < arrTestSource.size(); i++) {
				//this.CurSentence = UnTagSentence(this.arrTestSource.get(i));
				this.CurSentence = this.arrTestSource.get(i);
				this.arrTestResult.add(Decoder());
			}
			save(this.arrTestResult, this.output_file);
			//Evaluator eva = new Evaluator(this.arrTestResult, arrTestSource, bwlog, this.output_path+ this.evaluationError_file);
			//eva.Computer();
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
		this.arrTrainSource = new ArrayList<String>();
		if (this.bNewTrain == true) {
			this.model.newFeatureTemplates();
			this.model.init(this.train_file, bNewTrain);
		} else {
			this.preRoundIndexForTrain = this.model.load(model_file);
			this.model.init(this.train_file, bNewTrain);
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
		
		
		System.out.print("setnece:" + sentenceIndex + " begin:");// +df.format(newDate())); //new Date()为获取当前系统时间
		System.out.flush();
		long st1 = System.nanoTime();
		curRoundIndexForTrain++;
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
				
				ArrayList<String> arrSenseSet = new ArrayList(); 
				if(state.size>=1){
					arrSenseSet = GetNormalSetByWord(state.arrWord[state.size-1]);					
				}

				if (i > 0  && i< this.CurSentence.length()) {
					//if(canAction(state, curSChar, state.arrTag[state.size-1], 1) == true){					
					State tempCands = Append(state, curSChar, null, false); // append action
					if (tempCands.score > temAgenda[0].score) {
						if (state.bIsGold == true
								&& this.goldActions.get(i) == 1000 ) {
							tempCands.bIsGold = true;
						} else
							tempCands.bIsGold = false;
						heapSort.BestAgendaSort(temAgenda, tempCands);
					}
					//}				
				}
				if(i== this.CurSentence.length()){//最后一个伪字符 #_PU
					if(arrSenseSet.size()>0){
					    for(int m=0;m<arrSenseSet.size();m++){					    	
					    	State temCand = Finish(state, null, false, arrSenseSet.get(m));// end action
							if (temCand.score >= temAgenda[0].score) {
								if (state.bIsGold == true && this.goldActions.get(i) == 2000) {
									if(temCand.size>=1){										
										if(equalNormal(temCand, this.goldSentenceState, 1) == true)
										{
											temCand.bIsGold = true;
										}else{
											temCand.bIsGold = false;
										}
									}else{
										temCand.bIsGold = true;
									}
								} else
									temCand.bIsGold = false;
								heapSort.BestAgendaSort(temAgenda, temCand);
							}
					    }						
					}				
				}else {
					for (int k = 0; k < State.arrPOS.length; k++) {
						if(i==0){
							{//不规范候选
								State temCand = Sep(state, curSChar, k, null, false, "");// seperate action
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
						if(CanSeperate(state, curSChar,State.arrPOS[k]) == true){	
							if(arrSenseSet.size()>0){
							    for(int m=0;m<arrSenseSet.size();m++){							    	
							    	State temCand = Sep(state, curSChar, k, null, false, arrSenseSet.get(m));// seperate action
									if (temCand.score >= temAgenda[0].score) {	
										
										if (state.bIsGold == true && this.goldActions.get(i) == k ) {
											if(equalNormal(temCand, this.goldSentenceState,2)== true){
												temCand.bIsGold = true;
											}else{
												temCand.bIsGold = false;
											}											
										} else
											temCand.bIsGold = false;
										
										heapSort.BestAgendaSort(temAgenda, temCand);
									}
							    }								
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
				State bestState = Best(this.agenda, 1)[0];
				List<Integer> predActions = bestState.hisActions;			
				double bestScore = bestState.score;				
				double[] scores = updateParameters(predActions, bestState);
				
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
			
			double[] scores = updateParameters(predActions, this.agenda[0]);
			//curRoundIndexForTrain++;
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
				
				ArrayList<String> arrSenseSet = new ArrayList(); //对前一个字符进行标注化
				if(state.size>=1){
					arrSenseSet = GetNormalSetByWord(state.arrWord[state.size-1]);
				}
				
				if (i > 0 && i< this.CurSentence.length()) {//最后一个伪结束符只能sep					
						State tempCands = Append(state, curSChar, null, true); // append
						if (tempCands.score > temAgenda[0].score) {													// action
							heapSort.BestAgendaSort(temAgenda, tempCands);
						}					
				}
				if(i== this.CurSentence.length()){//最后一个伪字符 #_PU					
					if(arrSenseSet.size()>0){
					    for(int m=0;m<arrSenseSet.size();m++){
					    	State temCand = Finish(state, null, true, arrSenseSet.get(m));// seperate
							if (temCand.score > temAgenda[0].score) {															
								heapSort.BestAgendaSort(temAgenda, temCand);
							}
					    }						
					}
				}else{
					for (int k = 0; k < State.arrPOS.length; k++) {
						if(i==0){
							State temCand = Sep(state, curSChar, k, null, true, "");// seperate
							if (temCand.score > temAgenda[0].score) {															
								heapSort.BestAgendaSort(temAgenda, temCand);
							}
						}
						if(CanSeperate(state, curSChar,State.arrPOS[k]) == true){													
							if(arrSenseSet.size()>0){
							    for(int m=0;m<arrSenseSet.size();m++){
							    	State temCand = Sep(state, curSChar, k, null, true, arrSenseSet.get(m));// seperate
									if (temCand.score > temAgenda[0].score) {															
										heapSort.BestAgendaSort(temAgenda, temCand);
									}
							    }								
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
		String n_1="", n_2="", n_3=""; //previous three word of normalization  sequence
		String start_n_1 = "", end_n_1 = "", end_n_2 = "";
		int len_n_1 =0, len_n_2=0;
		int len_w_1 = 0, len_w_2 = 0;
		int size = state.size;
		String normalSent = ""; 
		
		
		if (size > 0) {
			
			if(state.lastAction != 2000)
			{
				w_0 = state.arrWord[size - 1];
				t_0 = state.arrTag[size - 1];
				//n_0 = state.arrSense[size - 1];
				w_1 = "#S#";
				t_1 = "#T#";
				n_1 = "#S#";
				w_2 = "#S#";
				t_2 = "#T#";
				n_2 = "#S#";
				if (size > 1) {
					w_1 = state.arrWord[size - 2];
					t_1 = state.arrTag[size - 2];
					n_1 = state.arrNormal[size - 2];
				}
				if (size > 2) {
					w_2 = state.arrWord[size - 3];
					t_2 = state.arrTag[size - 3];
					n_2 = state.arrNormal[size - 3];
				}
				if(size>3){
					n_3 = state.arrNormal[size - 4];
				}
				
				for(int j=size - 3; j>=0;j--){
					normalSent = state.arrNormal[j] + normalSent;
					if(normalSent.length()>3){//因为是四元，只取前面三个字符						
						break;
					}				
				}				
				//normalSent = normalSent + n_1;				
			}
			else
			{
				w_0 = "#S#";
				t_0 = "#T#";
				//n_0 = "#N#";
				w_1 = state.arrWord[size - 1];
				t_1 = state.arrTag[size - 1];
				n_1 = state.arrNormal[size - 1];
				w_2 = "#S#";
				t_2 = "#T#";
				if (size > 1) {
					w_2 = state.arrWord[size - 2];
					t_2 = state.arrTag[size - 2];
					n_2 = state.arrNormal[size - 2];
				}
				if(size>2){
					n_3 = state.arrNormal[size - 3];
				}
				for(int j=size - 2; j>=0;j--){
					normalSent = state.arrNormal[j] + normalSent;
					if(normalSent.length()>3) break;
				}				
			}
			if(normalSent.length()>3)
				normalSent.substring(normalSent.length()-3, normalSent.length());
			normalSent = normalSent + n_1;
			
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
			
			if (n_1.equals("#S#")) {
				len_n_1 = 0;				
			} else {
				len_n_1 = n_1.length();
				if(len_w_1>5) len_n_1 = 5;
			}
			
			if (n_2.equals("#S#")) {
				len_n_2 = 0;
			} else {
				len_n_2 = n_2.length();
				if(len_n_2>5) len_n_2 = 5;
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
			if (len_n_1 > 0) {
				start_n_1 = n_1.substring(0, 1);
				end_n_1 = n_1.substring(n_1.length() - 1, n_1.length());
			} else {
				start_n_1 = "S1";
				end_n_1 = "S1";
			}
			if (len_n_2 > 0) {
				end_n_2 = n_2.substring(n_2.length() - 1, n_2.length());
			} else {
				end_n_2 = "S2";
			}

			// 构造特征
			Feature fe = null;
			String strfeat = null;
			if (state.lastAction == 1000) {// app
				strfeat = "OrgConsecutiveChars=" + c_1	+ c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgConsecutiveChars.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgTagByChar=" + t_0 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTagByChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				strfeat = "OrgTaggedCharByFirstChar=" + c_0 + t_0 + c_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTaggedCharByFirstChar.get(strfeat);
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
				/*strfeat = "OrgSeenWords=" + w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgSeenWords.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				strfeat = "OrgLastWordByWord=" + w_1 + "_" + w_2;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgLastWordByWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				
				if(len_w_1 == 1){
					strfeat = "OrgOneCharWord=" + w_1;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapOrgOneCharWord.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					
					strfeat = "OrgTagOfOneCharWord=" + c_2 + c_1 + c_0 + t_1;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapOrgTagOfOneCharWord.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;				
				}
				
				strfeat = "OrgFirstAndLastChars=" + start_w_1 + end_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgFirstAndLastChars.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgLengthByLastChar=" + end_w_1 + len_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgLengthByLastChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgLengthByFirstChar=" + start_w_1 + len_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgLengthByFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;					
				
				strfeat = "OrgCurrentWordLastChar=" + end_w_2 + "_" + w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgCurrentWordLastChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgLastWordByLastChar="	+ end_w_2 + end_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgLastWordByLastChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgLengthByLastWord=" + w_2 + len_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgLengthByLastWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				strfeat = "OrgLastLengthByWord=" + len_w_2 + w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgLastLengthByWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
					
				strfeat = "OrgCurrentTag=" + w_1 + t_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgCurrentTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				//if(len_w_1<=2){
				strfeat = "OrgTagByWordAndPrevChar=" + w_1 + t_1 + end_w_2;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTagByWordAndPrevChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgTagByLastWord=" + w_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTagByLastWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgTagByWordAndNextChar=" + w_1 + t_1 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTagByWordAndNextChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;					
				//}
				
				strfeat = "OrgLastTagByWord=" + w_1 + t_2;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgLastTagByWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				strfeat = "OrgTagByLastChar=" + end_w_1 + t_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTagByLastChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				strfeat = "OrgLastTagByTag=" + t_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgLastTagByTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgTag0Tag1Size1=" + t_1 + t_0 + len_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTag0Tag1Size1.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgTag1Tag2Size1=" + t_2 + t_1 + len_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTag1Tag2Size1.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgTag0Tag1Tag2Size1=" + t_2 + t_1 + t_0 + len_w_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTag0Tag1Tag2Size1.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgLastTwoTagsByTag=" + t_2 + t_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgLastTwoTagsByTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
								
				strfeat = "OrgTagByChar=" + t_0 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTagByFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
								
				strfeat = "OrgFirstCharBy2Tags=" + t_0 + t_1 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgFirstCharBy2Tags.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;				

				strfeat = "OrgSeparateChars=" + c_1 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgSeparateChars.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;			
				
				strfeat = "OrgLastWordFirstChar=" + w_1 + "_" + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgLastWordFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgFirstCharLastWordByWord=" + start_w_1 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgFirstCharLastWordByWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;

				strfeat = "OrgTaggedSeparateChars=" + end_w_1 + t_1 + c_0 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTaggedSeparateChars.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgTagWordTag=" + t_2 + w_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTagWordTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;
				
				strfeat = "OrgWordTagTag="	+ w_2 + t_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgWordTagTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;				

				strfeat = "OrgTagByFirstChar="	+ start_w_1 + t_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapOrgTagByFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight : fe.weight;		
				
				for (int j = 0; j < len_w_1 - 1; ++j) {
					strfeat = "OrgTaggedCharByLastChar=" + w_1.substring(j, j + 1) + t_1 + end_w_1;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapOrgTaggedCharByLastChar.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
				}	*/
				
				
				
				//normalization
				strfeat = "NorSeenWords=" + n_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorSeenWords.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;

				strfeat = "NorLastWordByWord=" + n_1 + "_" + n_2;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorLastWordByWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;
				
				
				if(len_n_1 == 1){
					strfeat = "NorOneCharWord=" + n_1;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapNorOneCharWord.get(strfeat);
					if (fe != null)
						dScore += bAverage ?fe.aveWeight*1.0 : fe.weight*1.0;
					
					strfeat = "NorTagOfOneCharWord=" + c_2 + c_1 + c_0 + t_1;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapNorTagOfOneCharWord.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;			
				}
				
				strfeat = "NorFirstAndLastChars=" + start_n_1 + end_n_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorFirstAndLastChars.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;
				
				strfeat = "NorLengthByLastChar=" + end_n_1 + len_n_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorLengthByLastChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;
				
				strfeat = "NorLengthByFirstChar=" + start_n_1 + len_n_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorLengthByFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;					
				
				strfeat = "NorCurrentWordLastChar=" + end_n_2 + "_" + n_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorCurrentWordLastChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
				
				strfeat = "NorLastWordByLastChar="	+ end_n_2 + end_n_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorLastWordByLastChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
				
				strfeat = "NorLengthByLastWord=" + n_2 + len_n_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorLengthByLastWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;

				strfeat = "NorLastLengthByWord=" + len_n_2 + n_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorLastLengthByWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
					
				strfeat = "NorCurrentTag=" + n_1 + t_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorCurrentTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;

				//if(len_w_1<=2){
				strfeat = "NorTagByWordAndPrevChar=" + n_1 + t_1 + end_n_2;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorTagByWordAndPrevChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
				
				strfeat = "NorTagByLastWord=" + n_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorTagByLastWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
				
				strfeat = "NorTagByWordAndNextChar=" + n_1 + t_1 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorTagByWordAndNextChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;				
				
				
				strfeat = "NorLastTagByWord=" + n_1 + t_2;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorLastTagByWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;

				strfeat = "NorTagByLastChar=" + end_n_1 + t_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorTagByLastChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;

				strfeat = "NorLastTagByTag=" + t_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorLastTagByTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
				
				strfeat = "NorTag0Tag1Size1=" + t_1 + t_0 + len_n_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorTag0Tag1Size1.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
				
				strfeat = "NorTag1Tag2Size1=" + t_2 + t_1 + len_n_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorTag1Tag2Size1.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
				
				strfeat = "NorTag0Tag1Tag2Size1=" + t_2 + t_1 + t_0 + len_n_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorTag0Tag1Tag2Size1.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
				
				strfeat = "NorLastTwoTagsByTag=" + t_2 + t_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorLastTwoTagsByTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
								
				strfeat = "NorTagByChar=" + t_0 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorTagByFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;
								
				strfeat = "NorFirstCharBy2Tags=" + t_0 + t_1 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorFirstCharBy2Tags.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;				

				strfeat = "NorSeparateChars=" + c_1 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorSeparateChars.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;			
				
				strfeat = "NorLastWordFirstChar=" + n_1 + "_" + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorLastWordFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
				
				strfeat = "NorFirstCharLastWordByWord=" + start_n_1 + c_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorFirstCharLastWordByWord.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;

				strfeat = "NorTaggedSeparateChars=" + end_n_1 + t_1 + c_0 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorTaggedSeparateChars.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
				
				strfeat = "NorTagWordTag=" + t_2 + n_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorTagWordTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;
				
				strfeat = "NorWordTagTag="	+ n_2 + t_1 + t_0;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorWordTagTag.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;				

				strfeat = "NorTagByFirstChar="	+ start_n_1 + t_1;
				if(fvs != null)fvs.add(strfeat);
				fe = model.m_mapNorTagByFirstChar.get(strfeat);
				if (fe != null)
					dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;;		
				
				for (int j = 0; j < len_n_1 - 1; ++j) {
					strfeat = "NorTaggedCharByLastChar=" + n_1.substring(j, j + 1) + t_1 + end_n_1;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapNorTaggedCharByLastChar.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight*1.0 : fe.weight*1.0;
				}
				
				//语言模型 特征
				
				int len = normalSent.length();
				for(int i= len-len_n_1 ; i<len; i++){
					String word2 = "";
					String word3 = "";
					String word4 = "";
					if(i>=1){
						word2 =  normalSent.substring(i-1,i+1);
						word2 = CovertAddSpace(word2);
						Integer type2 = hsngramChar.get(word2);
						if(type2 != null ) //type2 =10;						
						{strfeat = "Gram2=Char"	+ type2;
						if(fvs != null)fvs.add(strfeat);
						fe = model.m_mapGram2.get(strfeat);
						if (fe != null)
							dScore += bAverage ? fe.aveWeight : fe.weight;	
						}
						
						if(i>=2){
							word3 =  normalSent.substring(i-2,i+1);
							word3 = CovertAddSpace(word3);
							Integer type3 = hsngramChar.get(word3);
							if(type3 != null ) //type3 =10;
							{					
							strfeat = "Gram3=Char"	+ type3;
							if(fvs != null)fvs.add(strfeat);
							fe = model.m_mapGram3.get(strfeat);
							if (fe != null)
								dScore += bAverage ? fe.aveWeight : fe.weight;	
							}
							
							
							if(i>=3){
								word4 =  normalSent.substring(i-3,i+1);	
								word4 = CovertAddSpace(word4);
								Integer type4 = hsngramChar.get(word4);
								if(type4 != null )// type4 =10;						
								{strfeat = "Gram4=Char"	+ type4;
								if(fvs != null)fvs.add(strfeat);
								fe = model.m_mapGram4.get(strfeat);
								if (fe != null)
									dScore += bAverage ? fe.aveWeight : fe.weight;	
								}
							}
						}
					}				
				}
				
				//基于词
				if(n_1.length()>0){
					String words =  n_1;
					Integer type4 = hsngramWord.get(words);
					if(type4 != null ) //type4 =10;						
					{strfeat = "Gram4=Word"	+ type4;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapGram4.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					}
				}
				if(n_2.length()>0){
					String words = n_2 + " " + n_1;
					Integer type2 = hsngramWord.get(words);
					if(type2 != null ) //type2 =10;						
					{strfeat = "Gram2=Word"	+ type2;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapGram2.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					}
				}
				if(n_3.length()>0){
					String words = n_3 + " " + n_2 + " " + n_1;
					Integer type3 = hsngramWord.get(words);
					if(type3 != null ) //type3 =10;						
					{strfeat = "Gram3=Word"	+ type3;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapGram3.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					}
				}
				
				
				//消歧词义特征  add by qiantao
				/*if(n_1!=null && n_1.length()>0){
					strfeat = "WordSense="	+ w_1 + n_1;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapWordSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;	
					
					strfeat = "LastWordAndWordSense="	+  n_1+w_0;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapLastWordAndWordSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					
					strfeat = "PreWordAndWordSense="	+  n_1+w_2;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapPreWordAndWordSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					
					strfeat = "EndPreAndWordSense="	+  n_1+end_w_2;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapEndPreAndWordSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;					
						
					
					strfeat = "LastCharAndWordSense=" + n_1 + c_0;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapLastCharAndWordSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					
					strfeat = "TagWordSense="	+ n_1 + t_1;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapTagWordSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					
					strfeat = "PreTagAndWordSense="	+  n_1 + t_2;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapPreTagAndWordSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					
					strfeat = "LastTagAndWordSense="	+  n_1 + t_0;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapLastTagAndWordSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					
					strfeat = "ThreeWordAndSense="	+  n_1+w_0+w_2;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapThreeWordAndSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					
					strfeat = "PreLastTagAndWordSense="	+  n_1+t_0+t_2;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapPreLastTagAndWordSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					
					if(n_2!=null && n_2.length()>0){
						
						strfeat = "TwoWordSense="	+  n_1+ n_2;
						if(fvs != null)fvs.add(strfeat);
						fe = model.m_mapTwoWordSense.get(strfeat);
						if (fe != null)
							dScore += bAverage ? fe.aveWeight : fe.weight;
						
						strfeat = "LastTagAndTwoWordSense="	+  n_1+ n_2 + t_0;
						if(fvs != null)fvs.add(strfeat);
						fe = model.m_mapLastTagAndTwoWordSense.get(strfeat);
						if (fe != null)
							dScore += bAverage ? fe.aveWeight : fe.weight;
						
						strfeat = "LastWordAndTwoWordSense="	+ n_1+ n_2 + w_0;
						if(fvs != null)fvs.add(strfeat);
						fe = model.m_mapLastWordAndTwoWordSense.get(strfeat);
						if (fe != null)
							dScore += bAverage ? fe.aveWeight : fe.weight;
						
					}					
				}*/
				
				/*if(n_2!=null && n_2.length()>0){
					strfeat = "LastTagAndPreWordSense="	+  n_2 + t_0;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapLastTagAndPreWordSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					
					strfeat = "LastWordAndPreWordSense="	+  n_2 + w_0;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapLastWordAndPreWordSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ? fe.aveWeight : fe.weight;
					
					strfeat = "LastCharAndPreWordSense="	+  n_2 + c_0;
					if(fvs != null)fvs.add(strfeat);
					fe = model.m_mapLastCharAndPreWordSense.get(strfeat);
					if (fe != null)
						dScore += bAverage ?fe.aveWeight : fe.weight;
				}*/
				
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
	private State Sep(State state, String curChar, int POSID, List<String> fvs, boolean bAverage, String preWordSense) {
		State newState = new State(state);
		newState.Sep(curChar, POSID, preWordSense);
		if(newState.score == this.MINVALUE)
			 newState.score=0;

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
	private State Finish(State state, List<String> fvs, boolean bAverage,String preWordSense) {
		State newState = new State(state);
		newState.Finish(preWordSense);
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
	
	public double[] updateParameters(List<Integer> predActions, State bestState)
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
			String preTmpWordSense = "";
			String preGoldWordSense = "";
			if(p < this.CurSentence.length()) curSChar = String.valueOf(CurSentence.charAt(p));

			int predAction = predActions.get(p);
			int goldAction = goldActions.get(p);
			
			if(tmpState.size>=1){
				preTmpWordSense = predAction==2000? this.GetPreNormal(bestState, p, true):this.GetPreNormal(bestState, p, false);
				preTmpWordSense = preTmpWordSense==null?"": preTmpWordSense;
			}
			if(goldState.size>=1) {
				preGoldWordSense = goldAction==2000?this.GetPreNormal(this.goldSentenceState, p, true): this.GetPreNormal(this.goldSentenceState, p, false);
				preGoldWordSense = preGoldWordSense==null?"": preGoldWordSense;
			}
			
			if( predAction == goldAction)
			{
				if(predAction < 1000 && predAction >= 0)
				{
					String curTmpWordSense = this.GetCurNormal(bestState, p);//还要词义相同
					curTmpWordSense = curTmpWordSense==null?"": curTmpWordSense;
					String curGoldWordSense = this.GetCurNormal(this.goldSentenceState, p);
					curGoldWordSense = curGoldWordSense==null?"": curGoldWordSense;
					if(curTmpWordSense.equals(curGoldWordSense)==false)
						break;				
					
					tmpState.Sep(curSChar, predAction, preTmpWordSense);
					goldState.Sep(curSChar, predAction, preGoldWordSense);
				}						
				else if(predAction == 1000)
				{
					tmpState.Add(curSChar);
					goldState.Add(curSChar);
				}
				else if(predAction == 2000)
				{
					System.out.println("Impossible.....");
					tmpState.Finish(preTmpWordSense);
					goldState.Finish(preGoldWordSense);
				}
				else
				{
					System.out.println("error gold action: " + Integer.toString(predAction));
				}
				//System.out.println("aa"+ tmpState.toString());
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
			String preTmpWordSense = "";
			String preGoldWordSense = "";
			if(p < this.CurSentence.length()) curSChar = String.valueOf(CurSentence.charAt(p));		
			
			int predAction = predActions.get(p);
			int goldAction = goldActions.get(p);
			if(tmpState.size>=1){
				preTmpWordSense = (predAction==2000)? this.GetPreNormal(bestState, p, true):this.GetPreNormal(bestState, p, false);
				preTmpWordSense = preTmpWordSense==null?"": preTmpWordSense;
			}
			if(goldState.size>=1){
				preGoldWordSense = (goldAction==2000)? this.GetPreNormal(this.goldSentenceState, p, true): this.GetPreNormal(this.goldSentenceState, p, false);
				preGoldWordSense = preGoldWordSense==null?"": preGoldWordSense;
			}
						
			if(predAction < 1000 && predAction >= 0)
			{
				tmpState.Sep(curSChar, predAction, preTmpWordSense);
			}						
			else if(predAction == 1000)
			{
				tmpState.Add(curSChar);
			}
			else if(predAction == 2000)
			{
				tmpState.Finish(preTmpWordSense);
			}
			else
			{
				System.out.println("error gold action: " + Integer.toString(predAction));
			}
			
			currentScore = GetLocalFeaturesScoreByZMS(tmpState, predFeatures, false);
			
			scores[0] += currentScore;
			
			
			if(goldAction < 1000 && goldAction >= 0)
			{
				goldState.Sep(curSChar, goldAction, preGoldWordSense);
			}						
			else if(goldAction == 1000)
			{
				goldState.Add(curSChar);
			}
			else if(goldAction == 2000)
			{
				goldState.Finish(preGoldWordSense);
			}
			else
			{
				System.out.println("error gold action: " + Integer.toString(goldAction));
			}
			//System.out.println("bb "+ goldState.toString());
			currentScore = GetLocalFeaturesScoreByZMS(goldState, goldFeatures, false);
			scores[1] += currentScore;
			
		}
		
		model.UpdateWeighth(goldFeatures,  1, curRoundIndexForTrain);
		model.UpdateWeighth(predFeatures, -1, curRoundIndexForTrain);
		
		return scores;
	}
	
	//tag sample:我_r 连_u 烧_v 开水_n 也_d 没_v 把握|001641_n 。_w
	public static String UnTagSentence(String tagSequence)
	{
		String sentence = "";
    	StringTokenizer token=new StringTokenizer(tagSequence," "); 
		while ( token.hasMoreElements() ){
			String tempStr = token.nextToken();
			int index = tempStr.indexOf("_");
			//System.out.println("untag "+tempStr);
			String theWordsense = tempStr.substring(0, index);
			int wordIndex =theWordsense.indexOf("|");
			String theWord ="";
			if(wordIndex<0){
				theWord=theWordsense;
			}else{
				theWord = theWordsense.substring(0,wordIndex);
			}
			
			//String thePOS = tempStr.substring(index+1, tempStr.length());
			sentence = sentence + theWord;			
		}
    	return sentence;
	}
	
	/**
	 * action:  1000: 表示app, 2000，final
	 * @param tagSequence
	 * @return
	 */
    public  List<Integer> getGoldActions(String tagSequence)
    {
    	List<Integer> goldActions = new ArrayList<Integer>();
    	this.goldSentenceState = new  State();
    	StringTokenizer token=new StringTokenizer(tagSequence," "); 
    	int wordNumber=0;
		while ( token.hasMoreElements() ){
			String tempStr = token.nextToken();
			int index = tempStr.indexOf("_");
			String theWordSense = tempStr.substring(0, index);
			int wordIndex = theWordSense.indexOf("|");	
			String theWord ="";
			String theSense = "";
			if(wordIndex<0){
				theWord = theWordSense;
				//goldSenses.add("");
				theSense=theWord;
			}else{
				theWord = theWordSense.substring(0,wordIndex);
				//goldSenses.add(theWordSense.substring(wordIndex+1, theWordSense.length()));
				theSense = theWordSense.substring(wordIndex+1, theWordSense.length());
			}
			
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
			goldSentenceState.arrWord[wordNumber]=theWord;
			goldSentenceState.arrNormal[wordNumber]=theSense;
			goldSentenceState.arrTag[wordNumber]=thePOS;
			wordNumber++;
			
		}
		goldSentenceState.size = wordNumber;
		
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
    		String theLastSense = state.arrNormal[length-1];
    		
        	if((model.m_wordFreq.containsKey(theLastWord) &&	model.m_wordFreq.get(theLastWord) > 10
        			&& !model.m_wordPOSSets.get(theLastWord).containsKey(theLastPos)) ||
        			(model.m_wordFreq.containsKey(theLastSense) &&	model.m_wordFreq.get(theLastSense) > 10
                			&& !model.m_wordPOSSets.get(theLastSense).containsKey(theLastPos)))
        	{
        		return false;
        	}
        	
        	if(model.m_posCloseSet.containsKey(theLastPos)
        			&& (!model.m_posCloseSet.get(theLastPos).contains(theLastWord) && !model.m_posCloseSet.get(theLastPos).contains(theLastSense) ))
        	{
        		return false;
        	}
        	
        	if(theLastSense !=null && theLastSense.length()>0){
	        	if((model.m_wordFreq.containsKey(theLastSense) &&	model.m_wordFreq.get(theLastSense) > 10)){
	        		if(model.m_wordPOSSets.containsKey(theLastSense) && !model.m_wordPOSSets.get(theLastSense).containsKey(theLastPos))
		    	    {
		        		return false;
		    	    }
	        	}
	    	
	    	   if(model.m_posCloseSet.containsKey(theLastPos)
	    			 && !model.m_posCloseSet.get(theLastPos).contains(theLastSense) )
	    	   {
	    		   return false;
	    	   }
        	}
    	}
    	return true;
    }
    
    public ArrayList<String> GetNormalSetByWord(String word){
    	ArrayList<String> senseSet = new ArrayList<String>();
    	senseSet.add(word);
    	String strTemp = this.hmWordSenseSet.get(word);//打 A|B|C  ABC表示词义或可替换单词 
    	if(strTemp !=null && strTemp.length()>0) {
	    	StringTokenizer token=new StringTokenizer(strTemp,"\\|");  
	    	while ( token.hasMoreElements()){
	    		senseSet.add(token.nextToken());    		
	    	}
    	}
    	
    	return senseSet;    	
    }  
    
   public void initialSense(){
	   this.hmWordSenseSet = new HashMap<String,String>();
		if(this.sense_file==null || this.sense_file.length()==0)
			return;
	   File file = new File(this.sense_file);
		BufferedInputStream fis;
		try {
			fis = new BufferedInputStream(new FileInputStream(file));
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis, "UTF8"));// 用50M的缓冲读取文本文件
			String line = "";
					
			while ((line = reader.readLine()) != null) {				
				if (line.trim().length() > 0) {	
					line = line.trim();
					int index = line.indexOf(" ");
					String word=line.substring(0,index);
					String senses= line.substring(index+1, line.length());
					hmWordSenseSet.put(word, senses);
				}
			}
			reader.close();
			
			fis.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }
   //initial language model
   public void initialLM(){
	   hsngramChar = new HashMap<String, Integer>();
	   hsngramWord = new HashMap<String, Integer>();
	   if(this.lmChar_file!=null && this.lmChar_file.length()>0)
	   {
		   File file = new File(this.lmChar_file);
			BufferedInputStream fis;
			try {
				fis = new BufferedInputStream(new FileInputStream(file));
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						fis, "UTF8"));// 用50M的缓冲读取文本文件
				String line = "";
						
				while ((line = reader.readLine()) != null) {				
					if (line.trim().length() > 0) {	
						line = line.trim();
						StringTokenizer token=new StringTokenizer(line,"\t"); 
						int count = token.countTokens();
						if(count==4){
						    String word = token.nextToken();
							token.nextToken();
							int iType = Integer.parseInt(token.nextToken());						
							hsngramChar.put(word, iType);
							
						}
						
					}
				}
				reader.close();			
				fis.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   }
	   
		if(this.lmWord_file !=null && this.lmWord_file.length()>0)
		 {
		   File file1 = new File(this.lmWord_file);
			BufferedInputStream fis1;
			try {
				fis1 = new BufferedInputStream(new FileInputStream(file1));
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						fis1, "UTF8"));// 用50M的缓冲读取文本文件
				String line = "";
						
				while ((line = reader.readLine()) != null) {				
					if (line.trim().length() > 0) {	
						line = line.trim();
						StringTokenizer token=new StringTokenizer(line,"\t"); 
						int count = token.countTokens();
						if(count==4){
						    String word = token.nextToken();
							token.nextToken();
							int iType = Integer.parseInt(token.nextToken());						
							hsngramWord.put(word, iType);
							
						}
						
					}
				}
				reader.close();			
				fis1.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	   
   }
   
   
   public String GetPreNormal(State state, int index, boolean  isFinish){
	   String strRet="";
	   if(isFinish == true){
		  return state.arrNormal[state.size-1];
	   }
	   int k=-1;
	   for(int i=0;i<state.size;i++ ){
		   k+=state.arrWord[i].length();
		   if(k>=index) {
			   if(i>0)  strRet = state.arrNormal[i-1];
			   break;
		   }
	   }
	   
	   return strRet;
   }
   
   public String GetCurNormal(State state, int index){
	   String strRet="";
	   int k=-1;
	   for(int i=0;i<state.size;i++ ){
		   k+=state.arrWord[i].length();
		   if(k>=index) {
			   strRet = state.arrNormal[i];
			   break;
		   }
	   }
	   
	   return strRet;
   }
   
   //比较最后一个词是标注是否相等。
   public boolean equalNormal(State trainState, State goldState, int k){
	   boolean bRet=false;
	   if(trainState.arrNormal[trainState.size-k]==null|| trainState.arrNormal[trainState.size-k].length()<1 ){
		   if(goldState.arrNormal[trainState.size-k]==null || goldState.arrNormal[trainState.size-k].length()<1){
			   bRet = true;
		   }else{
			   bRet = false;
		   }		   
	   }else{
		   if(goldState.arrNormal[trainState.size-k]==null || goldState.arrNormal[trainState.size-k].length()<1){
			   bRet = false;
		   }else{
			   if(trainState.arrNormal[trainState.size-k].equals(this.goldSentenceState.arrNormal[trainState.size-k])){
				   bRet = true;
			   }else{
				   bRet = false; 
			   }			   
		   }
	   }
	   return bRet;
	  
   }
  //字符间加一个空格 
  public String CovertAddSpace(String Str){
	  String sRet = "";
	  for(int i=0;i<Str.length();i++){
		  sRet += Str.substring(i,i+1)+" ";
	  }
	  return sRet.trim();
  }
    
}

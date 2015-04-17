package microblog;

import java.util.*;



public class Main {

	
	/**
	 * train command:  -train <train-file> <model-file> <number of iterations> <bNewTrain>
	 * test command:   -test <input-file>  <model-file> <output-file>
	 * @param args
	 */
	public static void main(String[] args) {
	
		// TODO Auto-generated method stub
		//CommandLineParser parser = new PosixParser();
		//try {
			
		//	CommandLine cmd = parser.parse(opts, args);
		/*
		if(args.length<7 || args.length>11){
			System.out.println("parameter error, please input again!");
			System.out.println("-train <train_file> <number of train> <model_file> <number of iterations> <bNewTrain> <search_width> <output_path");
			System.out.println("-test <test-file> <number of test> <model-file>  <output-file> <evaluation_file> <search_width>");
			System.out.println("<train_file> <number of train> <model_file> <number of iterations> <bNewTrain> <search_width> <test-file>  <number of test>");
			
		    return;
		}*/
		if(args[0].trim().equals("-train")){
			String train_file= args[1].trim();
			String model_file= args[2].trim();
			int number_of_train = Integer.parseInt(args[3].trim());
			int number_of_iterations= Integer.parseInt(args[4].trim());
			boolean bNewTrain= Boolean.parseBoolean(args[5].trim());
			int search_width = Integer.parseInt(args[6].trim());
			String output_path= args[7].trim();
			String sense_file=args[8].trim();  //鍗曡瘝璇箟鏂囦欢  姣忎竴琛岃〃绀猴細鍗曡瘝锛� sense1|sense2|sense3
			String lmChar_file = args[9].trim();
			String lmWord_file = args[10].trim();
			BeamSearch bs= new BeamSearch(train_file,number_of_train, model_file, number_of_iterations, bNewTrain, search_width, output_path, sense_file, output_path, lmChar_file,lmWord_file);		
			try {
				bs.trainProcess();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(args[0].trim().equals("-test")){
			String test_file= args[1].trim();
			String model_file= args[2].trim();			
			String out_file= args[3].trim();
			String evaluation_file= args[4].trim();
			int search_width = Integer.parseInt(args[5].trim());
			String sense_file=args[6].trim();
			String out_path = args[7].trim();
			String lmChar_file = args[8].trim();
			String lmWord_file = args[9].trim();
			BeamSearch bs= new BeamSearch(test_file, model_file, out_file, evaluation_file, search_width,sense_file, out_path, lmChar_file,lmWord_file);	
			bs.testProcess();
		}else if(args[0].trim().equals("-testNoEval")){
			String test_file= args[1].trim();
			String model_file= args[2].trim();
			//int number_of_test = Integer.parseInt(args[3].trim());
			String out_file= args[3].trim();			
			int search_width = Integer.parseInt(args[4].trim());
			String sense_file=args[5].trim();
			String out_path = args[6].trim();
			String lmChar_file = args[7].trim();
			String lmWord_file = args[8].trim();
			BeamSearch bs= new BeamSearch(test_file, model_file, out_file, "", search_width,sense_file, out_path, lmChar_file,lmWord_file);
			bs.testNoEvalProcess();		
		}
		else {//鍚屾椂璁粌娴嬭瘯
			String train_file= args[0].trim();
			String dev_file= args[1].trim();
			String test_file= args[2].trim();
			String model_file= args[3].trim();
			String output_path = args[4].trim();
			int number_of_iterations= Integer.parseInt(args[5].trim());	
			String sense_file=args[6].trim();
			int search_width = Integer.parseInt(args[7].trim());			
			String log_file = args[8].trim();
			String lmChar_file = args[9].trim();
			String lmWord_file = args[10].trim();
			int number_of_train = -1;
			int number_of_test = -1;
			int number_of_dev = -1;
			//int number_of_train = 100;
			//int number_of_test = 30;
			//int number_of_dev = 30;
			boolean bNewTrain= false;
			// 
			//int search_width = Integer.parseInt(args[6].trim());			
			//int number_of_train = Integer.parseInt(args[7].trim());
			//int number_of_test = Integer.parseInt(args[8].trim());	
			//boolean bNewTrain= Boolean.parseBoolean(args[9].trim());
			
			//String charpos_file = args[9].trim();
			//String wordpos_file = args[10].trim();
			BeamSearch bs= new BeamSearch(train_file,number_of_train, model_file, number_of_iterations, 
					bNewTrain, search_width, test_file, number_of_test,
					dev_file, number_of_dev, output_path,sense_file, log_file, lmChar_file,lmWord_file);	
			try {
				bs.trainDevTestProcess();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

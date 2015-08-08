package microblog;

import java.util.*;

public class Main {

	public static void main(String[] args) {
		if (args.length < 1) {
			SNThelp();
			return;
		}
		if (args[0].trim().equals("-train")) {
			String train_file = args[1].trim();
			String model_file = args[2].trim();
			int number_of_train = Integer.parseInt(args[3].trim());
			int number_of_iterations = Integer.parseInt(args[4].trim());
			boolean bNewTrain = Boolean.parseBoolean(args[5].trim());
			int search_width = Integer.parseInt(args[6].trim());
			String output_path = args[7].trim();
			String sense_file = args[8].trim();
			String lmChar_file = args[9].trim();
			String lmWord_file = args[10].trim();
			SNT bs = new SNT(train_file, number_of_train, model_file,
					number_of_iterations, bNewTrain, search_width, output_path,
					sense_file, output_path, lmChar_file, lmWord_file);
			try {
				bs.trainProcess();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (args[0].trim().equals("-test")) {
			String test_file = args[1].trim();
			String model_file = args[2].trim();
			String out_file = args[3].trim();
			String evaluation_file = args[4].trim();
			int search_width = Integer.parseInt(args[5].trim());
			String sense_file = args[6].trim();
			String out_path = args[7].trim();
			String lmChar_file = args[8].trim();
			String lmWord_file = args[9].trim();
			SNT bs = new SNT(test_file, model_file, out_file, evaluation_file,
					search_width, sense_file, out_path, lmChar_file,
					lmWord_file);
			bs.testProcess();
		} else if (args[0].trim().equals("-testNoEval")) {
			String test_file = args[1].trim();
			String model_file = args[2].trim();
			String out_file = args[3].trim();
			int search_width = Integer.parseInt(args[4].trim());
			String sense_file = args[5].trim();
			String out_path = args[6].trim();
			String lmChar_file = args[7].trim();
			String lmWord_file = args[8].trim();
			SNT bs = new SNT(test_file, model_file, out_file, "", search_width,
					sense_file, out_path, lmChar_file, lmWord_file);
			bs.testNoEvalProcess();
		} else if (args[0].trim().equals("-traintestEval")) {// training, test and evaluation simultaneously.
			String train_file = args[1].trim();
			String dev_file = args[2].trim();
			String test_file = args[3].trim();
			String model_file = args[4].trim();
			String output_path = args[5].trim();
			int number_of_iterations = Integer.parseInt(args[6].trim());
			String sense_file = args[7].trim();
			int search_width = Integer.parseInt(args[8].trim());
			String log_file = args[9].trim();
			String lmChar_file = args[10].trim();
			String lmWord_file = args[11].trim();
			boolean bNewTrain = Boolean.parseBoolean(args[12].trim());
			int number_of_train = -1;
			int number_of_test = -1;
			int number_of_dev = -1;
			SNT bs = new SNT(train_file, number_of_train, model_file,
					number_of_iterations, bNewTrain, search_width, test_file,
					number_of_test, dev_file, number_of_dev, output_path,
					sense_file, log_file, lmChar_file, lmWord_file);
			try {
				bs.trainDevTestProcess();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			SNThelp();
		}
	}

	private static void SNThelp() {
		System.out.println("parameter error, please input again!");
		System.out
				.println("-train <train_file> <model_file> <number of train>  <number of iterations> <bNewTrain> <search_width> <output_path> <dic_file> <charlm_file> <wordlm-file>");
		System.out
				.println("-test <test-file> <model-file> <output-file> <search_width> <dic_file> <out_path> <charlm_file> <wordlm-file>");
		System.out
				.println("-testNoEval <test_file> <model_file> <out_file> <search_width> <dic_file> <out_path> <charlm_file> <wordlm-file>");
		System.out
				.println("-traintestEval  <train_file> <dev_file> <test_file> <model_file> <output_path> <number of iterations> <search_width> <dic_file> <log_file> <charlm_file> <wordlm-file> <bNewTrain>");
		return;
	}
}

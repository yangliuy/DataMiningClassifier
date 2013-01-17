package com.pku.yangliu;

/**分类器主分类，依次执行数据预处理、朴素贝叶斯分类、KNN分类
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 *
 */
public class ClassifierMain {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//DataPreProcess DataPP = new DataPreProcess();
		//NaiveBayesianClassifier nbClassifier = new NaiveBayesianClassifier();
		KNNClassifier knnClassifier = new KNNClassifier();
		//DataPP.BPPMain(args);
		//nbClassifier.NaiveBayesianClassifierMain(args);
		knnClassifier.KNNClassifierMain(args);
	}
}

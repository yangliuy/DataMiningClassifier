package com.pku.yangliu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

/**利用朴素贝叶斯算法对newsgroup文档集做分类，采用十组交叉测试取平均值
 * 采用多项式模型,stanford信息检索导论课件上面言多项式模型比伯努利模型准确度高
 * 类条件概率P(tk|c)=(类c 下单词tk 在各个文档中出现过的次数之和+1)/(类c下单词总数+|V|)
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 */
public class NaiveBayesianClassifier {
	
	/**用贝叶斯法对测试文档集分类
	 * @param trainDir 训练文档集目录
	 * @param testDir 测试文档集目录
	 * @param classifyResultFileNew 分类结果文件路径
	 * @throws Exception 
	 */
	private void doProcess(String trainDir, String testDir,
			String classifyResultFileNew) throws Exception {
		// TODO Auto-generated method stub
		Map<String,Double> cateWordsNum = new TreeMap<String,Double>();//保存训练集每个类别的总词数
		Map<String,Double> cateWordsProb = new TreeMap<String,Double>();//保存训练样本每个类别中每个属性词的出现词数
		cateWordsProb = getCateWordsProb(trainDir);
		cateWordsNum = getCateWordsNum(trainDir);
		double totalWordsNum = 0.0;//记录所有训练集的总词数
		Set<Map.Entry<String,Double>> cateWordsNumSet = cateWordsNum.entrySet();
		for(Iterator<Map.Entry<String,Double>> it = cateWordsNumSet.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			totalWordsNum += me.getValue();
		}
		//下面开始读取测试样例做分类
		Vector<String> testFileWords = new Vector<String>();
		String word;
		File[] testDirFiles = new File(testDir).listFiles();
		FileWriter crWriter = new FileWriter(classifyResultFileNew);
		for(int i = 0; i < testDirFiles.length; i++){
			File[] testSample = testDirFiles[i].listFiles();
			for(int j = 0;j < testSample.length; j++){
				testFileWords.clear();
				FileReader spReader = new FileReader(testSample[j]);
				BufferedReader spBR = new BufferedReader(spReader);
				while((word = spBR.readLine()) != null){
					testFileWords.add(word);
				}
				//下面分别计算该测试样例属于二十个类别的概率
				File[] trainDirFiles = new File(trainDir).listFiles();
				BigDecimal maxP = new BigDecimal(0);
				String bestCate = null;
				for(int k = 0; k < trainDirFiles.length; k++){
					BigDecimal p = computeCateProb(trainDirFiles[k], testFileWords, cateWordsNum, totalWordsNum, cateWordsProb);
					if(k == 0){
						maxP = p;
						bestCate = trainDirFiles[k].getName();
						continue;
					}
					if(p.compareTo(maxP) == 1){
						maxP = p;
						bestCate = trainDirFiles[k].getName();
					}
				}
				crWriter.append(testSample[j].getName() + " " + bestCate + "\n");
				crWriter.flush();
			}
		}
		crWriter.close();
	}
	
	/**统计某类训练样本中每个单词的出现次数
	 * @param strDir 训练样本集目录
	 * @return Map<String,Double> cateWordsProb 用"类目_单词"对来索引的map,保存的val就是该类目下该单词的出现次数
	 * @throws IOException 
	 */
	public Map<String,Double> getCateWordsProb(String strDir) throws IOException{
		Map<String,Double> cateWordsProb = new TreeMap<String,Double>();
		File sampleFile = new File(strDir);
		File [] sampleDir = sampleFile.listFiles();
		String word;
		for(int i = 0;i < sampleDir.length; i++){
			File [] sample = sampleDir[i].listFiles();
			for(int j = 0; j < sample.length; j++){
				FileReader samReader = new FileReader(sample[j]);
				BufferedReader samBR = new BufferedReader(samReader);
				while((word = samBR.readLine()) != null){
					String key = sampleDir[i].getName() + "_" + word;
					if(cateWordsProb.containsKey(key)){
						double count = cateWordsProb.get(key) + 1.0;
						cateWordsProb.put(key, count);
					}
					else {
						cateWordsProb.put(key, 1.0);
					}
				}
			}
		}
		return cateWordsProb;	
	}
	
	/**计算某一个测试样本属于某个类别的概率
	 * @param Map<String, Double> cateWordsProb 记录每个目录中出现的单词及次数 
	 * @param File trainFile 该类别所有的训练样本所在目录
	 * @param Vector<String> testFileWords 该测试样本中的所有词构成的容器
	 * @param double totalWordsNum 记录所有训练样本的单词总数
	 * @param Map<String, Double> cateWordsNum 记录每个类别的单词总数
	 * @return BigDecimal 返回该测试样本在该类别中的概率
	 * @throws Exception 
	 * @throws IOException 
	 */
	private BigDecimal computeCateProb(File trainFile, Vector<String> testFileWords, Map<String, Double> cateWordsNum, double totalWordsNum, Map<String, Double> cateWordsProb) throws Exception {
		// TODO Auto-generated method stub
		BigDecimal probability = new BigDecimal(1);
		double wordNumInCate = cateWordsNum.get(trainFile.getName());
		BigDecimal wordNumInCateBD = new BigDecimal(wordNumInCate);
		BigDecimal totalWordsNumBD = new BigDecimal(totalWordsNum);
		for(Iterator<String> it = testFileWords.iterator(); it.hasNext();){
			String me = it.next();
			String key = trainFile.getName()+"_"+me;
			double testFileWordNumInCate;
			if(cateWordsProb.containsKey(key)){
				testFileWordNumInCate = cateWordsProb.get(key);
			}else testFileWordNumInCate = 0.0;
			BigDecimal testFileWordNumInCateBD = new BigDecimal(testFileWordNumInCate);
			BigDecimal xcProb = (testFileWordNumInCateBD.add(new BigDecimal(0.0001))).divide(totalWordsNumBD.add(wordNumInCateBD),10, BigDecimal.ROUND_CEILING);
			probability = probability.multiply(xcProb);
		}
		BigDecimal res = probability.multiply(wordNumInCateBD.divide(totalWordsNumBD,10, BigDecimal.ROUND_CEILING));
		return res;
	}

	/**获得每个类目下的单词总数
	 * @param trainDir 训练文档集目录
	 * @return Map<String, Double> <目录名，单词总数>的map
	 * @throws IOException 
	 */
	private Map<String, Double> getCateWordsNum(String trainDir) throws IOException {
		// TODO Auto-generated method stub
		Map<String,Double> cateWordsNum = new TreeMap<String,Double>();
		File[] sampleDir = new File(trainDir).listFiles();
		for(int i = 0; i < sampleDir.length; i++){
			double count = 0;
			File[] sample = sampleDir[i].listFiles();
			for(int j = 0;j < sample.length; j++){
				FileReader spReader = new FileReader(sample[j]);
				BufferedReader spBR = new BufferedReader(spReader);
				while(spBR.readLine() != null){
					count++;
				}		
			}
			cateWordsNum.put(sampleDir[i].getName(), count);
		}
		return cateWordsNum;
	}
	
	/**根据正确类目文件和分类结果文件统计出准确率
	 * @param classifyResultFile 正确类目文件
	 * @param classifyResultFileNew 分类结果文件
	 * @return double 分类的准确率
	 * @throws IOException 
	 */
	double computeAccuracy(String classifyResultFile,
			String classifyResultFileNew) throws IOException {
		// TODO Auto-generated method stub
		Map<String,String> rightCate = new TreeMap<String,String>();
		Map<String,String> resultCate = new TreeMap<String,String>();
		rightCate = getMapFromResultFile(classifyResultFile);
		resultCate = getMapFromResultFile(classifyResultFileNew);
		Set<Map.Entry<String, String>> resCateSet = resultCate.entrySet();
		double rightCount = 0.0;
		for(Iterator<Map.Entry<String, String>> it = resCateSet.iterator(); it.hasNext();){
			Map.Entry<String, String> me = it.next();
			if(me.getValue().equals(rightCate.get(me.getKey()))){
				rightCount ++;
			}
		}
		computerConfusionMatrix(rightCate,resultCate);
		return rightCount / resultCate.size();	
	}
	
	/**根据正确类目文件和分类结果文计算混淆矩阵并且输出
	 * @param rightCate 正确类目对应map
	 * @param resultCate 分类结果对应map
	 * @return double 分类的准确率
	 * @throws IOException 
	 */
	private void computerConfusionMatrix(Map<String, String> rightCate,
			Map<String, String> resultCate) {
		// TODO Auto-generated method stub	
		int[][] confusionMatrix = new int[20][20];
		//首先求出类目对应的数组索引
		SortedSet<String> cateNames = new TreeSet<String>();
		Set<Map.Entry<String, String>> rightCateSet = rightCate.entrySet();
		for(Iterator<Map.Entry<String, String>> it = rightCateSet.iterator(); it.hasNext();){
			Map.Entry<String, String> me = it.next();
			cateNames.add(me.getValue());
		}
		cateNames.add("rec.sport.baseball");//防止数少一个类目
		String[] cateNamesArray = cateNames.toArray(new String[0]);
		Map<String,Integer> cateNamesToIndex = new TreeMap<String,Integer>();
		for(int i = 0; i < cateNamesArray.length; i++){
			cateNamesToIndex.put(cateNamesArray[i],i);
		}
		for(Iterator<Map.Entry<String, String>> it = rightCateSet.iterator(); it.hasNext();){
			Map.Entry<String, String> me = it.next();
			confusionMatrix[cateNamesToIndex.get(me.getValue())][cateNamesToIndex.get(resultCate.get(me.getKey()))]++;
		}
		//输出混淆矩阵
		double[] hangSum = new double[20];
		System.out.print("    ");
		for(int i = 0; i < 20; i++){
			System.out.print(i + "    ");
		}
		System.out.println();
		for(int i = 0; i < 20; i++){
			System.out.print(i + "    ");
			for(int j = 0; j < 20; j++){
				System.out.print(confusionMatrix[i][j]+"    ");
				hangSum[i] += confusionMatrix[i][j];
			}
			System.out.println(confusionMatrix[i][i] / hangSum[i]);
		}
		System.out.println();
	}

	/**从分类结果文件中读取map
	 * @param classifyResultFileNew 类目文件
	 * @return Map<String, String> 由<文件名，类目名>保存的map
	 * @throws IOException 
	 */
	private Map<String, String> getMapFromResultFile(
			String classifyResultFileNew) throws IOException {
		// TODO Auto-generated method stub
		File crFile = new File(classifyResultFileNew);
		FileReader crReader = new FileReader(crFile);
		BufferedReader crBR = new BufferedReader(crReader);
		Map<String, String> res = new TreeMap<String, String>();
		String[] s;
		String line;
		while((line = crBR.readLine()) != null){
			s = line.split(" ");
			res.put(s[0], s[1]);	
		}
		return res;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public void NaiveBayesianClassifierMain(String[] args) throws Exception {
		 //TODO Auto-generated method stub
		//首先创建训练集和测试集
		CreateTrainAndTestSample ctt = new CreateTrainAndTestSample();
		NaiveBayesianClassifier nbClassifier = new NaiveBayesianClassifier();
		ctt.filterSpecialWords();//根据包含非特征词的文档集生成只包含特征词的文档集到processedSampleOnlySpecial目录下
		double[] accuracyOfEveryExp = new double[10];
		double accuracyAvg,sum = 0;
		for(int i = 0; i < 10; i++){//用交叉验证法做十次分类实验，对准确率取平均值	
			String TrainDir = "F:/DataMiningSample/TrainSample"+i;
			String TestDir = "F:/DataMiningSample/TestSample"+i;
			String classifyRightCate = "F:/DataMiningSample/classifyRightCate"+i+".txt";
			String classifyResultFileNew = "F:/DataMiningSample/classifyResultNew"+i+".txt";
			ctt.createTestSamples("F:/DataMiningSample/processedSampleOnlySpecial", 0.9, i,classifyRightCate);
			nbClassifier.doProcess(TrainDir,TestDir,classifyResultFileNew);
			accuracyOfEveryExp[i] = nbClassifier.computeAccuracy (classifyRightCate, classifyResultFileNew);
			System.out.println("The accuracy for Naive Bayesian Classifier in "+i+"th Exp is :" + accuracyOfEveryExp[i]);
		}
		for(int i = 0; i < 10; i++){
			sum += accuracyOfEveryExp[i];
		}
		accuracyAvg = sum / 10;
		System.out.println("The average accuracy for Naive Bayesian Classifier in all Exps is :" + accuracyAvg);
		
	}
}

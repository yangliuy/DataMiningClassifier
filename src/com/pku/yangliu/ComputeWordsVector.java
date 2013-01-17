package com.pku.yangliu;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Iterator;

/**计算文档的属性向量，将所有文档向量化
 * @author yangliu
 * @qq 772330184 
 * @mail yang.liu@pku.edu.cn
 */
public class ComputeWordsVector {
	
	/**计算文档的TF属性向量,直接写成二维数组遍历形式即可，没必要递归
	 * @param strDir 处理好的newsgroup文件目录的绝对路径
	 * @param trainSamplePercent 训练样例集占每个类目的比例
	 * @param indexOfSample 测试样例集的起始的测试样例编号
	 * @param wordMap 属性词典map
	 * @throws IOException 
	 */
	public void computeTFMultiIDF(String strDir, double trainSamplePercent, int indexOfSample, Map<String, Double> iDFPerWordMap, Map<String, Double> wordMap) throws IOException{
		File fileDir = new File(strDir);
		String word;
		SortedMap<String,Double> TFPerDocMap = new TreeMap<String,Double>();
		//注意可以用两个写文件，一个专门写测试样例，一个专门写训练样例，用sampleType的值来表示
		String trainFileDir = "F:/DataMiningSample/docVector/wordTFIDFMapTrainSample"+indexOfSample;
		String testFileDir = "F:/DataMiningSample/docVector/wordTFIDFMapTestSample"+indexOfSample;
		FileWriter tsTrainWriter = new FileWriter(new File(trainFileDir));
		FileWriter tsTestWrtier = new FileWriter(new File(testFileDir));
		FileWriter tsWriter = tsTrainWriter;
		File[] sampleDir = fileDir.listFiles();
		for(int i = 0; i < sampleDir.length; i++){
			String cateShortName = sampleDir[i].getName();
			System.out.println("compute: " + cateShortName);
			File[] sample = sampleDir[i].listFiles();
			double testBeginIndex = indexOfSample*(sample.length * (1-trainSamplePercent));//测试样例的起始文件序号
			double testEndIndex = (indexOfSample+1)*(sample.length * (1-trainSamplePercent));//测试样例集的结束文件序号
			System.out.println("dirName_total length:"+sampleDir[i].getCanonicalPath()+"_"+sample.length);
			System.out.println(trainSamplePercent + " length:"+sample.length * trainSamplePercent +" testBeginIndex:"+testBeginIndex+" testEndIndex"+ testEndIndex);	
			for(int j = 0;j < sample.length; j++){
				TFPerDocMap.clear();
				FileReader samReader = new FileReader(sample[j]);
				BufferedReader samBR = new BufferedReader(samReader);
				String fileShortName = sample[j].getName();
				Double wordSumPerDoc = 0.0;//计算每篇文档的总词数
				while((word = samBR.readLine()) != null){
					if(!word.isEmpty() && wordMap.containsKey(word)){//必须是属性词典里面的词，去掉的词不考虑
						wordSumPerDoc++;
						if(TFPerDocMap.containsKey(word)){
							Double count =  TFPerDocMap.get(word);
							TFPerDocMap.put(word, count + 1);
						}
						else {
							TFPerDocMap.put(word, 1.0);
						}
					}
				}
				//遍历一下当前文档的TFmap，除以文档的总词数换成词频,然后将词频乘以词的IDF，得到最终的特征权值，并且输出到文件
				//注意测试样例和训练样例写入的文件不同
				if(j >= testBeginIndex && j <= testEndIndex){
					tsWriter = tsTestWrtier;
				}
				else{
					tsWriter = tsTrainWriter;
				}
				Double wordWeight;
				Set<Map.Entry<String, Double>> tempTF = TFPerDocMap.entrySet();
				for(Iterator<Map.Entry<String, Double>> mt = tempTF.iterator(); mt.hasNext();){
					Map.Entry<String, Double> me = mt.next();
					//wordWeight =  (me.getValue() / wordSumPerDoc) * IDFPerWordMap.get(me.getKey());
					//由于计算IDF非常耗时，3万多个词的属性词典初步估计需要25个小时，先尝试认为所有词的IDF都是1的情况
					wordWeight =  (me.getValue() / wordSumPerDoc) * 1.0;
					TFPerDocMap.put(me.getKey(), wordWeight);
				}
				tsWriter.append(cateShortName + " ");
				String keyWord = fileShortName.substring(0,5);
				tsWriter.append(keyWord+ " ");
				Set<Map.Entry<String, Double>> tempTF2 = TFPerDocMap.entrySet();
				for(Iterator<Map.Entry<String, Double>> mt = tempTF2.iterator(); mt.hasNext();){
					Map.Entry<String, Double> ne = mt.next();
					tsWriter.append(ne.getKey() + " " + ne.getValue() + " ");
				}
				tsWriter.append("\n");	
				tsWriter.flush();
			}
		}
		tsTrainWriter.close();
		tsTestWrtier.close();
		tsWriter.close();
	}
	
	/**统计每个词的总的出现次数，返回出现次数大于3次的词汇构成最终的属性词典
	 * @param strDir 处理好的newsgroup文件目录的绝对路径
	 * @throws IOException 
	 */
	public SortedMap<String,Double> countWords(String strDir,Map<String, Double> wordMap) throws IOException{
		File sampleFile = new File(strDir);
		File [] sample = sampleFile.listFiles();
		String word;
		for(int i = 0; i < sample.length; i++){
			if(!sample[i].isDirectory()){
				if(sample[i].getName().contains("stemed")){
					FileReader samReader = new FileReader(sample[i]);
					BufferedReader samBR = new BufferedReader(samReader);
					while((word = samBR.readLine()) != null){
						if(!word.isEmpty() && wordMap.containsKey(word)){
							double count = wordMap.get(word) + 1;
							wordMap.put(word, count);
						}
						else {
							wordMap.put(word, 1.0);
						}
					}
				}	
			}
			else countWords(sample[i].getCanonicalPath(),wordMap);
		}
		//只返回出现次数大于3的单词
		SortedMap<String,Double> newWordMap = new TreeMap<String,Double>();
		Set<Map.Entry<String,Double>> allWords = wordMap.entrySet();
		for(Iterator<Map.Entry<String,Double>> it = allWords.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			if(me.getValue() > 3){
				newWordMap.put(me.getKey(),me.getValue());
			}
		}
		System.out.println("newWordMap" + newWordMap.size());
		return newWordMap;	
	}
	
	/**打印属性词典
	 * @param SortedMap<String,Double> 属性词典
	 * @throws IOException 
	 */
	void printWordMap(Map<String, Double> wordMap) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("printWordMap");
		int countLine = 0;
		File outPutFile = new File("F:/DataMiningSample/docVector/allDicWordCountMap.txt");
		FileWriter outPutFileWriter = new FileWriter(outPutFile);
		Set<Map.Entry<String,Double>> allWords = wordMap.entrySet();
		for(Iterator<Map.Entry<String,Double>> it = allWords.iterator(); it.hasNext();){
			Map.Entry<String, Double> me = it.next();
			outPutFileWriter.write(me.getKey()+" "+me.getValue()+"\n");
			countLine++;
		}
		System.out.println("WordMap size" + countLine);
	}
	
	/**计算IDF，即属性词典中每个词在多少个文档中出现过
	 * @param SortedMap<String,Double> 属性词典
	 * @return 单词的IDFmap
	 * @throws IOException 
	 */
	SortedMap<String,Double> computeIDF(String string, Map<String, Double> wordMap) throws IOException {
		// TODO Auto-generated method stub
		File fileDir = new File(string);
		String word;
		SortedMap<String,Double> IDFPerWordMap = new TreeMap<String,Double>();	
		Set<Map.Entry<String, Double>> wordMapSet = wordMap.entrySet();
		for(Iterator<Map.Entry<String, Double>> pt = wordMapSet.iterator(); pt.hasNext();){
			Map.Entry<String, Double> pe = pt.next();
			Double coutDoc = 0.0;
			String dicWord = pe.getKey();
			File[] sampleDir = fileDir.listFiles();
			for(int i = 0; i < sampleDir.length; i++){
				File[] sample = sampleDir[i].listFiles();
				for(int j = 0;j < sample.length; j++){
					FileReader samReader = new FileReader(sample[j]);
					BufferedReader samBR = new BufferedReader(samReader);
					boolean isExited = false;
					while((word = samBR.readLine()) != null){
						if(!word.isEmpty() && word.equals(dicWord)){
							isExited = true;
							break;
						}
					}
					if(isExited) coutDoc++;	
					}	
				}
			//计算单词的IDF
			Double IDF = Math.log(20000 / coutDoc) / Math.log(10);
			IDFPerWordMap.put(dicWord, IDF);
			}
		return IDFPerWordMap;
	}
}

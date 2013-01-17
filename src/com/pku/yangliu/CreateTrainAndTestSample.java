package com.pku.yangliu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

/**创建训练样例集合与测试样例集合
 * @author yangliu
 * @qq 772330184
 * @mail yang.liu@pku.edu.cn
 *
 */
public class CreateTrainAndTestSample {
	
	void filterSpecialWords() throws IOException {
		// TODO Auto-generated method stub
		String word;
		ComputeWordsVector cwv = new ComputeWordsVector();
		String fileDir = "F:/DataMiningSample/processedSample_includeNotSpecial";
		SortedMap<String,Double> wordMap = new TreeMap<String,Double>();
		wordMap = cwv.countWords(fileDir, wordMap);
		cwv.printWordMap(wordMap);//把wordMap输出到文件
		File[] sampleDir = new File(fileDir).listFiles();
		for(int i = 0; i < sampleDir.length; i++){
			File[] sample = sampleDir[i].listFiles();
			String targetDir = "F:/DataMiningSample/processedSampleOnlySpecial/"+sampleDir[i].getName();
			File targetDirFile = new File(targetDir);
			if(!targetDirFile.exists()){
				targetDirFile.mkdir();
			}
			for(int j = 0;j < sample.length; j++){	
				String fileShortName = sample[j].getName();
				if(fileShortName.contains("stemed")){
					targetDir = "F:/DataMiningSample/processedSampleOnlySpecial/"+sampleDir[i].getName()+"/"+fileShortName.substring(0,5);
					FileWriter tgWriter= new FileWriter(targetDir);
					FileReader samReader = new FileReader(sample[j]);
					BufferedReader samBR = new BufferedReader(samReader);
					while((word = samBR.readLine()) != null){
						if(wordMap.containsKey(word)){
							tgWriter.append(word + "\n");
						}
					}
					tgWriter.flush();
					tgWriter.close();
				}
			}
		}
	}
	
	void createTestSamples(String fileDir, double trainSamplePercent,int indexOfSample,String classifyResultFile) throws IOException {
		// TODO Auto-generated method stub
		String word, targetDir;
		FileWriter crWriter = new FileWriter(classifyResultFile);//测试样例正确类目记录文件
		File[] sampleDir = new File(fileDir).listFiles();
		for(int i = 0; i < sampleDir.length; i++){
			File[] sample = sampleDir[i].listFiles();
			double testBeginIndex = indexOfSample*(sample.length * (1-trainSamplePercent));//测试样例的起始文件序号
			double testEndIndex = (indexOfSample+1)*(sample.length * (1-trainSamplePercent));//测试样例集的结束文件序号
			for(int j = 0;j < sample.length; j++){				
				FileReader samReader = new FileReader(sample[j]);
				BufferedReader samBR = new BufferedReader(samReader);
				String fileShortName = sample[j].getName();
				String subFileName = fileShortName;
				if(j > testBeginIndex && j< testEndIndex){//序号在规定区间内的作为测试样本，需要为测试样本生成类别-序号文件，最后加入分类的结果，一行对应一个文件，方便统计准确率
					targetDir = "F:/DataMiningSample/TestSample"+indexOfSample+"/"+sampleDir[i].getName();
					crWriter.append(subFileName + " " + sampleDir[i].getName()+"\n");
					
					}
				else{//其余作为训练样本
					targetDir = "F:/DataMiningSample/TrainSample"+indexOfSample+"/"+sampleDir[i].getName();
				}
				targetDir = targetDir.replace("\\","/");
				File trainSamFile = new File(targetDir);
				if(!trainSamFile.exists()){
					trainSamFile.mkdir();
				}
				targetDir += "/"+subFileName;
				FileWriter tsWriter = new FileWriter(new File(targetDir));
				while((word = samBR.readLine()) != null){
					tsWriter.append(word + "\n");
				}
				tsWriter.flush();
				tsWriter.close();	
			}
		}
		crWriter.flush();
		crWriter.close();
	}
}

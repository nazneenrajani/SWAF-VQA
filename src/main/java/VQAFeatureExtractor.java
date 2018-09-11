import java.util.HashSet;
import java.util.Set;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VQAFeatureExtractor {
	String[] REOutputs;
	Map<Integer,Integer[]> quesType;
	Map<Integer,Integer[]> ansType;
	List<String> questionTypes;
	List<String> answerTypes;
	Map<String,List<Double>> confidences;  //qid~answer -> conf
	Map<String,String> eval_out;   // qid~answer -> target
	Map<Integer,String> questions; // qid -> question
	Map<Integer,Map<Integer,Double>> bow_feat;
	List<String> bow;

	public VQAFeatureExtractor(int nsys){
		bow = new ArrayList<String>();
		ansType = new HashMap<Integer,Integer[]>();
		questionTypes = new ArrayList<String>();
		quesType = new HashMap<Integer,Integer[]>();
		REOutputs = new String[nsys];
		confidences = new HashMap<String, List<Double>>();
		eval_out = new HashMap<String,String>();
		answerTypes = Arrays.asList("yes/no","number","other");
		questions = new HashMap<Integer,String>();
		bow_feat = new HashMap<Integer,Map<Integer,Double>>();
	}
	public void getFiles(String path){
		System.out.println(path);
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		int k=0;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				REOutputs[k] = path+"/"+listOfFiles[i].getName();
				System.out.println(REOutputs[k]);
				k++;
			}
		}
	}
	public void getFeatures(int sys, String file, int nsys, boolean isdev) throws IOException {
		Set<Integer> dev_ques = new HashSet<Integer>();
		BufferedReader featureReader = null;
		try {
			featureReader = new BufferedReader (new FileReader(file));
		} catch (FileNotFoundException e) {
			System.out.println("File not found " + file);
			System.exit (1);

		}
                if(isdev==true){
			BufferedReader br = new BufferedReader(new FileReader("/scratch/cluster/nrajani/VQA/resources/dev_ques"));
			String ques;
			while((ques = br.readLine()) != null){
				dev_ques.add(Integer.parseInt(ques));
			}
			br.close();
		}
		String line;
		while ((line = featureReader.readLine()) != null) {
			String[] parts = line.split(",");
			String answer = "";
			for(int i =0; i <parts.length-2;i++)
				answer += parts[i].toLowerCase();
			Integer qid = Integer.parseInt(parts[parts.length-2]);
			double conf = Double.parseDouble(parts[parts.length-1]);
			String key = qid.toString()+"~"+answer;
			if(isdev==true && !dev_ques.contains(qid))
				continue;
			if(!confidences.containsKey(key)){
				List<Double> system_confs = new ArrayList<Double>(nsys);
				for(int i =0; i <nsys;i++)
					system_confs.add(i, 0.0);
				system_confs.add(sys, conf);
				confidences.put(key, system_confs);
			}
			else{
				List<Double> system_confs = confidences.get(key);
				if(system_confs.get(sys)!=0.0)
					System.out.println("conf already exists");
				else{
					system_confs.add(sys, conf);
					confidences.put(key, system_confs);
				}
			}
		}
	}
	public void writeOutput(int nsys, String feature_file, String out_file, String type) throws IOException {
		Map<Integer,String> cnn = new HashMap<Integer,String>();
		BufferedReader br = null;
		try {
                        br = new BufferedReader (new FileReader("/scratch/cluster/nrajani/VQA/resources/val_vgg.txt"));
                } catch (FileNotFoundException e) {
                        System.out.println("File not found vgg");
                        System.exit (1);

                }
		String line;
		while((line = br.readLine())!=null){
			String[] parts = line.split("\t");
			Integer qid = Integer.parseInt(parts[0]);
			cnn.put(qid,parts[1]);
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(out_file));
		BufferedWriter bfeatures = new BufferedWriter(new FileWriter(feature_file));
		bfeatures.write("@relation vqa"+"\n");
		bfeatures.write("\n");
		int total = nsys+questionTypes.size()+answerTypes.size()+bow.size()+4096;
		System.out.println(total);
		for(int i=0;i<total;i++){
			int tmp=i+1;
			bfeatures.write("@attribute conf_"+tmp+" numeric\n");

		}
		bfeatures.write("@attribute target {w,c}\n");
		bfeatures.write("\n");
		bfeatures.write("@data\n");
		for(String key:confidences.keySet()){
			String target = "";
			if(type.equals("test"))
				target = "?";
			else{
				if(!eval_out.containsKey(key))
					target = "w";
				else
					target = eval_out.get(key);
			}
			String conf_str = "";
			List<Double> confs = confidences.get(key);
			for(int i =0;i<nsys;i++){
				if(confs.get(i)==null)
					conf_str += 0.0+",";
				else
					conf_str += confs.get(i)+",";
			}
			int qid = Integer.parseInt(key.split("~")[0]);
			if(quesType.containsKey(qid)){
				Integer[] onehot = quesType.get(qid);
				for(int i =0; i <onehot.length;i++){
					conf_str+=onehot[i] + ",";
				}
			}
			else
				System.out.println("No type for this question");
			if(ansType.containsKey(qid)){
				Integer[] onehot = ansType.get(qid);
				for(int i =0; i <onehot.length;i++){
					conf_str+=onehot[i] + ",";
				}
			}
			else
				System.out.println("No type for this answer");
			if(bow_feat.containsKey(qid)){
				Map<Integer,Double> feat = bow_feat.get(qid);
				for(int i =0; i< bow.size();i++){
					if(feat.containsKey(i)){
						conf_str+=feat.get(i)+",";
					}
					else
						 conf_str+="0.0,";
				}
			}
			else
				System.out.println("No bow for this question "+qid);
			Integer imgid = Integer.parseInt(Integer.toString(qid).substring(0,Integer.toString(qid).length()-1));
			if(cnn.containsKey(imgid)){
				String vgg = cnn.get(imgid);
				conf_str+=vgg;
			}	
			else
				 System.out.println("No vgg for this question "+qid);
			conf_str = conf_str.trim();
			String[] parts = key.split("~");
			bw.write(parts[0] + "\t"+ parts[1] + "\n");
			bfeatures.write(conf_str+","+target+"\n");
		}
		bw.close();
		bfeatures.close();
	}
	public void getQuestionFeatures(String ques_file) {
		// TODO Auto-generated method stub

	}
	public void eval(String key_file) throws IOException {
		BufferedReader featureReader = null;
		try {
			featureReader = new BufferedReader (new FileReader(key_file));
		} catch (FileNotFoundException e) {
			System.out.println("File not found " + key_file);
			System.exit (1);
		}
		String line;
		while ((line = featureReader.readLine()) != null) {
			String[] parts = line.split(",");
			String qid = parts[0];
			String answer ="";
			for(int i =1;i<parts.length;i++)
				answer += parts[i].toLowerCase();
			String key = qid + "~"+answer;
			//System.out.println(key+ " ");
			if(!eval_out.containsKey(key)){
				eval_out.put(key, "c");
			}
			//double eval_conf = Double.parseDouble(parts[2]);
			else{
				
				System.out.println("duplicate key " + key + "\t");
			}
		}
	}

	public void getQuesType( String quesfile, String type) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("/scratch/cluster/nrajani/VQA/resources/ques_types"));
		String ques;
		while((ques = br.readLine()) != null){
			questionTypes.add(ques);
		}
		br.close();
		assert questionTypes.size() == 65;
		BufferedReader featureReader = null;
		try {
			featureReader = new BufferedReader (new FileReader(quesfile));
		} catch (FileNotFoundException e) {
			System.out.println("File not found " + quesfile);
			System.exit (1);
		}
		String line;
		if(type.equals("test")){
			while ((line = featureReader.readLine()) != null) {
				String[] parts = line.split(",");
				String question = parts[1];
				Integer qid = Integer.parseInt(parts[0]);
				if(!quesType.containsKey(qid)){
					Integer[] onehot = new Integer[questionTypes.size()];
					String[] ques_parts = question.split(" ");
					int ind =-1;
					//System.out.println("**************************");
					for(int i=ques_parts.length-1;i >-1;i--){
						//System.out.println(question);
						String[] sub_ques = Arrays.copyOfRange(ques_parts, 0, i);
						question = strJoin(sub_ques," ");
						if(questionTypes.contains(question)){
							ind = questionTypes.indexOf(question);
							break;
						}
					}

					//int ind = questionTypes.indexOf(question);
					for (int i =0; i <onehot.length;i++){
						if(i==ind)
							onehot[i] = 1;
						else
							onehot[i] =0;
					}
					quesType.put(qid, onehot);
				}
			}
		}
		else{
			while ((line = featureReader.readLine()) != null) {
				String[] parts = line.split(",");
				String question = parts[1];
				Integer qid = Integer.parseInt(parts[0]);
				if(!quesType.containsKey(qid)){
					Integer[] onehot = new Integer[questionTypes.size()];
					int ind = questionTypes.indexOf(question);
					for (int i =0; i <onehot.length;i++){
						if(i==ind)
							onehot[i] = 1;
						else
							onehot[i] =0;
					}
					quesType.put(qid, onehot);
				}
			}
		}
	}
	public void getAnsType( String quesfile, String type) throws IOException {
		assert answerTypes.size() == 3;
		else{
			BufferedReader featureReader = null;
			try {
				featureReader = new BufferedReader (new FileReader(quesfile));
			} catch (FileNotFoundException e) {
				System.out.println("File not found " + quesfile);
				System.exit (1);
			}
			String line;
			while ((line = featureReader.readLine()) != null) {
				String[] parts = line.split(",");
				String answer = parts[2];
				Integer qid = Integer.parseInt(parts[0]);
				if(!ansType.containsKey(qid)){
					Integer[] onehot = new Integer[3];
					int ind = answerTypes.indexOf(answer);
					for (int i =0; i <onehot.length;i++){
						if(i==ind)
							onehot[i] = 1;
						else
							onehot[i] =0;
					}
					ansType.put(qid, onehot);
				}
			}
		}
	}

	public static boolean isNumeric(String str)
	{
		return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	public static String strJoin(String[] aArr, String sSep) {
		StringBuilder sbStr = new StringBuilder();
		for (int i = 0, il = aArr.length; i < il; i++) {
			if (i > 0)
				sbStr.append(sSep);
			sbStr.append(aArr[i]);
		}
		return sbStr.toString();
	}

	public void getBOW( String qidFile, String bowFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(bowFile));
		String line;
		while ((line = br.readLine()) != null) {
			bow.add(line);
		}
		br.close();
		br = new BufferedReader(new FileReader(qidFile));
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(",");
			if(parts.length>2)
				System.out.println("this is not right");
			Integer qid = Integer.parseInt(parts[0]);
			String question = parts[1];
			questions.put(qid, question);
		}
		br.close();
		for(String key:confidences.keySet()){
			Integer qid = Integer.parseInt(key.split("~")[0]);
			if(!bow_feat.containsKey(qid)){
				double count =0.0;
				String question = questions.get(qid);
				Map<Integer,Double> tmp = new HashMap<Integer,Double>();
				String[] parts = question.split(" ");
				for(int i =0; i <parts.length;i++){
					if(bow.contains(parts[i])){
						if(tmp.containsKey(bow.indexOf(parts[i]))){
							tmp.put(bow.indexOf(parts[i]),tmp.get(bow.indexOf(parts[i]))+1.0);
						}
						else{
							count++;
							tmp.put(bow.indexOf(parts[i]),1.0);
						}
					}
				}
				for(Integer i : tmp.keySet()){
					tmp.put(i,tmp.get(i)/count);
				}
				bow_feat.put(qid, tmp);
			}
		}
	}
}

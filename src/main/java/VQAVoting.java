

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VQAVoting {
	public static void main(String[] args) throws IOException{
		Map<Integer,String> demo = new HashMap<Integer,String>();
		Map<Integer,String> demo1 = new HashMap<Integer,String>();
		BufferedReader bw = new BufferedReader(new FileReader("/Users/nrajani/Downloads/vqa_system_outputs/HieCoAtt/tt"));
		//BufferedWriter bw1 = new BufferedWriter(new FileWriter("valbbox3"));
			String line;
			while((line=bw.readLine())!=null){
				String[] parts = line.split("\t");
				int qid = Integer.parseInt(parts[1]);
				demo.put(qid,parts[0]);
			}
			bw.close();
			int correct=0, incorrect =0;
			bw = new BufferedReader(new FileReader("/Users/nrajani/Downloads/vqa_system_outputs/DeeperLSTMQNormI/tt"));
			BufferedReader br = new BufferedReader(new FileReader("/Users/nrajani/Downloads/vqa_system_outputs/MCB/o"));
			while((line=bw.readLine())!=null){
				String[] parts = line.split("\t");
				int qid = Integer.parseInt(parts[1]);
				//union --- comment otherwise
				if(!demo.containsKey(qid))
					demo.put(qid, parts[0]);
				if(demo.containsKey(qid) && demo.get(qid).equals(parts[0])){
					demo1.put(qid, parts[0]);
				}
				else{
					incorrect++;
				}
			}
			bw.close();
			while((line=br.readLine())!=null){
				String[] parts = line.split("\t");
				int qid = Integer.parseInt(parts[1]);
				if(!demo.containsKey(qid))
					demo.put(qid, parts[0]);
				if(demo1.containsKey(qid) && demo1.get(qid).equals(parts[0])){
					correct++;
				}
				else{
					incorrect++;
				}
			}
			br.close();
			System.out.println(correct+"\t"+incorrect);
		}
}

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PostProcess {
	static String type = "stric";
	public static void main(String[] args) throws IOException {
		Map<String,String> uniq = new HashMap<String,String>();
		Map<String,Double> prob = new HashMap<String,Double>();
		Map<String,String> uniq_incorrect = new HashMap<String,String>();
		Map<String,Double> prob_incorrect = new HashMap<String,Double>();
		if (type =="strict"){
			try {
				BufferedReader br = new BufferedReader(new FileReader("resources/sorted_out_deep"));
				String line;
				while ((line = br.readLine()) != null) {
					String[] parts = line.split("\t");
					String key = parts[0];
					String ans = parts[1];
					double probab = Double.parseDouble(parts[3]);
					String output = parts[2];
					if(output.equals("c")){		
						if(uniq.containsKey(key)){
							String pot_ans = uniq.get(key);
							double pot_prob = prob.get(key+"~"+pot_ans);
							if(pot_prob<probab){
								uniq.remove(key);
								prob.remove(key+"~"+pot_ans);
								uniq.put(key, ans);
								prob.put(key+"~"+ans, probab);
							}
						}
						else{
							uniq.put(key, ans);
							prob.put(key+"~"+ans, probab);
						}
					}
				}
				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			System.out.println(uniq.size());
			System.out.println(uniq_incorrect.size());
			BufferedWriter bw = new BufferedWriter(new FileWriter("resources/OpenEnded_mscoco_val2014_deep-strict-qatype_results.json"));
			bw.write("[");
			int count = uniq.size()+uniq_incorrect.size();
			for(String key: uniq.keySet()){
				count--;
				String answer = uniq.get(key);
				double conf = prob.get(key+"~"+answer);
				if(count==0)
					bw.write("{\"answer\":\""+answer+"\",\"answer_prob\":"+conf+",\"question_id\":"+key+"}");
				else
					bw.write("{\"answer\":\""+answer+"\",\"answer_prob\":"+conf+",\"question_id\":"+key+"},");
			}
			for(String key: uniq_incorrect.keySet()){
				count--;
				String answer = uniq_incorrect.get(key);
				double conf = prob_incorrect.get(key+"~"+answer);
				if(count==0)
					bw.write("{\"answer\":\""+answer+"\",\"answer_prob\":"+conf+",\"question_id\":"+key+"}");
				else
					bw.write("{\"answer\":\""+answer+"\",\"answer_prob\":"+conf+",\"question_id\":"+key+"},");

			}
			bw.write("]");
			bw.close();
		}
		else{
			try {
				BufferedReader br = new BufferedReader(new FileReader("resources/sorted_out_deep"));
				String line;
				while ((line = br.readLine()) != null) {
					String[] parts = line.split("\t");
					String key = parts[0];
					String ans = parts[1];
					double probab = Double.parseDouble(parts[3]);
					String output = parts[2];
					if(output.equals("c")){		
						if(uniq.containsKey(key)){
							String pot_ans = uniq.get(key);
							double pot_prob = prob.get(key+"~"+pot_ans);
							if(pot_prob<probab){
								uniq.remove(key);
								prob.remove(key+"~"+pot_ans);
								uniq.put(key, ans);
								prob.put(key+"~"+ans, probab);
							}
						}
						else{
							if(uniq_incorrect.containsKey(key)){
								uniq_incorrect.remove(key);
								String pot_ans = uniq.get(key);
								prob_incorrect.remove(key+"~"+pot_ans);
								uniq.put(key, ans);
								prob.put(key+"~"+ans, probab);
							}
							else{
								uniq.put(key, ans);
								prob.put(key+"~"+ans, probab);
							}
						}
					}
					else{
						if(!uniq.containsKey(key)){
							if(!uniq_incorrect.containsKey(key)){
								uniq_incorrect.put(key, ans);
								prob_incorrect.put(key+"~"+ans, probab);
							}
							else{
								String pot_ans = uniq_incorrect.get(key);
								double pot_prob = prob_incorrect.get(key+"~"+pot_ans);
								if(pot_prob<probab){
									uniq_incorrect.remove(key);
									prob_incorrect.remove(key+"~"+pot_ans);
									uniq_incorrect.put(key, ans);
									prob_incorrect.put(key+"~"+ans, probab);
								}
							}
						}
					}
				}
				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			System.out.println(uniq.size());
			System.out.println(uniq_incorrect.size());
			BufferedWriter bw = new BufferedWriter(new FileWriter("resources/OpenEnded_mscoco_val2014_deep_results.json"));
			bw.write("[");
			int count = uniq.size()+uniq_incorrect.size();
			for(String key: uniq.keySet()){
				count--;
				String answer = uniq.get(key);
				double conf = prob.get(key+"~"+answer);
				if(count==0)
					bw.write("{\"answer\":\""+answer+"\",\"answer_prob\":"+conf+",\"question_id\":"+key+"}");
				else
					bw.write("{\"answer\":\""+answer+"\",\"answer_prob\":"+conf+",\"question_id\":"+key+"},");
			}
			for(String key: uniq_incorrect.keySet()){
				count--;
				String answer = uniq_incorrect.get(key);
				double conf = prob_incorrect.get(key+"~"+answer);
				if(count==0)
					bw.write("{\"answer\":\""+answer+"\",\"answer_prob\":"+conf+",\"question_id\":"+key+"}");
				else
					bw.write("{\"answer\":\""+answer+"\",\"answer_prob\":"+conf+",\"question_id\":"+key+"},");

			}
			bw.write("]");
			bw.close();
		}
	}

}

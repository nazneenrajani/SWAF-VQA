import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VQAQuestionFeatures {
	static Map<String,Integer> bow_count;
	static List<String> bagOfWords;

	public static void main(String[] args) throws IOException{
		bagOfWords = new ArrayList<String>();
		bow_count =  new HashMap<String,Integer>();
		BufferedReader br = new BufferedReader(new FileReader("bigram/val_ques_clean"));
		String line;
		try {
			while ((line = br.readLine()) != null) {
				String s = line.replace("?", "");
				String[] parts = s.split(" ");
				for(int i =0;i<parts.length;i++){
					if(!bow_count.containsKey(parts[i]))
						bow_count.put(parts[i],1);
					else
						bow_count.put(parts[i], bow_count.get(parts[i])+1);
				}
			}
			br.close();
			for(String s: bow_count.keySet()){
				if(bow_count.get(s)>4)
					bagOfWords.add(s);
			}
			
			System.out.println(bagOfWords.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter("resources/bow.txt"));
		for (String s: bagOfWords){
			bw.write(s);
			bw.write("\n");
		}
		bw.close();
	}

}

import java.io.IOException;

public class VQAEnsemble {

	public static void main(String[] args) throws IOException {
		String filetype = "test";
	        boolean isdev = true;	
		String sys_dir = "/scratch/cluster/nrajani/VQA/"+filetype;
		int nsys = 3;
		String feature_file = "/scratch/cluster/nrajani/VQA/resources/"+filetype+"-dev_bow.arff";
		String out_file = "/scratch/cluster/nrajani/VQA/resources/"+filetype+"-dev_bow";
		String key_file ="/scratch/cluster/nrajani/VQA/resources/"+filetype+"_key";
		String type = filetype;
		
		String ques_file = "/scratch/cluster/nrajani/VQA/resources/"+filetype+"_ques_type";
		String clean_ques = "bigram/"+filetype+"_ques_clean";
		
		String bowFile = "/scratch/cluster/nrajani/VQA/resources/bow.txt";
		
		String qidFile = "/scratch/cluster/nrajani/VQA/resources/"+filetype+"_qid_ques";
		VQAFeatureExtractor vfe = new VQAFeatureExtractor(nsys);
		vfe.getFiles(sys_dir);
		for(int sys=0; sys<nsys;sys++){
			vfe.getFeatures(sys, vfe.REOutputs[sys],nsys, isdev);
		}
		vfe.getQuesType(ques_file,type);
		vfe.getAnsType(ques_file,type);
		vfe.getBOW(qidFile,bowFile);
		vfe.eval(key_file);
		vfe.writeOutput(nsys,feature_file,out_file,type);

	}

}

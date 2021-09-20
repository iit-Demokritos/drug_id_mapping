package gr.demokritos.transformations;

import java.io.BufferedReader;
import java.io.FileReader;
//import java.io.InputStreamReader;
//import java.io.IOException;
import java.io.PrintWriter;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
//import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class TTDIdTransformer {

	public static void transformUniprot_DBtoCUIs (String dtiFolder) {

		
		try {
			List<String>targets = new LinkedList<String>();
			List<String> drugs = new LinkedList<String>();

			//Open ALL pos pair file and save list of drugs/targets/PosPairs 
        	FileReader readerPosGroundTruth1 = new FileReader(dtiFolder+"TTD/DTIs_GroundTruth_Pos.csv");
    		BufferedReader reader1 = new BufferedReader(readerPosGroundTruth1);
			String inputLine1 = reader1.readLine();
			while((inputLine1 = reader1.readLine() ) != null) {
				String [] line = inputLine1.split(",");
				String drug =line[0];
				String protein = line[1];
				if (!drugs.contains(drug))
					drugs.add(drug);
				if (!targets.contains(protein))
					targets.add(protein);
        	}
           	reader1.close();
        	readerPosGroundTruth1.close();
 
  
			//Create a new HashMap that will map DrugBankids to CUIs for all drugs
			HashMap<String,List<String>> drugDB_CUIs = new HashMap<String,List<String>>();
			//use drug_ChemBL_DB_ids and CACHE_DB_CUI_MAPP.tsv  to get Drugbank->CUI mapping
			for (String db_id: drugs) {
				List<String> drug1_cuis = getDrugCUIs(db_id, dtiFolder+"MRCONSO.RRF");
	
			    drugDB_CUIs.put(db_id, drug1_cuis);
	     	}
			System.out.println("DEBUG: Found CUIs for "+drugDB_CUIs.size()+" relevant drugs.");
	    	
			//Create a new HashMap that will map ENSEMBL ids to CUIs for all targets
			HashMap<String,String> targetUniprot_CUIs = new HashMap<String,String>();
			//use LC_targets and Metathesaurus file to get ENSEML_id->CUI mapping
			String metathesaurusFilepath = dtiFolder+"OpenTargets.ORG/MRSAT-CLEAN.RRF";
			for ( String uniprot_id : targets) {
				String target_cui = getTargetCUI(uniprot_id, metathesaurusFilepath);
				targetUniprot_CUIs.put(uniprot_id, target_cui);
	     	}
			System.out.println("DEBUG: Found CUIs for "+targetUniprot_CUIs.size()+" relevant targets.");
	
			//Now we use the Hashmaps created above to tranform groundtruth pairs to CUI pairs..
	        //Open the output CSV file to write positive Drug-target cui pairs -> input for ProcessNeo4j project
	 	   	PrintWriter writerPos= new PrintWriter(dtiFolder+"ProcessNeo4j/PositivePairs/drug-pairs.csv", "UTF-8");
	 	   	//Open the output CSV file to write negative Drug-target cui pairs -> input for ProcessNeo4j project
	 	   	PrintWriter writerNeg= new PrintWriter(dtiFolder+"ProcessNeo4j/NegativePairs/drug-pairs.csv", "UTF-8");
	
	 	   	int l=0, d=0, t=0;
	 	   	
	         //Then open file with the groundtruth
	        FileReader groundtruthFile = new FileReader(dtiFolder+"TTD/LC_DTIs_GroundTruth.csv");
	        BufferedReader groundtruthLines = new BufferedReader(groundtruthFile);
	       	String groundtruthLine;
	    	while((groundtruthLine = groundtruthLines.readLine() ) != null) {
	
	   	        if (l++==0)  //ignore headers line
		        	continue;
	
	    		String[] line = groundtruthLine.split(",");
	    		String drug_DB_id = line[0];
	    	    String target_uniprot_id = line[1];
	    	    //String targetName = line[2];
	    	        
	   	        //include only LC-related drug pairs (disease-specific approach)
	   	       // String LC_related = line[3];
	   	        //if (LC_related.equals("0"))
	   	        	//continue;
	  
	    	    List<String> drug_cuis = drugDB_CUIs.get(drug_DB_id);
	    	    String target_cui = targetUniprot_CUIs.get(target_uniprot_id);
	    	    
	    	    if ((drug_cuis==null) || (drug_cuis.isEmpty())) {
	    	    	//System.out.println("Error! CUIs not found for  "+drug_DB_id );
	    	    	if (line[4].equals("1")) 
	    	    		d++;
	    	    	continue;
	    	    }
	    	    if ((target_cui==null) || target_cui.equals("")) {
	    	    	//System.out.println("Error! CUIs not found for "+target_ENSG_id);
	    	    	if (line[4].equals("1")) 
	    	    		t++;
	    	    	continue;
	    	    }    
	    	    //deciding interaction 
			    String interaction = line[4];
	     	    for (int i = 0; i < drug_cuis.size(); i++) {
			    	        
			    	if (interaction.equals("0"))
			    	  	writerNeg.println(drug_cuis.get(i)+","+target_cui);
			    	else if (interaction.equals("1")) 
			    	  	writerPos.println(drug_cuis.get(i)+","+target_cui);
			    	else {
			    	   	System.out.println("Interaction information is wrong for drug pair");
			    	   	break;
			    	}
	    	    }
	    	        //just take 3 first drug pairs for testing now
	    	        //FOT: remove this line 
	    	        //if (l==3)
	    	        	//break;
	    	}
	    	System.out.println("NEW DEBUG: Could not find CUIs for "+d+" drugs and "+t+"targets...");
	    	
	    	groundtruthLines.close();
	    	groundtruthFile.close();
	    	writerPos.close();
	    	writerNeg.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public static String getTargetCUI (String target, String metathesaurusFilepath) throws Exception {
 		
		String cui="";
		FileReader metathesaurusFile = new FileReader(metathesaurusFilepath);
		BufferedReader inputLines = new BufferedReader(metathesaurusFile);
       	String inputLine;
    	while((inputLine = inputLines.readLine() ) != null) {
    	    if (inputLine.contains(target)) {
    	   		String[] line = inputLine.split("\\|");  
    	   		cui= line[0];
    	   		break;
    	    }
    	    
    	}
    	inputLines.close();
    	metathesaurusFile.close();
    	return cui;
	}


   	public static List<String> getDrugCUIs (String drug, String mappingRRFfile) throws Exception {
    		
    		List<String> cuis = new ArrayList<String>();
    		
    		//Open the TSV file with the DrugBank-UMLS mapping
    		BufferedReader br = new BufferedReader(new FileReader(mappingRRFfile));
    		String line;
    		while ((line = br.readLine()) != null ){
    			//looking for drug mapping lines
    			if (line.contains(drug)) {
    				String[] values = line.split("\\|");
    				if (!cuis.contains(values[0])) 
	    		       	cuis.add(values[0]);
	    		       	// System.out.println("Drug: "+drug+" , has CUI: "+values[0]);
    		    }
    		}
    		br.close();
    		return cuis;
   	}
}

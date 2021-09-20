package gr.demokritos.transformations;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.List;

import gr.demokritos.dtiExtraction.TTD.DrugEntry;


public class PubChem_DrugbankMapper {



	public static HashMap<String,DrugEntry> getPubChemtoDBmapping (String dtiFolder, HashMap<String,DrugEntry>  drug_entries, HashMap<String,String> drug_id_names,HashMap<String,String>  drug_id_cas, HashMap<String,String> drug_id_pubChem) throws Exception{
	    
		String drugbankXMLFile = dtiFolder+"DrugBank.xml";
		
		//Query Drugbank file to add DB ids of the ChemBL-ids of LC drugs
		return OpenXML.getDBidsforTTDdrugs (drugbankXMLFile, drug_entries, drug_id_names,drug_id_cas, drug_id_pubChem);
	}	


	public static void mapGroundtruthPosDrugbankIds (String dtiFolder, HashMap<String,DrugEntry>  drug_entries) throws Exception{
   		//Create a CSV File to save positive DTI pairs with DRUGBANK ids!
		PrintWriter writerGroundTruth= new PrintWriter(dtiFolder+"protein_exps/DTIs_GroundTruth_UniProt_Pos.csv", "UTF-8");
		writerGroundTruth.println("Drug,Target_Id,Gene_name,Related_to_disease,Interaction");

		int notfound=0;
		//Open CSV File to with positive DTI pairs (has ChemBl ids!)
		FileReader readerGroundTruthPubChem = new FileReader(dtiFolder+"protein_exps/TTD_DTIs_GroundTruth_PUBCHCID_UniProt.csv");
		BufferedReader inputLines = new BufferedReader(readerGroundTruthPubChem);
       	String inputLine = inputLines.readLine();//do not re-write headers line
    	while((inputLine = inputLines.readLine() ) != null) {
    	        String[] line = inputLine.split(",");  
    	        String drug_ttd_id = line[0];
    	        String drug_DB_id = drug_entries.get(drug_ttd_id).drugbank_id;
    	        if (drug_DB_id==null) {
    	        	notfound++;
    	        	//just for debugging
    	        	if (notfound<5)
    	        		System.out.println("Error! Drugbank id not found  in DRUGBANK.xml for drug with the following details: "+drug_ttd_id+","+drug_entries.get(drug_ttd_id).cas_num+","+drug_entries.get(drug_ttd_id).name+","+drug_entries.get(drug_ttd_id).pubchem_cid);
        	    	continue;
    	        }
    	        line[0]=drug_DB_id;
    	        writerGroundTruth.println(line[0]+","+line[1]+","+line[2]+",??,1");
        }
    	System.out.println(notfound+" ttd drug names/pchemcids/cas_nums not found in Drugbank");
    	inputLines.close();
    	readerGroundTruthPubChem.close();
     	writerGroundTruth.close();
	}
	

}

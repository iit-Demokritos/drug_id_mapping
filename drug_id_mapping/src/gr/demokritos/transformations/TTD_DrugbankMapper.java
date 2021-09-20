package gr.demokritos.transformations;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class TTD_DrugbankMapper {



	public static HashMap<String,DrugEntry> getPubChemtoDBmapping (String dtiFolder, HashMap<String,DrugEntry>  drug_entries, HashMap<String,String> drug_id_names,HashMap<String,String>  drug_id_cas, HashMap<String,String> drug_id_pubChem) throws Exception{
	    
		String drugbankXMLFile = dtiFolder+"DrugBank.xml";
		
		//Query Drugbank file to add DB ids of the ChemBL-ids of LC drugs
		return OpenXML.getDBidsforTTDdrugs (drugbankXMLFile, drug_entries, drug_id_names,drug_id_cas, drug_id_pubChem);
		
	}	

	public static HashMap<String,DrugEntry> searchinDBmapping (String dtiFolder, HashMap<String,DrugEntry>  drug_entries) throws Exception{
	    
		String drugbankMappingFile = dtiFolder+"drug-mappings.tsv";
		HashMap<String,String> pubChemIdsNotFound = new HashMap<String,String> ();
		
		//Query Drugbank file to add DB ids of the ChemBL-ids of LC drugs
		for (Map.Entry<String, DrugEntry> entry : drug_entries.entrySet()) {
			String ttd_id = entry.getKey();
		    DrugEntry drug_details = entry.getValue();
		    if (drug_details.drugbank_id==null) {
		    	String drugbank_id = findDrugbankId(drugbankMappingFile, drug_details);
		    	if (drugbank_id!=null) {
		    		drug_details.drugbank_id = drugbank_id;
		    		drug_entries.replace(ttd_id, drug_details);
		    	}
		    	else if (drug_details.pubchem_cid!=null)
		    		pubChemIdsNotFound.put(drug_details.pubchem_cid, ttd_id);
		    }
		}
		PubChemDrugExtractor pcde = new PubChemDrugExtractor();
		drug_entries = pcde.searchPubChemEnrichEntries(pubChemIdsNotFound, drug_entries);
		
		//Check again Drugbank file to add DB ids (new ids for drugs have been added from PubChem API)
		for (Map.Entry<String, DrugEntry> entry : drug_entries.entrySet()) {
			String ttd_id = entry.getKey();
		    DrugEntry drug_details = entry.getValue();
		    if (drug_details.drugbank_id==null) {
		    	String drugbank_id = findDrugbankId(drugbankMappingFile, drug_details);
		    	if (drugbank_id!=null) {
		    		drug_details.drugbank_id = drugbank_id;
		    		drug_entries.replace(ttd_id, drug_details);
		    	}
		    	else if (drug_details.pubchem_cid!=null)
		    		pubChemIdsNotFound.put(drug_details.pubchem_cid, ttd_id);
		    }
		}
		
		//Lastly, create an updated drugbank-mapping file, to keep all drug ids in a unified tsv!!
//		System.out.println("DEBUG: Save all ids in drug-mappings_updated.tsv");
//		saveUpdatedDrugBankMappingTSV(drug_entries, dtiFolder);
		//the command above can run only once!
		
		return drug_entries;
	}
		
	public static String findDrugbankId(String drugbankMappingFile, DrugEntry drug) throws Exception{
  		FileReader drugsMapReader = new FileReader(drugbankMappingFile);
		BufferedReader drugLines = new BufferedReader(drugsMapReader);
       	String drugline;//ignore headers line
    	while((drugline = drugLines.readLine() ) != null) {
    		String [] idList = {drug.name, drug.ttd_id, drug.pubchem_cid, drug.cas_num, drug.chembl_id, drug.zinc_id, drug.chebi_id, drug.kegg_cid, drug.bindingDB_id };
    		
    		for (String anyId:idList) {
    			if ((anyId!=null) && (drugline.contains(anyId))) {
    				drugLines.close();
    		    	drugsMapReader.close();
    		    	String[] line = drugline.split("\t");
    	    		return line[0];
    			}
    		}
    	}
    	drugLines.close();
    	drugsMapReader.close();
		return null;
	}
	
	public static void mapGroundtruthPosDrugbankIds (String dtiFolder, HashMap<String,DrugEntry>  drug_entries) throws Exception{
   		//Create a CSV File to save positive DTI pairs with DRUGBANK ids!
		PrintWriter writerGroundTruth= new PrintWriter(dtiFolder+"protein_exps/TTD/DTIs_GroundTruth_UniProt_Pos.csv", "UTF-8");
		writerGroundTruth.println("Drug,Target_Id,Gene_name,Related_to_disease,Interaction");

		int notfound=0;
		//Open CSV File to with positive DTI pairs (has ChemBl ids!)
		FileReader readerGroundTruthPubChem = new FileReader(dtiFolder+"protein_exps/TTD/TTD_DTIs_GroundTruth_PUBCHCID_UniProt.csv");
		BufferedReader inputLines = new BufferedReader(readerGroundTruthPubChem);
       	String inputLine = inputLines.readLine();//do not re-write headers line
    	while((inputLine = inputLines.readLine() ) != null) {
    	        String[] line = inputLine.split(",");  
    	        String drug_ttd_id = line[0];
    	        String drug_DB_id = drug_entries.get(drug_ttd_id).drugbank_id;
    	        if (drug_DB_id==null) {
    	        	notfound++;
    	        	//just for debugging
    	        	//if (notfound<5) {
    	        	//	DrugEntry drug= drug_entries.get(drug_ttd_id);
    	        	//	System.out.println("Error! Drugbank id not found  in DRUGBANK.xml for drug with the following details: "+drug_ttd_id+","+drug_entries.get(drug_ttd_id).cas_num+","+drug.name+","+drug.ttd_id+","+drug.pubchem_cid+","+drug.cas_num+","+drug.chembl_id+","+drug.zinc_id+","+drug.chebi_id+","+drug.kegg_cid);
    	        	//}
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

package gr.demokritos.transformations;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
//import java.util.Map.Entry;
import java.util.Set;

public class CreateDrugMappings {

	public static void main(String[] args) {
		String dtiFolder = args[0];
//		addDrugbankCuis(dtiFolder);
		addMissingFieldsFromKEGG(dtiFolder);
	}

	public static void addMissingFieldsFromKEGG (String dtiFolder) {
		
		try {
			
			HashMap<String,DrugEntry> kegg_drugs = getAllKEGGdrugs(dtiFolder+"KEGG/drug");

			List<String> keggIds = new LinkedList<String>();
			
			for (Map.Entry<String, DrugEntry> entry : kegg_drugs.entrySet()) {
				String keggId = entry.getKey(); 
				keggIds.add(keggId);
			}
			//Enrich drug-mappings.tsv file with drugbank-related CUIs
			String drugbankMappingFile = dtiFolder+"drug-mappings_updated2.tsv";
			PrintWriter writerDrugMap= new PrintWriter(dtiFolder+"drug-mappings_updated3.tsv", "UTF-8");
			
	 		FileReader drugsMapReader = new FileReader(drugbankMappingFile);
			BufferedReader drugLines = new BufferedReader(drugsMapReader);
	       	String drugline=drugLines.readLine();//copy headers line
	       	writerDrugMap.println(drugline);
	       	
	      	
	    	while((drugline = drugLines.readLine() ) != null) {
	    		String[] line = drugline.split("\t");  
	    	    String keggId = line[9];
	    	    
	    	    if(keggIds.contains(keggId))  {
	    	    	DrugEntry keggDrug = kegg_drugs.get(keggId);
	            	if (line[0].equals("null")) {
	            		if (keggDrug.drugbank_id!=null) {
	            			System.out.println("!!!!!!!!!!!Added missing keggDrug.DBId="+keggDrug.drugbank_id);
	            			line[0]=keggDrug.drugbank_id;
	            		}
	            	}	
	            	if (line[4].equals("null")) {
	            		if (keggDrug.cas_num!=null) {
	            			System.out.println("!!!!!!!!!!!Added missing keggDrug.cas_num="+keggDrug.cas_num);
	            			line[4]=keggDrug.cas_num;
	            		}
	            	}	
	            	if (line[3].equals("null")) {
	            		if (keggDrug.pubchem_cid!=null) {
	            			System.out.println("!!!!!!!!!!!For keggId="+keggId+"Added missing keggDrug.pubchem_cid="+keggDrug.pubchem_cid);
	            			line[3]=keggDrug.pubchem_cid;
	            		}
	            	}	
	            	if (line[7].equals("null")) {
	            		if (keggDrug.chebi_id!=null) {
	            			System.out.println("!!!!!!!!!!!For keggId="+keggId+"Added missing keggDrug.chebi_id="+keggDrug.chebi_id);
	            			line[7]=keggDrug.chebi_id;
	            		}
	            	}	
	            	drugline="";
	            	for (String item: line)
	            		drugline+=item+"\t";
	            	drugline=drugline.substring(0, drugline.length()-1);
	    	    }	
	    	    writerDrugMap.println(drugline);
	    	}
	    	
	    	System.out.println("Done!");
			writerDrugMap.close();
			drugLines.close();
			drugsMapReader.close();

		}catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static HashMap<String,DrugEntry> getAllKEGGdrugs(String keggFile){
		
		HashMap<String,DrugEntry> drug_entries = new HashMap<String,DrugEntry>();
		try {
	   		String drugId="";
			DrugEntry drug = new DrugEntry();
			FileReader drugsInfoReader = new FileReader(keggFile);
			BufferedReader drugLines = new BufferedReader(drugsInfoReader);
	       	String drugline;//ignore headers line
	
	    	while((drugline = drugLines.readLine() ) != null) {
	    		//System.out.println("line: "+(++lines));
	    		if (drugline.contains("ENTRY")) {
	      	    	drug = new DrugEntry();
	       			drug.kegg_id= drugline.substring(12,18);
	       			drugId = drug.kegg_id;
	       			drug_entries.put(drugId, drug);
	    			
	    		}
	    		else if (drugline.contains("CAS: ")) {
	    			String substring = drugline.substring(17);
	    			//int endIndex = substring.indexOf(" ");
	      	        drug.cas_num = substring;
	    	        drug_entries.replace(drugId, drug);
	    			
	    		}
	    		else if (drugline.contains("PubChem: ")) {
	    			String substring = drugline.substring(21);
	    			//int endIndex = substring.indexOf(" ");
	      	        drug.pubchem_cid = substring;
	    	        drug_entries.replace(drugId, drug);
	    			
	    		}
	    		else if (drugline.contains("ChEBI: ")) {
	    			String substring = drugline.substring(19);
	    			//int endIndex = substring.indexOf(" ");
	      	        drug.chebi_id = substring;
	    	        drug_entries.replace(drugId, drug);
	    			
	    		}
	    		else if (drugline.contains("DrugBank: ")) {
	    			String substring = drugline.substring(22);
	    			//int endIndex = substring.indexOf(" ");
	      	        drug.drugbank_id = substring;
	    	        drug_entries.replace(drugId, drug);
	    			
	    		}

	    	}
	     	drugLines.close();
	    	drugsInfoReader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return drug_entries;
	}

	public static void addDrugbankCuis (String dtiFolder) {
		
		try {
			//Enrich drug-mappings.tsv file with drugbank-related CUIs
			String drugbankMappingFile = dtiFolder+"drug-mappings_updated.tsv";
			PrintWriter writerDrugMap= new PrintWriter(dtiFolder+"drug-mappings_updated2.tsv", "UTF-8");
			String comma_separated_cuis = "null";		
			
	 		FileReader drugsMapReader = new FileReader(drugbankMappingFile);
			BufferedReader drugLines = new BufferedReader(drugsMapReader);
	       	String drugline=drugLines.readLine();//ignore headers line
	       	writerDrugMap.println(drugline+"\tUMLS_cuis");
	    	while((drugline = drugLines.readLine() ) != null) {
	    		writerDrugMap.print(drugline);
	    		String[] line = drugline.split("\t");  
	    	    String drugbankid = line[0]; 
	    	    if(drugbankid!=null) 
	    	    	comma_separated_cuis=getDrugCUIs(drugbankid, dtiFolder+"MRCONSO.RRF");
	    	    writerDrugMap.println("\t"+comma_separated_cuis);
	    	}
	    	
	    	System.out.println("Done!");
			writerDrugMap.close();
			drugLines.close();
			drugsMapReader.close();

		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void saveUpdatedDrugBankMappingTSV(HashMap<String,DrugEntry> drug_entries, String dtiFolder) {

		//We need to create a new hashmap with Drugbank ids and drug objects
		HashMap<String,DrugEntry> drugsToUpdate = new HashMap<String,DrugEntry> ();
		Collection<DrugEntry> drugsFound = drug_entries.values();
		for (DrugEntry drug:drugsFound) {
			if (drug.drugbank_id!=null)
				drugsToUpdate.put(drug.drugbank_id,drug);
		}
		Set<String> drugIdsCollection = drugsToUpdate.keySet();
		
		//Now create an updated mappings file
		try {
			PrintWriter writerDrugMap= new PrintWriter(dtiFolder+"drug-mappings_updated.tsv", "UTF-8");
			writerDrugMap.println("drugbankId\tname\tttd_id\tpubchem_cid\tcas_num\tchembl_id\tzinc_id\tchebi_id\tkegg_cid\tkegg_id\tbindingDB_id");
			
			//Open previous TSV File 
			FileReader readerPrevDrugMap = new FileReader(dtiFolder+"drug-mappings.tsv");
			BufferedReader inputLines = new BufferedReader(readerPrevDrugMap);
	       	String inputLine = inputLines.readLine();//headers line
	    	while((inputLine = inputLines.readLine() ) != null) {
	    		String[] line = inputLine.split("\t");  
	    	    String drugbankid = line[0]; 
	    	    String name = line[1];
	    	    String ttd_id = line[2];
	    	    String pubchem_cid = line[3];
	    	    String cas_num = line[4];
	    	    String chembl_id = line[5];
	    	    String zinc_id = line[6];
	    	    String chebi_id = line[7];
	    	    String kegg_cid = line[8];
	    	    String kegg_id = line[9];
	    	    String bindingDB_id = line[10];
	    	    
	    	    if (drugIdsCollection.contains(drugbankid)) {
	    	    	DrugEntry drugDetails = drugsToUpdate.get(drugbankid);
	    	    	if (ttd_id.equals("null"))
	    	    		ttd_id=drugDetails.ttd_id;
		    	    if (pubchem_cid.equals("null"))
		    	    	pubchem_cid=drugDetails.pubchem_cid;
		    	    if (cas_num.equals("null"))
	    	    		cas_num=drugDetails.cas_num;
		    	    if (chembl_id.equals("null"))
	    	    		chembl_id=drugDetails.chembl_id;
		    	    if (zinc_id.equals("null"))
	    	    		zinc_id=drugDetails.zinc_id;
		    	    if (chebi_id.equals("null"))
	    	    		chebi_id=drugDetails.chebi_id;
		    	    if (kegg_cid.equals("null"))
	    	    		kegg_cid=drugDetails.kegg_cid;
		    	    //if (kegg_id.equals("null"))
	    	    		//;
		    	    if (bindingDB_id.equals("null"))
	    	    		bindingDB_id=drugDetails.bindingDB_id;
	    	    }   
	    	    writerDrugMap.println(drugbankid+"\t"+name+"\t"+ttd_id+"\t"+pubchem_cid+"\t"+cas_num+"\t"+chembl_id+"\t"+zinc_id+"\t"+chebi_id+"\t"+kegg_cid+"\t"+kegg_id+"\t"+bindingDB_id);
	    	}
	    	inputLines.close();
	    	readerPrevDrugMap.close();
			writerDrugMap.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
  	public static String getDrugCUIs (String drug, String mappingRRFfile) throws Exception {
		
  		String comma_separated_cuis="null";
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
		
		if (!cuis.isEmpty()) {
			comma_separated_cuis="";
			for (String cui: cuis)
				comma_separated_cuis+=cui+",";
			comma_separated_cuis=comma_separated_cuis.substring(0, comma_separated_cuis.length()-1);
		}	
		return comma_separated_cuis;
	}
}

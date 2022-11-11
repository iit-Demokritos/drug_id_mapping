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
		//addDrugbankCuis(dtiFolder);
		//addMissingFieldsFromKEGG(dtiFolder);
		//updateTTDids(dtiFolder);
		//correctOutdatedTTD(dtiFolder);
		//enrichFromDGIdb(dtiFolder);
		enrichFromSIDER(dtiFolder);
		
	}

	
	public static void enrichFromDGIdb (String dtiFolder) {
		
		try {
			//This is the old file to update
			HashMap<String,String> dgidbNamesChembls = new HashMap<String,String>();
			HashMap<String,String> dgidbDBidsChembls = new HashMap<String,String>();
			FileReader drugsMapReader0 = new FileReader(dtiFolder+"DGIdb/drugs.tsv");
			BufferedReader drugLines0 = new BufferedReader(drugsMapReader0);
	       	String dline0=drugLines0.readLine();// headers line
	    	while((dline0 = drugLines0.readLine() ) != null) {
	    		String[] line = dline0.split("\t");  
	    		if (line.length<4)
	    			continue;
	    	    String source = line[3];
	    	    if (source.equals("DrugBank")) {
	    	    	String id = line[0];
	    	    	String chembl = line[2];
	    	    	if (chembl.length()==0)
	    	    		continue;
	    	    	chembl = chembl.substring(7);
	    	    	dgidbDBidsChembls.put(id, chembl);
	    	    }
	    	    else if (source.equals("ChemblDrugs")) {
	    	    	String name = line[1];
	    	    	String chembl = line[0].substring(7);
	    	    	dgidbNamesChembls.put(name, chembl);
	    	    }
	    	}    
	    	drugLines0.close();
	    	drugsMapReader0.close();
	    	Set<String> foundNames = dgidbNamesChembls.keySet();
	    	Set<String> foundDBids = dgidbDBidsChembls.keySet();
	    	
	    	
			//This is the old file to update
			String drugbankMappingFile = dtiFolder+"drug-mappings_updated2.tsv";
			//create new file with correct TTD ids
			PrintWriter writerDrugMap= new PrintWriter(dtiFolder+"drug-mappings_updated3.tsv", "UTF-8");
			
			int k=0;
			
			FileReader drugsMapReader = new FileReader(drugbankMappingFile);
			BufferedReader drugLines2 = new BufferedReader(drugsMapReader);
	       	String dline=drugLines2.readLine();//copy headers line
	       	writerDrugMap.println(dline);
	       	
	      	
	    	while((dline = drugLines2.readLine()) != null) {
	    		String[] line = dline.split("\t");
	    		String chembl = line[5];
	    		if (chembl.equals("null")) {
		    	    String dbid = line[0];
		    	    String name = line[1];
		    	    
		    	    //System.out.println("chembl: "+chembl +"  name: "+name+"  dbid: "+dbid);
		    	    
		    	    if(foundNames.contains(name))  {  
		    	    	chembl=dgidbNamesChembls.get(name);
		    	    	k++;
		    	    	//System.out.println("FOUND: "+chembl +" for name: "+name);
			    	    
		    	    }
		    	    else if (foundDBids.contains(dbid)) {
		    	    	chembl=dgidbDBidsChembls.get(dbid);
		    	    	k++;
		    	    	//System.out.println("FOUND: "+chembl +" for dbid: "+dbid);
		    	    }
		    	    line[5]=chembl;
		    	    String upd_line="";
		    	    for (String s:line) {
		    	    	upd_line+=s+"\t";
		    	    }	
		    	    
		    	    upd_line.substring(0, upd_line.length()-2);
		    	    
		    	    writerDrugMap.println(upd_line);
	    		}
	    		else
	    			writerDrugMap.println(dline);
	    	}	
	    	System.out.println("Done! Added "+k+" missing chembl ids");
			writerDrugMap.close();
			drugsMapReader.close();

		}
	    catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void correctOutdatedTTD (String dtiFolder) {
		
		try {
			//This is the old file to update
			String drugbankMappingFile = dtiFolder+"drug-mappings_updated.tsv";
			//create new file with correct TTD ids
			PrintWriter writerDrugMap= new PrintWriter(dtiFolder+"drug-mappings_updated2.tsv", "UTF-8");
			
			FileReader drugsMapReader = new FileReader(drugbankMappingFile);
			BufferedReader drugLines2 = new BufferedReader(drugsMapReader);
	       	String dline=drugLines2.readLine();//copy headers line
	       	writerDrugMap.println(dline);
	       	
	      	
	    	while((dline = drugLines2.readLine() ) != null) {
	    		String[] line = dline.split("\t");  
	    	    String ttdId = line[2];
	    	    String name = line[1];
	    	    
	    	    if(ttdId.contains("DAP"))  {  //outdated id
	    	    	line[2]=lookForTTDsynonym(ttdId, name,dtiFolder);
	            	dline="";
	            	for (String item: line)
	            		dline+=item+"\t";
	            	dline=dline.substring(0, dline.length()-1);
	    	    }
	    	    
	    	    writerDrugMap.println(dline);
	    	}
	    	
	    	System.out.println("Done!");
			writerDrugMap.close();
			drugsMapReader.close();

		}catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static String lookForTTDsynonym(String old_ttdId, String name, String dtiFolder) {
    	//This is the TTD file with drug info
		String synonymsTTDFile=dtiFolder+"TTD/P1-04-Drug_synonyms.txt";
    	String ttdId=old_ttdId;
		
		try {
			
			FileReader drugsSynonymsReader = new FileReader(synonymsTTDFile);
			BufferedReader synonymLines = new BufferedReader(drugsSynonymsReader);
			
			for (int i=0; i<22;i++)
				synonymLines.readLine();//skip header lines
	       	
			String dline;
	    	while((dline=synonymLines.readLine() ) != null) {
	    		if ((dline.contains("DRUGNAME\t"+name)) ||(dline.contains("SYNONYMS\t"+name))) {
	    			System.out.println("Correct "+ttdId);
	    	        String[] line = dline.split("\t"); 
	            	ttdId=line[0];
	            	break;
	    		}
        	}
	    	synonymLines.close();
        	drugsSynonymsReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ttdId;
	}
	
	public static void updateTTDids (String dtiFolder) {
		
		//This is the file to update
		String drugbankMappingFile = dtiFolder+"drug-mappings.tsv";
    	//This is the TTD file with drug info
		String drugFile=dtiFolder+"TTD/P1-03-TTD_crossmatching.txt";
    	
		
		try {
			
	  		//initially save all drug information that we can find from TTD file!
    		int lines=0;
    		HashMap<String,DrugEntry> drug_entries = new HashMap<String,DrugEntry>();
    		String drugId="";
    		DrugEntry drug = new DrugEntry();
    		FileReader drugsInfoReader = new FileReader(drugFile);
    		BufferedReader drugLines = new BufferedReader(drugsInfoReader);
           	String drugline;//ignore headers line
        	while((drugline = drugLines.readLine() ) != null) {
        		if (drugline.contains("DRUGNAME")) {
          	        String[] line = drugline.split("\t"); 
        	        if (line.length<3)
        	        	continue;
           			drug = new DrugEntry();
           			drug.name= line[2];
           			drug.ttd_id=line[0];
           			drugId = line[0];
           			drug_entries.put(drugId, drug);
      //   	        drug_id_names.put(drug.name,drugId);
        			
        		}
        		else if (drugline.contains("CASNUMBE")) {
          	        String[] line = drugline.split("\t"); 
        	        if (line.length<3)
        	        	continue;
        	        drug.cas_num = line[2].substring(4);
        	        drug_entries.replace(drugId, drug);
        // 	        drug_id_cas.put(drug.cas_num, drugId);
        			
        		}
        		else if (drugline.contains("PUBCHCID")) {
        	        String[] line = drugline.split("\t");
        	        if (line.length<3)
        	        	continue;
        	        drug.pubchem_cid = line[2];
         	        if (drug.pubchem_cid.contains(";"))
         	        	drug.pubchem_cid=drug.pubchem_cid.substring(0, drug.pubchem_cid.indexOf(';')-1);
        	        drug_entries.replace(drugId, drug);
         //	        drug_id_pubChem.put(drug.pubchem_cid, drugId);
        	    }
        		else if (drugline.contains("CHEBI_ID")) {
        	        String[] line = drugline.split("\t");
        	        if (line.length<3)
        	        	continue;
        	        
        	        if (line[2].contains(":"))
           	        	drug.chebi_id = line[2].substring(6);
           	        else {
           	        	String chembl = line[2];
           	        	chembl=chembl.replaceAll(" ", "");
           	        	drug.chembl_id = chembl;
           	        }	
         	        drug_entries.replace(drugId, drug);
         	        
        	    }
        	}
        drugLines.close();
        drugsInfoReader.close();
		

		//Enrich drug-mappings.tsv file with drugbank-related CUIs
		PrintWriter writerDrugMap= new PrintWriter(dtiFolder+"drug-mappings_updated.tsv", "UTF-8");
			
	 	FileReader drugsMapReader = new FileReader(drugbankMappingFile);
		BufferedReader drugLines2 = new BufferedReader(drugsMapReader);
	       	String dline=drugLines2.readLine();//copy headers line
	       	writerDrugMap.println(dline);
	       	
	      	
	    	while((dline = drugLines2.readLine() ) != null) {
	    		String[] line = dline.split("\t");  
	    	    String ttdId = line[2];
	    	    
	    	    if(drug_entries.containsKey(ttdId))  {  //update remaining features....
	    	    	DrugEntry d = drug_entries.get(ttdId);

	            	if ((line[4].equals("null")) && (d.cas_num!=null)) {
	            			System.out.println("!!!!!!!!!!!Added missing d.cas_num="+d.cas_num);
	            			line[4]=d.cas_num;
	            		
	            	}	
	            	if ((line[3].equals("null")) && (d.pubchem_cid!=null)) {
	            			System.out.println("!!!!!!!!!!!For ttdId="+ttdId+"Added missing d.pubchem_cid="+d.pubchem_cid);
	            			line[3]=d.pubchem_cid;
	            		
	            	}	
	            	if ((line[7].equals("null")) && (d.chebi_id!=null)) {
	            			System.out.println("!!!!!!!!!!!For ttdId="+ttdId+"Added missing d.chebi_id="+d.chebi_id);
	            			line[7]=d.chebi_id;
	            	
	            	}	
	            	dline="";
	            	for (String item: line)
	            		dline+=item+"\t";
	            	dline=dline.substring(0, dline.length()-1);
	    	    }
	    	    else {  //ttd_id not existing in drug-mappings file, try to find other common features in ttd_drugs
	    	    	
	    	    	for (DrugEntry d : drug_entries.values()) {
	    	    		if (((d.name!=null) && (dline.contains("\t"+d.name+"\t"))) || ((d.cas_num!=null) && (dline.contains("\t"+d.cas_num+"\t"))) || ((d.chebi_id!=null) && (dline.contains("\t"+d.chebi_id+"\t"))) ||((d.chembl_id!=null) && (dline.contains("\t"+d.chembl_id+"\t"))) ||((d.drugbank_id!=null) && (dline.contains(d.drugbank_id+"\t"))) ||((d.kegg_cid!=null) && (dline.contains("\t"+d.kegg_cid+"\t")))) {
	    	    			line[2] = d.ttd_id;
	    	    			dline="";
	    	    			for (int i=0; i<12; i++)
	    	    				dline+=line[i]+"\t";
	    	    			dline.substring(0,dline.length()-2);
	    	    			break;
	    	    		}	
	    	    	}	
	    	    }
	    	    writerDrugMap.println(dline);
	    	}
	    	
	    	System.out.println("Done!");
			writerDrugMap.close();
			drugLines.close();
			drugsMapReader.close();

		}catch (Exception e) {
			e.printStackTrace();
		}

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
  	
  	public static void enrichFromSIDER(String dtiFolder) {
		try {
			String ddiPath = dtiFolder;

			String drugmap1 = ddiPath+"drug-mappings_updated3.tsv";
			String drugmap2 = ddiPath+"drug-mappings_latest.tsv";
			
			
			BufferedReader br = new BufferedReader(new FileReader(drugmap1));
			PrintWriter writerDrugMap= new PrintWriter(drugmap2, "UTF-8");

			String line= br.readLine();
			writerDrugMap.println(line+"\tstitch_id");
			while ((line = br.readLine()) != null ){
				String[] values = line.split("\t");
				String dbid = values[0];
				String siderid = getSIDERMapping(dbid, ddiPath);
				if (siderid.equals("null"))
					siderid = getSIDERMapping2(dbid, ddiPath);
				String addition = siderid;
				if (!line.endsWith("\t"))
					addition="\t"+addition;
				writerDrugMap.println(line+addition);
			}
			br.close();
			writerDrugMap.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
  	}
  	

	static String getSIDERMapping(String db, String ddiPath) {
		
   		String id="null";
		String line;
		String drugMappings = ddiPath+"DrugBank-Sider_mapping.tsv";
		try {
			BufferedReader br = new BufferedReader(new FileReader(drugMappings));
	   		
			while ((line = br.readLine()) != null ){
				if(line.contains(db)) {
			        String[] values = line.split("\t");
			        id = values[3];
			        break;
				}    
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return id;

	}


	static String getSIDERMapping2(String db, String ddiPath) {
		
   		String name="null";
		String line;
		String drugMappings1 = ddiPath+"drug-mappings_updated3.tsv";
		try {
			BufferedReader br1 = new BufferedReader(new FileReader(drugMappings1));
	   		
			while ((line = br1.readLine()) != null ){
				if(line.contains(db)) {
			        String[] values = line.split("\t");
			        name = values[1];
			        break;
				}    
			}
			br1.close();
		}catch(Exception e) {
			e.printStackTrace();
		}

		String id="null";
		String drugMappings2 = ddiPath+"drug_SIDER_names.tsv";
		try {
			BufferedReader br = new BufferedReader(new FileReader(drugMappings2));
	   		
			while ((line = br.readLine()) != null ){
				String[] values = line.split("\t");
				if (values[1].equalsIgnoreCase(name)) {
			        id = values[0];
			        break;
				}    
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return id;

	}

}

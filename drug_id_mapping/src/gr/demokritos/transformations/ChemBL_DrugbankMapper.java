package gr.demokritos.transformations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
//import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jdom.input.SAXBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class ChemBL_DrugbankMapper {

	public static final String umls_api_key = "5e3ab18f-17fa-43f6-9cb2-41cccacc4ad5";

	public static HashMap<String,String> transformDBtoChemBL (String dtiFolder, List <String> db_ids) throws Exception{
	    
		String drugbankXMLFile = dtiFolder+"DrugBank.xml";

		//Query Drugbank file for  each LC drug to get external-identifiers>external-identifier><resource>ChEMBL</resource><identifier>CHEMBL112
		HashMap<String,String>  drug_DB_ChemBL_ids = OpenXML.getDrugChemBLids(drugbankXMLFile, db_ids);
 	        
		return drug_DB_ChemBL_ids;
	}	

	public static HashMap<String,String> addChemBLtoDBmapping (String dtiFolder, List <String> drugChemBLids, HashMap<String,String>  drug_DB_ChemBL_ids) throws Exception{
	    
		String drugbankXMLFile = dtiFolder+"DrugBank.xml";

		//Query Drugbank file to add DB ids of the ChemBL-ids of LC drugs
		drug_DB_ChemBL_ids = OpenXML.addDrugbankIds (drugbankXMLFile, drugChemBLids, drug_DB_ChemBL_ids);
		 
		return drug_DB_ChemBL_ids;
	}	

	public static void mapGroundtruthPosDrugbankIds (String dtiFolder, HashMap<String,String>  drug_DB_ChemBL_ids) throws Exception{
   		//Create a CSV File to save positive DTI pairs with DRUGBANK ids!
		PrintWriter writerGroundTruth= new PrintWriter(dtiFolder+"LC_DTIs_GroundTruth_Pos.csv", "UTF-8");
		writerGroundTruth.println("Drug,Target_Id,Target_name,Related_to_disease,Interaction");

		//Open CSV File to with positive DTI pairs (has ChemBl ids!)
		FileReader writerGroundTruthChemBL = new FileReader(dtiFolder+"LC_DTIs_GroundTruth_Pos_ChemBL.csv");
		BufferedReader inputLines = new BufferedReader(writerGroundTruthChemBL);
       	String inputLine = inputLines.readLine();//do not re-write headers line
    	while((inputLine = inputLines.readLine() ) != null) {
    	        String[] line = inputLine.split(",");  
    	        String drug_ChemBL_id = line[0];
    	        String drug_DB_id = drug_DB_ChemBL_ids.get(drug_ChemBL_id);
    	        if (drug_DB_id==null) {
    	        	//System.out.println("Error! Drugbank id not found  in DRUGBANK.xml for "+drug_ChemBL_id);
        	    	continue;
    	        }
    	        line[0]=drug_DB_id;
    	        writerGroundTruth.println(line[0]+","+line[1]+","+line[2]+",1,1");
        }
    	inputLines.close();
    	writerGroundTruthChemBL.close();
     	writerGroundTruth.close();
	}
	
	
	//Extend CACHE_DB_CUI_MAPP.tsv file with drugs from LC_DTIs_GroundTruth_Pos.csv
	public static void addAllDrugMappings(String dtiFolder, HashMap<String,String> drug_DB_ChemBL_ids) {
		try {
			
    		//First Open the TSV file to save existing DrugBank-UMLS mappings in an object
			List <String> drugsMapped = new LinkedList<String>();
    		BufferedReader br = new BufferedReader(new FileReader(dtiFolder+"CACHE_DB_CUI_MAPP.tsv"));
    		String line;
    		while ((line = br.readLine()) != null ){
    		        String[] values = line.split("\t");
    		        drugsMapped.add(values[0]);
    		}
    		br.close();

    		
    		System.out.println("DEBUG: Now updating CACHE_DB_CUI_MAPP.tsv");
    		//OPen the TSV file for writing now
    		FileWriter mappingFile = new FileWriter(dtiFolder+"CACHE_DB_CUI_MAPP.tsv",true);
			BufferedWriter mappingFileWriter = new BufferedWriter(mappingFile);
    		//Then open groundtruth_Pos file. For DB-ids not existing try to map their CUIs by calling the Metathesaurus service
			FileReader readerGroundTruth = new FileReader(dtiFolder+"LC_DTIs_GroundTruth_Pos.csv");
	    	BufferedReader reader = new BufferedReader(readerGroundTruth);
	
	    	String inputLine= reader.readLine();
	        while((inputLine = reader.readLine() ) != null) {
	        	String[] values = inputLine.split(",");
	        	String drugChemBLId = values[0];
	        	String drugDBid = drug_DB_ChemBL_ids.get(drugChemBLId);
	        	if ((drugDBid==null) || (drugsMapped.contains(drugDBid)))
	        		continue;
	        	String TGT = MetathesaurusAPIticketService.getTicketGrantingTicket(umls_api_key);
			    List<String> cuis =  getDrugCUIs (drugDBid,  TGT, dtiFolder);
			    if (cuis.isEmpty())
			    	continue;
			    for (String cui : cuis) {
			    	mappingFileWriter.append(drugDBid+"\t"+cui+"\t"+drugChemBLId);
		 	        mappingFileWriter.newLine();
		        }
	        	
	        }
	        reader.close();
	        readerGroundTruth.close();
			mappingFileWriter.close();
			mappingFile.close();

	        System.out.println("DEBUG: Saved all extra db-cui mappings from metathesaurus to the specified file");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static List<String> getDrugCUIs (String drug, String TGT, String dtiFolder) throws Exception {
		
		
		//Step3: call service with TGT to get ServiceTicket - serviceTicket lasts for 1 API call!
		String serviceTicket = MetathesaurusAPIticketService.getServiceTicket(TGT);
		List<String> cuis = new ArrayList<String>();
		
		String URLstring = "https://uts-ws.nlm.nih.gov/rest/search/current?string="+drug+"&inputType=sourceUi&searchType=exact&sabs=DRUGBANK&ticket="+serviceTicket;
		URL obj = new URL(URLstring);
	    URLConnection con = obj.openConnection();
	    HttpURLConnection http = (HttpURLConnection)con;
	    http.setRequestMethod("GET"); // PUT is another valid option
	    http.setDoOutput(true);
	    BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()));
	    String inputLine;
	    StringBuffer content = new StringBuffer();
	    while ((inputLine = in.readLine()) != null) {
	        content.append(inputLine);
	    }
	    in.close();
	    http.disconnect();	    	
	    
	    //System.out.println("UMLS JSON response for "+drug+": "+content.toString());

	    String jsonString =content.toString();
	    JSONObject jsonObject = (JSONObject) JSONValue.parse(jsonString);
	    JSONObject classType  = (JSONObject) jsonObject.get("result");
	    JSONArray json_cuis  = (JSONArray) classType.get("results");
	    
	    Iterator<JSONObject> iterator = json_cuis.iterator();
        while (iterator.hasNext()) {
        	JSONObject jsonCui = (JSONObject) iterator.next();
        	String cui = (String) jsonCui.get("ui");
        	
        	if (cui.equals("NONE")) {
        		System.out.println("DEBUG: Metathesaurus cannot find cuis of "+drug);
        		//cuis = getDrugCUIs(drug, dtiFolder+"CACHE_DB_CUI_MAPP.tsv");
        		break;
        	}
        	cuis.add(cui);
        }

	    return cuis;
	}
	
	public static void searchChemBLsNotFound (String dtiFolder, HashMap<String,DrugEntry> drug_entries){
		String drugbankMappingFile = dtiFolder+"drug-mappings.tsv";
		HashMap<String,DrugEntry> entries_chemBL = new HashMap<String,DrugEntry> ();
		
		try {
			PrintWriter writerDrugMap= new PrintWriter(dtiFolder+"drug-mappings_updated.tsv", "UTF-8");
			
			List<String> chemblsFound = new LinkedList<String>();
			Set<String> ttdIds = drug_entries.keySet();
			
			for (String ttdId: ttdIds) {
				DrugEntry drug = drug_entries.get(ttdId);
				if (drug.chembl_id!=null) {
					String chembl = drug.chembl_id;
		    		chembl=chembl.replace(".", "");
		    		chembl=chembl.replace(":", "");
		    		chembl=chembl.replace(";", "");
		    		drug.chembl_id=chembl;

		    		if (!chemblsFound.contains(chembl)) {
						chemblsFound.add(chembl);
						entries_chemBL.put(chembl, drug);

		    		}
				}	
			}
			List<String> chemblsToCheck = new LinkedList<String>();
			for (String chembl:chemblsFound) 
				chemblsToCheck.add(chembl);
	
	 		FileReader drugsMapReader = new FileReader(drugbankMappingFile);
			BufferedReader drugLines = new BufferedReader(drugsMapReader);
	       	String drugline;//ignore headers line
	    	while((drugline = drugLines.readLine() ) != null) {
	    		writerDrugMap.println(drugline);
	    		String[] line = drugline.split("\t");
	    		if (line.length<6) {
	    			System.out.print("prob-->");
	    			for (String l: line)
	    				System.out.print(l+" ");
	    			System.out.println();
	    			continue;
	    		}
	    		
	    		for (String chembl:chemblsFound) {
	    			if (line[5].equals(chembl)) {
	    				//System.out.println("&&&&&&&&&&//found "+chembl);
	    				//PROBLEM HERE...EXISTING CHEMBL ids not removed from toCheck list
		    			if ((chembl.contains("CHEMBL1173277")) || (chembl.contains("CHEMBL331858")))
		    				System.out.println("////////////////////////////////found "+chembl+"...should be removed from checklist");
		    			chemblsToCheck.remove(chembl);
		    			break;
		    		}	
	    		}	
	    	}
	    	
	    	drugLines.close();
	    	drugsMapReader.close();
	    	
	    	System.out.println("DEBUG: now update drug-mappings file with "+chemblsToCheck.size()+" chemBL ids queried");
	    	//now update drug-mappings file with chemBL ids queried
	    	for (String chembl:chemblsToCheck) {
	    		DrugEntry drug = entries_chemBL.get(chembl);
	    		//System.out.println("@@@check chembl: "+chembl);
    			if ((chembl.contains("CHEMBL1173277")) || (chembl.contains("CHEMBL331858")))
    				System.out.println("////////////////////////////////WTF????");
	    		String jsonString = queryUniChemAPI(chembl);
	    		//FOT: process JSON
	    		
	    		try {
		    		JSONArray jsonarray = (JSONArray) JSONValue.parse(jsonString);
		    		if (jsonarray.isEmpty())
		    			continue;
		    	    Iterator<JSONObject> iterator = jsonarray.iterator();
		            while (iterator.hasNext()) {
		            	JSONObject sourceObj = (JSONObject) iterator.next();
		            	String src_id = (String) sourceObj.get("src_id");
		            	String id = (String) sourceObj.get("src_compound_id");
		            	//System.out.println(src_id);
		            	if (src_id.equals("2")) {
		            		System.out.println("!!!!!!!!!!!!!!!!!BINGO.. found drugbankId="+id+" for chembl=" +chembl);
		            		drug.drugbank_id=id;
		            	}	
		            	else if ((src_id.equals("9")) && (id.length()>12))
		            		drug.zinc_id=id;
		            	else if (src_id.equals("22"))
		            		drug.pubchem_cid=id;
		            	else if (src_id.equals("31"))
		            		drug.bindingDB_id=id;
		            	else if (src_id.equals("7"))
		            		drug.chebi_id=id;
		            	else if (src_id.equals("6"))
		            		drug.kegg_cid=id;
			           
		            }
	    		}
	    		catch (Exception e) {
	    			System.out.println("erroneous json returned");
	    			//e.printStackTrace();
	    		}
	            writerDrugMap.println(drug.drugbank_id+"\t"+drug.name+"\t"+drug.ttd_id+"\t"+drug.pubchem_cid+"\t"+drug.cas_num+"\t"+drug.chembl_id+"\t"+drug.zinc_id+"\t"+drug.chebi_id+"\t"+drug.kegg_cid+"\t"+drug.kegg_id+"\t"+drug.bindingDB_id);
	    		
	    	}	
	    	writerDrugMap.close();
	    	
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String queryUniChemAPI (String chemBLid) {
		
		String jsonList="";
		try {
			String URLstring = "https://www.ebi.ac.uk/unichem/rest/src_compound_id_all/"+chemBLid+"/1";
			URL obj = new URL(URLstring);
			HttpURLConnection http = (HttpURLConnection) obj.openConnection();
		    //HttpURLConnection http = (HttpURLConnection)con;
		    http.setRequestMethod("GET"); // PUT is another valid option
		    http.setRequestProperty("Content-Type", "application/json; utf-8");
		    http.setDoOutput(true);
		    BufferedReader in = new BufferedReader( new InputStreamReader(http.getInputStream()));
		    String line;
		    while ((line = in.readLine()) != null) {
		        jsonList += line;
		    }
		    in.close();
		    http.disconnect();
		    //System.out.println(new Date().getTime()+" ALL GOOD");
		} catch (Exception e) {
			e.printStackTrace();
		}
		//System.out.println(jsonList);
		return jsonList;
	}

}

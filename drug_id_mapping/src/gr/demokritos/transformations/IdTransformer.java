package gr.demokritos.transformations;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class IdTransformer {

	public static HashMap<String, List<String>> drugCUIs = new HashMap<String, List<String>>();
	public static HashMap<String, List<String>> targetCUIs = new HashMap<String, List<String>>();
	//Step1: need a UMLS API key to get API token and query for target protein CUIs
	public static final String umls_api_key = "5e3ab18f-17fa-43f6-9cb2-41cccacc4ad5";

	public static void transformDBtoCUIs (String dtiFolder){
    
		//adding CUIs of some drugs that could not be found in Metathesaurus API by id
		
		try {
			//Step2: Get TGT based on API_key - TGT is valid for 8 hours!!
			String TGT = MetathesaurusAPIticketService.getTicketGrantingTicket(umls_api_key);
			//String TGT="TGT-483366-zZr91zEfukIN36EsMB3yt9lqLDhFwMPyMdOduCfjI5tscuMiKY-cas";
			
			//run only once for target DBid-CUI mapping...
			//then comment and continue using LCtargets.csv file
			//TargetCSVcreator.matchTargetNamesAndCUIs(dtiFolder, TGT);
						
			//Open the output CSV file to write positive Drug-target cui pairs -> input for ProcessNeo4j project
		    PrintWriter writerPos= new PrintWriter(dtiFolder+"ProcessNeo4j/PositivePairs/drug-pairs.csv", "UTF-8");
		   	//Open the output CSV file to write negative Drug-target cui pairs -> input for ProcessNeo4j project
	        PrintWriter writerNeg= new PrintWriter(dtiFolder+"ProcessNeo4j/NegativePairs/drug-pairs.csv", "UTF-8");

	        //add problematic drug CUIs in a file
	        PrintWriter writerProb= new PrintWriter(dtiFolder+"drug-cuis-missing.csv", "UTF-8");

    		//Open the CSV file with the DrugBank DTI pairs that interact/don't interact
    		String inputLine;
    		int l=0;
    		int noCUIsFound=0;
    		FileReader filerdr = new FileReader(dtiFolder+"LC_DTIs_GroundTruth.csv");
    		BufferedReader in = new BufferedReader(filerdr);
    		while(( inputLine = in.readLine() ) != null) {
    	        String[] line = inputLine.split(",");  
    	        if (l++==0)
    	        	continue;
    	        
    	        //include only LC-related drug pairs (disease-specific approach)
    	        String LC_related = line[3];
    	        if (LC_related.equals("0"))
    	        	continue;
  
    	        String drug = line[0];
    	        String target = line[1];
    	        System.out.println("Tranform ids of pair: "+drug+"-"+target);
    	        
    	        List<String> drug_cuis = drugCUIs.get(drug);
    	        List<String> target_cuis = targetCUIs.get(target);

    	        if (drug_cuis==null) {
    	        	drug_cuis = getDrugCUIs(drug, TGT, dtiFolder);
    	        	if (drug_cuis.isEmpty()) {
    	        		writerProb.println(drug);
    	        		noCUIsFound++;
    	        		continue;
    	        	}	
    	        	drugCUIs.put(drug, drug_cuis);
    	        }	
    	        if (target_cuis==null) {
        	        target_cuis = getTargetCUIs(target, dtiFolder);
        	    	targetCUIs.put(target, target_cuis);
    	        }
    	        for (int i = 0; i < drug_cuis.size(); i++) {
    	        	for (int j = 0; j < target_cuis.size(); j++) {
		    	        //deciding interaction from the oldest Drugbank version
		    	        String interaction = line[4];
		    	        if (interaction.equals("0"))
		    	        	writerNeg.println(drug_cuis.get(i)+","+target_cuis.get(j));
		    	        else if (interaction.equals("1"))
		    	        	writerPos.println(drug_cuis.get(i)+","+target_cuis.get(j));
		    	        else {
		    	        	System.out.println("Interaction information is wrong for drug pair: "+drug+"_"+target);
		    	        	break;
		    	        }
    	        	}
    	        }
    	        //just take 3 firs t drug pairs for testing now
    	        //FOT: remove this line 
    	        //if (l==5)
    	        	//break;
    		}
	        System.out.println("CUIs not found for: "+noCUIsFound+" drugs");
	  		writerPos.close();
	  		writerNeg.close();
	  		
	  		writerProb.close();
	  		
	  		in.close();
	  		filerdr.close();
    		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	
	public static List<String> getTargetCUIs (String target, String dtiFolder) throws Exception {
					
		List<String> cuis = new ArrayList<String>();
			
		//Open the CSV file with the DrugBank-UMLS mapping for targets
		BufferedReader br = new BufferedReader(new FileReader(dtiFolder+"LCtargets.csv"));
		String line;
		boolean foundDBid=false;
		while ((line = br.readLine()) != null ){
		     String[] values = line.split(",");
		        
		     if(values[2].equals(target)) {
		      	foundDBid=true;
		       	cuis.add(values[1]);
		       	System.out.println("Target: "+target+" , has CUI: "+values[1]);
		     }
		     else if (foundDBid)  //dont need to search the rest of the DrugBank ids....
		        	break;
		}
		br.close();
		return cuis;
		
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
        		cuis = getDrugCUIsFromFile(drug, dtiFolder);
        		break;
        	}
        	System.out.println("For drug "+drug+" found cui: "+cui);
            cuis.add(cui);
        }

	    return cuis;
	}
	
	public static List<String> getDrugCUIsFromFile (String drug, String dtiFolder) throws Exception {
					
		List<String> cuis = new ArrayList<String>();
			
		//Open the CSV file with the DrugBank-UMLS mapping for targets
		BufferedReader br = new BufferedReader(new FileReader(dtiFolder+"CACHE_DB_CUI_MAPP.tsv"));
		String line;
		boolean foundDBid=false;
		while ((line = br.readLine()) != null ){
		     String[] values = line.split("\t");
		        
		     if(values[0].equals(drug)) {
		      	foundDBid=true;
		       	cuis.add(values[1]);
		       	System.out.println("Drug: "+drug+" , has CUI: "+values[1]);
		     }
		     else if (foundDBid)  //dont need to search the rest of the DrugBank ids....
		        	break;
		}
		br.close();
		return cuis;
		
	}

	
}
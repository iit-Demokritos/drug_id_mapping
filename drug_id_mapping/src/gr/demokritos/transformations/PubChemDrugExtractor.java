package gr.demokritos.transformations;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.jdom.*;
import org.jdom.input.SAXBuilder;

public class PubChemDrugExtractor {
	
	
	public HashMap<String,DrugEntry> searchPubChemEnrichEntries(HashMap<String,String> pubChemIdsNotFound, HashMap<String,DrugEntry> drug_entries){
		
		int d=0;
		int p=0;
		Set<String> pubChemCids = pubChemIdsNotFound.keySet();
		for (String pubChemCid: pubChemCids) {
			Document document = queryPubChem(pubChemCid);
			
			//parse XML to get ChemBL, DB, Zinc in synonyms
			String ttd = pubChemIdsNotFound.get(pubChemCid);
			DrugEntry drug = drug_entries.get(ttd);
			try {
				List<Element> items = document.getRootElement().getChild("DocSum").getChildren("Item");
				for (Element item:items) {
					if (item.getAttributeValue("Name").equals("SynonymList"))  {
						List<Element> synonyms = item.getChildren();
						for (Element synonym:synonyms) {
							String id = synonym.getValue();
							if ((id.startsWith("DB")) &&(!id.contains("-"))) {
								drug.drugbank_id=id;
								d++;
							}
							else if (id.startsWith("CHEMBL"))
								drug.chembl_id=id;
							else if (id.startsWith("ZINC"))
								drug.zinc_id=id;
							else if (id.startsWith("CHEBI"))
								drug.chebi_id=id.substring(6);
							else if (id.startsWith("BDBM"))
								drug.bindingDB_id=id.substring(4);
							else if (id.startsWith("CAS"))
								drug.cas_num=id.substring(4);
						}
						drug_entries.replace(ttd, drug);
					}
				}
				Thread.sleep(200);
				
			}catch (NullPointerException e) {
				p++;
				
			}
			catch (Exception e) {
				System.out.println("Error! Response code: 400 for drug: "+pubChemCid);
				//e.printStackTrace();
				
			}
		}
		System.out.println("DEBUG: Harvested "+d+" missing drugbank ids from PubChem Entrez e-utilities API.");
		System.out.println("DEBUG: Received an XML record without synonyms for "+p+" pubchem ids.");
		return drug_entries;
	}
	
	public Document queryPubChem(String pubChemCid)  {
		
		Document document = new Document();
		try {
			String URLstring = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pccompound&id="+pubChemCid+"&api_key=7c9c1d983d0768a407fe13b8863842f8ff08";
			URL obj = new URL(URLstring);
			HttpURLConnection http = (HttpURLConnection) obj.openConnection();
		    //HttpURLConnection http = (HttpURLConnection)con;
		    http.setRequestMethod("GET"); // PUT is another valid option
		    http.setRequestProperty("Content-Type", "application/xml; utf-8");
		    http.setDoOutput(true);
		    BufferedReader in = new BufferedReader( new InputStreamReader(http.getInputStream()));
		    SAXBuilder saxBuilder = new SAXBuilder();
			document = saxBuilder.build(in);
		    in.close();
		    http.disconnect();
		    //System.out.println(new Date().getTime()+" ALL GOOD");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return document;
	}
}

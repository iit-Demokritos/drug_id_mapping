package gr.demokritos.transformations;
//import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.Enumeration;
//import java.util.Hashtable;
//import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.scireum.open.xml.NodeHandler;
import com.scireum.open.xml.StructuredNode;
import com.scireum.open.xml.XMLReader;

/**
 * Small example class which show how to use the {@link XMLReader}.
 */
public class OpenXML {

	public static HashMap<String,String> getDrugChemBLids (String drugbankXML, List<String> lc_drugs) throws Exception {
		//List<String> targets = new LinkedList<String>();
		HashMap<String,String> drug_DB_ChemBL_ids = new HashMap<String,String>();

		XMLReader r = new XMLReader();
		// We can add several handlers which are triggered for a given node
		// name. The complete sub-dom of this node is then parsed and made
		// available as a StructuredNode
		r.addHandler("drug", new NodeHandler() {

			@Override
			public void process(StructuredNode node) {
				try {
					
					String drugId = node.queryValue("drugbank-id").asString();
					//System.out.println("opening drug node"+drugId);
					//Check if the node we found in Drugbank.xml is a basic drug node (has Descriptions field) 
					//  and if it contains the drug we are looking for (compare drugbank id)
					if ((node.queryString("description")==null) || (!lc_drugs.contains(drugId)))
						return;
					if (drug_DB_ChemBL_ids.containsValue(drugId)) //ChemBLid for this drug has been already saved...
						return;
					//System.out.println("DEBUG: Examining LC drug "+drugId+" in DRUGBANK...");

					if (!node.isEmpty("external-identifiers")) { //<resource></resource><>CHEMBL112
						List<StructuredNode> identifiers = node.queryNodeList("external-identifiers/external-identifier");
						for (int i = 0; i < identifiers.size(); i++) {
							
							String resourceId=identifiers.get(i).queryValue("resource").asString();
							if (resourceId.equals("ChEMBL")) {
								String ChemBL_id = identifiers.get(i).queryValue("identifier").asString();
								drug_DB_ChemBL_ids.put(ChemBL_id,drugId);
						//		System.out.println("DEBUG: ...found ChEMBL id: "+ChemBL_id);
							}
		    	        } 
					}
					//throw new SAXException();
					
				} catch (XPathExpressionException e) {
					e.printStackTrace();
				}
				
			}
		});
		// Parse our little test file. Note, that this could be easily processed
		// with a DOM-parser and only serves as showcase. Real life input files
		// would be much bigger...
		try {

			r.parse(new FileInputStream(drugbankXML));
		} catch (SAXException e) {
		    e.printStackTrace();
		}
		return drug_DB_ChemBL_ids;
	}



	public static HashMap<String,DrugEntry> getDBidsforTTDdrugs (String drugbankXML, HashMap<String,DrugEntry>  drug_entries, HashMap<String,String> drug_id_names,HashMap<String,String>  drug_id_cas, HashMap<String,String> drug_id_pubChem) throws Exception {
		//List<String> targets = new LinkedList<String>();
		Set<String> namesList = drug_id_names.keySet();
		Set<String> casList =   drug_id_cas.keySet();
		Set<String> pubChemList =  drug_id_pubChem.keySet();

		//DEBUG
		//if (casList.contains("656247-17-5"))
			//System.out.println("Set contains 656247-17-5");

		
		XMLReader r = new XMLReader();
		// We can add several handlers which are triggered for a given node
		// name. The complete sub-dom of this node is then parsed and made
		// available as a StructuredNode
		r.addHandler("drug", new NodeHandler() {

			@Override
			public void process(StructuredNode node) {
				try {
					
					String drugId = node.queryValue("drugbank-id").asString();
					//System.out.println("opening drug node"+drugId);
					//Check if the node we found in Drugbank.xml is a basic drug node (has Descriptions field) 
					//  and if it contains the drug we are looking for (compare drugbank id)

					
					if (node.queryString("description")==null) 
						return;
					//System.out.println("DEBUG: Examining LC drug "+drugId+" in DRUGBANK...");
					
					String name = node.queryValue("name").asString();
					if (namesList.contains(name)) {
						String ttdId = drug_id_names.get(name);
						DrugEntry drug = drug_entries.get(ttdId);
						drug.drugbank_id=drugId;
						drug_entries.replace(ttdId, drug);
						//return;
					}
					
					
					String cas_number = node.queryValue("cas-number").asString();
					//FOT: PROBLEM HERE, NEVER FOUND!!!!
					if (cas_number.equals("656247-17-5"))
						System.out.println("FOUND INTEDANIB in DRUGBANK!!! should add: "+drugId);
					if (casList.contains(cas_number)) {
						String ttdId = drug_id_cas.get(cas_number);
						DrugEntry drug = drug_entries.get(ttdId);
						if (cas_number.equals("656247-17-5"))
							System.out.println(drug.name+" exists in search list! ");
						drug.drugbank_id=drugId;
						drug_entries.replace(ttdId, drug);
						//return;
					}

					if (!node.isEmpty("external-identifiers")) { //<resource></resource><>CHEMBL112
						List<StructuredNode> identifiers = node.queryNodeList("external-identifiers/external-identifier");
						for (int i = 0; i < identifiers.size(); i++) {
							
							String resourceId=identifiers.get(i).queryValue("resource").asString();
							if (resourceId.equals("PubChem Compound")) {
								String pubChem_id = identifiers.get(i).queryValue("identifier").asString();
								if (pubChemList.contains(pubChem_id)) {
									String ttdId = drug_id_pubChem.get(pubChem_id);
									DrugEntry drug = drug_entries.get(ttdId);
									drug.drugbank_id=drugId;
									drug_entries.replace(ttdId, drug);
									//return;
								}
						//		System.out.println("DEBUG: ...found ChEMBL id: "+ChemBL_id);
							}
		    	        } 
					}
					//throw new SAXException();
					
				} catch (XPathExpressionException e) {
					e.printStackTrace();
				}
				
			}
		});
		// Parse our little test file. Note, that this could be easily processed
		// with a DOM-parser and only serves as showcase. Real life input files
		// would be much bigger...
		try {

			r.parse(new FileInputStream(drugbankXML));
		} catch (SAXException e) {
		    e.printStackTrace();
		}
		return drug_entries;
	}


	
	
	public static HashMap<String,String> addDrugbankIds (String drugbankXML, List<String> drugChemBLids, HashMap<String,String> drug_ChemBL_DB_ids) throws Exception {

		
		XMLReader r = new XMLReader();
		// We can add several handlers which are triggered for a given node
		// name. The complete sub-dom of this node is then parsed and made
		// available as a StructuredNode
		r.addHandler("drug", new NodeHandler() {

			@Override
			public void process(StructuredNode node) {
				try {
					
					if (drugChemBLids.isEmpty())
						return;
					String drugId = node.queryValue("drugbank-id").asString();
					//System.out.println("opening drug node"+drugId);
					//Check if the node we found in Drugbank.xml is a basic drug node (has Descriptions field) 
					//  and if it contains the drug we are looking for (compare drugbank id)
					if (node.queryString("description")==null)
						return;
					if (drug_ChemBL_DB_ids.containsValue(drugId)) //ChemBLid for this drug has been already saved...
						return;
	
					if (!node.isEmpty("external-identifiers")) { //<resource></resource><>CHEMBL112
						List<StructuredNode> identifiers = node.queryNodeList("external-identifiers/external-identifier");
						for (int i = 0; i < identifiers.size(); i++) {
							String resourceId=identifiers.get(i).queryValue("resource").asString();
							if (resourceId.equals("ChEMBL")) {
								String ChemBL_id = identifiers.get(i).queryValue("identifier").asString();
								if(drugChemBLids.contains(ChemBL_id)) {
									drug_ChemBL_DB_ids.put(ChemBL_id, drugId);
							//		System.out.println("DEBUG: Added DRUGBANK id: "+drugId+" for ChEMBL id: "+ChemBL_id);
									drugChemBLids.remove(ChemBL_id);
								}	
							}
		    	        } 
					}
					//throw new SAXException();
					
				} catch (XPathExpressionException e) {
					e.printStackTrace();
				}
				
			}
		});
		// Parse our little test file. Note, that this could be easily processed
		// with a DOM-parser and only serves as showcase. Real life input files
		// would be much bigger...
		try {
			r.parse(new FileInputStream(drugbankXML));
		} catch (SAXException e) {
		    e.printStackTrace();
		}
		return drug_ChemBL_DB_ids;
	}
	

	public static void main (String [] args) {
		String dtiFolder = args[0];
		String drugbankXML = dtiFolder+"DrugBank.xml";
		
		PrintWriter writerDrugMap = null;
		HashMap <String,DrugEntry> drugmap = new HashMap <String,DrugEntry>(); 
		try {
   		//Create a CSV File to save ALL possible Drugbank drug-mappings
		writerDrugMap= new PrintWriter(dtiFolder+"drug-mappings.tsv", "UTF-8");
		writerDrugMap.println("drugbankId\tname\tttd_id\tpubchem_cid\tcas_num\tchembl_id\tzinc_id\tchebi_id\tkegg_cid\tkegg_id\tbindingDB_id");
		
		drugmap = extractAllDrugs(drugbankXML);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		for (Map.Entry<String, DrugEntry> entry : drugmap.entrySet()) {
		    String drugbankId = entry.getKey();
		    DrugEntry drug_details = entry.getValue();
		    writerDrugMap.println(drugbankId+"\t"+drug_details.name+"\t"+drug_details.ttd_id+"\t"+drug_details.pubchem_cid+"\t"+drug_details.cas_num+"\t"+drug_details.chembl_id+"\t"+drug_details.zinc_id+"\t"+drug_details.chebi_id+"\t"+drug_details.kegg_cid+"\t"+drug_details.kegg_id+"\t"+drug_details.bindingDB_id);
		}
	    writerDrugMap.close();	
		
	}
	public static HashMap <String,DrugEntry>  extractAllDrugs (String drugbankXML) throws Exception{		
		
		HashMap <String,DrugEntry> drugmap = new HashMap <String,DrugEntry>();
		
		XMLReader r = new XMLReader();
		// We can add several handlers which are triggered for a given node
		// name. The complete sub-dom of this node is then parsed and made
		// available as a StructuredNode
		r.addHandler("drug", new NodeHandler() {

			@Override
			public void process(StructuredNode node) {
				try {
					
					String drugId = node.queryValue("drugbank-id").asString();
					//System.out.println("opening drug node"+drugId);
					//Check if the node we found in Drugbank.xml is a basic drug node (has Descriptions field) 
					//  and if it contains the drug we are looking for (compare drugbank id)

					
					if (node.queryString("description")==null) 
						return;
					//System.out.println("DEBUG: Examining LC drug "+drugId+" in DRUGBANK...");
					
					DrugEntry drug = new DrugEntry();
					String name = node.queryValue("name").asString();
					drug.name=name;
					String cas_number = node.queryValue("cas-number").asString();
					drug.cas_num=cas_number;
					
					if (!node.isEmpty("external-identifiers")) { //<resource></resource><>CHEMBL112
						List<StructuredNode> identifiers = node.queryNodeList("external-identifiers/external-identifier");
						for (int i = 0; i < identifiers.size(); i++) {
							
							String resourceId=identifiers.get(i).queryValue("resource").asString();
							if (resourceId.equals("PubChem Compound")) {
								String pubChem_id = identifiers.get(i).queryValue("identifier").asString();
								drug.pubchem_cid=pubChem_id ;
							}
							else if (resourceId.equals("ChEBI")) {
								String chebi_id = identifiers.get(i).queryValue("identifier").asString();
								drug.chebi_id=chebi_id ;
							}
							else if (resourceId.equals("KEGG Compound")) {
								String id = identifiers.get(i).queryValue("identifier").asString();
								drug.kegg_cid=id ;
							}
							else if (resourceId.equals("Therapeutic Targets Database")) {
								String id = identifiers.get(i).queryValue("identifier").asString();
								drug.ttd_id=id ;
							}
							else if (resourceId.equals("KEGG Drug")) {
								String id = identifiers.get(i).queryValue("identifier").asString();
								drug.kegg_id=id ;
							}
							else if (resourceId.equals("ChEMBL")) {
								String id = identifiers.get(i).queryValue("identifier").asString();
								drug.chembl_id=id ;
							}
							else if (resourceId.equals("ZINC")) {
								String id = identifiers.get(i).queryValue("identifier").asString();
								drug.zinc_id=id ;
							}
							else if (resourceId.equals("BindingDB")) {
								String id = identifiers.get(i).queryValue("identifier").asString();
								drug.bindingDB_id=id ;
							}
							
						} 
					}
					drugmap.put(drugId, drug);
					//throw new SAXException();
					
				} catch (XPathExpressionException e) {
					e.printStackTrace();
				}
				
			}
		});
		// Parse our little test file. Note, that this could be easily processed
		// with a DOM-parser and only serves as showcase. Real life input files
		// would be much bigger...
		try {

			r.parse(new FileInputStream(drugbankXML));
		} catch (SAXException e) {
		    e.printStackTrace();
		}
		
		return drugmap;
	}


}


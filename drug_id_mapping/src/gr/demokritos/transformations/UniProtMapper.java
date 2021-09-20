package gr.demokritos.transformations;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;


public class UniProtMapper {



	public static HashMap<String,String> getUniProtmapping (String dtiFolder, Collection<String> uniProtNames) throws Exception{
	    
		String uniprotFile = dtiFolder+"protein_exps/TTD/uniprotmap";
		HashMap<String,String> uniprot_names_ids = new HashMap<String,String>();
		
		//Open mappings file
		FileReader uniprotReader = new FileReader(uniprotFile);
		BufferedReader inputLines = new BufferedReader(uniprotReader);
       	String inputLine;
    	while((inputLine = inputLines.readLine() ) != null) {
    	        String[] line = inputLine.split("\t");  
    	        if(uniProtNames.contains(line[1]))
    	        	uniprot_names_ids.put(line[1], line[0]);
        }
    	inputLines.close();
    	uniprotReader.close();
    	
		return uniprot_names_ids;
	}	


	public static void mapGroundtruthUniProtIds (String dtiFolder, HashMap<String,String>  uniprot_names_ids) throws Exception{
   		//Create a CSV File to save positive DTI pairs with uniprot ids!
		PrintWriter writerGroundTruth= new PrintWriter(dtiFolder+"protein_exps/TTD/DTIs_GroundTruth_Pos.csv", "UTF-8");
		writerGroundTruth.println("Drug,Target_Id,Gene_name,Related_to_disease,Interaction");

		int notfound=0;
		//Open CSV File to with positive DTI pairs (has uniprot names!)
		FileReader readerGroundTruthUniprot = new FileReader(dtiFolder+"protein_exps/TTD/DTIs_GroundTruth_UniProt_Pos.csv");
		BufferedReader inputLines = new BufferedReader(readerGroundTruthUniprot);
       	String inputLine = inputLines.readLine();//do not re-write headers line
    	while((inputLine = inputLines.readLine() ) != null) {
    	        String[] line = inputLine.split(",");  
    	        String uniProtName = line[1];
    	        String uniProt_id = uniprot_names_ids.get(uniProtName);
    	        if (uniProt_id==null) {
    	        	notfound++;
    	        	//System.out.println("Error! Drugbank id not found  in DRUGBANK.xml for "+drug_ChemBL_id);
        	    	continue;
    	        }
    	        line[1]=uniProt_id;
    	        writerGroundTruth.println(line[0]+","+line[1]+","+line[2]+",??,1");
        }
    	System.out.println(notfound+" uniprot ids not found in uniprotmap file");
    	inputLines.close();
    	readerGroundTruthUniprot.close();
     	writerGroundTruth.close();
	}
	

}

# Mapping drug ids from Drugbank, TTD, UMLS, KEGG, ChEMBL and other databases:

This project includes various transformation tools that create and enrich a [TSV file](https://github.com/iit-Demokritos/drug_id_mapping/blob/main/drug-mappings.tsv), which lists thousand of known drugs and all the available ids that could be found in drug databases.

In particular, we start from retrieving the drug information included in the latest Drugbank [1] (VERSION 5.1.8, RELEASED ON 2021-01-03) as well as in the latest Therapeutic Target Database [2] (VERSION 7.1.01, RELEASED ON 2019.07.14) in a file.
We then enrich the drug fields by querying the following sources:
-  the web services API of ChEMBL Database [3][4]
-  the PUG REST API of PubChem Database [5]
-  the drugs file in the FTP server of the KEGG Database [6][7][8]
-  the UMLS Metathesaurus vocabulary Database[9], using the MetamorphoSys tool 

## Mapping TSV file data format

The resulting file ([drug-mappings.tsv](https://github.com/iit-Demokritos/drug_id_mapping/blob/main/drug-mappings.tsv)) includes a tab-separated entry for each drug, including multiple ids that could be found and crossed-checked from the aforementioned databases.
For ids not found in none of the above sources, 'null' string is added. Multiple CUIs for a specific drug are separated with a comma separator(,).
An example of the format of the TSV data file is as follows:

```sh
drugbankId	name	ttd_id	pubchem_cid	cas_num	chembl_id	zinc_id	chebi_id	kegg_cid	kegg_id	bindingDB_id	UMLS_cuis
DB01149	Nefazodone	DAP000042	4449	83366-66-9	CHEMBL623	ZINC000000538065	7494	C07256	D08257	50069447	C0068485
DB01157	Trimetrexate	DAP000635	5583	52128-35-5	CHEMBL119	ZINC000000598852	9737	C11154	D06238	18268	C0085176
DB01248	Docetaxel	DAP000590	148124	114977-28-5	CHEMBL92	ZINC000085537053	4672	C11231	D02165	36351	C0246415,C0771375
DB02579	Acrylic Acid	D0E3MA	6581	79-10-7	CHEMBL1213529	ZINC000000895281	18308	C00511	null	null	null
...
```

## Java Project File Structure & running

The code includes the basic package gr.demokritos.tranformations with various classes serving different functionalities, e.g.:
- CreateDrugMappings class: main class, can be used to call all other classes of interest
- xx_DrugbankMapper class: Maps the ids of xx Database to Drugbank
- xxIdTransformer classes: Transforms the ids of xx Database to Drugbank and retrieve the respective UMLS_cuis
- OpenXML class: Parses the Drugbank XML file and retrieves all information of interest
- MetathesaurusAPIticketService class: creates a new TGT and API key, in order to query UMLS REST API (alternatively to using MetamorphoSys tool)

To run the aforementioned Java project, it is obvious that we need to have access to the following sources:
- Drugbank (to download the latest XML file)
- TTD (to download the drugs' information file in raw format)
- Entrez Programming Utilities (E-utilities) API (query PUG for PubChem ids and obtain a token to query for a TGT and an API key)
- KEGG (to download the KEGG drug file)
- UniChem API (to query for ChEMBL ids)
and also include needed jar libraries in the CLASSPATH.

## References
[1]: Wishart, D. S., Knox, C., Guo, A. C., Shrivastava, S., Hassanali, M., Stothard, P., ... & Woolsey, J. (2006). DrugBank: a comprehensive resource for in silico drug discovery and exploration. Nucleic acids research, 34(suppl_1), D668-D672.

[2]: Y. X. Wang, S. Zhang, F. C. Li, Y. Zhou, Y. Zhang, R. Y. Zhang, J. Zhu, Y. X. Ren, Y. Tan, C. Qin, Y. H. Li, X. X. Li, Y. Z. Chen* and F. Zhu*. Therapeutic Target Database 2020: enriched resource for facilitating research and early development of targeted therapeutics. Nucleic Acids Research. 48(D1): D1031-D1041 (2020). PubMed ID: 31691823

[3]: Mendez, D., Gaulton, A., Bento, A. P., Chambers, J., De Veij, M., Félix, E., ... & Leach, A. R. (2019). ChEMBL: towards direct deposition of bioassay data. Nucleic acids research, 47(D1), D930-D940.

[4]: Davies, M., Nowotka, M., Papadatos, G., Dedman, N., Gaulton, A., Atkinson, F., ... & Overington, J. P. (2015). ChEMBL web services: streamlining access to drug discovery data and utilities. Nucleic acids research, 43(W1), W612-W620.

[5]: Kim, S., Thiessen, P. A., Cheng, T., Yu, B., & Bolton, E. E. (2018). An update on PUG-REST: RESTful interface for programmatic access to PubChem. Nucleic acids research, 46(W1), W563-W570.

[6]: Kanehisa, M., & Goto, S. (2000). KEGG: kyoto encyclopedia of genes and genomes. Nucleic acids research, 28(1), 27-30.

[7]: Kanehisa, M. (2019). Toward understanding the origin and evolution of cellular organisms. Protein Science, 28(11), 1947-1951.

[8]: Kanehisa, M., Furumichi, M., Sato, Y., Ishiguro-Watanabe, M., & Tanabe, M. (2021). KEGG: integrating viruses and cellular organisms. Nucleic acids research, 49(D1), D545-D551.

[9]: Bodenreider, O. (2004). The unified medical language system (UMLS): integrating biomedical terminology. Nucleic acids research, 32(suppl_1), D267-D270.

## Licence & Required Citation
For any use of the drug-mappings.tsv file in your work, **a citation to the following paper is expected:**
*Bougiatiotis, K., Aisopos, F., Nentidis, A., Krithara, A., & Paliouras, G. (2020, August). Drug-Drug Interaction Prediction on a Biomedical Literature Knowledge Graph. In International Conference on Artificial Intelligence in Medicine (pp. 122-132). Springer, Cham.*

drug_id_mapping - NCSR Demokritos module Copyright 2021 Fotis Aisopos
The Java code and TSV file are provided **only for academic/research use and are licensed under the Apache License, Version 2.0 (the "License")**; you may not use this file except in compliance with the License. You may obtain a copy of the License at: https://www.apache.org/licenses/LICENSE-2.0 .

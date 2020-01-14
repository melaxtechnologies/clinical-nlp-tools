package com.melax.demo.format;

import java.io.File;

import edu.uth.clamp.nlp.structure.ClampNameEntity;
import edu.uth.clamp.nlp.structure.Document;

public class InputMergeXmiFormat extends InputFormat {
	File goldFile = null;
	File predictFile = null;
	
	public InputMergeXmiFormat goldFile( File goldFile ) {
		this.goldFile = goldFile;
		return this;
	}
	public InputMergeXmiFormat predictFile( File predictFile ) {
		this.predictFile = predictFile;
		return this;
	}
	
	/**
     * @return new xmi file that contains both doc1 and doc2 ClampNameEntity info ;
     * @throws Exception
     */	
	@Override
	public DocumentFormat toDoc() throws Exception {
		Document doc1 = new Document( goldFile );
		Document doc2 = new Document( predictFile );
		
		//for ClampNameEntity cne in doc1, set their SemanticTags as "ann1:: + cne original SemanticTag" to distinguish with doc2's NameEntity SemanticTags
		for( ClampNameEntity cne : doc1.getNameEntity() ) {
			cne.setSemanticTag( "ann1::" + cne.getSemanticTag() );
		}
		
		//for ClampNameEntity cne2 in doc2, label it in doc1 using the "ann2:: + cne2 original SemanticTag" as SemanticTag
		for( ClampNameEntity cne2 : doc2.getNameEntity() ) {
			ClampNameEntity cne1 = new ClampNameEntity( doc1.getJCas(), cne2.getBegin(), cne2.getEnd(), "ann2::" + cne2.getSemanticTag() );
			cne1.addToIndexes();
		}
		return new DocumentFormat( doc1 );
	}
	
	public static void main( String[] argv ) throws Exception {
		//Example of merge xmi files in input1 and input2 folder and save the merged xmi file into the mergeoutput folder
		File indir = new File( "data/xmi/input1/" );
		for( File file : indir.listFiles() ) {
			File file1 = file;
			File file2 = new File( "data/xmi/input2/" + file.getName() );
			Document doc = new InputMergeXmiFormat().goldFile( file1 ).predictFile( file2 ).toDoc().getClampDocument();		
			doc.save( "data/xmi/mergeoutput/" + file.getName() 	);
			System.out.println( file.getName() );
			
		}
				
	}
	

}

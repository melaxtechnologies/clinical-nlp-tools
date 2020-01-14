package com.melax.demo.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.uima.UIMAException;

import edu.uth.clamp.io.DocumentIOException;
import edu.uth.clamp.nlp.structure.ClampNameEntity;
import edu.uth.clamp.nlp.structure.ClampRelation;
import edu.uth.clamp.nlp.structure.Document;

/**
 * @author jingqiwang
 * 
 * collect corpus status, how many entities? how many relations?
 *
 */
public class CorpusStatus {

    public static void printStatus( List<File> files ) throws UIMAException, IOException, DocumentIOException {
        Map<String, Integer> countMap = new TreeMap<String, Integer>();
        
        for( File file : files ) {
            Document doc = new Document( file );            
            // For Named Entities;
            for( ClampNameEntity cne : doc.getNameEntity() ) {
                String sem = cne.getSemanticTag();
                countMap.putIfAbsent( sem, 0 );
                countMap.put( sem, countMap.get(sem) + 1 );
            }
            
            // For Relations;
            for( ClampRelation rel : doc.getRelations() ) {
                String sem =  rel.getSemanticTag() 
                        + " " + rel.getEntFrom().getSemanticTag() 
                        + " " + rel.getEntTo().getSemanticTag();
                countMap.putIfAbsent( sem, 0 );
                countMap.put( sem, countMap.get(sem) + 1 );
            }
        }        
        
        System.out.println( "Document count: " + files.size() );
        for( String key : countMap.keySet() ) {
            System.out.println( key + "\t" + countMap.get( key ) );
        }
    }
    
    
    public static List<File> getFileList( File indir ) throws UIMAException, IOException {
        List<File> ret = new ArrayList<File>();
        for( File file : indir.listFiles() ) {
            if( file.getName().startsWith( "." ) ) {
                continue;
            } else if( !file.getName().endsWith( ".xmi" ) ) {
                continue;
            }
            ret.add( file );
        }
        return ret;     
    }
    
    public static void printStatus( File indir ) throws UIMAException, IOException, DocumentIOException {
      printStatus( getFileList( indir ) );
      return;
    }
    
    public static void main( String[] argv ) throws UIMAException, IOException, DocumentIOException {
    	
    	//Example of getting corpus status, will print the number of different types of entities and relations
    	List<File> list = getFileList( new File( "xmi/input/" ) );
    	printStatus( list );
		return;
    }
    
}
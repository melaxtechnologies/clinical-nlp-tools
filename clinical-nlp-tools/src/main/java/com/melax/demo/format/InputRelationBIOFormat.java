package com.melax.demo.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;

import com.melax.example.format.InputEntityBIOFormat;

import edu.uth.clamp.nlp.core.NewlineSentDetector;
import edu.uth.clamp.nlp.uima.SentDetectorUIMA;
import edu.uth.clamp.nlp.core.SpaceTokenizer;
import edu.uth.clamp.nlp.uima.TokenizerUIMA;
import edu.uth.clamp.nlp.structure.ClampNameEntity;
import edu.uth.clamp.nlp.structure.ClampRelation;
import edu.uth.clamp.nlp.uima.DocProcessor;
import edu.uth.clamp.nlp.structure.Document;
import edu.uth.clamp.nlp.structure.XmiUtil;

/**
 * @author jingqiwang
 * 
 * Take relation BIO file as input
 * Generate clamp document
 *
 */
public class InputRelationBIOFormat extends InputFormat {
  public static final String LABELB = "B-";
  public static final String LABELI = "I-";

  private class NumberPair {
    int start = 0;
    int end = 0;
    String tag = "";
    public NumberPair( int start, int end, String tag ) {
      this.start = start;
      this.end = end;
      this.tag = tag;
    }
    public int start() {
      return this.start;
    }
    public int end() {
      return this.end;
    }
    public String tag() {
      return this.tag;
    }
  }
  
  /* the bio file */
  File bioFile = null;
  
  
  InputRelationBIOFormat bio( File bioFile ) {
    this.bioFile = bioFile;    
    return this;
  }
  
  private List<NumberPair> parseEntities( List<NumberPair> tokens ) {
    List<NumberPair> ret = new ArrayList<NumberPair>();
    for( int i = 0; i < tokens.size(); i++ ) {
      String tag = tokens.get(i).tag();
      if( tag.startsWith( LABELB ) ) {
        int start = i;
        int end = i + 1;
        String sem = tag.substring( LABELB.length() );
        for( int j = i + 1; j < tokens.size(); j++ ) {
          end = j;
          if( !tokens.get(j).tag().equals( LABELI + sem ) ) {
            break;
          }
        }
        ret.add( new NumberPair( start, end, sem ) );
      }
    }
    
    return ret;
  }


  /**
   * @param bioFile the bio file;
   * @return the clamp document;
   * @throws UIMAException UIMA exception;
   * @throws IOException IO exception;
   */
  private Document parseBIOFile(File bioFile ) throws IOException, UIMAException {
    BufferedReader infile = new BufferedReader( new FileReader( bioFile ) );
    String line = "";
    StringBuffer sb = new StringBuffer();
    
    // load tokens and bios;
    List<NumberPair> tokens = new ArrayList<NumberPair>();
    List<NumberPair> primaryTokens = new ArrayList<NumberPair>();
    
    while( ( line = infile.readLine() ) != null ) {
      if( line.trim().isEmpty() ) {
        sb.append( "\n" );
        continue;
      }
      String token = line.trim().split( "\\s" )[0];
      String primary = line.trim().split( "\\s" )[1];
      String tag = line.trim().split( "\\s" )[2];
      
      tokens.add( new NumberPair( sb.length(), sb.length() + token.length(), tag ) );
      primaryTokens.add( new NumberPair( sb.length(), sb.length() + token.length(), primary) );
      sb.append( token );
      sb.append( " " );
    }
    infile.close();
    
    // create document;
    JCas aJCas = XmiUtil.createJCas();
    aJCas.setDocumentText( sb.toString() );
    Document doc = new Document( aJCas );
    // Sent detector and tokenizer;
    DocProcessor sent = new SentDetectorUIMA( NewlineSentDetector.getDefault() );
    DocProcessor token = new TokenizerUIMA( SpaceTokenizer.getDefault() );
    sent.process( doc );
    token.process( doc );
    
    ClampNameEntity primary = null;
    // parse primary bios;
    for( NumberPair entity : parseEntities( primaryTokens ) ) {
      int start = tokens.get( entity.start() ).start();
      int end = tokens.get( entity.end() - 1 ).end();
      String semantic = entity.tag();
      ClampNameEntity cne = new ClampNameEntity( aJCas, start, end, semantic );
      if( semantic.startsWith( "primary-" ) ) {
        //semantic = semantic.substring( 8 );
        cne.setSemanticTag( semantic );
        primary = cne;
      }
      cne.addToIndexes();
    }
    
    // parse bios;
    for( NumberPair entity : parseEntities( tokens ) ) {
      int start = tokens.get( entity.start() ).start();
      int end = tokens.get( entity.end() - 1 ).end();
      String semantic = entity.tag();
      ClampNameEntity cne = new ClampNameEntity( aJCas, start, end, semantic );
      cne.addToIndexes();
      
      ClampRelation rel = new ClampRelation( primary, cne, "hasAttr" );
      rel.addToIndexes();
    }
    return doc;
  }

  @Override
  public DocumentFormat toDoc() throws IOException, UIMAException {
    Document doc = parseBIOFile( this.bioFile );
    return new DocumentFormat( doc );
  }
  
  
  public static void main( String[] argv ) {
	//Example of converting the relation BIO file to the clamp document
	  File indir = new File( "input/" );
	  File outdir = new File( "output/" ); 
      
      InputRelationBIOFormat ibo=new InputRelationBIOFormat();
      
      for( File file : indir.listFiles() ) {
          if( file.getName().startsWith( "." ) ) {
            continue;
          }
      
      File biofile = new File(indir + "/" + file);	    	
      Document doc=ibo.parseBIOFile(biofile);
      doc.save(outdir+ "/" + file.getName().replace(".bio", ".xmi"));
            
      
      }
	  
  }
  
}

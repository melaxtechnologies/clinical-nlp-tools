package com.melax.demo.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;

import edu.uth.clamp.io.DocumentIOException;
import edu.uth.clamp.nlp.core.NewlineSentDetector;
import edu.uth.clamp.nlp.uima.SentDetectorUIMA;
import edu.uth.clamp.nlp.core.SpaceTokenizer;
import edu.uth.clamp.nlp.uima.TokenizerUIMA;
import edu.uth.clamp.nlp.structure.ClampNameEntity;
import edu.uth.clamp.nlp.uima.DocProcessor;
import edu.uth.clamp.nlp.structure.Document;
import edu.uth.clamp.nlp.structure.XmiUtil;

/**
 * @author jingqiwang
 * 
 * Take entity BIO file as input
 * Generate clamp document
 *
 */
public class InputEntityBIOFormat extends InputFormat {
  
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
  
  public static final String LABELB = "B-";
  public static final String LABELI = "I-";
  
  /* The logger */
  static Logger LOGGER = Logger.getLogger( InputEntityBIOFormat.class.getName() );
  
  /* the bio file */
  File bioFile = null;
  
  /**
   * @param bioFile the bioFile;
   * @return this format;
   */
  InputEntityBIOFormat bio( File bioFile ) {
    this.bioFile = bioFile;
    return this;
  }
  
  /**
   * @param tokens list of tokens, offsets, and bios;
   * @return list of entities. ( token indexes and semantics of the entities )
   */
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
  private Document parseBIOFile( File bioFile ) throws UIMAException, IOException {
    BufferedReader infile = new BufferedReader( new FileReader( bioFile ) );
    String line = "";
    StringBuffer sb = new StringBuffer();
    
    // load tokens and bios;
    List<NumberPair> tokens = new ArrayList<NumberPair>();
    while( ( line = infile.readLine() ) != null ) {
      if( line.trim().isEmpty() ) {
        sb.append( "\n" );
        continue;
      }
      String token = line.trim().split( "\\s" )[0];
      String tag = line.trim().split( "\\s" )[1];
      tokens.add( new NumberPair( sb.length(), sb.length() + token.length(), tag ) );
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
    
    // parse bios;
    for( NumberPair entity : parseEntities( tokens ) ) {
      int start = tokens.get( entity.start() ).start();
      int end = tokens.get( entity.end() - 1 ).end();
      String semantic = entity.tag();
      ClampNameEntity cne = new ClampNameEntity( aJCas, start, end, semantic );
      cne.addToIndexes();
    }
    return doc;
  }
  
  /* (non-Javadoc)
   * @see edu.uth.clamp.format.InputFormat#toDoc()
   */
  @Override
  public DocumentFormat toDoc() throws UIMAException, IOException {
    Document doc = parseBIOFile( this.bioFile );
    return new DocumentFormat( doc );
  }
  
  public static void main( String[] argv ) throws UIMAException, IOException, DocumentIOException {

	  	//Example of converting the BIO file to the clamp document
	  
	    File indir = new File( "bio/" );
	    File outdir = new File( "clampoutput/" );
	    InputEntityBIOFormat ibo=new InputEntityBIOFormat();
	    
	    for( File file : indir.listFiles() ) {
	    	if (file.getName().startsWith(".")) {
	    		continue;
	    	}  	
	    	
    	File biofile = new File(indir + "/" + file);	    	
	    Document doc=ibo.parseBIOFile(biofile);
	    doc.save(outdir+ "/" + biofile.getName().replace(".bio", ".xmi"));
		    
	    }
	    
	    
	    
	  }



}

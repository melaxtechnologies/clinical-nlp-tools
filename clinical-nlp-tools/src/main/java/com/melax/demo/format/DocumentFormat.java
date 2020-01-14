package com.melax.demo.format;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import opennlp.tools.util.Span;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.InvalidXMLException;

import com.melax.poc.relbio.DocumentFormat;

import edu.uth.clamp.nlp.attr.RelationSentence;
import edu.uth.clamp.nlp.core.ClampSentDetector;
import edu.uth.clamp.nlp.core.ClampTokenizer;
import edu.uth.clamp.nlp.core.DictBasedSectionHeaderIdf;
import edu.uth.clamp.nlp.core.OpenNLPPosTagger;
import edu.uth.clamp.nlp.uima.DocProcessor;
import edu.uth.clamp.nlp.uima.PosTaggerUIMA;
import edu.uth.clamp.nlp.core.SectionHeaderIdf;
import edu.uth.clamp.nlp.uima.SectionHeaderIdfUIMA;
import edu.uth.clamp.nlp.uima.SentDetectorUIMA;
import edu.uth.clamp.nlp.uima.TokenizerUIMA;
import edu.uth.clamp.nlp.structure.ClampNameEntity;
import edu.uth.clamp.nlp.structure.ClampRelation;
import edu.uth.clamp.nlp.structure.ClampSentence;
import edu.uth.clamp.nlp.structure.ClampToken;
import edu.uth.clamp.nlp.structure.Document;
import edu.uth.clamp.nlp.structure.XmiUtil;

public class DocumentFormat extends InputFormat {
  public static final String LABELB = "B-";
  public static final String LABELI = "I-";
  public static final String LABELO = "O";
  
  
  Document doc;  
  
  public DocumentFormat( Document doc ) throws IOException {
    super();
    this.doc = doc;
  }
  
  public DocumentFormat doc ( Document doc ) {
    this.doc = doc;
    return this;
  }
  
  DocumentFormat xmi( File xmiFile ) throws UIMAException, IOException {
    this.doc = new Document( xmiFile );
    return this;
  }

  DocumentFormat txt( String txtFile ) throws UIMAException, IOException {
    this.doc = new Document( txtFile );
    return this;
  }

  DocumentFormat detectSents( SentDetectorUIMA sentDetector ) throws AnalysisEngineProcessException {
    sentDetector.process( doc );
    return this;
  }
  
  DocumentFormat tokenize(TokenizerUIMA tokenizer) throws AnalysisEngineProcessException {
    tokenizer.process( doc );
    return this;
  }
  
  public DocumentFormat toTokenizedDoc() {
    

    return this;
  }
  
  public DocumentFormat toNewOffset( String text ) {

    return this;
  }
  
  public DocumentFormat toNewOffset( File txtFile ) throws IOException {
    String content = FileUtils.readFileToString( txtFile );
    return toNewOffset( content );
  }
  

  @Override
  public DocumentFormat toDoc() {
    return this;
  }

  @Override
  public Document getClampDocument() {
    return this.doc;
  }

  public DocumentFormat saveTxt( File txtFile ) throws IOException {
	FileWriter outfile = new FileWriter( txtFile );
	outfile.write( this.doc.getFileContent() );
	outfile.close();
    return this;
  }
  
  public DocumentFormat saveAnn( File annFile ) throws IOException {
	StringBuffer sb = new StringBuffer();
	
	Map<String, String> entityMap = new HashMap<String, String>();
	int index = 1;
	for( ClampNameEntity cne : doc.getNameEntity() ) {
		String key = cne.getKey();
		if( !entityMap.containsKey( key ) ) {
			entityMap.put( key, "T" + index );
			sb.append( "T" );
			sb.append( index );
			sb.append( "\t" );
			sb.append( cne.getSemanticTag() );
			sb.append( " " );
			sb.append( cne.getBegin() );
			sb.append( " " );
			sb.append( cne.getEnd() );
			sb.append( "\t" );
			sb.append( cne.textStr().replace( "\n", " " ) );
			sb.append( "\n" );			
			index += 1;
		}
	}
	for( ClampRelation rel : doc.getRelations() ) {
		ClampNameEntity cne = rel.getEntFrom();
		String key = cne.getKey();
		if( !entityMap.containsKey( key ) ) {
			entityMap.put( key, "T" + index );
			sb.append( "T" );
			sb.append( index );
			sb.append( "\t" );
			sb.append( cne.getSemanticTag() );
			sb.append( " " );
			sb.append( cne.getBegin() );
			sb.append( " " );
			sb.append( cne.getEnd() );
			sb.append( "\t" );
			sb.append( cne.textStr().replace( "\n", " " ) );
			sb.append( "\n" );			
			index += 1;
		}
		cne = rel.getEntTo();
		key = cne.getKey();
		if( !entityMap.containsKey( key ) ) {
			entityMap.put( key, "T" + index );
			sb.append( "T" );
			sb.append( index );
			sb.append( "\t" );
			sb.append( cne.getSemanticTag() );
			sb.append( " " );
			sb.append( cne.getBegin() );
			sb.append( " " );
			sb.append( cne.getEnd() );
			sb.append( "\t" );
			sb.append( cne.textStr().replace( "\n", " " ) );
			sb.append( "\n" );			
			index += 1;
		}
	}
	
	
	index = 1;
	for( ClampRelation rel : doc.getRelations() ) {
		String from = rel.getEntFrom().getKey();
		String to = rel.getEntTo().getKey();
		if( !entityMap.containsKey( from ) ) {
			System.out.println( "not found:" + from );
		}
		if( !entityMap.containsKey( to ) ) {
			System.out.println( "not found:" + to );
		}

		sb.append( "R" );
		sb.append( index );
		sb.append( "\t" );
		sb.append( "attrOf " );
		sb.append( "Arg1:" );
		sb.append( entityMap.get(from) );
		sb.append( " " );
		sb.append( "Arg2:" );
		sb.append( entityMap.get(to) );
		sb.append( "\n" );
		index += 1;
	}
	
	FileWriter outfile = new FileWriter( annFile );
	outfile.write( sb.toString() );
	outfile.close();
	  
    return this;
  }
  
  public DocumentFormat saveSent( File sentFile ) {
    return this;
  }

  public DocumentFormat savePipeSemEval14( File pipeFile ) {
    return this;
  }
  
  public DocumentFormat savePipeSemEval15( File pipeFile ) {
    return this;
  }
  
  public DocumentFormat saveNCBIDisease (File ncbiFile) {
    return this;
  }

  public DocumentFormat saveEntityBIO( File bioFile ) {
    return this;
  }
  
  public DocumentFormat saveRelationBIO( File bioFile ) {
    return this;
  }
  
  public DocumentFormat saveXmi( File xmiFile ) {
    return this;
  }
  
  public String getRelationBIOString( String primarySemantic ) throws InvalidXMLException, IOException {
    StringBuffer sb = new StringBuffer();
    
    for( ClampSentence sent : doc.getCachedSentences() ) {
      if( XmiUtil.selectRelation( doc.getJCas(), sent.getBegin(), sent.getEnd() ).isEmpty() ) {
        // !! try to reduce the negative instances;
        continue;
      }
      for( ClampNameEntity cne : sent.getEntities() ) {
        if( !cne.getSemanticTag().equals( primarySemantic ) ) {
          continue;
        }
        
        List<String> tags = getTags( sent, cne );
        
        RelationSentence relSent = new RelationSentence( sent, cne );
        for( int i = 0; i < relSent.length(); i++ ) {
          sb.append( relSent.getToken(i) + "\t" + relSent.getFeatureBIO(i) + "\t" + tags.get(i) + "\n" );
        }
      }
    }  
    return sb.toString();
  }
  
  protected List<String> getTags(ClampSentence sent, ClampNameEntity primary) {
    // 1. get bios;
    Map<Span, String> tokenBIOMap = new HashMap<Span, String>();

    for (ClampRelation rel : XmiUtil.selectRelation(sent.getJCas(),
        sent.getBegin(), sent.getEnd())) {
      if (!rel.getEntFrom().getUimaEnt().equals(primary.getUimaEnt())) {
        continue;
      }

      ClampNameEntity cne = rel.getEntTo();
      String sem = cne.getSemanticTag();

      int i = 0;
      for (ClampToken token : cne.getTokens()) {
        if (i == 0) {
          tokenBIOMap.put(new Span(token.getBegin(), token.getEnd()), LABELB
              + sem);
        } else {
          tokenBIOMap.put(new Span(token.getBegin(), token.getEnd()), LABELI
              + sem);
        }
        i += 1;
      }
    }

    List<String> ret = new ArrayList<String>();
    for (ClampToken token : sent.getTokens()) {
      Span key = new Span(token.getBegin(), token.getEnd());
      if (tokenBIOMap.containsKey(key)) {
        ret.add(tokenBIOMap.get(key));
      } else {
        ret.add(LABELO);
      }
    }

    return ret;
  }
  
  public String getEntityBIOString() {
	  StringBuilder sb = new StringBuilder();
	  
	  //JCas aJCas = this.doc.getJCas();
	  
	  final String LABELB = "B-";
	  final String LABELI = "I-";
	  final String LABELO = "O";

      Map<Span, String> tokenBIOMap = new HashMap<Span, String>();
      for( ClampNameEntity cne : doc.getNameEntity() ) {
          int i = 0;
          for( ClampToken token : cne.getTokens() ) {
              if( i == 0 ) {
                  tokenBIOMap.put( new Span( token.getBegin(), token.getEnd() ), LABELB + cne.getSemanticTag() );
              } else {
                  tokenBIOMap.put( new Span( token.getBegin(), token.getEnd() ), LABELI + cne.getSemanticTag() );
              }
              i += 1;
          }
      }

      for( ClampSentence sent : doc.getCachedSentences() ) {
          for( ClampToken token : sent.getTokens() ) {
              Span key = new Span( token.getBegin(), token.getEnd() );
              String tokenStr = token.textStr();
              String bio = LABELO;
              if( tokenBIOMap.containsKey( key ) ) {
                  bio = tokenBIOMap.get( key );
              }
              sb.append( tokenStr);
              sb.append( "\t" );
              sb.append( bio );
              sb.append( "\n" );
          }
          sb.append( "\n" );
	    }
	  
	  return sb.toString();
  }
  
  /**
   * remove relations that cross different sentences;
   * 
   * @return this format with updated clamp document; 
   */
  public DocumentFormat removeCrossSentRelations() {
    return this;
  }
  
  /**
   * @param keepOnlyInThisSet keep only entities whose semantics in this set;
   * @return this format with updated clamp document;
   */
  public DocumentFormat filterEntities( Set<String> keepOnlyInThisSet ) {
    for( ClampRelation rel : doc.getRelations() ) {
      if( keepOnlyInThisSet.contains( rel.getEntFrom().getSemanticTag() ) ) {
        rel.clear();
      }
      if( keepOnlyInThisSet.contains( rel.getEntTo().getSemanticTag() ) ) {
        rel.clear();
      }
    }
    for( ClampNameEntity cne : doc.getNameEntity() ) {
      if( keepOnlyInThisSet.contains( cne.getSemanticTag() ) ) {
        cne.clear();
      }
    }
    return this;
  }
  
  /**
   * @param keepOnlyInThisSet keep only relations whose semantics in this set;
   * @return this format with updated clamp document;
   */
  public DocumentFormat filterRelations( Set<String> keepOnlyInThisSet ) {
    for( ClampRelation rel : doc.getRelations() ) {
      String semantic = rel.getSemanticTag();
      if( keepOnlyInThisSet.contains( semantic ) ) {
        rel.clear();
      }
    }
    return this;
  }
  
  /**
   * @param semanticMapping mapping from old entity types to new entity types;
   * @return this format with updated clamp document;
   */
  public DocumentFormat convertEntityTypes( Map<String, String> semanticMapping ) {
    for( ClampNameEntity cne : doc.getNameEntity() ) {
      String semantic = cne.getSemanticTag();
      if( semanticMapping.containsKey( semantic ) ) {
        cne.setSemanticTag( semanticMapping.get( semantic ) );
      }
    }
    return this;
  }
  
  /**
   * @param semanticMapping mapping from old relation types to new relation types;
   * @return this format with updated clamp document;
   */
  public DocumentFormat convertRelationTypes( Map<String, String> semanticMapping ) {
    for( ClampRelation rel : doc.getRelations() ) {
      String semantic = rel.getSemanticTag();
      if( semanticMapping.containsKey( semantic ) ) {
        rel.setSemanticTag( semanticMapping.get( semantic ) );
      }
    }
    return this;
  }

  /**
   * @return this format with sentences detected;
   * @throws IOException the IOException
   * @throws AnalysisEngineProcessException the UIMA exception
   */
  public DocumentFormat detectSents() throws AnalysisEngineProcessException, IOException {
    for( ClampSentence sent : this.doc.getSentences() ) {
      sent.clear();
    }
    DocProcessor proc = new SentDetectorUIMA( ClampSentDetector.getDefault() );
    proc.process( this.doc );
    return this;
  }
  
  /**
   * @return this format with tokenized document;
   * @throws IOException the IOException
   * @throws AnalysisEngineProcessException the UIMA exception
   */
  public DocumentFormat tokenize() throws IOException, AnalysisEngineProcessException {
    for( ClampToken token : this.doc.getTokens() ) {
      token.clear();
    }
    DocProcessor proc = new TokenizerUIMA( ClampTokenizer.getDefault() );
    proc.process( this.doc );
    return this;
  }

  public DocumentFormat detectSections() throws IOException, AnalysisEngineProcessException {
    DocProcessor proc = new SectionHeaderIdfUIMA( DictBasedSectionHeaderIdf.getDefault() );
    proc.process( this.doc );
    return this;
  }
  
  public DocumentFormat detectSections( SectionHeaderIdfUIMA proc ) throws AnalysisEngineProcessException {
    proc.process( this.doc );    
    return this;
  }

  public DocumentFormat posTagging() throws IOException, AnalysisEngineProcessException {
    DocProcessor proc = new PosTaggerUIMA( OpenNLPPosTagger.getDefault() );
    proc.process( this.doc );
    return this;
  }
  
  public DocumentFormat preprocessing() throws AnalysisEngineProcessException, IOException {
    this.detectSents();
    this.tokenize();
    this.detectSections();
    this.posTagging();
    return this;
  }
  
  public static void main(String[] argv) throws UIMAException, IOException {
  
	//Example of using getRelationBIOString to convert in xmi file to relation BIO file 
	  
    File infile = new File("train.xmi");
    File outfile = new File("train.txt");
    // Specify the Primary semantictype in relations e.g. "problem", "drug", "test"
    String primarySemantic = "problem";

    BufferedWriter writer = new BufferedWriter(new FileWriter(outfile));
	String result = new String();
											
	DocumentFormat docformat = new DocumentFormat(new Document( infile ));	
	result = docformat.getRelationBIOString(primarySemantic);		
	System.out.println(result);			
	writer.write(result + "\n");	
	writer.close();
	
  }
}

package com.melax.demo.format;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.uima.UIMAException;
import org.apache.uima.util.FileUtils;

import com.melax.example.format.InputBratFormat;

import edu.uth.clamp.io.DocumentIOException;
import edu.uth.clamp.nlp.structure.ClampNameEntity;
import edu.uth.clamp.nlp.structure.ClampRelation;
import edu.uth.clamp.nlp.structure.Document;

/**
 * @author jingqiwang 
 * 
 * Take a brat format txt files and an ann file
 * Generate the CLAMP Document;
 */
public class InputBratFormat extends InputFormat {
  /* The logger */
  static Logger LOGGER = Logger.getLogger( InputBratFormat.class.getName() );
  
  /* The txt file */
  private File txtFile = null;
  
  /* The ann file */
  private File annFile = null;

  /**
   * @param txtFile the txt file;
   * @return this format;
   */
  public InputBratFormat txtFile(File txtFile) {
    this.txtFile = txtFile;
    return this;
  }

  /**
   * @param annFile the ann file;
   * @return this format;
   */
  public InputBratFormat annFile(File annFile) {
    this.annFile = annFile;
    return this;
  }

  private Document parseBratFile(File txtFile, File annFile) throws IOException, UIMAException {
    LOGGER.fine( "textFile=[" + txtFile + "], annFile=[" + annFile + "]" );
    // 1. load file content;
    Document doc = new Document(txtFile);

    // 2. load ann;
    String fileContent = FileUtils.file2String(annFile, "UTF-8");
    Map<String, ClampNameEntity> entityMap = new HashMap<String, ClampNameEntity>();
    Map<String, List<String>> relationMap = new HashMap<String, List<String>>();

    // 3. parse entities;
    for (String line : fileContent.split("\\n")) {
      if( !line.startsWith( "T" ) ) {
        continue;
      }
      String[] splitStr = line.trim().split("\t");
      if( splitStr.length != 3 ) {
        LOGGER.warning( "column count are wrong, line ignored. line=[" + line + "]" );
        continue;
      }
      String entityId = splitStr[0];
      String semantic = splitStr[1];
      if( semantic.indexOf( ";" ) >= 0 ) {
        LOGGER.warning( "disjoint entities, line ignored. line=[" + line + "]" );
        continue;
      }
      //String entity = splitStr[2];
      String startStr = semantic.split( "\\s" )[1];
      String endStr = semantic.split( "\\s" )[2];
      semantic = semantic.split( "\\s" )[0];
      int start = Integer.parseInt(startStr);
      int end = Integer.parseInt(endStr);
      ClampNameEntity cne = new ClampNameEntity(doc.getJCas(), start,
          end, semantic);
      cne.addToIndexes();
      entityMap.put(entityId, cne);      
    }

    // 4. parse relation;
    for (String line : fileContent.split("\\n")) {
      if( !line.startsWith( "R" ) ) {
        continue;
      }
      if( line.indexOf( "\t" ) < 0 ) {
        continue;
      }
      // parseRelations
      String[] splitStr = line.trim().split("\\s");
      String semantic = line.trim().split( "\\s" )[1];
      String fromId = splitStr[2].split(":")[1];
      String toId = semantic + ":" + splitStr[3].split(":")[1];
      if (!relationMap.containsKey(fromId)) {
        relationMap.put(fromId, new ArrayList<String>());
      }
      relationMap.get(fromId).add(toId);
    }

    // 5. create relations;
    for (String fromId : relationMap.keySet()) {
      ClampNameEntity fromNE = entityMap.get( fromId );
      if( fromNE == null ) {
        LOGGER.warning( "cannot find relation from entity, fromId=[" + fromId + "]");
        continue;
      }
      for( String toId : relationMap.get( fromId ) ) {
        int pos = toId.lastIndexOf( ":" );
        String semantic = toId.substring(0, pos);
        toId = toId.substring( pos + 1 );
        ClampNameEntity toNE = entityMap.get( toId );
        if( toNE == null ) {
          LOGGER.warning("cannot find relation from entity, toId=[" + toId + "]");
          continue;
        }        
        ClampRelation relation = new ClampRelation( fromNE, toNE, semantic );
        relation.addToIndexes();
      }
    }
    return doc;
  }

  /* (non-Javadoc)
   * @see edu.uth.clamp.format.InputFormat#toDoc()
   */
  @Override
  public DocumentFormat toDoc() throws Exception {
    if (this.txtFile == null || this.annFile == null) {
      LOGGER.severe( "cannot create document. txtFile=[" + txtFile + "], annFile=[" + annFile + "]" );
      return null;
    }
    Document doc = parseBratFile( txtFile, annFile );
    return new DocumentFormat( doc );
  }
  
  
  public static void main( String[] argv ) throws UIMAException, IOException, DocumentIOException {
	   
	  // Example of converting brat format file to the clamp document
	  File txtfile = new File( "text.txt" );
	  File annfile = new File( "text.ann" );
	  
	  InputBratFormat ibrat=new InputBratFormat();
	  Document doc = ibrat.parseBratFile(txtfile, annfile);
	  doc.save("text.xmi");
	  
  }
  

}

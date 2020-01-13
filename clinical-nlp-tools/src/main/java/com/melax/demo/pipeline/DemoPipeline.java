package com.melax.demo.pipeline;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.uima.UIMAException;

import edu.uth.clamp.config.ConfigUtil;
import edu.uth.clamp.config.ConfigurationException;
import edu.uth.clamp.io.DocumentIOException;
import edu.uth.clamp.nlp.structure.Document;
import edu.uth.clamp.nlp.uima.DocProcessor;

public class DemoPipeline {
  
  public static void main( String[] argv ) throws ConfigurationException, IOException, DocumentIOException, UIMAException {
    
    List<DocProcessor> pipeline = ConfigUtil.importPipelineFromJar( new File ("pipeline/test.pipeline.jar"));
    
    Document doc = new Document( "sample_1041.txt" );
    
    for( DocProcessor proc : pipeline ) {
      proc.process( doc );
    }
    
    
    
    doc.save( "1041.xmi" );
    return;
  }

}

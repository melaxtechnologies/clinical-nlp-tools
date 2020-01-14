package com.melax.demo.format;

import edu.uth.clamp.nlp.structure.Document;

public abstract class InputFormat {
  
  public Document getClampDocument() throws Exception{
    return this.toDoc().getClampDocument();
  }
  
  abstract public DocumentFormat toDoc() throws Exception;
}

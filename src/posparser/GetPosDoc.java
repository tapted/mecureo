// Decompiled by DJ v3.4.4.74 Copyright 2003 Atanas Neshkov  Date: 11/02/2003 12:04:19 PM
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   SimplePipe.java

package posparser;

import opennlp.common.*;
import opennlp.common.xml.NLPDocument;

/**
 * A Singleton that creates a default Pipeline using
 * English preprocessed files and handles Exceptions.<p>
 * When using FODLOCReader exceptions should never occur.<br>
 * The Pipeline is created to use Sentence Detection, Part-of-Speech
 * Tagging and Name Finding.
 *
 */
public class GetPosDoc {

	Pipeline pipeline;

	public GetPosDoc() {
		System.err.print("Initialising NLP Parser... ");
		String args1[] = {
			"opennlp.grok.preprocess.sentdetect.EnglishSentenceDetectorME",
			"opennlp.grok.preprocess.tokenize.EnglishTokenizerME",
			"opennlp.grok.preprocess.postag.EnglishPOSTaggerME",
			"opennlp.grok.preprocess.namefind.EnglishNameFinderME"
		};

		try {
			pipeline = new Pipeline(args1);
			System.err.println("Initialised.");
		} catch(PipelineException pipelineexception) {
			System.out.println("Pipeline error: " + pipelineexception.toString());
		}
	}

	public NLPDocument getDoc(Object input) {
		try {
			return pipeline.run(input);
		} catch(PipelineException pipelineexception) {
			System.out.println("Pipeline error: " + pipelineexception.toString());
		}
		return null;
	}

}

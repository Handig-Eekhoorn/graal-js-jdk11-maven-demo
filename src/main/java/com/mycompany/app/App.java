/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.mycompany.app;

import java.io.IOException;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;

/**
 * Simple benchmark for Graal.js via GraalVM Polyglot Context and ScriptEngine.
 * 
 * Modified by hannes@handig-eekhoorn.at to demonstrate differences
 * in map handling between Nashorn and GraalVM.
 */
public class App {

    public static final int WARMUP = 0;
    public static final int ITERATIONS = 1;

    public static final String[] SOURCES = new String[]{
    		"map.key1; ",
    		"map['key1']; ",
    		"map.get('key1'); ",
    		
    		"map.key2; ",
    		"map['key2']; ",
    		"map.get('key2'); "
    };
    
    public static void main(String[] args) throws Exception {
//        benchGraalPolyglotContext();
        tryGraalScriptEngine();
        tryNashornScriptEngine();
    }

    static long benchGraalPolyglotContext() throws IOException {
        System.out.println("=== Graal.js via org.graalvm.polyglot.Context === ");
        long sum = 0;
        try (Context context = Context.create()) {
        	for(final String srcS: SOURCES) {
	            Source src = Source.newBuilder("js", srcS, "src.js")
	            .build();
				context.eval(src);
	
	            Value primesMain = context.getBindings("js").getMember("testMapget");
	            System.out.println("warming up ...");
	            for (int i = 0; i < WARMUP; i++) {
	                Value r = primesMain.execute();
	                System.out.println("r="+r);
	            }
	            System.out.println("warmup finished, now measuring");
	            for (int i = 0; i < ITERATIONS; i++) {
	                long start = System.currentTimeMillis();
	                primesMain.execute();
	                long took = System.currentTimeMillis() - start;
	                sum += took;
	                System.out.println("iteration: " + took);
	            }
        	}
    	} // context.close() is automatic
        return sum;
    }

    static long tryNashornScriptEngine() throws IOException {
        System.out.println("");
        System.out.println("==[ Nashorn via javax.script.ScriptEngine ]==");
        ScriptEngine nashornEngine = new ScriptEngineManager().getEngineByName("nashorn");
        if (nashornEngine == null) {
            System.out.println("*** Nashorn not found ***");
            return 0;
        } else {
            return tryScriptEngineIntl(nashornEngine);
        }
    }

    static long tryGraalScriptEngine() throws IOException {
        System.out.println("");
        System.out.println("===[ Graal.js via javax.script.ScriptEngine ]===");
//        ScriptEngine graaljsEngine = new ScriptEngineManager().getEngineByName("graal.js");

        ScriptEngine graaljsEngine = GraalJSScriptEngine.create(null,
		        Context.newBuilder("js")
		        .allowHostAccess(HostAccess.ALL)
		        .allowHostClassLookup(s -> {
		        	System.out.println("allowHostClassLookup["+s+"]");
		        	return true;
		        })
		        .option("js.ecmascript-version", "2021")
		);

        
        if (graaljsEngine == null) {
            System.out.println("*** Graal.js not found ***");
            return 0;
        } else {
            return tryScriptEngineIntl(graaljsEngine);
        }
    }

    private static long tryScriptEngineIntl(ScriptEngine eng) throws IOException {
        long sum = 0L;
        try {
        	final Map<String,Object> map = new TestMap();
        	Bindings b = eng.createBindings();
        	b.put("map", map);

            for (String src: SOURCES) {
                long start = System.currentTimeMillis();
                
                System.out.println("==");
                System.out.println(src);
                Object eval = eng.eval(src, b);
                System.out.println("returned: "+eval);
                
                long took = System.currentTimeMillis() - start;
                sum += took;
//                System.out.println("iteration: " + (took));
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return sum;
    }

}

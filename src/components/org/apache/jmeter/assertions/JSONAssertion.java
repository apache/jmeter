/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Checks if the result is a well-formed JSON content 
 * 
 */
public class JSONAssertion extends AbstractTestElement implements Serializable, Assertion, ThreadListener {
    private static final Logger log = LoggingManager.getLoggerForClass();
    
    private static final long serialVersionUID = 240L;

    /**
     * Returns the result of the Assertion. Here it checks wether the Sample
     * was json welll-formed be considered successful. If so the returned
     * AssertionResult will reflect the success of the Sample.Otherwise 
     * an AssertionResult containing a FailureMessage will be returned.
     */
    @Override
    public AssertionResult getResult(SampleResult response) {
        // no error as default
        AssertionResult result = new AssertionResult(getName());
        String resultData = response.getResponseDataAsString();
       
        if (resultData.length()==0) {
            return result.setResultForNull();
        }else{
            if(!validate(resultData)){
                result.setFailure(true);
                result.setFailureMessage("ResultData is not Json");
            }
        }
        return result;
    }

    @Override
    public void threadStarted() {

    }

    @Override
    public void threadFinished() {

    }

    private CharacterIterator it ;
    private char c;
    private int col;

    /**
     * Verify a string is  JSON-formed
     * 
     * @param input The string to be verified.
     * @return true-legal ，false-illegal
     */
    public boolean validate(String input) {
        input = input.trim();
        boolean ret = valid(input);
        return ret;

    }



    private boolean valid(String input) {
        if ("".equals(input)) {
            return true;
        }

        boolean ret = true;
        it = new StringCharacterIterator(input);
        c = it.first();
        col = 1;
        if (!value()) {
            ret = error("value", 1);
        } else {
            skipWhiteSpace();
            if (c != CharacterIterator.DONE) {
                ret = error("end", col);
            }
        }
        return ret;

    }

    private boolean value() {
        return literal("true") || literal("false") || literal("null") || string() || number() || object() || array();
    }

    private boolean literal(String text) {
        CharacterIterator ci = new StringCharacterIterator(text);
        char t = ci.first();

        if (c != t) {
            return false;
        }

        int start = col;
        boolean ret = true;
        for (t = ci.next(); t != CharacterIterator.DONE; t = ci.next()) {
            if (t != nextCharacter()) {
                ret = false;
                break;
            }
        }
        nextCharacter();
        if (!ret) {
            error("literal " + text, start);
        }
        return ret;
    }

    private boolean array() {
        return aggregate('[', ']', false);
    }

    private boolean object() {
        return aggregate('{', '}', true);
    }

    private boolean aggregate(char entryCharacter, char exitCharacter, boolean prefix) {
        if (c != entryCharacter) {
            return false;
        }
        nextCharacter();
        skipWhiteSpace();
        if (c == exitCharacter) {
            nextCharacter();
            return true;
        }

        for (;;) {
            if (prefix) {
                int start = col;
                if (!string()) {
                    return error("string", start);
                }
                skipWhiteSpace();
                if (c != ':') {
                    return error("colon", col);
                }
                nextCharacter();
                skipWhiteSpace();
            }
            if (value()) {
                skipWhiteSpace();
                if (c == ',') {
                    nextCharacter();
                } else if (c == exitCharacter) {
                    break;
                } else {
                    return error("comma or " + exitCharacter, col);
                }
            } else {
                return error("value", col);
            }
            skipWhiteSpace();
        }
        nextCharacter();
        return true;
    }

    private boolean number() {
        if (!Character.isDigit(c) && c != '-') {
            return false;
        }
        int start = col;
        if (c == '-') {
            nextCharacter();
        }
        if (c == '0') {
            nextCharacter();
        } else if (Character.isDigit(c)) {
            while (Character.isDigit(c)){
                nextCharacter();
            }
        } else {
            return error("number", start);
        }
        if (c == '.') {
            nextCharacter();
            if (Character.isDigit(c)) {
                while (Character.isDigit(c)){
                    nextCharacter();
                }
            } else {
                return error("number", start);
            }
        }

        if (c == 'e' || c == 'E') {
            nextCharacter();
            if (c == '+' || c == '-') {
                nextCharacter();
            }
            if (Character.isDigit(c)) {
                while (Character.isDigit(c)){
                    nextCharacter();
                }
            } else {
                return error("number", start);
            }
        }
        return true;
    }



    private boolean string() {
        if (c != '"') {
            return false;
        }

        int start = col;
        boolean escaped = false;
        for (nextCharacter(); c != CharacterIterator.DONE; nextCharacter()) {
            if (!escaped && c == '\\') {
                escaped = true;
            } else if (escaped) {
                if (!escape()) {
                    return false;
                }
                escaped = false;
            } else if (c == '"') {
                nextCharacter();
                return true;
            }
        }
        return error("quoted string", start);
    }

    private boolean escape() {
        int start = col - 1;
        if (" \\\"/bfnrtu".indexOf(c) < 0) {
            return error("escape sequence  \\\",\\\\,\\/,\\b,\\f,\\n,\\r,\\t  or  \\uxxxx ", start);
        }
        if (c == 'u') {
            if (!ishex(nextCharacter()) || !ishex(nextCharacter()) || !ishex(nextCharacter())
                || !ishex(nextCharacter())) {
                return error("unicode escape sequence  \\uxxxx ", start);
            }
        }
        return true;
    }

    private boolean ishex(char d) {
        return "0123456789abcdefABCDEF".indexOf(c) >= 0;
    }

    private char nextCharacter() {
        c = it.next();
        ++col;
        return c;
    }

    private void skipWhiteSpace() {
        while (Character.isWhitespace(c)) {
            nextCharacter();
        }
    }

    private boolean error(String type, int col) {
        String logmsg=String.format("type: %s, col: %s%s", type, col, System.getProperty("line.separator"));
        log.debug(logmsg);
        return false;
    }

}



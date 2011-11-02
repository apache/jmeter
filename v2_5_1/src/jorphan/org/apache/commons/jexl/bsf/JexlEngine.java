/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.commons.jexl.bsf;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Vector;

import org.apache.bsf.BSFDeclaredBean;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.BSFEngineImpl;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.Script;
import org.apache.commons.jexl.ScriptFactory;
//import org.apache.jorphan.logging.LoggingManager;
//import org.apache.log.Logger;

// See JIRA: JEXL-39

/**
 * BSFEngine for Commons JEXL.
 */
public class JexlEngine extends BSFEngineImpl {

//    private static final Logger log = LoggingManager.getLoggerForClass();

    private JexlContext jc;

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked") // super-class does not use generics
    @Override
    public void initialize(BSFManager mgr, String lang,
            @SuppressWarnings("rawtypes") Vector declaredBeans) // super-class does not use generics
            throws BSFException {
        super.initialize(mgr, lang, declaredBeans);
        jc = JexlHelper.createContext();
        for (int i = 0; i < declaredBeans.size(); i++) {
            BSFDeclaredBean bean = (BSFDeclaredBean) declaredBeans.elementAt(i);
            jc.getVars().put(bean.name, bean.bean);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void terminate() {
        if (jc != null) {
            jc.getVars().clear();
            jc = null;
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void declareBean(BSFDeclaredBean bean) throws BSFException {
        jc.getVars().put(bean.name, bean.bean);
    }

    /** {@inheritDoc} */
    @Override
    public void undeclareBean(BSFDeclaredBean bean) throws BSFException {
        jc.getVars().remove(bean.name);
    }

    /** {@inheritDoc} */
    public Object eval(String fileName, int lineNo, int colNo, Object expr)
            throws BSFException {
        if (expr == null) {
            return null;
        }
        try {
            Script jExpr = null;
            if (expr instanceof File) {
                jExpr = ScriptFactory.createScript((File) expr);
            } else if (expr instanceof URL) {
                jExpr = ScriptFactory.createScript((URL) expr);
            } else {
                jExpr = ScriptFactory.createScript((String) expr);
            }
            return jExpr.execute(jc);
        } catch (Exception e) {
            // TODO Better messages
            throw new BSFException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void exec(String fileName, int lineNo, int colNo, Object script)
            throws BSFException {
        if (script == null) {
            return;
        }
        try {
            Script jExpr = null;
            if (script instanceof File) {
                jExpr = ScriptFactory.createScript((File) script);
            } else if (script instanceof URL) {
                jExpr = ScriptFactory.createScript((URL) script);
            } else {
                jExpr = ScriptFactory.createScript((String) script);
            }
            jExpr.execute(jc);
        } catch (Exception e) {
            throw new BSFException(e.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void iexec(String fileName, int lineNo, int colNo, Object script)
            throws BSFException {
        exec(fileName, lineNo, colNo, script);
    }

    /** {@inheritDoc} */
    public Object call(Object object, String name, Object[] args)
            throws BSFException {
        try {
            Class<?>[] types = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                types[i] = args[i].getClass();
            }
            Method m = object.getClass().getMethod(name, types);
            return m.invoke(object, args);
        } catch (Exception e) {
            throw new BSFException(e.getMessage());
        }
    }

}

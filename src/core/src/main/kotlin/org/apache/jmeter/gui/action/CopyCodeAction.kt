/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.gui.action

import com.google.auto.service.AutoService
import org.apache.jmeter.dsl.DslPrinterTraverser
import org.apache.jmeter.gui.GuiPackage
import org.apache.jorphan.gui.GuiUtils
import org.slf4j.LoggerFactory
import java.awt.event.ActionEvent

@AutoService(Command::class)
public class CopyCodeAction : AbstractAction() {
    private companion object {
        private val log = LoggerFactory.getLogger(CopyCodeAction::class.java)
    }
    private val actionNames = setOf(ActionNames.COPY_CODE)

    override fun getActionNames(): Set<String> = actionNames

    override fun doAction(e: ActionEvent) {
        val gui = GuiPackage.getInstance()
        val selectedNodes = gui?.treeListener?.selectedNodes ?: return
        val dslWriter = DslPrinterTraverser()
        for (node in selectedNodes) {
            val tree = gui.treeModel.getCurrentSubTree(node)
            try {
                tree.traverse(dslWriter)
            } catch (e: Throwable) {
                log.warn("Unable to copy DSL for node {}", node, e)
                return
            }
        }
        GuiUtils.copyTextToClipboard(dslWriter.toString())
    }
}

package org.apache.jmeter.protocol.http.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.action.AbstractActionWithNoRunningTest;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.util.JMeterUtils;

public class CorrelationRuleFile extends AbstractActionWithNoRunningTest {

    private static final Set<String> commands = new HashSet<>();

    static {
        commands.add(ActionNames.CORRELATION_IMPORT_RULE);
    }

    public CorrelationRuleFile() {
        super();
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    protected void doActionAfterCheck(ActionEvent e) throws IllegalUserActionException {
        JMeterUtils.reportInfoToUser("Import Rule works", "Works");
    }

}

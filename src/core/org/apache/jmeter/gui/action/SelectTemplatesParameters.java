package org.apache.jmeter.gui.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * Dialog used for template customs parameters input
 */
public class SelectTemplatesParameters extends JDialog implements ActionListener{
    private static final long serialVersionUID = 1;

    // Minimal dimensions for dialog box
    private static final int MINIMAL_BOX_WIDTH = 500;
    private static final int MINIMAL_BOX_HEIGHT = 300;

    private final JButton cancelButton = new JButton(JMeterUtils.getResString("cancel")); //$NON-NLS-1$
    private final JButton validateButton = new JButton(JMeterUtils.getResString("validate_threadgroup")); //$NON-NLS-1$
    
    private Map<String, String> parameters;
    private Map<String, JLabeledTextField> buttonsParameters = new LinkedHashMap<>();
    
    public static void launch(Map<String, String> parameters) {
        SelectTemplatesParameters frame = new SelectTemplatesParameters(parameters);
        frame.setVisible(true);
}

    public SelectTemplatesParameters(Map<String, String> parameters) {
        super((JFrame) null, JMeterUtils.getResString("template_title"), true); //$NON-NLS-1$
        this.parameters = parameters;
        init();
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        JButton testButton = new JButton("le bouton au pif");
        cancelButton.addActionListener(this);
        validateButton.addActionListener(this);

        this.setLayout(new BorderLayout());
        this.add(testButton, BorderLayout.SOUTH);
        this.setMinimumSize(new Dimension(MINIMAL_BOX_WIDTH, MINIMAL_BOX_HEIGHT));
        ComponentUtil.centerComponentInWindow(this, 40); // center position and 40% of screen size

        JPanel gridbagpanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);
        int parameterCount = 0;

        for(Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            JLabeledTextField paramLabel = new JLabeledTextField(key + " : ");
            paramLabel.setText(value);
            buttonsParameters.put(key, paramLabel);

            gbc.gridy = parameterCount;
            gridbagpanel.add(paramLabel, gbc.clone());

            parameterCount++;
        }
        this.add(gridbagpanel, BorderLayout.CENTER);
        // Bottom buttons bar
        JPanel actionBtnBar = new JPanel(new FlowLayout());
        actionBtnBar.add(validateButton);
        actionBtnBar.add(cancelButton);
        this.add(actionBtnBar, BorderLayout.SOUTH);
    }

    private void initConstraints(GridBagConstraints gbc) {
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0,0,5,0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Object source = e.getSource();
        if (source == cancelButton) {
            this.dispose();
            return;
        } else if (source == validateButton) {
            for(Entry<String, String> entry : parameters.entrySet()) {
                String valueToSet = buttonsParameters.get(entry.getKey()).getText();
                entry.setValue(valueToSet);
            }
            this.dispose();
            return;
        }
    }
}

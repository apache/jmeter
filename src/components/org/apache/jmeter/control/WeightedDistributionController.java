package org.apache.jmeter.control;

import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;


public class WeightedDistributionController extends InterleaveControl {
	private static final long serialVersionUID = 8554248250211263894L;

	public static final String WEIGHT = "WeightedDistributionController.weight";

	public static final int MIN_WEIGHT = 0;
	public static final int MAX_WEIGHT = 999999;
	public static final int DFLT_WEIGHT = MIN_WEIGHT;

	private static final int UNSET_CUMULATIVE_PROBABILITY = -1;

	private transient int cumulativeProbability;
	private transient JMeterTreeNode node;

	public WeightedDistributionController() {
		node = null;
		cumulativeProbability = UNSET_CUMULATIVE_PROBABILITY;
	}

	public int getCumulativeProbability() {
		if (cumulativeProbability == UNSET_CUMULATIVE_PROBABILITY) {
			if (getNode() != null) {
				cumulativeProbability = 0;
				Enumeration<JMeterTreeNode> children = getNode().children();
				while (children.hasMoreElements()) {
					JMeterTreeNode currChild = children.nextElement();
					if (currChild.isEnabled()) {
						TestElement currTestElement = currChild.getTestElement();
						if (currTestElement instanceof Controller || currTestElement instanceof Sampler) {
							cumulativeProbability += currTestElement.getPropertyAsInt(WEIGHT, DFLT_WEIGHT);
						}
					}
				}
			}
		}

		return cumulativeProbability;
	}

	public void resetCumulativeProbability() {
		cumulativeProbability = UNSET_CUMULATIVE_PROBABILITY;
	}

	public float calculateProbability(int weight) {
		if (getCumulativeProbability() > 0) {
			return ((float) weight / getCumulativeProbability());
		}

		return 0.0f;
	}

	public JMeterTreeNode getNode() {
		if (node == null || node.getTestElement() != this || node.getParent() == null) {
			node = GuiPackage.getInstance().getNodeOf(this);
		}
		return node;
	}

	@Override
	protected void resetCurrent() {
		current = determineCurrentTestElement();
	}

	@Override
	protected void incrementCurrent() {
		super.incrementCurrent();
		current = determineCurrentTestElement();
	}

	private int determineCurrentTestElement() {
		if (getCumulativeProbability() > 0) {
			int currentRandomizer = ThreadLocalRandom.current().nextInt(getCumulativeProbability());
			List<TestElement> subControllers = getSubControllers();
			for (int currSubCtrlIdx = 0; currSubCtrlIdx < subControllers.size(); currSubCtrlIdx++) {
				TestElement currSubController = subControllers.get(currSubCtrlIdx);
				if (currSubController.isEnabled()) {
					int currWeight = currSubController.getPropertyAsInt(WEIGHT, DFLT_WEIGHT);
					if (currWeight >= currentRandomizer) {
						return currSubCtrlIdx;
					} else {
						currentRandomizer -= currWeight;
					}
				}
			}
		}
		return 0;
	}

}

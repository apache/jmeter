![Apache JMeter logo](https://jmeter.apache.org/images/logo.svg)

## Launching Apache JMeter from Eclipse

You can use the Gradle task `runGui` to run the JMeter GUI, or you can follow the instructions
below to add a Java Application launch, which will for example, allow you to use the debugger to
run JMeter.

These instructions assume you have configured Eclipse to use the classpath
as suggested in [CONTRIBUTING.md](CONTRIBUTING.md).

Create a new Java Application launch configuration.

On the Main tab, enter the following as the main class:

> `org.apache.jmeter.NewDriver`

On the Arguments tab, in the Working Directory area, pick the radio
button next to `Other` and enter the following in the text box:

> `${workspace_loc}/jmeter/bin`

 where `jmeter` is the name of the JMeter project.

 **It would be nicer to use `${project_loc}/bin` but unfortunately the Eclipse Debug view does not seem to preserve any of the project variables**

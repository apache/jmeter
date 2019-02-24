![Apache JMeter logo](https://jmeter.apache.org/images/logo.svg)

## Launching Apache JMeter from Eclipse

You can use the Gradle task `runGui` to run the JMeter GUI, or you can follow the instructions
below to add a Java Application launch, which will for example, allow you to use the debugger to
run JMeter.

These instructions assume you have configured Eclipse to use the classpath
as suggested in [CONTRIBUTING.md](CONTRIBUTING.md).

## Launching from Eclipse

Note: you can use Gradle task `runGui` to start JMeter, however Eclipse does not support
debugging of the Gradle tasks.

Alternative option is to create a `Launch Configuration`:

1. Create a new `Java Application` launch configuration
1. Use `dist` for `Project`
1. Use `org.apache.jmeter.NewDriver` for the main class
1. On the arguments tab use the following for `Working Directory`: `${workspace_loc:jmeter/bin}`

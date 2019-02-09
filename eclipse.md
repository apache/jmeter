![Apache JMeter logo](https://jmeter.apache.org/images/logo.svg)
# Eclipse Configuration for JMeter project

## Eclipse settings

The following files should be excluded from build output:

> `*.metaprops`

See `Project Preferences/Java Compiler/Building/Output Folder/Filtered Resources`


## Eclipse.classpath

**This has been tested with Eclipse 3.2 up to 4.6.1. It may not work with other versions.**

The file `eclipse.classpath` is intended as a starter `.classpath` file
for building JMeter using Eclipse version 3 to 4.6.

Make sure to execute the `ant download_jars` task to download and install the jars referred
to in the classpath before creating the Eclipse project.

If you do after creating project, then don't forget to refresh Eclipse project.

Note that Eclipse is not easy to use for creating jar files.
However, it is easy to use Eclipse to run Ant.

The following targets may prove useful:

  - `clean` - Clean up to force a build from source
  - `package-only` - creates the jars
  - `package` - compiles everything and then packages it
  - `run_gui` - compiles, packages, and then start the JMeter GUI from the jars

## Invoking Ant targets inside Eclipse

You can use the `Run As --> Ant Build` and select target, or you can use
the `Windows --> Show View --> Ant View`. Then select the `build.xml` file and
drag and drop to the `Ant View`.
Now you can invoke targets by clicking on them.
Note that if you invoke for example the `compile` target, and get error
messages about


> Unable to find a javac compiler;
  com.sun.tools.javac.Main is not on the classpath.
  Perhaps JAVA_HOME does not point to the JDK


it just means that your Eclipse project is set up with JRE libraries instead of JDK libraries.
The suggested fix is to add a JDK in `Window --> Preferences --> Java --> Installed JREs`.

Then do a `Project --> Properties` and select `Java Build Path` in the left pane, and then
select the `Libraries` tab in the right pane. Scroll to the bottom, select the `JRE System Library`,
and click `Remove`. Then click `Add library...` , select `JRE System Library`, and then select
the JDK.

Now it should work when you invoke the `compile` target.


## Finishing the build using Ant

Find the `build.xml` file in the project,
right click on it, and click `Run As --> Ant Build`.

Make sure you select the `package` target.

This will compile any remaining classes,
and then create all the jars.

Now refresh the project (you should add this to the Ant build properties)

## Launching from Eclipse

You can use the Ant target `run_gui` to run the JMeter GUI, or you can follow the instructions
below to add a Java Application launch, which will for example, allow you to use the debugger to
run JMeter.

These instructions assume you have configured Eclipse to use the classpath
as suggested in eclipse.classpath, and have run `ant package` to compile
the RMI classes and build the jars.

Create a new Java Application launch configuration.

On the Main tab, enter the following as the main class:

> `org.apache.jmeter.NewDriver`

On the Arguments tab, in the Working Directory area, pick the radio
button next to `Other` and enter the following in the text box:

> `${workspace_loc}/jmeter/bin`

 where `jmeter` is the name of the JMeter project.

 **It would be nicer to use `${project_loc}/bin` but unfortunately the Eclipse Debug view does not seem to preserve any of the project variables**

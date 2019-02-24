# How to contribute

Want to show Apache JMeter some love? Help out by contributing!

## :beetle: Found a bug?
Log it in our bugzilla:

* *https://jmeter.apache.org/issues.html* 
* or send a note to the *user mailing list*. 
Be sure to include all relevant information, like the versions of JMeter you’re using as long as Java version. 
A Test plan  that caused the issue as well as any error messages are also very helpful.

## :question: Need help?
Simply contact:

* our user mailing list *https://jmeter.apache.org/mail2.html#JMeterUser* 
* or ask question on [stackoverflow](https://stackoverflow.com/questions/tagged/jmeter).

## :bar_chart: What needs to be developed?

See:

* https://bz.apache.org/bugzilla/buglist.cgi?bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&bug_status=NEEDINFO&bug_severity=Blocker&bug_severity=Critical&bug_severity=Major&bug_severity=Normal&bug_severity=Regression&bug_severity=Minor&bug_severity=Trivial&product=JMeter&order=Bug%20Number&list_id=164231
* https://bz.apache.org/bugzilla/buglist.cgi?bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&bug_status=NEEDINFO&bug_severity=Enhancement&product=JMeter&order=Bug%20Number&list_id=164232

## Development setup

### Gradle

You might find useful Gradle commands in [gradle.md](gradle.md)

### <a name="intellij"></a>IntelliJ IDEA

You require IntelliJ 2018.3.1 or newer.

1. Open the build.gradle.kts file with IntelliJ IDEA and choose "Open as Project"
1. Make sure "Create separate module per source set" is selected
1. Make sure "Use default gradle wrapper" is selected
1. In the "File already exists" dialogue, choose "Yes" to overwrite
1. In the "Open Project" dialogue, choose "Delete Existing Project and Import"

### Eclipse

You can generate the Eclipse projects by running

    ./gradlew eclipse

Then you can import the generated projects into Eclipse

1. Install Eclipse 4.5 (Mars) at least
1. Install the Groovy Eclipse plugin from http://dist.springsource.org/snapshot/GRECLIPSE/e4.5/
1. Make sure you have a Java 8 compatible JDK configured in your workspace
1. In `Window->Preferences->Groovy->Compiler`, check `Enable Script folder support` and add `**/*.gradle.kts`
1. Import all projects using the "Import Existing Projects into Workspace" wizard

The following files should be excluded from build output:

    *.metaprops

See `Project Preferences/Java Compiler/Building/Output Folder/Filtered Resources`

## :star2: Have a patch?
The best way to make sure your issue or feature is addressed is to submit a patch.
We accept patches through: 

* pull requests
* patch attached to bugzilla.

However, before sending a patch, please make sure that the following applies:

* Your commit message is descriptive.
* Your patch doesn't have useless merge commits.
* Your coding style is similar to ours.
* Your patch is 100% tested. JUnit are welcome. 
* All tests checks pass (run `ant test`)
* You understand that we're very grateful for your patch!

## :heart: Adding something new?
We do love to enhance Apache JMeter with you to become more powerful! 
The best way to workout your idea is to discuss it first at our dev mailing list:

* https://jmeter.apache.org/mail2.html#JMeterDev

Please, if you can, don't just throw us the code of a new feature; lets figure first together 
what would be the best approach regarding the current architecture and future plans, 
before any development. 
This way we all get sure that your idea is aligned with the codebase and you can enjoy 
your happy coding even more :)

## :closed_book: Want to write docs?

Documentation is very valuable to us. 

It is located in **xdocs** folder in XML format. 

You can contribute as you would for code through patch or PR.

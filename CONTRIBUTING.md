# How to contribute

Want to show Apache JMeter some love? Help out by contributing!

## :beetle: Found a bug

Log it in GitHub issues:

* https://github.com/apache/jmeter/issues
* or send a note to the [user mailing list](https://jmeter.apache.org/mail2.html#JMeterUser).

Be sure to include all relevant information, like the versions of JMeter you’re using as long as Java version.
A Test plan  that caused the issue as well as any error messages are also very helpful.

## :question: Need help

Contact:

* [Our users mailing list](https://jmeter.apache.org/mail2.html#JMeterUser)
* or ask question on [stackoverflow](https://stackoverflow.com/questions/tagged/jmeter).

## :bar_chart: What needs to be developed

See:

* [Open issues for JMeter](https://github.com/apache/jmeter/issues)
* [Enhancement requests for JMeter](https://github.com/apache/jmeter/issues?q=is%3Aopen+label%3Aenhancement)

## Development setup

### Gradle

You might find useful Gradle commands in [gradle.md](gradle.md)

### <a name="intellij"></a>IntelliJ IDEA

You require IntelliJ 2018.3.1 or newer.

1. Open the build.gradle.kts file with IntelliJ IDEA and choose `Open as Project`
1. Make sure `Create separate module per source set` is selected
1. Make sure `Use default gradle wrapper` is selected
1. In the `File already exists` dialogue, choose `Yes` to overwrite
1. In the `Open Project` dialogue, choose `Delete Existing Project and Import`

### Eclipse

Eclipse can import Gradle projects automatically via `Import...->Gradle project` wizard.

Optionally you can generate an Eclipse project by running

    ./gradlew eclipse

The steps to import the sources (based on Eclipse 2019-06) into Eclipse are as follows:

1. Install `Eclipse IDE for Java Developers`
1. Install the Groovy Eclipse plugin and select Groovy Compiler 2.4 (some tests are written in Groovy)
1. Install `Kotlin for Eclipse` plugin (builds scripts are written in Kotlin)
1. Make sure you have a Java 8 compatible JDK configured in your workspace
1. Open `File->Import...`
1. Select `Existing Gradle Project` and click `Next`
1. Read `How to experience the best Gradle integration` and click `Next`
1. Then you might just click `Finish`

## :star2: Have a patch

The best way to make sure your issue or feature is addressed is to submit a patch.
We accept patches through:

* pull requests
* patch attached to [JMeter developers mailing list](https://jmeter.apache.org/mail2.html#JMeterDev).

However, before sending a patch, please make sure that the following applies:

* Your commit message is descriptive.
* Your patch doesn't have useless merge commits.
* Your coding style is similar to ours.
* Your patch is 100% tested. JUnit are welcome.
* All tests checks pass (run `./gradlew check`)
* You understand that we're very grateful for your patch!

## :heart: Adding something new

We do love to enhance Apache JMeter with you to become more powerful!
The best way to work out your idea is to discuss it first at our dev mailing list:

* [JMeter developers mailing list](https://jmeter.apache.org/mail2.html#JMeterDev)

Please, if you can, don't just throw us the code of a new feature; lets figure first together
what would be the best approach regarding the current architecture and future plans,
before any development.
This way we all get sure that your idea is aligned with the codebase, and you can enjoy
your happy coding even more :)

## :closed_book: Want to write docs

Documentation is very valuable to us.

It is located in **[xdocs](xdocs)** folder in XML format.

You can contribute as you would for code through patch or *PR* (pull request).

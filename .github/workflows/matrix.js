// The script generates a random subset of valid jdk, os, timezone, and other axes.
// You can preview the results by running "node matrix.js"
// See https://github.com/vlsi/github-actions-random-matrix
let fs = require('fs');
let os = require('os');
let {MatrixBuilder} = require('./matrix_builder');
const matrix = new MatrixBuilder();
matrix.addAxis({
  name: 'java_distribution',
  values: [
    {value: 'corretto', vendor: 'amazon', weight: 1},
    {value: 'liberica', vendor: 'bellsoft', weight: 1},
    {value: 'microsoft', vendor: 'microsoft', weight: 1},
    {value: 'oracle', vendor: 'oracle', weight: 1},
    // There are issues running Semeru JDK with Gradle 8.5
    // See https://github.com/gradle/gradle/issues/27273
    // {value: 'semeru', vendor: 'ibm', weight: 4},
    {value: 'temurin', vendor: 'eclipse', weight: 1},
    {value: 'zulu', vendor: 'azul', weight: 1},
  ]
});

const eaJava = '22';

matrix.addAxis({
  name: 'java_version',
  // Strings allow versions like 18-ea
  values: [
    '17',
    '21',
    eaJava,
  ]
});

matrix.addAxis({
  name: 'tz',
  values: [
    'America/New_York',
    'Pacific/Chatham',
    'UTC'
  ]
});

matrix.addAxis({
  name: 'os',
  title: x => x.replace('-latest', ''),
  values: [
    // TODO: X11 is not available. Un-comment when https://github.com/burrunan/gradle-cache-action/issues/48 is resolved
    // 'ubuntu-latest',
    'windows-latest',
    'macos-latest'
  ]
});

// Test cases when Object#hashCode produces the same results
// It allows capturing cases when the code uses hashCode as a unique identifier
matrix.addAxis({
  name: 'hash',
  values: [
    {value: 'regular', title: '', weight: 42},
    {value: 'same', title: 'same hashcode', weight: 1}
  ]
});
matrix.addAxis({
  name: 'locale',
  title: x => x.language + '_' + x.country,
  values: [
    {language: 'de', country: 'DE'},
    {language: 'fr', country: 'FR'},
    // TODO: fix :src:dist-check:batchBUG_62847
    // Fails with "ERROR o.a.j.u.JMeterUtils: Could not find resources for 'ru_EN'"
    // {language: 'ru', country: 'RU'},
    {language: 'tr', country: 'TR'},
  ]
});

matrix.setNamePattern(['java_version', 'java_distribution', 'hash', 'os', 'tz', 'locale']);

// Semeru uses OpenJ9 jit which has no option for making hash codes the same
matrix.exclude({java_distribution: {value: 'semeru'}, hash: {value: 'same'}});
// Ignore builds with JAVA EA for now, see https://github.com/apache/jmeter/issues/6114
matrix.exclude({java_version: eaJava})
matrix.imply({java_version: eaJava}, {java_distribution: {value: 'oracle'}})
// TODO: Semeru does not ship Java 21 builds yet
matrix.exclude({java_distribution: {value: 'semeru'}, java_version: '21'});
// Ensure at least one job with "same" hashcode exists
matrix.generateRow({hash: {value: 'same'}});
// Ensure at least one Windows and at least one Linux job is present (macOS is almost the same as Linux)
matrix.generateRow({os: 'windows-latest'});
// TODO: un-comment when xvfb will be possible
// matrix.generateRow({os: 'ubuntu-latest'});
// Ensure there will be at least one job with Java 17
matrix.generateRow({java_version: "17"});
// Ensure there will be at least one job with Java 21
matrix.generateRow({java_version: "21"});
// Ensure there will be at least one job with Java EA
// matrix.generateRow({java_version: eaJava});
const include = matrix.generateRows(process.env.MATRIX_JOBS || 5);
if (include.length === 0) {
  throw new Error('Matrix list is empty');
}
include.sort((a, b) => a.name.localeCompare(b.name, undefined, {numeric: true}));
include.forEach(v => {
  // Pass locale via Gradle arguments in case it won't be inherited from _JAVA_OPTIONS
  // In fact, _JAVA_OPTIONS is non-standard and might be ignored by some JVMs
  let gradleArgs = [
    `-Duser.country=${v.locale.country}`,
    `-Duser.language=${v.locale.language}`,
  ];
  v.extraGradleArgs = gradleArgs.join(' ');
});
include.forEach(v => {
  let jvmArgs = [];
  // Extra JVM arguments passed to test execution
  let testJvmArgs = [];
  if (v.hash.value === 'same') {
    testJvmArgs.push('-XX:+UnlockExperimentalVMOptions', '-XX:hashCode=2');
  }
  // Gradle does not work in tr_TR locale, so pass locale to test only: https://github.com/gradle/gradle/issues/17361
  jvmArgs.push(`-Duser.country=${v.locale.country}`);
  jvmArgs.push(`-Duser.language=${v.locale.language}`);
  v.java_distribution = v.java_distribution.value;
  v.java_vendor = v.java_distribution.vendor;
  if (v.java_distribution === 'oracle') {
      v.oracle_java_website = v.java_version === eaJava ? 'jdk.java.net' : 'oracle.com';
  }
  v.non_ea_java_version = v.java_version === eaJava ? '' : v.java_version;
  if (v.java_distribution !== 'semeru' && Math.random() > 0.5) {
    // The following options randomize instruction selection in JIT compiler
    // so it might reveal missing synchronization
    v.name += ', stress JIT';
    v.testDisableCaching = 'JIT randomization should not be cached';
    jvmArgs.push('-XX:+UnlockDiagnosticVMOptions');
    if (v.java_version >= 8) {
      // Randomize instruction scheduling in GCM
      // share/opto/c2_globals.hpp
      jvmArgs.push('-XX:+StressGCM');
      // Randomize instruction scheduling in LCM
      // share/opto/c2_globals.hpp
      jvmArgs.push('-XX:+StressLCM');
    }
    if (v.java_version >= 16) {
      // Randomize worklist traversal in IGVN
      // share/opto/c2_globals.hpp
      jvmArgs.push('-XX:+StressIGVN');
    }
    if (v.java_version >= 17) {
      // Randomize worklist traversal in CCP
      // share/opto/c2_globals.hpp
      jvmArgs.push('-XX:+StressCCP');
    }
  }
  v.extraJvmArgs = jvmArgs.join(' ');
  v.testExtraJvmArgs = testJvmArgs.join(' ::: ');
  delete v.hash;
});

console.log(include);

let filePath = process.env['GITHUB_OUTPUT'] || '';
if (filePath) {
    fs.appendFileSync(filePath, `matrix<<MATRIX_BODY${os.EOL}${JSON.stringify({include})}${os.EOL}MATRIX_BODY${os.EOL}`, {
        encoding: 'utf8'
    });
}

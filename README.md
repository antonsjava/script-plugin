
# script-plugin

  The plugin generate script for starting any class containing "public static void main(" string.

  I use it mainly for testing purposes. whenewer I need to start application outside IDE. Plugin 
  generates script and then I modified them so I uncomment application to start and add parameters I need.
	
  Script generates two form for script. First one sets classpath using argument file (only for java 9+). 
  Second one uses environment variable CLASSPATH (this can be problematic for long classpath on some systems).  

  Classpath is targets directly to maven repo (Useful for fast testing dependent libraries). It is also possible 
  to instruct plugin to copy dependant libraries to target directory (Useful if you want to copy current 
  application state to someone else or just backup it for later test). 
  

## Usage

Simple generation of unix script to target directory. Just try an look to ./target/script directory

```
mvn io.github.antonsjava:script-plugin::script 
mvn io.github.antonsjava:script-plugin::script -Dscript.pack=true
```

In reality I use two scripts for starting plugin

mvn-script-run.sh - for simple script with maven repo deps.

```
mvn io.github.antonsjava:script-plugin::script -Dscript.destination=/tmp/script -Dscript.includeTest=true "$@"
```

mvn-script-pack.sh - for script with packed maven repo deps.

```
mvn io.github.antonsjava:script-plugin::script -Dscript.destination=/tmp/script -Dscript.includeTest=true -Dscript.pack=true "$@"
```

## Configuration

Plugin uses several properties for configuration. Each property xxxx has equivalent script.xxxx system property

 - **destination** (default: target/script) - path to destination directory
 - **filename** (default: ${artifactId}) - base name for script generation
 - **includeTest** (default: false) - if true also test deps will be included on classpath
 - **includeRepo** (default: false) - if true also source repo directory will be included.
 - **exec** - fqn of class to be executed in script. (all others will be in script commented) If you add params they will be copied too.
 - **shell** (default: /bin/bash) - shell for unix script
 - **unix** (default: true) - if true unix script will be generated
 - **windows** (default: false) - if true windows script will be generated
 - **pack** (default: false) - if true classpath deps will be copied to lib directory


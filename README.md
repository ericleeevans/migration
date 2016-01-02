# Asset Migration Utilities for Scala

The code in this project can be used to automate a "migration graph" - i.e., a set of one or more "transitions" that will bring some asset or assets up to a required "state". An *asset* may be any material maintained by a software package (for example, the schema and data in a database). A *transition* is any sequence of actions (such as an executable script) needed to move an asset from the state that it is currently in, to another state required by some version of the software package.

This project contains _common_ Scala code that can be used to build migration tools for specific asset types. The following projects provide such tools that are ready to use in your application:

* *[sql-migration](http://)*  - Migrate relational database installations via annotaiotn of SQL scripts.
* *[cassandra-migration](http://)* - Migrate Cassandra installations via annotation of CQL scripts.

## Motivation

For a software package with many releases, it can be tedious to maintain migration scripts for every combination of "before" and "after" release.
It's easier for each new release to include a single script to transition the package's assets from their state in the *previous* release (and possibly also a single "rollback" script to revert those assets back to the state needed by the previous release).
However, someone installing a new release after skipping several previous releases is faced with finding out which transition scripts must be executed, in which order, and must typically do so manually. 

Instead, a software package could use these migration utilities to automatically determine the shortest migration "path" - i.e., the shortest sequence of transition scripts - that must be executed to bring its assets to the state required by the current release, regardless of the previously installed release. 
The software package's installation/upgrade procedure could then execute this sequence automatically.
All this requires only a simple set of annotations to each transition script. 


## Example

A common example would be a software package that creates and mantains data in a database. 
The first release might include an "setup" script that creates the database schema and might also populate initial values in it. 
Subsequent releases might require changes to the database schema and sometimes *modification of existing data* that conforms to a previous release's schema - i.e., the exsiting database must be *migrated* so that it can be used by the new release. 

Each such release might include a  script that applies those changes to the database maintained by the previous release. 
But as the number of releases grows, it gets trickier to know which scripts to run (and in which order) when applying a new release against an installation that is several releases behind the current one.
Your installation procedure could use these migration utilities to find the shortest sequence of scripts needed to migrate your data from the previously installed release to the new release, and then execute those scripts in the proper order.

To do this you just need to annotate each transition script with metadata that specifies the database's *before-state* (i.e., before the script is executed), its *after-state*, and an *is-destructive* flag.
The *before-state* and *after-state* can have any String value, but you might typically use some form of your software package's release version (e.g., "1.1.0").
The *is-destructive* flag is a boolean value that indicates whether the transition executed by the script is irreversible (for example, if the script deletes data that cannot be automatically recovered). 
This is only used by the utilities if your package asks it to find the shortest non-destructive migration path (i.e., a sequence containing only reversible transitions).

The possible migrations for such a software package constitute a *state-transition* graph. For example:

~~~
        1.0.a         ------------> 1.1.1 <-----------
          ^          |                                |
          |          v                                v
 . <--> 1.0.0 <--> 1.1.0 <--> 1.2.0 <--> 1.2.5 <--> 1.3.3 --> 2.0.0
 ^                                                              ^
 |                                                              |
  --------------------------------------------------------------
~~~

In this example, the first release must create and initialize its database by executing a transition script.
It also includes a "rollback" script to delete its entire database.
The first script brings the database from the non-existent state (shown as "." in the graph) to the initial state "1.0.0".
This script is annotated with a *before-state* of "." and an *after-state* of "1.0.0", and the *is-destuctive* flag "false".
The "rollback" script is annotated with *before-state* "1.0.0", *after-state* ".", and *is-destructive* "true".

A subsequent release needs to migrate the database to state 1.1.0, and supports a rollback to state 1.0.0. 
So it includes one script annotated with *before-state* "1.0.0" and *after-state*  "1.1.0", and a rollback script annotated with *before-state* "1.1.0" and *after-state* "1.0.0" (note that the *is-destuctive* flag might be "true" or"false" in either of these scripts).

Some other things to note about this graph:

* After release 1.0.0, an experimental branch 1.0.a was created but never actually released.
Installing 1.0.a requires a database transition from 1.0.0, but no rollback  script is provided to go back to 1.0.0, since it is not needed.
There is thus no way to migrate an installation of a 1.0.a database to any subsequent release.

* To upgrade an installation of release 1.0.0 or 1.1.0 to release 1.3.3, there are two possible migration paths.
However, the migration utilities will determine that the path going through 1.1.1 is shorter, and will return that sequence of scripts.

* A brand-new installation of any of the releases between 1.0.0 through 1.3.3 requires executing multiple transition scripts.
But a brand-new installation of release 2.0.0 will require executing only a single transition script.


## Classes

### Core Traits:

* `Transition` - Trait that represents a single transition in a migration graph (i.e., *before-state*, *after-state*, and *is-destructive* flag).
* `MigrationGraph` - Trait that represents a migration graph. Given the Set of all available `Transition`s, it can determine the sequence of those Transitions that constitutes a migration "path" from one state to another (if any such path exists).

### Parsing Transition Metadata

Given some text (e.g., the content of a transition script), these classes support parsing the transition metadata from that text:

* `TransitionMetaData` - Case class implementation of `Transition`.
* `TransitionMetaDataParser` - Trait that can parse an optional `TransitionMetaData` instance from a String.
* `RegexTransitionMetadataParser` - Implementation of `TransitionMetaDataParser` that can use a Regex that matches the three components of `TransitionMetaData`.
* `KeyValueTransitionMetadataParser` - Implementation of `TransitionMetaDataParser` for text in which the TransitionMetadata fields are provided in a key/value format.

### Finding Transition Metadata

Metadata for a given transition is assumed to be embedded in the file that contains the instructions for executing that transition (e.g., the metadata might be in the comments of an executable script).
A software package would typically provide these files as resources in its installation.
The following classes provide support for finding these resources in various packaging scenarios:

* `ResourceResolver` - Trait for resolving transition resources under a given URL. If the implementation supports the URL's protocol, it returns a (possibly empty) Set of the names of all resources found *directly* within the URL's path (non-recursive).
* `FileResourceResolver` - Implementation of `ResourceResolver` for resources in the filesystem (URL protocol "`file:`").
* `JarFileResourceResolver` - Implementation of `ResourceResolver` for resources within a JAR file (URL protocol "`jar:`").
* `BundleResourceResolver` - Implementation of `ResourceResolver` for resources within an OSGi bundle (URL protocol "`bundle:`").

Given a `TransitionMetaDataParser` and one or more `ResourceResolver`s, these classes provide what you need to to find all possible transitions and the resources that implement them:
  
* `ResourceTransition` - Case class implementation of `Transition` that also contains the path to the transition resource.
* `ResourceTransitionFinder` - Utility class for finding and parsing transition metadata from resource files on the classpath. Given a ClassLoader, a classpath directory that contains transition resources (e.g., scripts), a `TransitionMetaDataParser`, and one or more instances of `ResourceResolver`, it will return the Set of all available `ResourceTransition`s.

### Tying it together

Given a `ResourceTransitionFinder` you can obtain the Set of `ResourceTransition`s which would be needed by an implementation of the `MigrationGraph` trait. 
This trait can already determine the shortest migration path between any two states, but an implementation might also support the *execution* of the transition scripts for the specific technology (e.g., such as SQL scripts).


---

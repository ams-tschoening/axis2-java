== Why is a custom launch configuration needed? ==

Maven is integrated into Eclipse using the "m2e" plugin and this includes executing/debugging main
Maven goal from Eclipse's context menu for projects, like with other Java/web apps etc. While this
might be enough already to debug a project in Eclipse, one can find docs and tutorials creating
custom launch configurations anyway. Additionally, the Axis2 projects seem to have some problem with
the build which makes them incompatible with executing the available configs of the default Eclipse
context menu, which are "install" or "test" only, without other goals. The Axis2 projects instead
need to always be executed using the goal "clean" as well or pre-compiled classes from former builds
will be deleted, but not re-created, resulting in various compile time errors and failing builds. So
to be able to specify twi goals a custom launch configuration is needed anyway.

http://maven.apache.org/surefire/maven-surefire-plugin/examples/debugging.html
http://mail-archives.apache.org/mod_mbox/axis-java-dev/201709.mbox/<165709498.20170905111249%40am-soft.de>

== Why store it at root level? ==

Axis2 consists of many individual projects and creating launch configurations for all of them seems
to be quite some maintenance work, especially if all their settings are most likely identical. So
currently only one configuration at the project root is maintained and one only needs to keep two
things in mind: This only works if "Debug As" is used from the context menu of an individual project
instead of using "Debug As" at workspace level, because the one configuration uses the name of the
current project as a variable which is only available at project level of course.

Additionally, it is important ot only add individual "Java Project"s as source folders, not the
whole workspace and not the main project "axis2", which is no Java project. The reasion is that
otherwise source code is found during debugging, but because it's not of a Java project, one doesn't
get any Javadoc, runtime data in hovering text etc. without that debugging is unnecessary difficult.

== Custom launch configuration is not part of the context menu! ==

The custom launch configuration is not part of the context menu for Maven projects of Eclipse, but
needs to be manually chosen using "Debug As/Debug Configurations/Maven Build". I didn't find a way
yet to add it to the context menu, if possible at all.

Keep in mind that one needs to choose/execute the config at project level!

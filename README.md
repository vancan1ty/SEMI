SEMI - Simple Eclipse Maven Integration
====

This project demonstrates a very simple model of integrating eclipse with maven.  Instead of integrating maven plugins into the complex eclipse incremental build system, this plugin leverages the maven eclipse mojo (with the standard eclipse:eclipse goal) to simply build an eclipse project from the pom file, and then forget about maven till the next time the pom changes.

The idea is that, when somewhat more polished, this could be a radically simpler approach to maven integration without any of the "lifecycle mapping" problems which characterize the M2Eclipse setup experience.  

The idea behind this plugin is that you would be able to get an experience more like IntelliJ IDEA eclipse integration than current M2E.  Right now the reality falls far short of this idea!

Supported Features
-------------------

Import existing maven projects.

Run maven goals from within eclipse.

Maven output in the console.

Todo list
----------

Get New Project wizard working.

Get automatic eclipse rebuild triggered on pom change.

Fix project renames.

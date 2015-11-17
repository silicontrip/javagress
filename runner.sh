#!/bin/sh

mainclass=$( basename $0 | sed s/.sh$//)

echo $mainclass

java -cp s2-geometry-java.jar:jackson-annotations-2.3.3.jar:jackson-core-2.3.4.jar:jackson-databind-2.3.4.jar:portaltools.jar $mainclass "$@"

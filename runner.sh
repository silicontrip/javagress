#!/bin/sh

mainclass=$( basename $0 | sed s/.sh$//)

echo $mainclass

java -cp cellserver.jar:vecmath-1.5.1.jar:guava-18.0.jar:s2-geometry-java.jar:jackson-annotations-2.9.6.jar:jackson-core-2.9.6.jar:jackson-databind-2.9.6.jar:portaltools.jar $mainclass "$@"

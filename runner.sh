#!/bin/sh

mainclass=$( basename $0 | sed s/.sh$//)

echo $mainclass

#java -cp cellserver.jar:vecmath-1.5.1.jar:guava-18.0.jar:s2-geometry-java.jar:jackson-annotations-2.9.6.jar:jackson-core-2.9.6.jar:jackson-databind-2.9.6.jar:portaltools.jar $mainclass "$@"

java -cp cellserver.jar:jackson-annotations-2.15.2.jar:jackson-core-2.15.2.jar:jackson-databind-2.15.2.jar:portaltools.jar:s2-geometry-java.jar:vecmath.jar $mainclass "$@"

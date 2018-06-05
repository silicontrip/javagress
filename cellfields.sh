#!/bin/sh

mainclass=$( basename $0 | sed s/.sh$//)

#echo $mainclass

java -cp vecmath-1.5.1.jar:jackson-annotations-2.3.3.jar:jackson-core-2.3.4.jar:jackson-databind-2.3.4.jar:portaltools.jar:CellServer/cellserver.jar:CellServer/lib/json-java.jar:CellServer/lib/mongo-java-driver-3.0.2.jar:CellServer/lib/s2-geometry-java.jar:CellServer/lib/guava-18.0.jar $mainclass "$@"


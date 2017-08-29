
CLASSES=CellServer.class showcell.class

CP=lib/s2-geometry-java.jar:lib/json-java.jar:.

all: cellserver.jar

cellserver.jar: classes
	jar -cf cellserver.jar $(CLASSES) 

classes: $(CLASSES)

%.class: %.java
	javac  -classpath $(CP) -encoding utf8 -Xlint:deprecation -Xlint:unchecked  $<

clean:
	rm *.class


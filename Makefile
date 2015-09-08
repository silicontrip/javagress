
CLASSES = Field.class Line.class Link.class Point.class Portal.class PortalFactory.class  teamCount.class  multilinker.class  alllinker.class multilinker2.class manylinks.class Poly.class PolyPoint.class CombinationFactory.class BlockList.class maxfields.class targetlinker.class

CP=jackson-annotations-2.3.3.jar:jackson-core-2.3.4.jar:jackson-databind-2.3.4.jar:.

all: portaltools.jar


portaltools.jar: classes
	jar -cf portaltools.jar $(CLASSES) 'PortalFactory$$1.class' 'PortalFactory$$2.class' 'PortalFactory$$3.class'

classes: $(CLASSES)

%.class: %.java
	javac  -classpath $(CP) -encoding utf8 -Xlint:deprecation -Xlint:unchecked  $<

clean:
	rm *.class


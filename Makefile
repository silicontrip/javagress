
CLASSES = DrawTools.class Field.class Line.class Link.class Point.class Portal.class PortalFactory.class  teamCount.class \
Poly.class  PolyPoint.class BlockList.class \
multilinker.class  alllinker.class multilinker2.class manylinks.class \
maxfields.class targetlinker.class megaplan.class portalquery.class \
PolyObject.class Polygon.class Polyline.class Marker.class Circle.class PolyPoint.class PolyType.class \
Arguments.class PortalSelectionStrategy.class PortalSelectionRangeStrategy.class PortalSelectionBoxStrategy.class PortalSelectionTriangleStrategy.class


#PolyPoint.class CombinationFactory.class BlockList.class 

CP=jackson-annotations-2.3.3.jar:jackson-core-2.3.4.jar:jackson-databind-2.3.4.jar:.

all: portaltools.jar


portaltools.jar: classes
	jar -cf portaltools.jar $(CLASSES) 'PortalFactory$$1.class' 'PortalFactory$$2.class' 'PortalFactory$$3.class' 'DrawTools$$1.class'

classes: $(CLASSES)

%.class: %.java
	javac  -classpath $(CP) -encoding utf8 -Xlint:deprecation -Xlint:unchecked  $<

clean:
	rm *.class



CLASSES = DrawTools.class Field.class Line.class Link.class Point.class Portal.class PortalFactory.class  teamCount.class \
Poly.class  PolyPoint.class BlockList.class \
layerlinker.class multilinker.class  alllinker.class multilinker2.class manylinks.class  cyclonelinker.class \
maxfields.class targetlinker.class megaplan.class portalquery.class spiner.class draw.class guardlink.class \
cellfields.class cellper.class exolinker.class portalshadow.class\
PolyObject.class Polygon.class Polyline.class Marker.class Circle.class PolyPoint.class PolyType.class \
Arguments.class PortalSelectionStrategy.class PortalSelectionRangeStrategy.class PortalSelectionBoxStrategy.class PortalSelectionTriangleStrategy.class \
MUCache.class RunTimer.class UniformDistribution.class UniformDistributionException.class 


#PolyPoint.class CombinationFactory.class BlockList.class 

CP=json-java.jar:vecmath.jar:s2-geometry-java.jar:jackson-annotations-2.15.2.jar:jackson-core-2.15.2.jar:jackson-databind-2.15.2.jar:.

all: portaltools.jar


portaltools.jar: classes
	jar -cf portaltools.jar $(CLASSES) 'PortalFactory$$1.class' 'PortalFactory$$2.class' 'PortalFactory$$3.class'

classes: $(CLASSES)

%.class: %.java
	javac  -classpath $(CP) -encoding utf8 -Xlint:deprecation -Xlint:unchecked  $<

clean:
	rm *.class


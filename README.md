# javagress
## tools to help create different styles of fielding.
These tools talk to a db backend (via a web api) without the web api these tools do nothing.

### alllinker 
Tool to find the largest field with the fewest blocking links.

`alllinker.sh [-E <max number of ENL blocks>] [-R <max number of RES blocks>] <portal range> [<portal_range>] [<portal_range>]`

### multilinker2
Tool to create multiple layers from a source field.

### manylinks
Identifies possible links to create from a keylist file. (used for chasing connector badge)

### maxfields
Generates the maximum possible fields in an area. (used for chasing mind controller badge)

### targetlinker
Generates the most layers over a single target. 

## Portal range 
The portal range is a descriptive argument to describe a range of portals.
It can be one of
`portal:range` 
portal is either lat,lng or a portal name. (remember that portal names are not unique)
range is in km.  If range is omitted 0 is assumed.
`./file.txt`
which contains a list of type portal
`portal/portal`
Gets portal bounded by the square defined by the 2 portals.
`portal/portal/portal`
get portals bounded by the triangle defined by the 3 portals
`[{drawtools description}]`
returns all portals within a 3 point polygon.  Will only handle a 3 point polygon (not a polyline or a different number of points)

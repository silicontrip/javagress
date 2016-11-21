# javagress
## tools to help create different styles of fielding.
These tools talk to a db backend (via a web api) without the web api these tools do nothing.  Although they can now work from a file:// url for offline operation. 
These tools also require the Jackson Library to perform json parsing. https://github.com/FasterXML/jackson

## common arguments.

All tools should accept the following arguments:

`-E n specify the maximum number of ENL blocking links. 0 to cross no ENL link, do not specify to allow any number.`
`-R n specify the maximum number of RES blocking links. 0 to cross no RES link, do not specify to allow any number.`
`-C #rrggbb specify colour for drawtools output.` 
`-L output drawtools plan as polylines.`

the following arguments work in some tools:

`-M print field MU rather than area.  this requires the MU database.`
`-T lat,lng use only fields covering this point`


### alllinker 
Tool to find the largest field with the fewest blocking links.  (replaced by layerlinker)

`alllinker.sh [-E <max number of ENL blocks>] [-R <max number of RES blocks>] <portal range> [<portal_range>] [<portal_range>]`

For portal range see below

### multilinker2
Tool to create multiple layers from a source field. This is an early version of megaplan.

`multilinker2  <3 point portal range> <number of layers>`

the 3 point range must either be portal/portal/portal or a drawtools description

### manylinks
Identifies possible links to create from a keylist file. (used for chasing connector badge)

`manylinks <portal_range> [<portal_range>]`

the second range is usually a keylist.

### maxfields
Generates the maximum possible fields in an area. (used for chasing mind controller badge)

`maxfields <portal_range>`

### targetlinker
Generates the most layers over a single target.   Also creates cyclone plans.  Target functionality is being added to other tools.

`targetlinker [-c cadence] [-r] <lat> <lng> <portal_range>`

cadence is a number between 0 and 2
-r is for reverse direction.  You may need to try all 6 combinations of reverse and cadence to find the best number of fields.

## Portal range 
The portal range is a descriptive argument to describe a range of portals.

It can be one of

`portal:range` 
portal is either lat,lng or a portal name. (portal names are not unique)
range is in km.  If range is omitted 0 is assumed.

`./file.txt`
which contains a list of type portal. this is very slow.

`portal/portal`
Gets portal bounded by the square defined by the 2 portals.

`portal/portal/portal`
get portals bounded by the triangle defined by the 3 portals

`[{drawtools description}]`
returns all portals within a 3 point polygon.  Will only handle a 3 point polygon (not a polyline or a different number of points)

## backend api

the backend provides 2 api calls. getportals and getlinks.

getportals returns portals bounded by different shapes depending on the arguments

Return portals in rect `?ll=portal&l2=portal`

Return portals in triangle `?ll=portal&l2=portal&l3=portal`

Return portals in range (in km) `?ll=portal&rr=range`

Return single portal `?ll=portal`

it returns a json string

`[{"8a9064c9e20743d9b0dc03c109129403.11":{"guid":"8a9064c9e20743d9b0dc03c109129403.11","title":"Ringwood Station North Entrance","health":75,"rescount":7,"team":"E","level":5,"lat":-37815723,"lng":145228945},"ed4f888bfce24edca1215756e35d9710.16":{"guid":"ed4f888bfce24edca1215756e35d9710.16","title":"First Church of Christ, Scientist, Ringwood","health":100,"rescount":8,"team":"R","level":7,"lat":-37816373,"lng":145229531}]`

getlinks simply returns the links without parameters. 

`[{"guid":"38902c27142d41af9543bccc5e35b92e.9","dguid":"74a77bbb02e042cea4097069be9b05c4.16","dlat":-37652568,"dlng":145516969,"oguid":"e407aef6dd61448c885ca7e4f04ad2c0.16","olat":-37207996,"olng":145429963,"team":"E"},{"guid":"09aa5481b22141bfb5b0c2381e35dd8b.b_b","dguid":"47d3c4b53725438da0c42496501cea84.16","dlat":-37511943,"dlng":145120831,"oguid":"e8679a16658444c0bd3c9097f3395fc9.16","olat":-37511696,"olng":145120909,"team":"R"},{"guid":"f269aece351743418be19136f2a4aef7.9","dguid":"609593d1de734438aa5a11fa2fb58e40.16","dlat":-37686553,"dlng":143362732,"oguid":"f3b8b3073ef4411b8978da0d21be256d.16","olat":-38386340,"olng":142518279,"team":"R"}]`

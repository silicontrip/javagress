// ==UserScript==
// @name         linksFields
// @category       Layer
// @version        0.5.0
// @updateURL      https://github.com/silicontrip/javagress/raw/refs/heads/master/linksFields.user.js
// @downloadURL    https://github.com/silicontrip/javagress/raw/refs/heads/master/linksFields.user.js
// @namespace    http://tampermonkey.net/
// @description  convert drawtools links to fields or fields to links
// @author       silicontrip
// @match        https://*.ingress.com/intel*
// @match        https://intel.ingress.com/*
// @match        http://intel.ingress.com/*
// @grant        none
// ==/UserScript==

function wrapper(plugin_info) {
	// ensure plugin framework is there, even if iitc is not yet loaded
	if(typeof window.plugin !== 'function') window.plugin = function() {};

	window.plugin.linksFields = {
		raw: [],
		portalCache: {},
		edgeCache: {},
		blockCache: {},
		blockLayer: null,
		unloadedDialog: null,
        selectedPortal: null,
		BLOCK_STYLE : {
			color: '#F0F0F0',
			opacity: 1,
			weight: 5,
			clickable: false,
			dashArray: [12],
			smoothFactor: 10,
		},
		pathLayer : null,
		PATH_STYLE : {
			color: '#C0C040',
			opacity: 1,
			weight: 1.5,
			clickable: false,
			dashArray: [6,4],
			smoothFactor: 10,
		},
		// Static methods called from outside the linksFields instance
		setup: function() {
			console.log(">>> links & fields run planner - setup");
			$('#toolbox').append('<a onclick="window.plugin.linksFields.toFields();return false;" >To Fields</a>');
			$('#toolbox').append('<a onclick="window.plugin.linksFields.toLinks();return false;" >To Links</a>');
			$('#toolbox').append('<a onclick="window.plugin.linksFields.runPlan();return false;" >Run Plan</a>');
			addHook('portalAdded', window.plugin.linksFields.portalAdded);
			addHook('linkAdded', window.plugin.linksFields.edgeAdded); // the idea is that you can scan around the map and find blockers.
			addHook('portalDetailLoaded', window.plugin.linksFields.portalDetails);
			addHook('mapDataRefreshEnd', window.plugin.linksFields.updateBlockLayer);
			window.plugin.linksFields.blockLayer = L.layerGroup([]);
			window.plugin.linksFields.pathLayer = L.layerGroup([]);
			window.map.on('layeradd', window.plugin.linksFields.layerAdd);
			window.addLayerGroup('Plan Blockers', window.plugin.linksFields.blockLayer, false);
			window.addLayerGroup('Plan Path', window.plugin.linksFields.pathLayer, false);

			var plan = window.plugin.linksFields.linkify(window.plugin.linksFields.getDrawTools());
		},
		portalDetails: function(data) {
			var portal = data.details;
			portal.guid = data.guid;
            //console.log(portal);
            window.plugin.linksFields.selectedPortal = portal;
			window.plugin.linksFields.portalCache[portal.guid] = portal;
			window.plugin.linksFields.updatePlan();
		//	localStorage.setItem("window.plugin.linksFields.portalCache", JSON.stringify(window.plugin.linksFields.portalCache));
		},
		portalAdded: function(data) {
			// may want to limit this to portals in the plan
			//console.log("Portal added: " + data.portal.options.guid);
			var portal = data.portal.options.data;
			portal.guid = data.portal.options.guid;

			var dt = window.plugin.linksFields.getDrawTools();
			// console.log("PLAN: " + JSON.stringify(dt));

			//console.log("linkify");
			var plan = window.plugin.linksFields.linkify(dt);

			// console.log("CACHE: " + JSON.stringify(window.plugin.linksFields.portalCache));
			//console.log("PL of PLAN");
			for (var pl of plan) {
                if (pl.type == 'polyline')
                {
                    if ((portal.latE6 == pl.latLngs[0].lat * 1000000 && portal.lngE6 == pl.latLngs[0].lng*1000000) ||
                        (portal.latE6 == pl.latLngs[1].lat * 1000000 && portal.lngE6 == pl.latLngs[1].lng*1000000))
                    {
                        // console.log("PLAN PORTAL: " + JSON.stringify(pl));
                        if (portal.guid in window.plugin.linksFields.portalCache)
                        {
                            // don't overwrite if there is no title
                            if ('title' in portal)
                            {
                                window.plugin.linksFields.portalCache[portal.guid] = portal;
                            }
                            //console.log("PORTAL EXISTS: " + JSON.stringify(portal));
                        }
                        else
                        {
                            window.plugin.linksFields.portalCache[portal.guid] = portal;
                            // is unloaded dialog shown?
                            var unloaded = $('#unloaded');
                            //console.log("UNLOADED: " + JSON.stringify(unloaded));
                            if (unloaded.length > 0)
                            {
                                // check that all portals are cached.
                                var missing = window.plugin.linksFields.checkCache(plan);
                                if (missing.length > 0)
                                {
                                    var html = window.plugin.linksFields.htmlUnloaded(missing);
                                    unloaded.html(html);
                                } else {
                                    // close DIALOG
                                    //console.log("DIALOG:");
                                    //console.log($("ui-dialog-portal"));
                                    //console.log(window.dialog);
                                    //$("ui-dialog-portal").dialog('close');
                                    // run plan
                                    window.plugin.linksFields.unloadedDialog.dialog('close');
                                    window.plugin.linksFields.runPlan();
                                }
                            }

                        }
                        //			localStorage.setItem("window.plugin.linksFields.portalCache", JSON.stringify(window.plugin.linksFields.portalCache));
                        //			break;
                    }
                }
			}
            // console.log("<<< portalAdded");
		},
		edgeAdded: function(data) {
			// limit to links that intersect the plan
			//console.log("Edge added: " + data.link.options.guid);
			var link = data.link.options.data;
			link.guid = data.link.options.guid;
			window.plugin.linksFields.edgeCache[link.guid] = link;
			// see if link blocks plan and update blocker layer
		},
		layerAdd: function(e) {
			if (e.layer === window.plugin.linksFields.blockLayer)
            {
				window.plugin.linksFields.updateBlockLayer();
            }
			if (e.layer === window.plugin.linksFields.pathLayer)
            {
				window.plugin.linksFields.updatePathLayer();
            }
		},
		updateBlockLayer: function() {
			// this only needs to update if the plan has changed or new links are added...
			if (!window.map.hasLayer(window.plugin.linksFields.blockLayer))
            {
				return;
            }
			// highlight the blockers !
			window.plugin.linksFields.blockLayer.clearLayers();
			var link_list = window.plugin.linksFields.linkify(window.plugin.linksFields.getDrawTools());
			var blockers = window.plugin.linksFields.getBlockers(link_list);
			for (var link of blockers)
			{
				//console.log("BLOCKER LINK: " +  JSON.stringify(link));
				// need to uniq the blockers...
                var poly;
				for (var ll of link.enl) {
					poly = L.geodesicPolyline(ll.latLngs , window.plugin.linksFields.BLOCK_STYLE);
					window.plugin.linksFields.blockLayer.addLayer(poly);
					//poly = L.polyline(ll.latLngs , window.plugin.linksFields.BLOCK_STYLE);
					//poly.addTo(window.plugin.linksFields.blockLayer);
				}
				for (ll of link.res) {
					//poly = L.polyline(ll.latLngs , window.plugin.linksFields.BLOCK_STYLE);
					//poly.addTo(window.plugin.linksFields.blockLayer);
					poly = L.geodesicPolyline(ll.latLngs , window.plugin.linksFields.BLOCK_STYLE);
					window.plugin.linksFields.blockLayer.addLayer(poly);
				}
                for (ll of link.neu) {
					poly = L.geodesicPolyline(ll.latLngs , window.plugin.linksFields.BLOCK_STYLE);
					window.plugin.linksFields.blockLayer.addLayer(poly);
				}
			}
		},
		updatePathLayer: function() {
			if (!window.map.hasLayer(window.plugin.linksFields.pathLayer))
            {
				return;
            }
			window.plugin.linksFields.pathLayer.clearLayers();
			var link_list = window.plugin.linksFields.linkify(window.plugin.linksFields.getDrawTools());
			var validity = window.plugin.linksFields.check_plan(link_list);
			var old_link = null;
			for (var i =0; i < link_list.length; i++)
			{
				var link = link_list[i];
				//console.log("PATH LINK: " + JSON.stringify(link));
				if (validity[i][0] != "exists")
				{
					if (old_link)
					{
                        var poly;
                        if (link.type == 'polyline')
                        {
                            poly = L.polyline([old_link.latLngs[0],link.latLngs[0]] , window.plugin.linksFields.PATH_STYLE);
                        }
                        if (link.type == 'marker')
                        {
                            poly = L.polyline([old_link.latLngs[0],link.latLng] , window.plugin.linksFields.PATH_STYLE);
                        }
						poly.addTo(window.plugin.linksFields.pathLayer);
						poly.addTo(window.plugin.linksFields.blockLayer);
					}
                    if (link.type == 'polyline')
                    {
                        old_link = link;
                    }
                    if (link.type == 'marker')
                    {
                        old_link = {};
                        old_link.latLngs = [];
                        old_link.latLngs[0] = link.latLng;
                    }
				}
			}
		},
		// instance methods. called from within the linksFields instance
		rippleBlocked: function(linkList) {
			// this is a way to re-sort the link order to remove blocking fields.
			// still doesn't handle links with fields on 2 sides.
			// I don't quite know the logic either.
			let change = true;
			//  do this until no blockers.
			while (change) {
				const valid = this.check_plan(linkList);
				change = false;
				for (let i =0; i< valid.length; i++)
				{
					if (valid[i][0] === 'blocked')
					{
						const link = linkList[i];
						linkList[i] = linkList[i-1];
						linkList[i-1] = link;
						change = true;
						break;
					}
				}
			}
			return linkList;
		},
		rippleMore: function(linkList) {
			// this is a way to re-sort the link order to remove links with fields on 2 sides.
			let change = true;
			//  do this until no blockers.
			while (change) {
				const valid = this.check_plan(linkList);
				change = false;
				for (let i =valid.length-1; i>= 0; i--)
				{
					if (valid[i][0] === 'inner')
					{
						const link = linkList[i];
						linkList[i] = linkList[i+1];
						linkList[i+1] = link;
						change = true;
						break;
					}
				}
			}
			return linkList;
		},
		sortExisting: function(link_list) {
			// This moves existing links to the top of the plan.
			var exist_list = link_list.filter(this.linkInPlay);

			// Filter out existing links from the original array
			const remainingLinks = link_list.filter(link => !exist_list.includes(link));

			// Concatenate sorted existing links with remaining non-existing links
			return exist_list.concat(remainingLinks);
		},
        linkOriginEqPortal(link, portal)
        {
            //console.log(link);
            //console.log(portal);
            var loLat;
            var loLng;
            if (link.type == "polyline") {
                loLat = link.latLngs[0].lat;
                loLng = link.latLngs[0].lng;
            } else if ( link.type == "marker") {
                loLat = link.latLng.lat;
                loLng = link.latLng.lng;
            }
            const pLat = portal.latE6 / 1000000.0;
            const pLng = portal.lngE6 / 1000000.0;
            return (loLat == pLat && loLng == pLng);
        },
        sortPortalToTop: function(link_list, selected_portal)
        {
			var sort_list =[];
            var i;
			for (i=link_list.length-1;i>=0;i--)
			{
                var link = link_list[i];
                if (this.linkOriginEqPortal(link,selected_portal))
                {
                    sort_list.push(link);
                    link_list.splice(i,1);
                }
            }
			if (sort_list.length > 0)
            {
				return sort_list.concat(link_list);
            }
            return link_list;
        },
        sortPortalToBottom: function(linkList, selectedPortal)
        {
			const sortList =[];
            //console.log(linkList);
            //console.log("length: " + linkList.length);
			for (let i=linkList.length-1;i>=0;i--)
			{
                var link = linkList[i];
                //console.log(i);
                //console.log(link);
                if (this.linkOriginEqPortal(link,selectedPortal))
                {
                    sortList.push(link);
                    linkList.splice(i,1);
                }
            }
            //console.log(linkList.concat(sortList));
			return linkList.concat(sortList);
        },
		countSBUL: function(guid) {
			// count the number of SBULs on a portal
			const portalMods = this.portalCache[guid];

			if (portalMods === undefined || !('mods' in portalMods))
            {
				return -1;
            }
            if (portalMods.team != window.PLAYER.team.slice(0,1))
            {
                return 0;
            }
			let existingSBUL = 0;
			for (const mod of portalMods.mods)
            {
				if (mod && mod.name === 'SoftBank Ultra Link')
                {
					existingSBUL++;
                }
            }

			return existingSBUL;
		},
		slotsAvailable: function(guid) {
			// count the number of free slots for this player
			const portalMods = this.portalCache[guid];

			if (portalMods === undefined || !('mods' in portalMods)) {
				return -1;
			}

            if (portalMods.team != window.PLAYER.team.slice(0,1))
            {
                return 2;
            }

			let ownerCount = 2;
			let total = 4;

			for (const mod of portalMods.mods) {
                if ( !(mod === null))
                {
                    if (mod && mod.owner === window.PLAYER.nickname) {
                        ownerCount--;
                    }
                    total--;
                }
			}

			return ownerCount < total ? ownerCount : total;
		},
		makeHtml: function(link_list) {
			var html;
			html = '<button type="button" onclick="window.plugin.linksFields.sortPlan()">Sort Links</button>';
			html += '<button type="button" onclick="window.plugin.linksFields.sortPlanMore()">Sort More</button>';

			html += ' <button type="button" onclick="window.plugin.linksFields.export_plan()">Export</button>';
			html += ' <button type="button" onclick="window.plugin.linksFields.sortToTop()">Selected Portal to Top</button>';
			html += ' <button type="button" onclick="window.plugin.linksFields.sortToBottom()">Selected Portal to Bottom</button>';

			html += '<hr/>';
			html += '<table><tr><td style="width:5px;"></td><td>Start Portal</td><td></td><td>End Portal</td><td>ENL</td><td>RES</td><td>NEU</td><td></td><td></td></tr>';
			var key_req = {};
			var src_req = {};
			var i ;
			var link;

			var status = "";
			// get the status of each link
			var validity = this.check_plan(link_list);

			// check for existing blocking links...
			// get_blockers

			var blockers = this.getBlockers(link_list);

			for (i=0;i<link_list.length;i++)
			{
				link = link_list[i];

				if (link.type == 'polyline') {
					var colour="";
					if (validity[i][0] == 'blocks')
					{
						colour = "#c04040";
						status += this.link_str(link) + " blocks<br/>";
						for (var flink of validity[i][1])
                        {
							status += this.link_str(flink) + "<br/>";
                        }
					}
					if (validity[i][0] == 'blocked')
                    {
						colour = "#c08040";
                    }
					if (validity[i][0] == 'inner')
                    {
						colour = "#c040c0";
                    }
					if (validity[i][0] == 'more')
					{
						colour = "#c08080";
						status += this.link_str(link) + " make more than 2 fields<br/>";
					}
					if (validity[i][0] == 'same')
					{
						colour = "#c08080";
						status += this.link_str(link) + " makes 2 fields on the same side<br/>";
					}
					if (validity[i][0] == 'complete')
                    {
						colour = "#40c040";
                    }
					if (validity[i][0] == 'exists')
                    {
						colour = "#c0c0c0";
                    }

					var oGuid = this.getPointGuid(link.latLngs[0]);
					var dGuid = this.getPointGuid(link.latLngs[1]);

					// check the number of outbound links
					if (validity[i][0] != 'exists')
					{
						if (oGuid in src_req)
						{
							src_req[oGuid]++;
							// check for SBUL
							if (src_req[oGuid] > 8)
							{
								var sbulNeeded = Math.floor((src_req[oGuid]-1) / 8);
								var free = this.slotsAvailable(oGuid);
								var sbul = this.countSBUL(oGuid);

								// if one is -1 they both should be
								if (free == -1 || sbul == -1)
								{
									colour = "#c04040";
									status += this.getPortalLink(oGuid) + " too many outbound links (Portal Details not loaded)<br/>";
								}

								else if (sbul >= sbulNeeded)
								{
									// would like to notify this once per portal
									//colour = "#80c040";
									status += this.getPortalLink(oGuid) + " has SBUL<br/>";
								}
								else if (sbulNeeded - sbul <= free)
								{
									colour = "#c0c040";
									status += this.getPortalLink(oGuid) + " requires SBUL<br/>";
								}
								else
								{
									colour = "#c04040";
									status += this.getPortalLink(oGuid) + " too many outbound links (not enough free mods)<br/>";
								}
							}
						}
						else
						{
							var pLinks = window.getPortalLinks(oGuid);
                            var portal = this.portalCache[oGuid];
                            //console.log(portal);
                            if (portal.team == window.PLAYER.team.slice(0,1))
                            {
                                src_req[oGuid] = pLinks.out.length +1;
                            }
                            else
                            {
                                src_req[oGuid] = 1;
                            }
						}
						if (dGuid in key_req)
                        {
							key_req[dGuid]++;
                        }
						else
                        {
							key_req[dGuid] = 1;
                        }
					}

					html +='<tr>';
					html +='<td style="background-color:' + colour + ';"> </td>';
					html +='<td>'+ this.getPortalLink(oGuid) + '</td>';
					html +='<td><a onclick="window.plugin.linksFields.edit('+i+',\'swap\')">\u27f7</a></td>';
					html +='<td>'+ this.getPortalLink(dGuid) + '</td>';

					if (blockers[i].enl.length > 0)
                    {
						html += '<td>' + blockers[i].enl.length + '</td>';
                    }
					else
                    {
						html += '<td></td>';
                    }

					if (blockers[i].res.length > 0)
                    {
						html += '<td>' + blockers[i].res.length + '</td>';
                    }
					else
                    {
						html += '<td></td>';
                    }

                    if (blockers[i].neu.length > 0)
                    {
						html += '<td>' + blockers[i].neu.length + '</td>';
                    }
					else
                    {
						html += '<td></td>';
                    }

					//html +='<td><a onclick="window.plugin.linksFields.rangePopup('+link.distance+')">'+link.distance+'</a></td>';

					if (validity[i][0] == 'exists')
                    {
						// can't move existing links
						html +='<td></td>';
                    }
					else
					{
						// draw valid up and down arrows
						// although I don't like this UI method of moving links.
						html +='<td>';
						if (i>0 && validity[i-1][0] != 'exists')
                        {
							html +='<a onclick="window.plugin.linksFields.edit('+i+',\'up\')">\u2191</a>';
                        }
						if (i>0 && validity[i-1][0] != 'exists' && i<link_list.length-1)
                        {
							html+='-'; // maybe would like a bullet
                        }
						if (i<link_list.length-1)
                        {
							html+='<a onclick="window.plugin.linksFields.edit('+i+',\'down\')">\u2193</a>';
                        }
						html +='</td>';
					}
					html+= '</tr>';
				}
                if (link.type == 'marker') {
                    var oGuid = this.getPointGuid(link.latLng);
					html +='<tr>';
					html +='<td style="background-color:;"> </td>';
					html +='<td>'+ this.getPortalLink(oGuid) + '</td>';
					html +='<td></td>';
					html +='<td></td>';
                    html += '<td></td>';
                    html += '<td></td>';
                    html += '<td></td>';
						html +='<td>';
						if (i>0 && validity[i-1][0] != 'exists')
                        {
							html +='<a onclick="window.plugin.linksFields.edit('+i+',\'up\')">\u2191</a>';
                        }
						if (i>0 && validity[i-1][0] != 'exists' && i<link_list.length-1)
                        {
							html+='-'; // maybe would like a bullet
                        }
						if (i<link_list.length-1)
                        {
							html+='<a onclick="window.plugin.linksFields.edit('+i+',\'down\')">\u2193</a>';
                        }
                    html +='</td>';
                    html+= '</tr>';

                }
			}
			html +='</table>';
			html +="<hr/>";

			if (status === "")
            {
				html += "<p>VALID</p>";
            }
			else
            {
				html += "<p>" + status + "</p>";
            }

			html +="<hr/>";

			html += '<table><tr><td>Source Portal</td><td>Links</td></tr>';
			var keys = Object.keys(src_req);
			for (i=0;i<keys.length;i++)
			{
				html += '<tr>';
				html +='<td>' + this.getPortalLink(keys[i])+'</td>';
				html+= '<td>' + src_req[keys[i]] + '</td></tr>';
			}
			html +='</table>';
			html +="<hr/>";
			html += '<table><tr><td>Destination Portal</td><td>Keys</td></tr>';
			keys = Object.keys(key_req);
			for (i=0;i<keys.length;i++)
			{
				html += '<tr>';
				html +='<td>' + this.getPortalLink(keys[i]) + '</td>';
				html+= '<td>' + key_req[keys[i]] + '</td></tr>';
			}
			return html;
		},
		htmlUnloaded: function(unloaded) {
			var html = "";
			if (unloaded.length > 0)
			{
				for (var pt of unloaded)
				{

					var latlng = [pt.lat,pt.lng].join();
					//var latlng = '[' + pt.lat +','+pt.lng+']';
				// how do I move the map without reloading...
					//var jsSingleClick = 'window.selectPortalByLatLng(\''+latlng+'\');return false';
					var jsSingleClick = 'window.map.setView(['+latlng+'],15);return false';
					// var jsSingleClick = 'L.DomUtil.setPosition(window.map._mapPane, L.LatLng(\''+latlng+'\'));return false;';

					var a = $('<a>',{
						text: latlng,
						onClick: jsSingleClick,
					})[0].outerHTML;
					html += '<div class="portalTitle">'+a+'</div>';
				}
			}
			return html;
		},
		export_plan: function (e)
		{
			var fp_grid = window.plugin.linksFields.getDrawTools();
			var validity = this.check_plan(fp_grid);

            // Create a map of portal titles to an array of portal objects with that title
            const portalTitleMap = {};
            for (const guid in this.portalCache) {
                const portal = this.portalCache[guid];
                if (portal.title) {
                    if (!portalTitleMap[portal.title]) {
                        portalTitleMap[portal.title] = [];
                    }
                    portalTitleMap[portal.title].push(portal);
                }
            }

            const portalSuffixes = {}; // guid -> suffix

            for (const title in portalTitleMap) {
                const portals = portalTitleMap[title];
                if (portals.length > 1) {
                    // Calculate center point
                    let centerLat = 0;
                    let centerLng = 0;
                    for (const p of portals) {
                        centerLat += p.latE6 / 1000000;
                        centerLng += p.lngE6 / 1000000;
                    }
                    centerLat /= portals.length;
                    centerLng /= portals.length;
                    const centerPoint = { lat: centerLat, lng: centerLng };

                    for (const p of portals) {
                        const portalPoint = { lat: p.latE6 / 1000000, lng: p.lngE6 / 1000000 };
                        const distance = Math.round(this.haversine_distance(centerPoint, portalPoint));
                        const bearing = this.calculate_bearing(centerPoint, portalPoint);
                        const compass = this.bearing_to_compass(bearing);
                        portalSuffixes[p.guid] = ` [${compass} ${distance}m]`;
                    }
                }
            }


			var exp = "<pre>";
			exp += "===== KEYS REQUIRED =====\n";
			var keyreq={};
			var gl;
			for (gl of fp_grid) {
                if (gl.type == 'polyline') {
                    var loc = gl.latLngs;
                    //console.log("loc: " + JSON.stringify(loc));
                    var guid = window.plugin.linksFields.getPointGuid(loc[1]);
                    var title = window.plugin.linksFields.portalCache[guid].title;
                    if (portalSuffixes[guid]) {
                        title += portalSuffixes[guid];
                    }
                    keyreq[title]=0;
                }
			}
			for (var i = 0 ; i < fp_grid.length; i++) {
				if (validity[i] != 'exists')
				{
                    gl = fp_grid[i];
                    if (gl.type == 'polyline') {
                        loc = gl.latLngs;
                        //console.log("loc: " + JSON.stringify(loc));
                        guid = window.plugin.linksFields.getPointGuid(loc[1]);
                        title = window.plugin.linksFields.portalCache[guid].title;
                        if (portalSuffixes[guid]) {
                            title += portalSuffixes[guid];
                        }

                        keyreq[title]++;
                    }
			 	}
			}
			for (var pk in keyreq) { exp += pk + ": " + keyreq[pk] + "\n"; }

			exp += "===== LINK ORDER =====\n";
			var count =1;
			for (i = 0 ; i < fp_grid.length; i++) {
				if (validity[i] != 'exists')
				{
					var gd =fp_grid[i];
                    //console.log("GD: "+ JSON.stringify(gd));
					//var guid;
                    if (gd.type == 'polyline') {
                        guid = window.plugin.linksFields.getPointGuid(gd.latLngs[0]);
                        var stitle = window.plugin.linksFields.portalCache[guid].title;
                        if (portalSuffixes[guid]) {
                            stitle += portalSuffixes[guid];
                        }
                        guid = window.plugin.linksFields.getPointGuid(gd.latLngs[1]);
                        var dtitle = window.plugin.linksFields.portalCache[guid].title;
                        if (portalSuffixes[guid]) {
                            dtitle += portalSuffixes[guid];
                        }

                        var kcount;
                        if (stitle in keyreq)
                        {
                            kcount = keyreq[stitle];
                        } else {
                            kcount = 0;
                        }
                        exp += count +". " + "("+ kcount + ") " + stitle + " \u2192 " + dtitle + "\n";

                        count ++;
                    }
                    if (gd.type == 'marker') {
                        guid = window.plugin.linksFields.getPointGuid(gd.latLng);
                        var stitle = window.plugin.linksFields.portalCache[guid].title;
                        if (portalSuffixes[guid]) {
                            stitle += portalSuffixes[guid];
                        }
                        var kcount;
                        if (stitle in keyreq)
                        {
                            kcount = keyreq[stitle];
                        } else {
                            kcount = 0;
                        }
                        exp += count +". " + "("+ kcount + ") " + stitle + "\n";
                        count ++;
                    }
				}
			}
			exp += "</pre>\n";
			dialog({
				html: '<div id="export">' + exp + '</div>',
				dialogClass: 'ui-dialog-portal',
				title: 'Export Plan',
				id: 'portal-exportplan',
				width: 550
			});
		},
		checkCache: function(linkList) {
			const missing = [];
			// var uniq = {};
			for (const link of linkList)
			{
                if (link.type == 'polyline')
                {
                    const loc1 = link.latLngs[0];
                    const loc2 = link.latLngs[1];

                    if (!missing.some(ll => this.point_equal(ll, loc1))) {
                        if (this.getPointGuid(loc1) === null) {
                            missing.push(loc1);
                        }
                    }

                    if (!missing.some(ll => this.point_equal(ll, loc2))) {
                        if (this.getPointGuid(loc2) === null) {
                            missing.push(loc2);
                        }
                    }
                }
                if (link.type == 'marker')
                {
                    const loc1 = link.latLng;
                    if (!missing.some(ll => this.point_equal(ll, loc1))) {
                        if (this.getPointGuid(loc1) === null) {
                            missing.push(loc1);
                        }
                    }

                }

			}
			return missing;
		},
		runPlan: function() {
			var plan = window.plugin.linksFields.linkify(window.plugin.linksFields.getDrawTools());

			// check that all portals are cached.
			var unloaded = window.plugin.linksFields.checkCache(plan);
            var html;
			if (unloaded.length > 0)
			{
				html = window.plugin.linksFields.htmlUnloaded(unloaded);
				window.plugin.linksFields.unloadedDialog = dialog({
					html: '<div id="unloaded">' + html + '</div>',
					dialogClass: 'ui-dialog-portal',
					title: 'Portals Not Loaded.',
					id: 'portal-unloaded',
					width: 250
				});
			}
			else
			{
				// move existing links to the top of the plan
				plan = window.plugin.linksFields.sortExisting(plan);
				// sort out the blocked links...
				//plan = window.plugin.linksFields.rippleBlocked(plan);
				// re-save the modified plan
				window.plugin.drawTools.drawnItems.clearLayers();
				window.plugin.drawTools.import(plan);
				window.plugin.drawTools.save();

				html = window.plugin.linksFields.makeHtml(plan);
				dialog({
					html: '<div id="linkslist">' + html + '</div>',
					dialogClass: 'ui-dialog-portal',
					title: 'Plan',
					id: 'portal-runplan',
					width: 550
				});
			}
		},
		sortPlan: function() {
			var plan = window.plugin.linksFields.getDrawTools();
			// move existing links to the top of the plan
			plan = window.plugin.linksFields.sortExisting(plan);
			// sort out the blocked links...
			plan = window.plugin.linksFields.rippleBlocked(plan);
			// re-save the modified plan
			window.plugin.drawTools.drawnItems.clearLayers();
			window.plugin.drawTools.import(plan);
			window.plugin.drawTools.save();
			window.plugin.linksFields.updatePlan();
		},
		sortPlanMore: function() {
			var plan = window.plugin.linksFields.getDrawTools();
			// move existing links to the top of the plan
			plan = window.plugin.linksFields.sortExisting(plan);
			// sort out the blocked links...
			plan = window.plugin.linksFields.rippleMore(plan);
			// re-save the modified plan
			window.plugin.drawTools.drawnItems.clearLayers();
			window.plugin.drawTools.import(plan);
			window.plugin.drawTools.save();
			window.plugin.linksFields.updatePlan();
		},
        sortToTop: function()
        {
            //console.log(window.selectedPortal);
            if (window.selectedPortal == null)
            {
                return;
            }
            var portal = window.plugin.linksFields.portalCache[window.selectedPortal];
			var plan = window.plugin.linksFields.getDrawTools();
            plan = window.plugin.linksFields.sortPortalToTop(plan,portal);
			plan = window.plugin.linksFields.sortExisting(plan);
			window.plugin.drawTools.drawnItems.clearLayers();
			window.plugin.drawTools.import(plan);
			window.plugin.drawTools.save();
			window.plugin.linksFields.updatePlan();
            window.plugin.linksFields.updatePathLayer();

        },
        sortToBottom: function()
        {
            //console.log(window.selectedPortal);
            if (window.selectedPortal == null)
            {
                return;
            }
            var portal = window.plugin.linksFields.portalCache[window.selectedPortal];
			var plan = window.plugin.linksFields.getDrawTools();
            plan = window.plugin.linksFields.sortPortalToBottom(plan,portal);

			plan = window.plugin.linksFields.sortExisting(plan);
			window.plugin.drawTools.drawnItems.clearLayers();
			window.plugin.drawTools.import(plan);
			window.plugin.drawTools.save();
			window.plugin.linksFields.updatePlan();
            window.plugin.linksFields.updatePathLayer();

        },
		updatePlan: function() {
			var plan = window.plugin.linksFields.getDrawTools();
			// move existing links to the top of the plan
			//plan = window.plugin.linksFields.sortExisting(plan);
			// sort out the blocked links...
			//plan = window.plugin.linksFields.rippleBlocked(plan);
			// re-save the modified plan
			//window.plugin.drawTools.drawnItems.clearLayers();
			//window.plugin.drawTools.import(plan);
			//window.plugin.drawTools.save();
			var html = window.plugin.linksFields.makeHtml(plan);
			$('#linkslist').html(html);
		},
		getPlanPortals: function() {
			const linkList = window.plugin.linksFields.linkify(window.plugin.linksFields.getDrawTools());
			const portalGuids = {};
			for (const link of linkList)
			{
                if (link.type == 'polyline')
                {
                    const oGuid = window.plugin.linksFields.getPointGuid(link.latLngs[0]);
                    const dGuid = window.plugin.linksFields.getPointGuid(link.latLngs[1]);
                    // check that these portals exist
                    if (oGuid && !portalGuids[oGuid]) {
                        portalGuids[oGuid] = window.plugin.linksFields.portalCache[oGuid];
                    }
                    if (dGuid && !portalGuids[dGuid]) {
                        portalGuids[dGuid] = window.plugin.linksFields.portalCache[dGuid];
                    }
                }
			}
			return portalGuids;
		},
		getPointGuid: function(point) {
			const guid = Object.keys(window.plugin.linksFields.portalCache).find(key => {
				const cachedPortal = window.plugin.linksFields.portalCache[key];
				return cachedPortal && cachedPortal.latE6 === point.lat * 1000000 && cachedPortal.lngE6 === point.lng*1000000;
			});

			if (guid) {
				return guid;
			}

            for (const g in window.portals)
            {
                if (window.portals.hasOwnProperty(g))
                {
                    const portal = window.portals[g].options.data;
                    if (portal.latE6 == point.lat * 1000000 && portal.lngE6 == point.lng*1000000)
                    {
                        window.plugin.linksFields.portalCache[g]=portal;
                        return g;
                    }
                }
            }
			return null;
		},
		getPortalLink: function(guid) {

			if (guid === null)
            {
				return null;
            }
			var portal = window.plugin.linksFields.portalCache[guid];
		// how do we have a GUID but no portal?

			if (portal === undefined)
            {
				return null;
            }
			//console.log("PORTAL: " + JSON.stringify(portal));

			var lat = portal.latE6 / 1000000;
			var lng = portal.lngE6 / 1000000;

			var latlng = [lat,lng].join();
			var jsSingleClick = 'window.renderPortalDetails(\''+guid+'\');return false';
			var jsDoubleClick = 'window.zoomToAndShowPortal(\''+guid+'\', ['+latlng+']);return false';
			var perma = '/intel?ll='+lat+','+lng+'&z=17&pll='+lat+','+lng;
			if (!portal.title)
			{
				portal.title='undefined';
				jsSingleClick = 'window.renderPortalDetails(\''+guid+'\');window.plugin.linksFields.updatePlan();return false';
			}

			//Use Jquery to create the link, which escape characters in TITLE and ADDRESS of portal
			var a = $('<a>',{
				//"class": 'help',
				text: portal.title,
				//title: portal.address,
				href: perma,
				onClick: jsSingleClick,
				onDblClick: jsDoubleClick
			})[0].outerHTML;
			var div = '<div class="portalTitle">'+a+'</div>';
			return div;
		},
		edit: function(item,op) {
			var link_list = window.plugin.linksFields.linkify(window.plugin.linksFields.getDrawTools());
			var link;
			if (op == 'swap')
			{
				link = link_list[item];
				var newLatLngs = [link.latLngs[1],link.latLngs[0]];
				link.latLngs = newLatLngs;
			}
			if (op == 'up')
            {
				if (item > 0)
				{
					link = link_list[item];
					link_list[item] = link_list[item-1];
					link_list[item-1] = link;
				}
            }
			if (op == 'down')
            {
				if (item <link_list.length)
				{
					link = link_list[item];
					link_list[item] = link_list[item+1];
					link_list[item+1] = link;
				}
            }

			window.plugin.drawTools.drawnItems.clearLayers();
			window.plugin.drawTools.import(link_list);
			window.plugin.drawTools.save();

			window.plugin.linksFields.updatePlan();
			window.plugin.linksFields.updatePathLayer();
		//window.plugin.linksFields.updateBlockLayer();
		},
		// static method
		getBlockers: function(plan) {
			const keys = Object.keys(window.plugin.linksFields.edgeCache);
			const blockers = [];
			for (let l=0; l < plan.length; l++)
			{
                const block = { 'enl': [], 'res': [], 'neu': [] };

				const li = plan[l];
                if (li.type == 'polyline')
                {
                    for (let i=0; i< keys.length;i++)
                    {
                        const mapLink = window.plugin.linksFields.edgeCache[keys[i]];
                        const inLink = { latLngs: [ { 'lat': (mapLink.oLatE6 / 1000000), 'lng': (mapLink.oLngE6 / 1000000) } , { 'lat': (mapLink.dLatE6 / 1000000), 'lng': (mapLink.dLngE6 / 1000000) }] };
                        //console.log("intel link: " + JSON.stringify(inLink));
                        //console.log("plan link: " + JSON.stringify(li));
                        if (window.plugin.linksFields.intersect(inLink.latLngs,li.latLngs))
                        {
                            //console.log("INTERSECT: " + JSON.stringify(mapLink));
                            //console.log(mapLink);
                            if (mapLink.team == 'E') {
                                block.enl.push(inLink);
                            } else if (mapLink.team == 'R') {
                                block.res.push(inLink);
                            } else if (mapLink.team == 'M') {
                                // new for machina links
                                // some places they are refered to as neutral and some places machina
                                block.neu.push(inLink);
                            }
                        }
                    }
                }
                blockers.push(block);

			}
			return blockers;
		},
		linkInPlay: function(li) {
            // just like linkExists but for links that already created

            if (li.type == 'polyline') {
                const keys = Object.keys(window.plugin.linksFields.edgeCache);
                //console.log("link cache count: " + keys.length);
                for (const key of keys) {
                    const mapLink = window.plugin.linksFields.edgeCache[key];
                    // check that link is correct faction
                    if (mapLink.team === window.PLAYER.team.slice(0,1)) {
                        const inLink = { latLngs: [
                            { 'lat': (mapLink.oLatE6 / 1000000), 'lng': (mapLink.oLngE6 / 1000000) },
                            { 'lat': (mapLink.dLatE6 / 1000000), 'lng': (mapLink.dLngE6 / 1000000)}] };
                        //console.log("link: " + JSON.stringify(window.plugin.linksFields.edgeCache[keys[i]]));
                        if (window.plugin.linksFields.link_equal(inLink, li)) {
                            return true;
                        }
                    }
                }
            }
            return false;
		},
		tri_sign: function (p1,p2,p3)
		{
			return (p1.lng - p3.lng) * (p2.lat - p3.lat) - ( p2.lng - p3.lng ) * (p1.lat - p3.lat);
		},
		interior_tri : function (p,l1,l2,l3,corner)
		{

		// not spherical geometry safe.
			var tmp;
			if (corner) {
				if (window.plugin.linksFields.tri_sign(l1,l2,l3) > 0)
				{
					tmp = l1;
					l1 = l2;
					l2 = tmp;
				}
			} else {
				if (window.plugin.linksFields.tri_sign(l1,l2,l3) < 0)
				{
					tmp = l1;
					l1 = l2;
					l2 = tmp;
				}
			}

			var b1 = window.plugin.linksFields.tri_sign (p,l1,l2) <= 0;
			var b2 = window.plugin.linksFields.tri_sign (p,l2,l3) <= 0;
			var b3 = window.plugin.linksFields.tri_sign (p,l3,l1) <= 0;

			return ((b1 == b2) && (b2 == b3));
		},
		contains_portal : function(field,portals)
		{
			var contained_portal=[];

			//console.log("portals: " + JSON.stringify(portals));
			var portalkeys = Object.keys(portals);
			for (var pk of portalkeys)
			{
				var p = portals[pk];
				var point = {'lat': (p.latE6/1000000), 'lng': (p.lngE6/1000000)};
				//console.log("FIELD: " + JSON.stringify(field));
				//console.log("CONTAINS PORTAL: " + JSON.stringify(point));
				if (window.plugin.linksFields.interior_tri(point,field.latLngs[0],field.latLngs[1],field.latLngs[2],false))
                {
					contained_portal.push(point);
                }
			}
			return contained_portal;
		},
		point_equal : function(p1,p2)
		{
			return p1.lat == p2.lat && p1.lng == p2.lng;
		},
        haversine_distance : function (p1, p2)
        {
            const R = 6367e3; // Ingress Earth Radius in metres

            const lat1 = p1.lat * Math.PI / 180;
            const lon1 = p1.lng * Math.PI / 180;
            const lat2 = p2.lat * Math.PI / 180;
            const lon2 = p2.lng * Math.PI / 180;

            const dLat = lat2 - lat1;
            const dLon = lon2 - lon1;

            const a =
                  Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                  Math.cos(lat1) * Math.cos(lat2) *
                  Math.sin(dLon / 2) * Math.sin(dLon / 2);

            const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return R * c; // Distance in metres
        },
        calculate_bearing: function(p1, p2) {
            const lat1 = p1.lat * Math.PI / 180;
            const lon1 = p1.lng * Math.PI / 180;
            const lat2 = p2.lat * Math.PI / 180;
            const lon2 = p2.lng * Math.PI / 180;

            const y = Math.sin(lon2 - lon1) * Math.cos(lat2);
            const x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1);
            const brng = Math.atan2(y, x);
            return (brng * 180 / Math.PI + 360) % 360; // bearing in degrees
        },
        bearing_to_compass: function(bearing) {
            const directions = ['N', 'NE', 'E', 'SE', 'S', 'SW', 'W', 'NW'];
            const index = Math.floor((bearing + 22.5) / 45) % 8;
            return directions[index];
        },
		get_prevents : function(grid)
		{
            //console.log(grid);
			var fields = window.plugin.linksFields.fieldify(grid);
			var portals = window.plugin.linksFields.getPlanPortals();
			var field_dep = [];

			for (var fd of fields)
			{
                if (fd.type == 'polygon')
                {
                    var prevents=[];
                    var fdpt = window.plugin.linksFields.contains_portal(fd,portals);
                    //console.log("FDPT: " + JSON.stringify(fdpt));
                    for (var pt of fdpt)
                    {
                        for (var li of grid)
                        {
                            if (li.type == 'polyline')
                            {
                                if (window.plugin.linksFields.point_equal(pt,li.latLngs[0]) && window.plugin.linksFields.haversine_distance(pt,li.latLngs[1]) > 2000)
                                {
                                    prevents.push(li);
                                }
                            }
                        }
                    }
                    field_dep.push({"field":fd, "prevents": prevents});
                }
			}
			return field_dep;
		},
		get_links : function(order,pt)
		{
			var rt = [];
			for (var link of order)
            {
                if (link.type == 'polyline')
                {
                    if (window.plugin.linksFields.point_equal(link.latLngs[0],pt) || window.plugin.linksFields.point_equal(link.latLngs[1],pt))
                    {
                        rt.push(link);
                    }
                }
            }

			return rt;

		},
		common_point : function(l1,l2)
		{
			if (window.plugin.linksFields.point_equal(l1.latLngs[0],l2.latLngs[0]) || window.plugin.linksFields.point_equal(l1.latLngs[0],l2.latLngs[1]))
            {
				return l1.latLngs[0];
            }
			if (window.plugin.linksFields.point_equal(l1.latLngs[1],l2.latLngs[0]) || window.plugin.linksFields.point_equal(l1.latLngs[1],l2.latLngs[1]))
            {
				return l1.latLngs[1];
            }
			return null;
		},
		complete_field : function(order,link)
		{
            if (link.type == 'polyline')
            {
                const p1link = window.plugin.linksFields.get_links(order,link.latLngs[0]);
                const p2link = window.plugin.linksFields.get_links(order,link.latLngs[1]);

                const complete_fields = [];

                for (let l1 of p1link)
                {
                    for (let l2 of p2link) {
                        const pt = window.plugin.linksFields.common_point(l1,l2);
                        if (pt !== null)
                        {
                            complete_fields.push(pt);
                        }
                    }
                }

                return complete_fields;
            }
            return [];
		},
		// field2 inside field1?
		field_contains: function(f1,f2)
		{
			for (var pt of f2)
            {
				if (!window.plugin.linksFields.interior_tri(pt,f1[0],f1[1],f1[2],true))
                {
					return false;
                }
            }
			return true;
		},
		field_equal: function (f1,f2)
		{
			return (
				(window.plugin.linksFields.point_equal(f1[0],f2[0]) || window.plugin.linksFields.point_equal(f1[0],f2[1]) || window.plugin.linksFields.point_equal(f1[0],f2[2])) &&
				(window.plugin.linksFields.point_equal(f1[1],f2[0]) || window.plugin.linksFields.point_equal(f1[1],f2[1]) || window.plugin.linksFields.point_equal(f1[1],f2[2])) &&
				(window.plugin.linksFields.point_equal(f1[2],f2[0]) || window.plugin.linksFields.point_equal(f1[2],f2[1]) || window.plugin.linksFields.point_equal(f1[2],f2[2]))
			);
		},
		link_equal : function(l1,l2)
		{

            if ("latLngs" in l1 && "latLngs" in l2)
            {
                return (window.plugin.linksFields.point_equal(l1.latLngs[0],l2.latLngs[0]) && window.plugin.linksFields.point_equal(l1.latLngs[1],l2.latLngs[1])) || (window.plugin.linksFields.point_equal(l1.latLngs[1],l2.latLngs[0]) && window.plugin.linksFields.point_equal(l1.latLngs[0],l2.latLngs[1]));
            }
            return false;
		},
// check if field blocks any links not yet made.
// based on the dependencies "deps"
// and the order of links so far "order"

//needs update for short links (2km) that can now be thrown under fields.
		check_block : function(field, deps, order)
		{
			var blocked = [];
			for (var fd of deps)
			{
				//console.log("fd: " + JSON.stringify(fd));
				if (window.plugin.linksFields.field_equal(field,fd.field.latLngs))
				{
					for (var li of fd.prevents)
					{
						var bl = true;
						for (var l2 of order)
                        {
							if (window.plugin.linksFields.link_equal(l2,li))
                            {
								bl = false;
                            }
                        }
						if (bl)
                        {
							blocked.push(li);
                        }
					}
				}
			}
			return blocked;
		},
		link_str: function(link)
		{
			var oGuid = window.plugin.linksFields.getPointGuid(link.latLngs[0]);
			var dGuid = window.plugin.linksFields.getPointGuid(link.latLngs[1]);
			return window.plugin.linksFields.getPortalLink(oGuid) + " \u2192 " + window.plugin.linksFields.getPortalLink(dGuid);
		},
		check_plan : function (grid)
		{
            //console.log(grid);
			var order = [];
			var ll = grid.length;
			var field_prevents = window.plugin.linksFields.get_prevents(grid);
			var result = [];
			var i;

		//console.log("FIELD_PREVENTS: " + JSON.stringify(field_prevents));

			for (i=0; i< ll; i++)
			{
				var link = grid[i];
				var cf = window.plugin.linksFields.complete_field(order,link);
				var cc = cf.length;
                var newfield;
                var inner;
		//console.log("LINK: " + i + " COMPLETES FIELDS: " + JSON.stringify(cf));

				if (cc === 0) {
					result[i] = ["valid"];
				} else if (cc == 1 || cc == 2) {
					newfield = [];
					var same = false;
					newfield.push([ link.latLngs[0], link.latLngs[1], cf[0] ]);
					if (cc == 2)
					{
						newfield.push ( [ link.latLngs[0], link.latLngs[1], cf[1] ]);
						result[i] = ["complete"];
						if (window.plugin.linksFields.field_contains(newfield[0],newfield[1]) || window.plugin.linksFields.field_contains(newfield[1],newfield[0]))
						{
							//console.log("LINK: " + i + " COMPLETES FIELDS: " + JSON.stringify(cf));
							// this is a lot of work just to hunt down some fields.
							inner=[];
							if (window.plugin.linksFields.field_contains(newfield[0],newfield[1]))
							{
								inner = [
									{'type': 'polyline', latLngs: [link.latLngs[0],cf[1]]},
									{'type': 'polyline', latLngs: [link.latLngs[1],cf[1]]}
								];
							} else {
								inner = [
									{'type': 'polyline', latLngs: [link.latLngs[0],cf[0]]},
									{'type': 'polyline', latLngs: [link.latLngs[1],cf[0]]}
								];
							}
							// console.log("INNER LINKS: " + JSON.stringify(inner));
							result[i] = ['same',inner];
							same = true;

						}
					}

					if (!same) {
						var field_block = [];
						for (var fd of newfield)
						{
							var bb = window.plugin.linksFields.check_block(fd,field_prevents,order);
							field_block = field_block.concat(bb);
						}

						if (field_block.length === 0)
						{
							result[i] = ["complete"];
						}
						else
						{
							result[i] = ["blocks",field_block];
						}
					}

				} else {
					// may want to add inner links here too.
					// result[i] = ["more"];
					newfield = [];
					for (var pt of cf)
                    {
						newfield.push ( [ link.latLngs[0], link.latLngs[1], pt ]);
                    }
					// console.log("NEWFIELD: " + JSON.stringify(newfield));

					// all fields on one side
					// 1 field on one side
					// multiple fields on both sides
					inner = [];
					for (var f1 of newfield)
                    {
						for (var j=0; j < newfield.length; j++)
						{
							var f2 = newfield[j];
							if (!window.plugin.linksFields.field_equal(f1,f2))
                            {
								if (window.plugin.linksFields.field_contains(f1,f2))
                                {
									inner.push(	{'type': 'polyline', latLngs: [link.latLngs[0],cf[j]]});
									inner.push(	{'type': 'polyline', latLngs: [link.latLngs[1],cf[j]]});
								}
                            }

						}
                    }
					// three or more fields.
					result[i] = ["more",inner];

				}
				order.push(link);
			}

			//console.log("RESULT: " +JSON.stringify(result));

		// highlight blocked links
			for (i=0; i< result.length; i++)
            {
				if (result[i][0] == 'blocks')
				{
					for (var flink of result[i][1])
					{
						for (j=0; j<grid.length; j++)
                        {
							if (window.plugin.linksFields.link_equal(grid[j],flink))
                            {
								result[j][0] = 'blocked';
                            }
                        }
					}
				}
            }
		// highlight inner field for links which make more than 2 fields
		//console.log("HIGHLIGHT INNER");
            for (i=0; i< result.length; i++)
            {
                if (result[i][0] == 'same' || result[i][0] == 'more')
                {
                    for (flink of result[i][1])
                    {
                        for (j=0; j<grid.length; j++)
                        {
                            if (window.plugin.linksFields.link_equal(grid[j],flink))
                            {
                                result[j][0] = 'inner'; // if not existing
                            }
                        }
                    }
                }
            }

		// highlight existing links
			for (i=0; i< ll; i++)
			{
				link = grid[i];
				if (window.plugin.linksFields.linkInPlay(link)) {
					result[i][0] = 'exists';
				}
			}
			return result;

		},
		// crappy shit shit euclidian intersection. Sorry guys this won't work for those long curvy links
		// TODO: write a proper intersection check.
		intersect: function(l0,l1)
		{
			var p0 = l0[0];
			var p1 = l0[1];
			var p2 = l1[0];
			var p3 = l1[1];

			var s1 = {lat: 0, lng: 0};
			var s2 = {lat: 0, lng: 0};
			s1.lat = p1.lat - p0.lat;
			s1.lng = p1.lng - p0.lng;

			s2.lat = p3.lat - p2.lat;
			s2.lng = p3.lng - p2.lng;

			var base = (( -s2.lng) * s1.lat + s1.lng * s2.lat);


			if (base === 0)
            {
				return false;
            }

			var s = ((-s1.lat) * (p0.lng - p2.lng) + s1.lng * (p0.lat - p2.lat)) / base;
			var t = ( s2.lng * (p0.lat - p2.lat) - s2.lat * (p0.lng - p2.lng)) / base;

			var roundingNumber = 1000000;

			s = Math.round(s * roundingNumber) / roundingNumber;
			t = Math.round(t * roundingNumber) / roundingNumber;

			if (s>0 && s<1 && t>0 && t<1)
            {
				return true;
            }

			return false;
		},
		getDrawTools: function() {
			var dataStr = localStorage['plugin-draw-tools-layer'];
			if (dataStr === undefined) return;

			var data = JSON.parse(dataStr);
			return data;
		},
		fieldExists: function(fa,fi) {
			if (fa.length===0)
            {
				return false;
            }
			for (var l=0; l< fa.length;l++) {
				if (this.field_equal(fi.latLngs,fa[l].latLngs))
                {
					return true;
                }
			}
			return false;
		},
		// replaced by field_equal
//		compareField: function(f1,f2) {
//          return (
//                (this.compareLoc(f1.latLngs[0],f2.latLngs[0]) || this.compareLoc(f1.latLngs[0],f2.latLngs[1]) || this.compareLoc(f1.latLngs[0],f2.latLngs[2])) &&
//                (this.compareLoc(f1.latLngs[1],f2.latLngs[0]) || this.compareLoc(f1.latLngs[1],f2.latLngs[1]) || this.compareLoc(f1.latLngs[1],f2.latLngs[2])) &&
//                (this.compareLoc(f1.latLngs[2],f2.latLngs[0]) || this.compareLoc(f1.latLngs[2],f2.latLngs[1]) || this.compareLoc(f1.latLngs[2],f2.latLngs[2]))
//		    );
//		},
		linkExists: function(la,li) {
			if (la.length===0)
            {
				return false;
            }
			for (var l=0; l< la.length;l++)
            {
				if (this.link_equal(li,la[l]))
                {
					return true;
                }
            }
			return false;
		},
//		compareLink: function(l1,l2) {
//            //     console.log(">>> compareLink: " + JSON.stringify([l1.latLngs,l2.latLngs]) );
//            return ( this.compareLoc(l1.latLngs[0],l2.latLngs[0]) &&  this.compareLoc(l1.latLngs[1],l2.latLngs[1])) ||
//                (this.compareLoc(l1.latLngs[0],l2.latLngs[1]) &&  this.compareLoc(l1.latLngs[1],l2.latLngs[0]));
//		},
		// replaced by point_equal
//		compareLoc: function(loc1,loc2) {
//			return loc1.lat == loc2.lat && loc1.lng == loc2.lng;
//		},
		fieldify: function(drawtools) {
			var ff = [];
			for (var l1 =0; l1< drawtools.length; l1++) {
				var dto1 = drawtools[l1];
				var l3=0;
				var dto3={};
				if (dto1.type == 'polyline') {
					for (var l2=0; l2 <drawtools.length; l2++) {

						var dto2 = drawtools[l2];
						var nff={};
						if (dto2.type == 'polyline') {
							if (this.point_equal(dto1.latLngs[0], dto2.latLngs[0])) {
								for ( l3=0; l3 <drawtools.length; l3++) {
									//console.log("[" + l1 + ","+ l2 + "," + l3 + "]");
									dto3 = drawtools[l3];
									if (dto3.type == 'polyline') {
										if ((this.point_equal(dto1.latLngs[1],dto3.latLngs[0]) && this.point_equal(dto2.latLngs[1], dto3.latLngs[1])) || (this.point_equal(dto1.latLngs[1] , dto3.latLngs[1]) && this.point_equal(dto2.latLngs[1], dto3.latLngs[0])) ) {
											nff = {type: "polygon", color: dto1.color, latLngs:[ dto1.latLngs[0], dto1.latLngs[1], dto2.latLngs[1] ]};
											if (!this.fieldExists(ff,nff))
                                            {
												ff.push(nff);
                                            }
										}
									}
								}
							}
							if (this.point_equal(dto1.latLngs[0], dto2.latLngs[1])) {

								for ( l3=0; l3 <drawtools.length; l3++) {
									// console.log("[" + l1 + ","+ l2 + "," + l3 + "]");

									dto3 = drawtools[l3];
									if (dto3.type == 'polyline') {

										if ((this.point_equal(dto1.latLngs[1],dto3.latLngs[0]) && this.point_equal(dto2.latLngs[0], dto3.latLngs[1])) || (this.point_equal(dto1.latLngs[1], dto3.latLngs[1]) && this.point_equal(dto2.latLngs[0] , dto3.latLngs[0])) ) {
											nff = {type: "polygon", color: dto1.color, latLngs:[ dto1.latLngs[0], dto1.latLngs[1], dto2.latLngs[0] ]};
											if (!this.fieldExists(ff,nff))
                                            {
												ff.push(nff);
                                            }
										}
									}
								}
							}
						}
					}
				}
			}

			return ff;
		},
		linkify: function(drawtools) {
			var ll = [];
			for (var dt =0; dt< drawtools.length; dt++) {
				var dto = drawtools[dt];
				var pt= 0;
				var dtn;
                var ptl;
				if (dto.type == 'polygon') {
                    ptl = dto.latLngs.length;

					for (pt=1; pt<ptl; pt++) {
						dtn = {type: 'polyline', latLngs: [{'lat': dto.latLngs[pt-1].lat,'lng': dto.latLngs[pt-1].lng},{'lat': dto.latLngs[pt].lat,'lng': dto.latLngs[pt].lng}], color: dto.color};
						if (!this.linkExists(ll,dtn))
                        {
							ll.push(dtn);
                        }
					}

					dtn = {type: 'polyline', latLngs: [{'lat': dto.latLngs[ptl-1].lat,'lng': dto.latLngs[ptl-1].lng},{'lat': dto.latLngs[0].lat,'lng': dto.latLngs[0].lng}], color: dto.color};
					if (!this.linkExists(ll,dtn))
                    {
						ll.push(dtn);
                    }
				}
				else if (dto.type == 'polyline') {
                    ptl = dto.latLngs.length;

					for (pt=1; pt<ptl; pt++) {
						dtn = {type: 'polyline', latLngs: [{'lat': dto.latLngs[pt-1].lat,'lng': dto.latLngs[pt-1].lng},{'lat': dto.latLngs[pt].lat,'lng': dto.latLngs[pt].lng}], color: dto.color};
						if (!this.linkExists(ll,dtn))
                        {
							ll.push(dtn);
                        }
					}
                } else if (dto.type == 'marker') {
                    ll.push(dto);
				} else {
					// don't handle these objects
					ll[JSON.stringify(dto)]=true;
				}
			}

			return ll;
		},
		toLinks: function() {
			//console.log(">>> toLinks");
			var data = this.getDrawTools();
			window.plugin.drawTools.drawnItems.clearLayers();
			var ll = this.linkify(data);
			//console.log("links: " + ll.length);

			window.plugin.drawTools.import(ll);
			window.plugin.drawTools.save();
		},
		toFields: function() {
			//console.log(">>> toFields");

			var data = this.getDrawTools();
			window.plugin.drawTools.drawnItems.clearLayers();
			var ll = this.linkify(data);
			//console.log("Pre links: " + ll.length);
			var ff = this.fieldify(ll);
			//console.log("Fields: " + ff.length);
			//console.log(ff.length);
			window.plugin.drawTools.import(ff);
			window.plugin.drawTools.save();

		}

	};

	var setup =  window.plugin.linksFields.setup;

	// PLUGIN END //////////////////////////////////////////////////////////

	setup.info = plugin_info; //add the script info data to the function as a property
	if(!window.bootPlugins) window.bootPlugins = [];
	window.bootPlugins.push(setup);
	// if IITC has already booted, immediately run the 'setup' function
	if(window.iitcLoaded && typeof setup === 'function') setup();
} // wrapper end
// inject code into site context
var script = document.createElement('script');
var info = {};
if (typeof GM_info !== 'undefined' && GM_info && GM_info.script) info.script = { version: GM_info.script.version, name: GM_info.script.name, description: GM_info.script.description };
script.appendChild(document.createTextNode('('+ wrapper +')('+JSON.stringify(info)+');'));
(document.body || document.head || document.documentElement).appendChild(script);

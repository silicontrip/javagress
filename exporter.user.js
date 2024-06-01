// ==UserScript==
// @id             exporter@user
// @name           IITC exporter
// @category       Misc
// @version        0.0.12
// @namespace      https://github.com/jonatkins/ingress-intel-total-conversion
// @description    portals and links exporter
// @include        https://www.ingress.com/intel*
// @include        http://www.ingress.com/intel*
// @match          https://www.ingress.com/intel*
// @match          http://www.ingress.com/intel*
// @include        https://intel.ingress.com/*
// @include        http://intel.ingress.com/*
// @match          https://intel.ingress.com/*
// @match          http://intel.ingress.com/*
// @grant          none
// ==/UserScript==

function wrapper(plugin_info)
{
    // ensure plugin framework is there, even if iitc is not yet loaded
    if(typeof window.plugin !== 'function') window.plugin = function() {};

    //PLUGIN AUTHORS: writing a plugin outside of the IITC build environment? if so, delete these lines!!
    //(leaving them in place might break the 'About IITC' page or break update checks)
    plugin_info.buildName = 'exporter.0.0.12';
    plugin_info.dateTimeVersion = '0001';
    plugin_info.pluginId = '';
    //END PLUGIN AUTHORS NOTE

	window.plugin.exporter = function() {};

	window.plugin.exporter.setup = function()
	{
		console.log('exporter - setup');
		$('#toolbox').append('<a onclick="window.plugin.exporter.export();return false;" >EXPORT</a>');
	};
	window.plugin.exporter.export = function() {
	var allPortals = {};
	var allLinks = [];
	var guid = "";
	for (guid in window.portals) {
		if (window.portals.hasOwnProperty(guid))
		{
			if (window.portals[guid].options.data.title !== undefined) {
				var opt =  window.portals[guid].options.data;
                //console.log(opt);
				allPortals[guid] =  { guid: opt.guid,  // is the same as guid
					title: opt.title,
					health: opt.health,
					team: opt.team,
					level: opt.level,
					lat: opt.latE6,
					lng: opt.lngE6
				};
			}
		}
	}
	for (guid in window.links)
		if (window.links.hasOwnProperty(guid))
        {
             opt = window.links[guid].options.data;
			allLinks.push( { guid: opt.oGuid,
                            dguid: opt.dGuid,
                            dlat: opt.dLatE6,
                            dlng: opt.dLngE6,
                            olat: opt.oLatE6,
                            olng: opt.oLngE6,
                            team: opt.team
            });
        }

	dialog({
		html: '<p><a onclick="$(\'.ui-dialog-portalSet-copy textarea\').select();">Select all</a> and press CTRL+C to copy it.</p><textarea readonly onclick="$(\'.ui-dialog-portalSet-copy textarea\').select();" >'+JSON.stringify(allPortals)+'</textarea><p><a onclick="$(\'.ui-dialog-linkSet-copy textarea\').select();">Select all</a> and press CTRL+C to copy it.</p><textarea readonly onclick="$(\'.ui-dialog-linkSet-copy textarea\').select();" >'+JSON.stringify(allLinks)+'</textarea> ',
		width: 600,
		dialogClass: 'ui-dialog-export-copy',
		title: 'Export'
        });
    }



    var setup = plugin.exporter.setup;

    setup.info = plugin_info; //add the script info data to the function as a property
    if(!window.bootPlugins) window.bootPlugins = [];
    window.bootPlugins.push(setup);
    // if IITC has already booted, immediately run the 'setup' function
    if(window.iitcLoaded && typeof setup === 'function') setup();
}

// inject code into site context
var script = document.createElement('script');
var info = {};
if (typeof GM_info !== 'undefined' && GM_info && GM_info.script) info.script = { version: GM_info.script.version, name: GM_info.script.name, description: GM_info.script.description };
script.appendChild(document.createTextNode('('+ wrapper +')('+JSON.stringify(info)+');'));
(document.body || document.head || document.documentElement).appendChild(script);


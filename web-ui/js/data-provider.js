var myModule = angular.module('dataProvider', ['utils']);
myModule.factory('dataProvider', function($q, $http, utils) {		

	// helpers
	var constructSelectQuery = function(type) {
		return [
			"SELECT ?meter",
			"WHERE {",
			"	?meter <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + type,
			"}"
		].join('\n');
	};
	var sparqlToHumanType = function(type) {
		for (var key in CONFIG.SPARQL.types) {
			if (CONFIG.SPARQL.types[key] === type) {
				return key;
			}
		}
		return null;
	};

	// Event Emitter Pattern support
	var events = [];
	var instance = {
		on: function(event, handler) {
			var that = this;
			event.split(' ').forEach(function(e) {
				if(!events[e]) {
					events[e] = [];
				}
				events[e].push(handler);
			});
		},

		off: function(event, handler) {
			var that = this;
			if(events[event]) {
				if (!handler) {
					events[event] = null;
				} else {
					var index = events[event].indexOf(handler);
					if (index > -1) {
						events[event].splice(index, 1);
					}
				}
			} 
		},

		trigger: function(event, data) {
			var that = this;
			if(events[event]) {
				events[event].forEach(function(handler) {
					handler(data);
				});
			}
		}
	};

	// data storage
	instance.meters = {
		heat: [],
		electric: []
	};

	// SPARQL Endpoint support
	instance.getHeatMeters = function() {
		return $http.post(CONFIG.URLS.tripleStore, constructSelectQuery(CONFIG.SPARQL.types.heat)).success(function(data) {
			instance.meters.heat = data;
			instance.trigger("heatMetersUpdate", instance.meters.heat);	
        });
	};
	instance.getElectricMeters = function() {
		return $http.post(CONFIG.URLS.tripleStore, constructSelectQuery(CONFIG.SPARQL.types.electric)).success(function(data) {
			instance.meters.electric = data;
			instance.trigger("electricMetersUpdate", instance.meters.electric);	
        });
	};

	// WAMP support
	instance.onMessage = function(args) {
		utils.parse(args[0]).then(function(result) {
			var type = sparqlToHumanType(result.object);
			if (type) {
				instance.meters[type].push({
					uri: result.subject
				});	
				instance.trigger(type + "MetersUpdate", instance.meters[type]);			
			} else {
				console.warn("Unknown sensor type: ", type);
			}
		});
    };
    var connection = new autobahn.Connection({
		url: CONFIG.URLS.messageBus,
		realm: 'realm1'
	});
	connection.onopen = function (session) {
		session.subscribe(CONFIG.TOPICS.device_registered, instance.onMessage);
	};
	connection.open();	

	return instance;

});
var myModule = angular.module('dataProvider', ['utils']);
myModule.factory('dataProvider', function($q, $http, utils) {		

	// helpers
	var constructSelectQuery = function(types) {
		var _types = types.map(function(type) {
			return "{ ?meter <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + type + "> }";
		}).join(" UNION ");

		return [
			"SELECT ?meter ?type",
			"WHERE {",
				_types,
				"?meter <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type",
			"}"
		].join('\n');
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
	instance.meters = [];

	// SPARQL Endpoint support
	/*
	instance.getHeatMeters = function() {
		var config = {
			params: {
				query: constructSelectQuery(CONFIG.SPARQL.types.heat)
			},
			headers: { Accept: "application/sparql-results+json" }
		};
		return $http.get(CONFIG.URLS.tripleStore, config).success(function(data) {
			instance.meters.heat = data.results.bindings.map(function(binding) {
				return {
					uri: binding.meter.value
				};
			});
			instance.trigger("heatMetersUpdate", instance.meters.heat);	
        });
	};
	instance.getElectricMeters = function() {
		var config = {
			params: {
				query: constructSelectQuery(CONFIG.SPARQL.types.electric)
			},
			headers: { Accept: "application/sparql-results+json" }
		};
		return $http.get(CONFIG.URLS.tripleStore, config).success(function(data) {
			instance.meters.electric = data.results.bindings.map(function(binding) {
				return {
					uri: binding.meter.value
				};
			});
			instance.trigger("electricMetersUpdate", instance.meters.electric);	
        });
	};
	*/
	instance.getMeters = function() {
		var config = {
			params: {
				query: constructSelectQuery([
					CONFIG.SPARQL.types.heat,
					CONFIG.SPARQL.types.electric
				])
			},
			headers: { Accept: "application/sparql-results+json" }
		};
		return $http.get(CONFIG.URLS.tripleStore, config).success(function(data) {
			instance.meters = data.results.bindings.map(function(binding) {
				return {
					uri: binding.meter.value,
					type: utils.sparqlToHumanType(binding.type.value)
				};
			});
			instance.trigger("metersUpdate", instance.meters);	
        });
	};

	// WAMP support
	instance.onMessage = function(args) {
		utils.parse(args[0]).then(function(result) {
			var type = utils.sparqlToHumanType(result.object);
			if (type) {
				instance.meters.push({
					uri: result.subject
				});	
				instance.trigger("metersUpdate", instance.meters);			
			} else {
				console.warn("Unknown sensor type: ", result.object);
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

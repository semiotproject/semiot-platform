var myModule = angular.module('dataProvider', ['commonUtils', 'rdfUtils']);
myModule.factory('dataProvider', function($q, $http, $interval, commonUtils, rdfUtils) {		

	// helpers
	var getPrefixes = function() {
		var str = [];
		for (var key in CONFIG.SPARQL.prefixes) {
			str.push("PREFIX " + key + ": " + CONFIG.SPARQL.prefixes[key]);
		}
		return str.join('\n') + '\n';
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
	instance.systems = [];

	// SPARQL Endpoint support
	instance.executeQuery = function(query, callback) {
		var config = {
			params: {
				query: getPrefixes() + query
			},
			headers: { Accept: "application/sparql-results+json" }
		};
		return $http.get(CONFIG.URLS.tripleStore, config).success(callback);
	};
	instance.fetchSystemEndpoint = function(uri, callback) {
		return this.executeQuery(CONFIG.SPARQL.queries.getSystemEndpoint.format(uri), callback);
	};
	instance.fetchSystems = function() {
		return this.executeQuery(CONFIG.SPARQL.queries.getAllSystems, function(data) {
			instance.systems = data.results.bindings.map(function(binding) {
				return {
					name: binding.label.value,
					uri: binding.uri.value
				};
			});
			instance.trigger("systemsUpdate", instance.systems);	
        });
	};

	// TSDB support
	instance.fetchArchiveTestimonials = function(uri, from, till) {
		return $http.get(CONFIG.URLS.tsdb, {});
	}

	instance.getSystems = function() {
		return this.systems;
	}

	// WAMP support
	instance.onMessage = function(args) {
		rdfUtils.parseTTL(args[0]).then(function(triples) {
			var resource = rdfUtils.parseTriples(triples);
			if (!instance.systems.find(function(system) { // if system is new
				return system.uri === resource.uri;
			})) {
				instance.systems.push({
					uri: resource.uri,
					name: resource.get("http://www.w3.org/2000/01/rdf-schema#label")
				});	
				instance.trigger("systemsUpdate", instance.systems);					
			}
		});
    };

	commonUtils.subscribe(
		CONFIG.URLS.messageBus,
		CONFIG.TOPICS.device_registered,
		instance.onMessage
	);
	
	return instance;

});

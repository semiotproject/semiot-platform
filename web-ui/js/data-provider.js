var myModule = angular.module('dataProvider', ['utils']);
myModule.factory('dataProvider', function($q, $http, utils) {		

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
					type: utils.sparqlToHumanType(binding.type.value),
					uri: binding.uri.value
				};
			});
			instance.trigger("systemsUpdate", instance.systems);	
        });
	};
	instance.getSystems = function() {
		return this.systems;
	}

	// WAMP support
	instance.onMessage = function(args) {
		utils.parse(args[0]).then(function(result) {
			console.log(arguments);
			var type = utils.sparqlToHumanType(result.object);
			if (type) {
				instance.systems.push({
					uri: result.subject,
					type: type
				});	
				instance.trigger("systemsUpdate", instance.systems);			
			} else {
				console.warn("Unknown sensor type: ", result.object);
			}
		});
    };

	utils.subscribe(
		CONFIG.URLS.messageBus,
		CONFIG.TOPICS.device_registered,
		instance.onMessage
	);

	var str = [
		"@prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> .", 
		"@prefix hmtr: <http://purl.org/NET/ssnext/heatsystems#> .", 
		"@prefix ssncom: <http://purl.org/NET/ssnext/communication#> .", 

		"<coap://localhost:3131> a hmtr:HeatMeter ;", 
		"    ssn:hasSubsystem <coap://localhost:3131/temperature> ;", 
		"    ssn:hasSubsystem <coap://localhost:3131/heat> .", 

		"<coap://localhost:3131/temperature> a ssn:Sensor ;", 
		"   ssn:observes hmtr:Temperature ;", 
		"   ssncom:hasCommunicationEndpoint <coap://localhost:3131/temperature/obs> ;", 
		"    ssncom:hasCommunicationEndpoint <ws://localhost/ws?topic=coap://localhost:3131/temperature/obs> .", 

		"<coap://localhost:3131/heat> a ssn:Sensor ;", 
		"    ssn:observes hmtr:Heat ;", 
		"    ssncom:hasCommunicationEndpoint <coap://localhost:3131/heat/obs> ;", 
		"    ssncom:hasCommunicationEndpoint <ws://localhost/ws?topic=coap://localhost:3131/heat/obs>.", 

		"<coap://localhost:3131/temperature/obs> a ssncom:CommunicationEndpoint ;", 
		"    ssncom:protocol 'COAP' .", 
		"<coap://localhost:3131/heat/obs> a ssncom:CommunicationEndpoint ;", 
		"    ssncom:protocol 'COAP' .", 
		"<ws://localhost/ws?topic=coap://localhost:3131/temperature/obs> a ssncom:CommunicationEndpoint ;", 
		"    ssncom:protocol 'WAMP' .", 
		"<ws://localhost/ws?topic=coap://localhost:3131/heat/obs> a ssncom:CommunicationEndpoint ;", 
		"    ssncom:protocol 'WAMP' ." 
	].join('\n');
	//instance.onMessage([str]);

	return instance;

});

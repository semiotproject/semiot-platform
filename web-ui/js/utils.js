var myModule = angular.module('utils', []);
myModule.factory('utils', function($q) {

	var parser = N3.Parser();
	var instance = {
		parse: function(str) {
			var defer = $q.defer();

			var triples = [];

			parser.parse(str, function(error, triple, prefixes) {
				if (triple) {
					triples.push(triple);
				} else {
					defer.resolve(triples);
				}
			});	

			return defer.promise;
		},
	    sparqlToHumanType: function(type) {
			for (var key in CONFIG.SPARQL.types) {
				if (CONFIG.SPARQL.types[key] === type) {
					return key;
				}
			}
			return null;
		},
		getChartConfig: function(type, data) {
			return {
				options: {
					chart: {
						type: 'spline'
					},
				},
				series: [
					{
						name: type,
						data: data
					}
				],
				title: {
					text: type
				},
				xAxis: {
					type: 'time',
					tickPixelInterval: 150
				},
				yAxis: {
					title: {
						text: type
					}
				}
			}
		},
		parseTopicFromEndpoint: function(endpoint) {
			var prefix = "ws://wamprouter/ws?topic=";
			return endpoint.substr(prefix.length);
		},
		subscribe: function(url, topic, callback) {
			var connection = new autobahn.Connection({
				url: url,
				realm: 'realm1'
			});
			connection.onopen = function (session) {
				session.subscribe(topic, callback);
			};
			connection.open();
			return connection;	
		} 
	};

	return instance;
});
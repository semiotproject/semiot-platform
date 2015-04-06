var myModule = angular.module('commonUtils', []);
myModule.factory('commonUtils', function($q) {

	var instance = {
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
						pointStart: (new Date()).getTime(),
						name: type,
						data: data
					}
				],
				title: {
					text: type
				},
				xAxis: {
					type: 'datetime',
					dateTimeLabelFormats: { // don't display the dummy year
						month: '%e. %b',
						year: '%b'
					},
				},
				yAxis: {
					title: {
						text: type
					}
				},
		        tooltip: {
                    formatter: function() {
	                    return  '<b>' + this.series.name +'</b><br/>' +
	                        Highcharts.dateFormat('%e - %b - %Y',
	                                              new Date(this.x));
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
		},
		getMockHeatObservation: function() {
			return  [
				"@prefix hmtr: <http://purl.org/NET/ssnext/heatmeters#> .",
				"@prefix meter: <http://purl.org/NET/ssnext/meters/core#> .",
				"@prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> .",
				"@prefix xsd: <http://www.example.org/> .",
				"@prefix : <coap://fdsfs:6500/meter/heat#> .",

				":54543543534 a hmtr:HeatObservation ;",
				"    ssn:observationResultTime \"3433\"^^xsd:dateTime ;",
				"    ssn:observedBy <coap://fdsfs:6500/meter> ;",
				"    ssn:observationResult :54543543534-result .",

				":54543543534-result a hmtr:HeatSensorOutput ;",
				"    ssn:isProducedBy <coap://fdsfs:6500/meter> ;",
				"    ssn:hasValue :54543543534-resultvalue .",

				":54543543534-resultvalue a hmtr:HeatValue ;",
				"    meter:hasQuantityValue \"{0}\"^^xsd:float .".format(Math.ceil(Math.random() * 50))
			].join('\n');
		},
		getMockNewSystem: function() {
			return [								
				"@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .",
				"@prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> .",
				"@prefix hmtr: <http://purl.org/NET/ssnext/heatmeters#> .",
				"@prefix ssncom: <http://purl.org/NET/ssnext/communication#> .",

				"<coap://10.1.1.2:545/meter> a hmtr:HeatMeter ;",
				"    rdfs:label \"Heat Meter #6666\" ;",
				"    ssn:hasSubsystem <coap://10.1.1.2:545/meter/temperature> ;",
				"    ssn:hasSubsystem <coap://10.1.1.2:545/meter/heat> .",

				"<coap://10.1.1.2:545/meter/temperature> a ssn:Sensor ;",
				"    ssn:observes hmtr:Temperature ;",
				"    ssncom:hasCommunicationEndpoint <coap://10.1.1.2:545/meter/temperature/obs> .",

				"<coap://10.1.1.2:545/meter/heat> a ssn:Sensor ;",
				"    ssn:observes hmtr:Heat ;",
				"    ssncom:hasCommunicationEndpoint <coap://10.1.1.2:545/meter/heat/obs> .",

				"<coap://10.1.1.2:545/meter/temperature/obs> a ssncom:CommunicationEndpoint ;",
				"    ssncom:protocol \"COAP\" .",
				"<coap://10.1.1.2:545/meter/heat/obs> a ssncom:CommunicationEndpoint ;",
				"    ssncom:protocol \"COAP\" ." 
			].join('\n');
		} 
	};

	return instance;
});
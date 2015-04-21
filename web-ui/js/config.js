"use strict";

var CONFIG = (function() {
	return {
		URLS: {
			messageBus: "ws://machine3-ailab.tk/ws",
			tripleStore: "http://machine3-ailab.tk:3030/ds/query",
			tsdb: "http://machine2-ailab.tk:4242/api/query?start={0}&end={1}&m=sum:{2}"
		},
		TOPICS: {
			"device_registered": 'ru.semiot.devices.newandobserving',
			"device_remove": 'ru.semiot.devices.remove'
		},
		SPARQL: {
			prefixes: {
				rdfs: '<http://www.w3.org/2000/01/rdf-schema#>',
				ssn: '<http://purl.oclc.org/NET/ssnx/ssn#>',
				rdf: '<http://www.w3.org/1999/02/22-rdf-syntax-ns#>', 
				ssncom: '<http://purl.org/NET/ssnext/communication#>'
			},
			types: {
				heatSystem: "http://purl.org/NET/ssnext/heatmeters#HeatMeter",
				electricSystem: "http://purl.org/NET/ssnext/electricmeters#ElectricMeter",
				temperature: "http://purl.org/NET/ssnext/heatmeters#Temperature",
				heat: "http://purl.org/NET/ssnext/heatmeters#Heat",
				observationResult: "http://purl.org/NET/ssnext/meters/core#hasQuantityValue"
			},
			queries: {
				getAllSystems: [
					"SELECT ?label ?uri",
					"WHERE {",
					"	?uri a ssn:System ;",
					"		a ?type .",
					"		?uri rdfs:label ?label .",
					"	FILTER NOT EXISTS {",
					"		?subClass rdfs:subClassOf ?type .",
					"		?uri a ?subClass .",
					"		FILTER (?subClass != ?type)",
					"	}",
					"}"
				].join('\n'),
				getSystemEndpoint: [
					"SELECT ?endpoint ?type {",
					"	<{0}> ssn:hasSubsystem ?subsystem .",
					"	?subsystem ssn:observes ?type .",
					"	?subsystem ssncom:hasCommunicationEndpoint ?endpoint .",
					"	?endpoint ssncom:protocol 'WAMP' .",
					"}"
				].join('\n')
			}
		}
	};
}());
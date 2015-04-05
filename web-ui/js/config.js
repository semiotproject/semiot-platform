"use strict";

var CONFIG = (function() {
	return {
		URLS: {
			messageBus: "ws://machine3-ailab.tk/ws",
			tripleStore: "http://machine3-ailab.tk:3030/ds/query"
		},
		TOPICS: {
			"device_registered": 'ru.semiot.devices.register'
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
				heat: "http://purl.org/NET/ssnext/heatmeters#Heat"
			},
			queries: {
				getAllSystems: [
					"SELECT ?label ?type ?uri",
					"WHERE {",
					"	?uri a ssn:System ;",
					"		a ?type .",
					"		?uri rdfs:label ?label .",
					"	FILTER NOT EXISTS {",
					"		?subClass rdfs:subClassOf ?type .",
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
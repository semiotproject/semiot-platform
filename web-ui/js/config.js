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
			types: {
				heat: "http://purl.org/NET/ssnext/heatmeters#HeatMeter",
				electric: "http://purl.org/NET/ssnext/electricmeters#ElectricMeter"
			}
		}
	};
}());
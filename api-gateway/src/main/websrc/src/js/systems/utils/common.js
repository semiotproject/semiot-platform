"use strict";

export default ["$q", "CONFIG", function($q, CONFIG) {

    let counter = 666;

    let instance = {
        ensureArray: function(data) {
            if (!data) {
                return [];
            }
            if (!Array.isArray(data)) {
                return [data];
            }
            return data;
        },
        normalizeTSDBData: function(type, result) {
            let data = [];
            if (result.data[0]) {
                let dps = result.data[0].dps;
                let localTime = (new Date()).getTime() + CONFIG.TIMEZONE_OFFSET;
                for (let timestamp in dps) {
                    if (timestamp * 1000 < localTime) {
                        data.push([timestamp * 1000 + CONFIG.TIMEZONE_OFFSET, dps[timestamp]]);
                    }
                }
            }
            return data;
        },
        parseTopicFromEndpoint: function(endpoint) {
            let prefix = "ws://wamprouter/ws?topic=";
            return endpoint.substr(prefix.length);
        },
        parseMetricFromType(sensorType) {
            return encodeURIComponent(sensorType).split("%").join("/");
        },
        getMockMachineToolStateObservation: function() {
            const TIMESTAMP = "43534654";
            const DATETIME = "fgdfgfdgfd";

            const STATES = [
                "IsTurnedOff",
                "IsInExecutionOfTask",
                "IsOutOfMaterial",
                "IsUnderMaintenance",
                "IsOutOfCommission"
            ].map((i) => { return `mcht:${i}`; });

            return `
                @prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> .
                @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
                @prefix qudt: <http://www.qudt.org/qudt/owl/1.0.0/qudt/#> .
                @prefix mcht: <http://purl.org/NET/ssnext/machinetools#> .
                @prefix : <http://example.com/222.173.190.239.254.113> .

                :mt-0-${TIMESTAMP} a ssn:Observation ;
                  ssn:observedProperty mcht:MachineToolWorkingState ;
                  ssn:observedBy :mt-0-st ;
                  ssn:observationResultTime "${DATETIME}"^^xsd:dateTime;
                  ssn:observationResult :mt-0-${TIMESTAMP}-result .

                :mt-0-${TIMESTAMP}-result a ssn:SensorOutput ;
                    ssn:hasValue :mt-0-${TIMESTAMP}-result-value .

                :mt-0-${TIMESTAMP}-result-value a qudt:Enumeration ;
                    ssn:hasValue ${STATES[Math.floor(Math.random() * 5)]} .
            `;
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
            return `
                @prefix rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#> .\n@prefix ssn: <http:\/\/purl.oclc.org\/NET\/ssnx\/ssn#> .\n@prefix mcht: <http:\/\/purl.org\/NET\/ssnext\/machinetools#> .\n@prefix ssncom: <http:\/\/purl.org\/NET\/ssnext\/communication#> .\n@prefix saref: <http:\/\/ontology.tno.nl\/saref#> .\n@prefix xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#> .\n@prefix geo: <http:\/\/www.w3.org\/2003\/01\/geo\/wgs84_pos#> .\n@prefix dul: <http:\/\/www.loa-cnr.it\/ontologies\/DUL.owl#> .\n@prefix qudt-quantity: <http:\/\/qudt.org\/vocab\/quantity#> .\n@prefix qudt-unit: <http:\/\/qudt.org\/vocab\/unit#> .\n@prefix qudt: <http:\/\/qudt.org\/schema\/qudt#> .\n@prefix dcterms: <http:\/\/purl.org\/dc\/terms\/> .\n@prefix proto: <http:\/\/w3id.org\/semiot\/ontologies\/proto#> .\n@prefix ws: <https:\/\/raw.githubusercontent.com\/semiotproject\/semiot-platform\/master\/device-proxy-service-drivers\/netatmo-weatherstation\/src\/main\/resources\/ru\/semiot\/platform\/drivers\/netatmo\/weatherstation\/prototype.ttl#> .\n\n<http:\/\/demo.semiot.ru\/systems\/3320914595> a ssn:System, proto:Individual ;\n    proto:hasPrototype ws:NetatmoWeatherStationOutdoorModule ;\n    ssn:hasSubSystem <http:\/\/demo.semiot.ru\/sensors\/3320914595-temperature> ;\n    ssn:hasSubSystem <http:\/\/demo.semiot.ru\/sensors\/3320914595-humidity> ;\n    dcterms:identifier \"3320914595\"^^xsd:string ;\n    geo:location [\n\ta geo:Point ;\n\tgeo:lat \"55.818018\"^^xsd:string ;\n        geo:long \"37.711456\"^^xsd:string ;\n        geo:alt \"141.0\"^^xsd:string ;\n    ] ;\n    .\n\n<http:\/\/demo.semiot.ru\/sensors\/3320914595-temperature>\n    a ssn:SensingDevice, proto:Individual ;\n    proto:hasPrototype ws:NetatmoWeatherStationOutdoorModule-TemperatureSensor ;\n    dcterms:identifier \"3320914595-temperature\"^^xsd:string ;\n    .\n\n<http:\/\/demo.semiot.ru\/sensors\/3320914595-humidity>\n    a ssn:SensingDevice, proto:Individual ;\n    proto:hasPrototype ws:NetatmoWeatherStationOutdoorModule-HumiditySensor ;\n    dcterms:identifier \"3320914595-humidity\"^^xsd:string ;\n    .
            `;
        }
    };

    return instance;
}];
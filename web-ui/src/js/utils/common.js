"use strict";

export default function($q, CONFIG) {

    let counter = 666;

    let instance = {
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
            return [
                "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .",
                "@prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> .",
                "@prefix hmtr: <http://purl.org/NET/ssnext/heatmeters#> .",
                "@prefix ssncom: <http://purl.org/NET/ssnext/communication#> .",

                "<coap://10.1.1.2:{0}/meter> a hmtr:HeatMeter ;".format(counter++),
                "    rdfs:label \"Heat Meter #{0}\" ;".format(counter),
                "    ssn:hasSubsystem <coap://10.1.1.2:{0}/meter/temperature> ;".format(counter),
                "    ssn:hasSubsystem <coap://10.1.1.2:{0}/meter/heat> .".format(counter),

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
}
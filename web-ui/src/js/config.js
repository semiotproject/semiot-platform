"use strict";

// if local development, communicate with DEFAULT_HOSTNAME, otherwise - with web-UI host
const DEFAULT_HOSTNAME = "demo.semiot.ru";
const hostname = location.hostname === "localhost" ? DEFAULT_HOSTNAME : location.hostname;

const TSDB_BASE_URL = `http://${hostname}:4242/api/query`;

export default {
    COOKIE_NAME: "ru.semiot",
    URLS: {
        messageBus: "ws://" + hostname + ":8080/ws",
        tripleStore: "http://" + hostname + ":3030/ds/query",
        tsdb: {
            archiveQuantity: `${TSDB_BASE_URL}?start={0}&end={1}&m=sum:{2}`,
            archiveEnum: `${TSDB_BASE_URL}?start={0}&end={1}&m=sum:{2}{enum_value=*}`,
            last: `${TSDB_BASE_URL}/last/{0}`
        }
    },
    TOPICS: {
        "device_registered": 'ru.semiot.devices.newandobserving',
        "device_turned_off": 'ru.semiot.devices.turnoff',
        "device_remove": 'ru.semiot.devices.remove'
    },
    SPARQL: {
        prefixes: {
            rdfs: 'http://www.w3.org/2000/01/rdf-schema#',
            ssn: 'http://purl.oclc.org/NET/ssnx/ssn#',
            rdf: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
            ssncom: 'http://purl.org/NET/ssnext/communication#',
            saref: 'http://ontology.tno.nl/saref#',
            mcht: 'http://purl.org/NET/ssnext/machinetools#',
            qudt: 'http://www.qudt.org/qudt/owl/1.0.0/qudt/#',
            "qudt-quantity": 'http://qudt.org/vocab/quantity#',
            "qudt-unit": "http://qudt.org/vocab/unit#",
            om: 'http://purl.org/ifgi/om#'
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
                "SELECT DISTINCT ?label ?uri ?state",
                "WHERE {",
                "   ?uri a ssn:System ;",
                "       saref:hasState ?state ;",
                "       rdfs:label ?label .",
                "}"
            ].join('\n'),
            getSystemEndpoint: `
                SELECT ?endpoint ?type ?observationType ?propLabel ?valueUnitLabel {
                  <{0}> ssn:hasSubsystem ?subsystem .
                  ?subsystem ssn:observes ?type .
                  ?subsystem ssn:hasMeasurementCapability ?mc .
                  ?mc ssn:forProperty ?prop .
                  ?prop rdfs:label ?propLabel .
                  ?mc ssn:hasMeasurementProperty ?mp .
                  ?mp ssn:hasValue ?v .
                  ?v a ?observationType .
                  ?v ssn:hasValue ?valueUnit .
                  ?valueUnit rdfs:label ?valueUnitLabel .
                  ?subsystem ssncom:hasCommunicationEndpoint ?endpoint .
                  ?endpoint ssncom:protocol 'WAMP' .
                }
            `,
            getSystemName: [
                "SELECT ?label {",
                "   <{0}> rdfs:label ?label .",
                "}"
            ].join('\n'),
            getMachineToolStates: `
                SELECT ?stateURI ?stateLabel ?stateDescription
                WHERE {
                    ?stateURI rdf:type mcht:MachineToolWorkingStateValue ;
                        rdfs:label ?stateLabel ;
                        rdfs:comment ?stateDescription .
                }
            `
        }
    },
    TIMEZONE_OFFSET: 3 * 3600 * 1000
};

"use strict";

// change it to target hostname when develop
const DEFAULT_HOSTNAME = "demo.semiot.ru";
const hostname = location.hostname;

const TSDB_BASE_URL = `http://${hostname}/`;

export default {
    COOKIE_NAME: "ru.semiot",
    VERSION: `@VERSION`,
    URLS: {
        currentUser: `http://${hostname}/user`,
        messageBus: `ws://${hostname}:8080/ws`,
        tripleStore: `http://${hostname}:3030/ds/query`,
        tsdb: {
            // archiveQuantity: `${TSDB_BASE_URL}?start={0}&end={1}&m=sum:{2}`,
            archiveQuantity: `${TSDB_BASE_URL}systems/{0}/observations?sensor_id={1}&start={2}`,
            archiveEnum: `${TSDB_BASE_URL}?start={0}&end={1}&m=sum:{2}{enum_value=*}`,
            last: `${TSDB_BASE_URL}/last/{0}`
        },
        analyze: {
            query: "http://" + hostname + ":8085/api/query",
            events: "http://" + hostname + ":8085/api/query/{0}/events"
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
            qudt: 'http://qudt.org/schema/qudt#',
            "qudt-quantity": 'http://qudt.org/vocab/quantity#',
            "qudt-unit": "http://qudt.org/vocab/unit#",
            proto: "http://w3id.org/semiot/ontologies/proto#",
            om: 'http://purl.org/ifgi/om#',
            ext: 'http://w3id.org/qudt/vocab/quantity/ext#',
            dcterm: "http://purl.org/dc/terms/"
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
                "SELECT DISTINCT ?label ?id ?uri",
                "WHERE {",
                "   ?uri a ssn:System ;",
                "       proto:hasPrototype/rdfs:label ?label ;",
                "        dcterm:identifier ?id .",
                "}"
            ].join('\n'),
            getSystemSensors: `
                SELECT ?instance ?type ?observationType ?propLabel ?valueUnitLabel {
                  <{0}> ssn:hasSubSystem ?instance .
                  ?instance proto:hasPrototype ?subsystem .
                  ?subsystem ssn:observes ?type .
                  ?subsystem ssn:hasMeasurementCapability ?mc .
                  ?mc ssn:forProperty ?prop .
                  ?prop rdfs:label ?propLabel .
                  ?mc ssn:hasMeasurementProperty ?mp .
                  ?mp ssn:hasValue ?v .
                  ?v a ?observationType .
                  ?v ssn:hasValue ?valueUnit .
                  ?valueUnit rdfs:label ?valueUnitLabel .
                }
            `,
            getSystemName: [
                "SELECT ?label ?id {",
                "   <{0}> proto:hasPrototype/rdfs:label ?label ;",
                "       dcterm:identifier ?id .",
                "}"
            ].join('\n'),
            getSystemTopic: `
                SELECT ?topic {
                    graph ?g {
                        <{0}> ssncom:hasCommunicationEndpoint ?endpoint .
                        ?endpoint ssncom:protocol "WAMP" ;
                            ssncom:topic ?topic .
                    }
                }
            `,
            getMachineToolStates: `
                SELECT ?stateURI ?stateLabel ?stateDescription
                WHERE {
                    ?stateURI rdf:type mcht:MachineToolWorkingStateValue ;
                        rdfs:label ?stateLabel ;
                        rdfs:comment ?stateDescription .
                }
            `,
            getPrototypeLabel: `
                SELECT ?label WHERE {
                    <{0}> rdfs:label ?label .
                }
            `
        }
    },
    TIMEZONE_OFFSET: -1 * new Date().getTimezoneOffset() * 60 * 1000 // in ms
};

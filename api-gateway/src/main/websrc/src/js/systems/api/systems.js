export default function(CONFIG, $http, $q, WAMP) {

    function extractSystemNameFromAPIMessage(msg) {
        return msg['rdfs:label'] ? msg['rdfs:label']['@value'] : msg['@id'];
    }

    function getIdFromURI(uri) {
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    function extractSystemFromAPIMessage(msg, index = -1) {
        return {
            index,
            uri: msg['@id'],
            id: msg['dcterms:identifier'],
            name: extractSystemNameFromAPIMessage(msg)
        };
    }

    return {
        loadSystems() {
            console.info('loading systems list');
            const defer = $q.defer();

            $http({
                url: CONFIG.URLS.systems.list,
                headers: {
                    'Accept': 'application/json'
                }
            }).success((res) => {
                try {
                    defer.resolve(res['hydra:member'].map((s, index) => {
                        const id = getIdFromURI(s['@id']);
                        return extractSystemFromAPIMessage(s, index);
                    }));
                } catch(e) {
                    console.error(`unable to parse systems: e = `, e);
                    defer.resolve([]);
                }
            });

            return defer.promise;
        },
        loadSystem(uri) {
            console.info(`loading system ${uri}`);
            const defer = $q.defer();

            $http({
                url: uri,
                headers: {
                    'Accept': 'application/json'
                }
            }).success((res) => {
                try {
                    const id = getIdFromURI(res['@id']);
                    defer.resolve(
                        {
                            uri: res['@id'],
                            id: id,
                            name: extractSystemNameFromAPIMessage(res),
                            sensors: res['ssn:hasSubSystem'].map((s) => {
                                return {
                                    uri: s['@id']
                                };
                            })
                        }
                    );
                } catch(e) {
                    console.error(`unable to parse systems: e = `, e);
                    defer.resolve([]);
                }
            });

            return defer.promise;
        },
        subscribeForNewSystems(callback) {
            WAMP.subscribe({
                topic: CONFIG.TOPICS['device_registered'],
                callback: (msg) => {
                    callback(extractSystemFromAPIMessage(JSON.parse(msg[0])));
                }
            });
            /*
            setInterval(() => {
                const msg = {
                    "@id":"http://localhost/systems/4218746842",
                    "@type":["proto:Individual", "ssn:System"],
                    "ssn:hasSubSystem":[
                        {
                            "@id":"http://localhost/systems/4218746842/subsystems/4218746842-humidity",
                            "@type":["proto:Individual", "ssn:SensingDevice"],
                            "dcterms:identifier":"4218746842-humidity",
                            "proto:hasPrototype":"ws:NetatmoWeatherStationOutdoorModule-HumiditySensor"
                        },
                        {
                            "@id":"http://localhost/systems/4218746842/subsystems/4218746842-temperature",
                            "@type":["proto:Individual", "ssn:SensingDevice"],
                            "dcterms:identifier":"4218746842-temperature",
                            "proto:hasPrototype":"ws:NetatmoWeatherStationOutdoorModule-TemperatureSensor"
                        }
                    ],
                    "dcterms:identifier":"4218746842",
                    "proto:hasPrototype": "ws:NetatmoWeatherStationOutdoorModule",
                    "geo:location":{
                        "@type":"geo:Point",
                        "geo:alt":"26.0",
                        "geo:lat":"60.051676687191",
                        "geo:long":"30.431828492065"
                    },
                    "@context":{
                        "proto:hasPrototype":{"@type":"@id"},
                        "ssn:hasSubSystem":{"@type":"@id"},
                        "qudt":"http://qudt.org/schema/qudt#",
                        "xsd":"http://www.w3.org/2001/XMLSchema#",
                        "rdfs":"http://www.w3.org/2000/01/rdf-schema#",
                        "qudt-unit":"http://qudt.org/vocab/unit#",
                        "ssn":"http://purl.oclc.org/NET/ssnx/ssn#",
                        "geo":"http://www.w3.org/2003/01/geo/wgs84_pos#",
                        "qudt-quantity":"http://qudt.org/vocab/quantity#",
                        "proto":"http://w3id.org/semiot/ontologies/proto#",
                        "dcterms":"http://purl.org/dc/terms/",
                        "dul":"http://www.loa-cnr.it/ontologies/DUL.owl#",
                        "ws":"https://raw.githubusercontent.com/semiotproject/semiot-platform/master/device-proxy-service-drivers/netatmo-weatherstation/src/main/resources/ru/semiot/platform/drivers/netatmo/weatherstation/prototype.ttl#"
                    }
                };
                callback(extractSystemFromAPIMessage(msg));
            }, 1000);
            */
        },
        unsubscribeFromNewSystems() {
            console.info(`unsubscribing from new systems..`);
            // WAMP.unsubscribe(getWAMPTopicFromSensor(sensor));
        }
    };
}
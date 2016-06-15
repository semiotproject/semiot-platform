export default ["CONFIG", "HTTP", "WAMP", function(CONFIG, HTTP, WAMP) {

    function extractSystemNameFromAPIMessage(msg) {
        return msg['rdfs:label'] ? msg['rdfs:label']['@value'] : msg['@id'];
    }

    function getIdFromURI(uri) {
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    function extractSystemFromAPIMessage(msg, index = -1) {
        return {
            index: index + 1,
            uri: msg['@id'],
            id: msg['dcterms:identifier'],
            name: extractSystemNameFromAPIMessage(msg)
        };
    }

    return {
        loadSystems() {
            console.info('loading systems list');
            return HTTP.get(CONFIG.URLS.systems.list).then((res) => {
                return res['hydra:member'].map((s, index) => {
                    const id = getIdFromURI(s['@id']);
                    return extractSystemFromAPIMessage(s, index);
                });
            }, (e) => {
                console.error(`unable to parse systems: e = `, e);
            });
        },
        loadSystem(uri) {
            console.info(`loading system ${uri}`);


            return HTTP.get(uri).then((res) => {
                const subsystems = res['ssn:hasSubSystem'];
                let processes = res['semiot:supportedProcess'];
                if (!processes) {
                    processes = [];
                } else if (!Array.isArray(processes)) {
                    processes = [];
                }
                return {
                    uri: res['@id'],
                    id: getIdFromURI(res['@id']),
                    name: extractSystemNameFromAPIMessage(res),
                    sensors: subsystems.filter((s) => {
                        return s["@type"].indexOf("ssn:SensingDevice") > -1;
                    }).map((s) => {
                        return {
                            uri: s['@id']
                        };
                    }),
                    actuators: subsystems.filter((s) => {
                        return s["@type"].indexOf("semiot:ActuatingDevice") > -1;
                    }).map((s) => {
                        return {
                            uri: s['@id']
                        };
                    }),
                    processes: processes.map((p) => {
                        return {
                            uri: p["id"],
                            id: p["dcterm:identifier"]
                        };
                    })
                };
            }, (e) => {
                console.error(`unable to parse system: e = `, e);
            });
        },
        subscribeForNewSystems(callback) {
            WAMP.subscribe({
                topic: CONFIG.TOPICS['device_registered'],
                callback: (msg) => {
                    callback(extractSystemFromAPIMessage(JSON.parse(msg[0])));
                }
            });
        },
        unsubscribeFromNewSystems() {
            console.info(`NOT_IMPLEMENTED: nsubscribing from new systems..`);
            // WAMP.unsubscribe(getWAMPTopicFromSensor(sensor));
        }
    };
}];
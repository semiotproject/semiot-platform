import HTTP from '../services/http';
import WAMP from '../services/wamp';
import CONFIG from '../config';
import commonUtils from '../utils/common';

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

export default {
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
            const subsystems = commonUtils.ensureArray(res['ssn:hasSubSystem']);
            const processes = commonUtils.ensureArray(res['semiot:supportedProcess']);
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
                        uri: p["@id"],
                        id: p["dcterms:identifier"]
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
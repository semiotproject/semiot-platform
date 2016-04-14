"use strict";

import { EventEmitter } from 'events';
import N3 from 'n3';

function isDeviceOnline(state) {
    return state === "http://ontology.tno.nl/saref#OnState";
}

export default function(
    $q,
    rdfUtils,
    wampUtils,
    sparqlUtils,
    commonUtils,
    CONFIG
) {

    let systems = [];

    let models = {};

    class Instance extends EventEmitter {
        constructor() {
            super();
            // this.launchTestSystems();
        }

        subscribe() {
            wampUtils.subscribe([
                {
                    topic:  CONFIG.TOPICS.device_registered,
                    callback: this.onDeviceRegistered.bind(this)
                }
            ]);
        }

        launchTestSystems() {
            setInterval(() => {
                this.onDeviceRegistered([commonUtils.getMockNewSystem()]);
            }, 1000);
        }

        onDeviceRegistered(args) {
            try {
            rdfUtils.parseTTL(args[0]).then((triples) => {

                    let N3Store = N3.Store();

                    N3Store.addPrefixes(CONFIG.SPARQL.prefixes);
                    N3Store.addTriples(triples);

                    // can not find another way to get system from description
                    let system = N3Store.find(null, "ssn:hasSubSystem", null, "")[0];
                    let uri = system.subject;
                    let id = N3Store.find(uri, "http://purl.org/dc/terms/identifier", null, "")[0].object;
                    let proto = N3Store.find(uri, "proto:hasPrototype", null, "")[0].object;
                    let label;
                    if (proto) {
                        this.getPrototypeLabel(proto).then((l) => {
                            if (!systems.find(function(system) { // if system is new
                                return system.uri === uri;
                            })) {
                                systems.push({
                                    index: systems.length + 1,
                                    uri,
                                    // fixme: add model label to id
                                    name: `${l} / ${N3.Util.getLiteralValue(id)}`,
                                    isOnline: true
                                });
                                this.emit("systemsUpdate", systems);
                            }
                        });
                    }
                });
            } catch(e) {
                console.error(e);
            }
        }
        getPrototypeLabel(uri) {
            const defer = $q.defer();

            if (!models[uri]) {
                console.info(`no model ${uri} is found in cache; fetching via SPARQL..`);
                sparqlUtils.executeQuery(CONFIG.SPARQL.queries.getPrototypeLabel.format(uri), (data) => {
                    const label = data.results.bindings[0].label.value;
                    models[uri] = label;
                    console.info(`added new model label for proto = ${uri}; label is "${label}"`);
                    defer.resolve(label);
                });
            } else {
                console.info(`found model label in cache; using..`);
                defer.resolve(models[uri]);
            }

            return defer.promise;
        }
        onDeviceTurnedOff(payload) {
            console.info('received message on `turnOff` topic, processing..');
            // prefix saref: <http://ontology.tno.nl/saref#> <coap://winghouse.semiot.ru:60005/meter> saref:hasState saref:OnState.

            rdfUtils.parseTTL(payload[0]).then((triples) => {
                let N3Store = N3.Store();

                N3Store.addPrefixes(CONFIG.SPARQL.prefixes);
                N3Store.addTriples(triples);

                let triple = N3Store.find(null, "saref:hasState", null, "")[0];

                let uri = triple.subject;
                let state = triple.object;

                systems.forEach((system, index) => {
                    if (system.uri === uri) {
                        systems[index].isOnline = isDeviceOnline(state);
                        console.info(`changin network state for ${uri}; now is ${systems[index].isOnline ? "online" : "offline"}`);

                        this.emit("systemsUpdate", systems);
                    }
                });
            });
        }
        fetchSystems() {
            let defer = $q.defer();
            sparqlUtils.executeQuery(CONFIG.SPARQL.queries.getAllSystems, function(data) {
                systems = data.results.bindings.sort(function(a, b) {
                    return a.label.value > b.label.value ? 1 : -1;
                }).map(function(binding, index) {
                    return {
                        index: index + 1,
                        name: `${binding.label.value} / ${binding.id.value}`,
                        uri: binding.uri.value
                    };
                });
                const s = systems.sort((a, b) => {
                    return a.index > b.index ? 1 : -1;
                });
                defer.resolve(s);
            });
            return defer.promise;
        }
        removeSystem(id) {
            console.warn(`remove method is not implemented`);
            /*
            if (this.connection) {
                this.connection.publish(CONFIG.TOPICS.device_remove, [uri]);
            }
            systems.forEach((system, i) => {
                if (system.uri === uri) {
                    systems.splice(i, 1);
                }
            });
            this.emit("systemsUpdate", systems);
            */
        }
        getSystems() {
            return systems;
        }
        getSystemByURI(uri) {
            return systems.find(function(system) {
                return system.uri === uri;
            });
        }
        getSystemsInRange(from, to) {
            let response = [];
            for (let i = from; i < to; i++) {
                if (systems[i]) {
                    response.push(systems[i]);
                }
            }
            return response;
        }
    }

    return new Instance();
}


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
            rdfUtils.parseTTL(args[0]).then((triples) => {

                let N3Store = N3.Store();

                N3Store.addPrefixes(CONFIG.SPARQL.prefixes);
                N3Store.addTriples(triples);

                // can not find another way to get system from description
                let system = N3Store.find(null, "ssn:hasSubSystem", null, "")[0];
                let uri = system.subject;
                let label = N3Store.find(uri, "rdfs:label", null, "")[0].object;

                if (!systems.find(function(system) { // if system is new
                    return system.uri === uri;
                })) {
                    systems.push({
                        index: systems.length + 1,
                        uri,
                        name: N3.Util.getLiteralValue(label),
                        isOnline: true
                    });
                    this.emit("systemsUpdate", systems);
                }
            });
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
                defer.resolve(systems);
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


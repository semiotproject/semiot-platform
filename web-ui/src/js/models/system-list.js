"use strict";

import { EventEmitter } from 'events';
import N3 from 'n3';

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

            this.connection = wampUtils.subscribe(
                CONFIG.URLS.messageBus,
                [
                    {
                        topic:  CONFIG.TOPICS.device_registered,
                        callback: this.onDeviceRegistered
                    },
                    {
                        topic:  CONFIG.TOPICS.device_turned_off,
                        callback: this.onDeviceTurnedOff
                    }
                ]
            ).session;

            // this.launchTestSystems();
        }

        launchTestSystems() {
            setInterval(() => {
                this.onDeviceRegistered([commonUtils.getMockNewSystem()]);
            }, 1000);
        }

        onDeviceRegistered(args) {
            rdfUtils.parseTTL(args[0]).then((triples) => {
                let resource = rdfUtils.parseTriples(triples);
                if (!systems.find(function(system) { // if system is new
                    return system.uri === resource.uri;
                })) {
                    systems.push({
                        index: systems.length + 1,
                        uri: resource.uri,
                        name: resource.get("http://www.w3.org/2000/01/rdf-schema#label"),
                        state: "online"
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
                        systems[index].isOnline = state === "http://ontology.tno.nl/saref#OnState";
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
                        name: binding.label.value,
                        uri: binding.uri.value,
                        isOnline: binding.state.value === "http://ontology.tno.nl/saref#OnState"
                    };
                });
                defer.resolve(systems);
            });
            return defer.promise;
        }
        removeSystem(uri) {
            if (this.connection) {
                this.connection.publish(CONFIG.TOPICS.device_remove, [uri]);
            }
            systems.forEach((system, i) => {
                if (system.uri === uri) {
                    systems.splice(i, 1);
                }
            });
            this.emit("systemsUpdate", systems);
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


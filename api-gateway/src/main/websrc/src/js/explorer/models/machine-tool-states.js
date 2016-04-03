"use strict";

export default function(
    sparqlUtils,
    CONFIG
) {

    let states = [];

    class Instance {
        constructor() {}
        getStates() {
            return states;
        }
        fetchStates() {
            console.info('fetching machine tool states...');
            return sparqlUtils.executeQuery(CONFIG.SPARQL.queries.getMachineToolStates, (res) => {
                states = res.results.bindings.map(function(binding, index) {
                    // ?stateURI ?stateLabel ?stateDescription
                    return {
                        uri: binding.stateURI.value,
                        label: binding.stateLabel.value,
                        description: binding.stateDescription.value
                    };
                });

                console.info('result states of machine tool: ', states);
            });
        }
    }

    return new Instance();
}


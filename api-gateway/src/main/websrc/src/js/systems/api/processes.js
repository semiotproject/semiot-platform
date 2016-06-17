export default ["CONFIG", "HTTP", "WAMP", "$q", "commonUtils", function(CONFIG, HTTP, WAMP, $q, commonUtils) {

/*

"{
    "@id": "_:b3",
    "@type": "semiot:CommandResult",
    "semiot:commandResultTime": {
        "@type": "xsd:dateTime",
        "@value": "2016-06-17T07:49:52.953Z"
    },
    "semiot:isResultOf": {
        "@type": "semiot:StartCommand",
        "http://purl.org/dc/terms/identifier": "light-startcommand",
        "semiot:forProcess": {
            "@id": "https://demo.semiot.ru/systems/1231231231231230/processes/light"
        },
        "dul:associatedWith": {
            "@id": "https://demo.semiot.ru/systems/1231231231231230"
        },
        "dul:hasParameter": [
            {
                "@type": "semiot:MappingParameter",
                "semiot:forParameter": {
                    "@id": "https://raw.githubusercontent.com/semiotproject/semiot-drivers/master/mock-…rces/ru/semiot/drivers/mocks/plainlamp/prototype.ttl#PlainLamp-Shine-Lumen"
                },
                "dul:hasParameterDataValue": 8
            },
            {
                "@type": "semiot:MappingParameter",
                "semiot:forParameter": {
                    "@id": "https://raw.githubusercontent.com/semiotproject/semiot-drivers/master/mock-…rces/ru/semiot/drivers/mocks/plainlamp/prototype.ttl#PlainLamp-Shine-Color"
                },
                "dul:hasParameterDataValue": 4000
            }
        ]
    },
    "dul:associatedWith": {
        "@id": "https://demo.semiot.ru/systems/1231231231231230"
    },
    "@context": {
        "xsd": "http://www.w3.org/2001/XMLSchema#",
        "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
        "dul": "http://www.loa-cnr.it/ontologies/DUL.owl#",
        "semiot": "http://w3id.org/semiot/ontologies/semiot#"
    }
}"

*/

    function parseParameter(obj) {
        obj['dul:hasParameterDataValue'] = "";
        if (obj["sh:property"] && obj["sh:property"]["sh:defaultValue"]) {
            obj['dul:hasParameterDataValue'] = obj["sh:property"] && obj["sh:property"]["sh:defaultValue"];
        }
        return obj;
    }

    function parseOperations(uri, context, o) {
        return commonUtils.ensureArray(o["hydra:expects"]).map((e) => {
            const result = {
                method: o["hydra:method"],
                id: e["dcterms:identifier"],
                label: e["rdfs:label"]["@value"],
                body: $.extend(e, {
                    '@context': context
                }),
                uri
            };
            if (e['dul:hasParameter']) {
                result.body = $.extend(result.body, {
                    'dul:hasParameter': e['dul:hasParameter'].map(parseParameter)
                });
            }
            return result;
        });
    }

    function parseWAMPTopicFromCommandResults(res) {
        const operations = commonUtils.ensureArray(res["hydra-filter:viewOf"]["hydra:operation"]);
        const subscriptionOperation = operations.find((o) => {
            return o['@type'] === "hydra-pubsub:SubscribeOperation";
        });
        return {
            endpoint: subscriptionOperation['hydra-pubsub:endpoint'],
            topic: subscriptionOperation['hydra-pubsub:topic']
        };
    }

    function extractCommandResultFromWAMPMessage(data) {
        return {
            value: data["semiot:isResultOf"]["dcterms:identifier"],
            timestamp: data["semiot:commandResultTime"]["@value"]
        };
    }

    return {
        loadProcessInformation(uri) {
            console.info(`loading process info from ${uri}`);
            const defer = $q.defer();
            const process = {
                uri
            };

            HTTP.get(uri).then((res) => {
                process.id = res['dcterms:identifier'];
                process.operations = parseOperations(uri, res['@context'], res['hydra:supportedOperation']);
                process.commandResults = [];

                this.loadCommandResultsAndWAMPTopic(res['apidoc:commandResults']).then((resultAndTopic) => {
                    process.result = resultAndTopic.result;
                    process.wamp = resultAndTopic.wamp;
                    defer.resolve(process);
                });
            });

            return defer.promise;
        },
        loadCommandResultsAndWAMPTopic(uri) {
            console.info(`loading command results from ${uri}`);
            return HTTP.get(uri).then((res) => {
                let commandResult;
                if (!res["hydra:member"]) {
                    commandResult = {};
                } else if (Array.isArray(res["hydra:member"])) {
                    commandResult = res["hydra:member"][0];
                }
                return {
                    result: {
                        timestamp: (new Date(commandResult['dul:hasEventTime'])).toString(),
                        value: commandResult['semiot:isResultOf']['dcterms:identifier']
                    },
                    wamp: parseWAMPTopicFromCommandResults(res)
                };
            });
        },
        performOperation(o) {
            return HTTP.query(o.uri, o.method, o.body);
        },
        subscribe(endpoint, topic, callback) {
            WAMP.subscribe({
                topic,
                endpoint,
                callback: (msg) => {
                    callback(extractCommandResultFromWAMPMessage(JSON.parse(msg[0])));
                }
            });
        }
    };

}];
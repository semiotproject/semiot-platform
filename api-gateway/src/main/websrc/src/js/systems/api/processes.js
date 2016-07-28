import CONFIG from '../config';
import HTTP from '../services/http';
import WAMP from '../services/wamp';
import commonUtils from '../utils/common';

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
                'dul:hasParameter': commonUtils.ensureArray(e['dul:hasParameter']).map(parseParameter)
            });
        }
        return result;
    });
}

function parseWAMPTopicFromCommandResults(res) {
    const hydraOperationRoot = res["hydra-filter:viewOf"] ? res["hydra-filter:viewOf"] : res["hydra-filter:viewTemplate"];
    const operations = commonUtils.ensureArray(hydraOperationRoot["hydra:operation"]);
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

export default {
    loadProcessInformation(uri) {
        console.info(`loading process info from ${uri}`);
        return new Promise((resolve, reject) => {
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
                    resolve(process);
                });
            });
        });
    },
    loadCommandResultsAndWAMPTopic(uri) {
        console.info(`loading command results from ${uri}`);
        return HTTP.get(uri).then((res) => {
            res = JSON.parse(`{
  "@id" : "https://test.semiot.ru/systems/1231231231231236/processes/light/commandResults",
  "@type" : [ "void:Dataset", "hydra:Collection" ],
  "void:classPartition" : {
    "void:class" : "semiot:CommandResult"
  },
  "hydra-filter:viewTemplate" : {
    "@type" : "hydra-filter:ViewTemplate",
    "hydra:mapping" : [ {
      "@type" : "hydra-filter:DirectMapping",
      "hydra-filter:comparator" : "hydra-filter:lessOrEquals",
      "hydra:property" : "dul:hasEventTime",
      "hydra:required" : false,
      "hydra:variable" : "end"
    }, {
      "@type" : "hydra-filter:DirectMapping",
      "hydra-filter:comparator" : "hydra-filter:greaterOrEquals",
      "hydra:property" : "dul:hasEventTime",
      "hydra:required" : true,
      "hydra:variable" : "start"
    } ],
    "hydra:template" : "/systems/1231231231231236/processes/light/commandResults{?start,end}"
  },
  "hydra:member" : [ ],
  "hydra:operation" : {
    "@type" : "hydra-pubsub:SubscribeOperation",
    "hydra-pubsub:endpoint" : "wss://test.semiot.ru/wamp",
    "hydra-pubsub:protocol" : "hydra-pubsub:WAMP",
    "hydra-pubsub:publishes" : "semiot:CommandResult",
    "hydra-pubsub:topic" : "1231231231231236.commandresults.light"
  },
  "@context" : {
    "hydra" : "http://www.w3.org/ns/hydra/core#",
    "hydra-pubsub" : "http://w3id.org/semiot/ontologies/hydra-pubsub#",
    "hydra-filter" : "http://w3id.org/semiot/ontologies/hydra-filter#",
    "rdfs" : "http://www.w3.org/2000/01/rdf-schema#",
    "ssn" : "http://purl.oclc.org/NET/ssnx/ssn#",
    "saref" : "http://ontology.tno.nl/saref#",
    "void" : "http://rdfs.org/ns/void#",
    "dcterms" : "http://purl.org/dc/terms/",
    "qudt-quantity" : "http://qudt.org/vocab/quantity#",
    "qudt-unit" : "http://qudt.org/vocab/unit#",
    "qudt" : "http://qudt.org/schema/qudt#",
    "proto" : "http://w3id.org/semiot/ontologies/proto#",
    "dul" : "http://www.loa-cnr.it/ontologies/DUL.owl#",
    "geo" : "http://www.w3.org/2003/01/geo/wgs84_pos#",
    "xsd" : "http://www.w3.org/2001/XMLSchema#",
    "semiot" : "http://w3id.org/semiot/ontologies/semiot#",
    "apidoc" : "https://test.semiot.ru/doc#",
    "ssn:hasValue" : {
      "@type" : "@id"
    },
    "void:class" : {
      "@type" : "@id"
    },
    "hydra-filter:viewOf" : {
      "@type" : "@id"
    },
    "hydra-pubsub:publishes" : {
      "@type" : "@id"
    },
    "hydra-pubsub:protocol" : {
      "@type" : "@id"
    },
    "hydra-pubsub:endpoint" : {
      "@type" : "xsd:anyURI"
    },
    "dul:associatedWith" : {
      "@type" : "@id"
    },
    "dul:hasParticipant" : {
      "@type" : "@id"
    },
    "semiot:forProcess" : {
      "@type" : "@id"
    },
    "hydra:member" : {
      "@container" : "@set"
    }
  }
}`)
            let commandResult;
            if (res["hydra:member"] && res["hydra:member"].length > 0) {
                commandResult = res["hydra:member"][0];
            }
            const r = {
                wamp: parseWAMPTopicFromCommandResults(res)
            };
            if (commandResult) {
                r.result = {
                    timestamp: (new Date(commandResult['dul:hasEventTime'])).toString(),
                    value: commandResult['semiot:isResultOf']['dcterms:identifier']
                };
            }
            return r;
        });
    },
    performOperation(o) {
        return HTTP.post(o.uri, o.body);
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
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
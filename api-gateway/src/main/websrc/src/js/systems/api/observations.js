import CONFIG from '../config';
import HTTP from '../services/http';
import WAMP from '../services/wamp';
import moment from 'moment';

function extractObservationFromWAMPMessage(msg) {
    return {
        timestamp: (new Date(msg['ssn:observationResultTime'])).getTime(),
        value: parseFloat(msg['ssn:observationResult']['ssn:hasValue']['qudt:quantityValue'])
    };
}

function getWAMPTopicFromSensor(sensor) {
    return 'fixme';
}

export default {
    loadObservations(observationsURI, range) {
        console.info(`loading observations of ${observationsURI} from ${moment(range[0]).format('YYYY-MM-DDTHH:mm:ssZ')} to ${moment(range[1]).format('YYYY-MM-DDTHH:mm:ss')}`);

        const start = encodeURIComponent(moment(range[0]).format('YYYY-MM-DDTHH:mm:ssZ'));
        const end = encodeURIComponent(moment(range[1]).format('YYYY-MM-DDTHH:mm:ss'));

        return HTTP.get(`${observationsURI}&start=${start}&end=${end}`).then((res) => {
            return res["hydra:member"].map((obs) => {
                return {
                    timestamp: (new Date(obs['ssn:observationResultTime'])).getTime(),
                    value: parseFloat(obs['ssn:observationResult']['ssn:hasValue']['qudt:quantityValue'])
                };
            });
        });
    },
    loadWAMPTopic(observationsURI) {
        console.info(`loading WAMP topic from oobservationsURI ${observationsURI}`);
        return HTTP.get(`${observationsURI}`).then((res) => {
            const hydraOperationRoot = res["hydra-filter:viewOf"] ? res["hydra-filter:viewOf"] : res;
            const subscriptionOperation = hydraOperationRoot['hydra:operation'];
            return {
                endpoint: subscriptionOperation['hydra-pubsub:endpoint']['@value'],
                topic:  subscriptionOperation['hydra-pubsub:topic']
            };
        }, (e) => {
            console.error(`failed to load observation list: error = `, e);
        });
    },
    subscribeForNewObservations(endpoint, topic, callback) {
        WAMP.subscribe({
            topic: topic,
            endpoint: endpoint,
            callback: (msg) => {
                callback(extractObservationFromWAMPMessage(JSON.parse(msg[0])));
            }
        });
    },
    unsubscribeFromNewObservations(sensor) {
        console.info(`unsubscribing from new observations of sensor()..`);
        WAMP.unsubscribe(getWAMPTopicFromSensor(sensor));
    }
};
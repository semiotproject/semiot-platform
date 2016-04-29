export default function(CONFIG, $http, $q, WAMP) {

    function extractObservationFromWAMPMessage(msg) {
        return {
            timestamp: (new Date(msg['ssn:observationResultTime'])).getTime(),
            value: parseFloat(msg['ssn:observationResult']['ssn:hasValue']['qudt:quantityValue'])
        };
    }

    function getWAMPTopicFromSensor(sensor) {
        return 'fixme';
    }

    return {
        loadObservations(observationsURI, range) {
            console.info(`loading observations of ${observationsURI} from ${moment(range[0]).format('YYYY-MM-DDTHH:mm:ss')} to ${moment(range[1]).format('YYYY-MM-DDTHH:mm:ss')}`);
            const defer = $q.defer();

            const start = moment(range[0]).format('YYYY-MM-DDTHH:mm:ss');
            const end = moment(range[1]).format('YYYY-MM-DDTHH:mm:ss');

            $http.get(`${observationsURI}&start=${start}&end=${end}`).success((res) => {
                try {
                    defer.resolve(res['hydra:member'].map((obs) => {
                        return {
                            timestamp: (new Date(obs['ssn:observationResultTime'])).getTime(),
                            value: parseFloat(obs['ssn:observationResult']['ssn:hasValue']['qudt:quantityValue'])
                        };
                    }));
                } catch(e) {
                    console.error(`failed to parse observation list: error = `, e);
                    defer.resolve([]);
                }
            });

            return defer.promise;
        },
        subscribeForNewObservations(sensor, callback) {
            WAMP.subscribe([{
                topic: getWAMPTopicFromSensor(sensor),
                callback: (msg) => {
                    callback(extractObservationFromWAMPMessage(JSON.parse(msg[0])));
                }
            }]);
            /*
            setInterval(() => {
                const msg = {
                    "@id":"http://localhost/systems/2680224023/subsystems/2680224023/observations/humidity#1461510343",
                    "@type":"ssn:Observation",
                    "ssn:observationResult": {
                        "@id":"http://localhost/systems/2680224023/subsystems/2680224023/observations/humidity#1461510343-result",
                        "@type":"ssn:SensorOutput",
                        "ssn:hasValue": {
                            "@id":"http://localhost/systems/2680224023/subsystems/2680224023/observations/humidity#1461510343-resultvalue",
                            "@type":"qudt:QuantityValue",
                            "qudt:quantityValue": 60 + Math.random() * 10
                        },
                        "ssn:isProducedBy":"http://localhost/systems/2680224023/subsystems/2680224023-humidity"
                    },
                    "ssn:observationResultTime": moment().format(),
                    "ssn:observedBy":"http://localhost/systems/2680224023/subsystems/2680224023-humidity",
                    "ssn:observedProperty":"http://w3id.org/qudt/vocab/quantity/ext#RelativeHumidity",
                    "@context": {
                        "ssn":"http://purl.oclc.org/NET/ssnx/ssn#",
                        "xsd":"http://www.w3.org/2001/XMLSchema#",
                        "qudt":"http://qudt.org/schema/qudt#",
                        "qudt-quantity":"http://qudt.org/vocab/quantity#",
                        "ssn:isProducedBy":{"@type":"@id"},
                        "ssn:observedBy":{"@type":"@id"},
                        "ssn:observedProperty":{"@type":"@id"},
                        "ssn:observationResultTime":{"@type":"xsd:dateTime"}
                    }
                };
                callback(extractObservationFromWAMPMessage(msg));
            }, 1000);
            */
        },
        unsubscribeFromNewObservations(sensor) {
            console.info(`unsubscribing from new observations of sensor()..`);
            WAMP.unsubscribe(getWAMPTopicFromSensor(sensor));
        }
    };
}
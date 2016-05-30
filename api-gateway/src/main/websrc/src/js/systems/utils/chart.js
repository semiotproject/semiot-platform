"use strict";

export default ["CONFIG", "commonUtils", function(CONFIG, commonUtils) {

    const baseChartConfig = {
        useHighStocks: true,
        ignoreHiddenSeries : false,
        xAxis: {
            type: 'datetime',
            showEmpty: true
        },
        options: {
            useUTC: false,
            navigator: {
                enabled: true
            },
            rangeSelector: {
                enabled: false
            }
        }
    };

    Highcharts.setOptions({
        global: {
            timezoneOffset: (new Date()).getTimezoneOffset()
        }
    });

    // store pair `uri`: { ... }
    let STATE_MAP = {};

    const instance = {
        getObservationChartConfig: function(title) {
            return $.extend({}, baseChartConfig, {
                options: $.extend({}, baseChartConfig.options, {
                    chart: {
                        type: "spline"
                    }
                }),
                title: {
                    text: title
                },
                series: [
                    {}
                ]
            });
        },
        getStateChartConfig: function(title, states) {
            STATE_MAP = {};

            states.forEach((s, index) => {
                let uri = commonUtils.parseMetricFromType(s.uri);
                STATE_MAP[uri] = {
                    index: index,
                    label: s.label,
                    description: s.description
                };
            });

            function getItemByURI(uri) {
                let item = {};
                for (let key in STATE_MAP) {
                    if (key === uri) {
                        item = STATE_MAP[key];
                        break;
                    }
                }
                return item;
            }

            var yFormatter = function() {
                return getItemByURI(this.value).label;
            };
            var xFormatter = function() {
                return states[this.y].description;
            };

            var conf = $.extend({}, baseChartConfig, {
                yAxis: {
                    categories: Object.keys(STATE_MAP),
                    endOnTick: true,
                    minRange: states.length,
                    labels: {
                        formatter: yFormatter
                    }
                },
                title: {
                    text: title
                },
                series: [
                    {
                        step: 'center',
                        tooltip: {
                            pointFormatter: xFormatter
                        }
                    }
                ]
            });

            return conf;
        },

        parseObservationChartData(data) {

            // uncomment this to test
            /*
            data = JSON.parse(`{
              "@id" : "http://demo.semiot.ru/systems/113496665/observations?start=2016-04-19T12:44:15Z",
              "@type" : [ "void:Dataset", "hydra:PartialCollectionView" ],
              "hydra-filter:viewOf" : {
                "@id" : "http://demo.semiot.ru/systems/113496665/observations",
                "@type" : [ "void:Dataset", "hydra:Collection" ],
                "void:classPartition" : {
                  "void:class" : "ssn:Observation"
                },
                "hydra-filter:viewTemplate" : {
                  "@type" : "hydra-filter:ViewTemplate",
                  "hydra:mapping" : [ {
                    "@type" : "hydra-filter:DirectMapping",
                    "hydra-filter:comparator" : "hydra-filter:lessOrEquals",
                    "hydra:property" : "ssn:observationResultTime",
                    "hydra:required" : false,
                    "hydra:variable" : "end"
                  }, {
                    "@type" : "hydra-filter:DirectMapping",
                    "hydra-filter:comparator" : "hydra-filter:greaterOrEquals",
                    "hydra:property" : "ssn:observationResultTime",
                    "hydra:required" : true,
                    "hydra:variable" : "start"
                  } ],
                  "hydra:template" : "/systems/113496665/observations{?start,end}"
                },
                "hydra:operation" : {
                  "@type" : "hydra-pubsub:SubscribeOperation",
                  "hydra-pubsub:endpoint" : {
                    "@type" : "xsd:anyURI",
                    "@value" : "ws://wamprouter:8080/ws"
                  },
                  "hydra-pubsub:protocol" : {
                    "@id" : "hydra-pubsub:WAMP"
                  },
                  "hydra-pubsub:publishes" : {
                    "@id" : "ssn:Observation"
                  },
                  "hydra-pubsub:topic" : "113496665"
                }
              },
              "hydra:member" : [ {
                "@type" : "ssn:Observation",
                "ssn:observationResult" : {
                  "@type" : "ssn:SensorOutput",
                  "ssn:hasValue" : {
                    "@type" : "qudt:QuantityValue",
                    "qudt:quantityValue" : "6.1E0"
                  },
                  "http://purl.oclc.org/NET/ssnx/ssn#isProducedBy" : "http://localhost/sensors/113496665"
                },
                "ssn:observationResultTime" : "2016-04-19T13:24:25Z",
                "http://purl.oclc.org/NET/ssnx/ssn#observedBy" : "http://localhost/sensors/113496665",
                "ssn:observedProperty" : "qudt-quantity:ThermodynamicTemperature"
              }, {
                "@type" : "ssn:Observation",
                "ssn:observationResult" : {
                  "@type" : "ssn:SensorOutput",
                  "ssn:hasValue" : {
                    "@type" : "qudt:QuantityValue",
                    "qudt:quantityValue" : "5.9E0"
                  },
                  "http://purl.oclc.org/NET/ssnx/ssn#isProducedBy" : "http://localhost/sensors/113496665"
                },
                "ssn:observationResultTime" : "2016-04-19T12:44:15Z",
                "http://purl.oclc.org/NET/ssnx/ssn#observedBy" : "http://localhost/sensors/113496665",
                "ssn:observedProperty" : "qudt-quantity:ThermodynamicTemperature"
              }, {
                "@type" : "ssn:Observation",
                "ssn:observationResult" : {
                  "@type" : "ssn:SensorOutput",
                  "ssn:hasValue" : {
                    "@type" : "qudt:QuantityValue",
                    "qudt:quantityValue" : "9.9E1"
                  },
                  "http://purl.oclc.org/NET/ssnx/ssn#isProducedBy" : "http://localhost/sensors/113496665"
                },
                "ssn:observationResultTime" : "2016-04-19T12:44:15Z",
                "http://purl.oclc.org/NET/ssnx/ssn#observedBy" : "http://localhost/sensors/113496665",
                "ssn:observedProperty" : "http://w3id.org/qudt/vocab/quantity/ext#RelativeHumidity"
              }, {
                "@type" : "ssn:Observation",
                "ssn:observationResult" : {
                  "@type" : "ssn:SensorOutput",
                  "ssn:hasValue" : {
                    "@type" : "qudt:QuantityValue",
                    "qudt:quantityValue" : "1.0E2"
                  },
                  "http://purl.oclc.org/NET/ssnx/ssn#isProducedBy" : "http://localhost/sensors/113496665"
                },
                "ssn:observationResultTime" : "2016-04-19T13:24:25Z",
                "http://purl.oclc.org/NET/ssnx/ssn#observedBy" : "http://localhost/sensors/113496665",
                "ssn:observedProperty" : "http://w3id.org/qudt/vocab/quantity/ext#RelativeHumidity"
              } ],
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
                "apidoc" : "http://demo.semiot.ru/doc#",
                "ssn:hasValue" : {
                  "@type" : "@id"
                },
                "void:class" : {
                  "@type" : "@id"
                },
                "ssn:isProducedBy" : {
                  "@type" : "@id"
                },
                "ssn:observedBy" : {
                  "@type" : "@id"
                },
                "ssn:observedProperty" : {
                  "@type" : "@id"
                },
                "ssn:observationResultTime" : {
                  "@type" : "xsd:dateTime"
                }
              }
            }`);
            */
            try {
                return data['hydra:member'].map((m) => {
                    return [
                        (new Date(m['ssn:observationResultTime'])).getTime(),
                        parseFloat(m['ssn:observationResult']['ssn:hasValue']['qudt:quantityValue'])
                    ];
                }).sort((a, b) => {
                    return a[0] > b[0] ? 1 : -1;
                });
            } catch(e) {
                console.error('bad formed JSON LD; error: ', e);
                return [];
            }
        },
        observationsToSerie(data) {
            return data.map(this.observationsToChartPoint.bind(this)).sort((a, b) => {
                return a[0] > b[0] ? 1 : -1;
            });
        },
        observationsToChartPoint(obs) {
            return [
                obs.timestamp,
                obs.value
            ];
        },
        parseStateChartData(data) {
            let values = [];

            data.map((item) => {
                for (let timestamp in item.dps) {
                    values.push([timestamp * 1000, this.parseStateChartValue(item.tags.enum_value)]);
                }
            });

            return values.sort((a, b) => {
                return a[0] > b[0] ? 1 : -1;
            });
        },
        parseStateChartValue(value) {
            value = commonUtils.parseMetricFromType(value);
            return STATE_MAP[value] ? STATE_MAP[value].index : 0;
        }
    };

    return instance;
}];
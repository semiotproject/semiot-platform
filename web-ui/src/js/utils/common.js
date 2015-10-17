"use strict";

export default function($q, machineToolStates, CONFIG) {

    let counter = 666;

    let instance = {
        getChartConfig: function(type, data) {
            return {
                useHighStocks: true,
                xAxis: {
                    type: 'datetime'
                },
                options: {
                    chart: {
                        type: "spline"
                    },
                    navigator: {
                        enabled: true
                    },
                    rangeSelector: {
                        enabled: false
                    },
                    useUTC: false,
                    timezoneOffset: 3 * 60
                },
                series: [
                    {
                        pointStart: (new Date()).getTime(),
                        name: type,
                        data: data
                    }
                ],
                title: {
                    text: type
                }
            };
        },
        getStepChartConfig: function(type, data) {
            let map = {};

            let states = machineToolStates.getStates();
            states.forEach((s, index) => {
                let uri = s.uri.replace(':', '_').replace('#', '-');
                map[uri] = {
                    index: index + 1,
                    label: s.label,
                    description: s.description
                };
            });

            function getItemByURI(uri) {
                let item = {};
                for (let key in map) {
                    if (key === uri) {
                        item = map[key];
                        break;
                    }
                }
                return item;
            }

            let values = [];

            data.map((item) => {
                for (let timestamp in item.dps) {
                    values.push([timestamp * 1000, map[item.tags.enum_value] ? map[item.tags.enum_value].index : 0 ]);
                }
            });
            values = values.sort((a, b) => {
                return a[0] > b[0] ? 1 : -1;
            });

            console.log('parsed data is: ', values);

            return {
                xAxis: {
                    type: 'datetime'
                },
                useHighStocks: true,
                yAxis: {
                    categories: Object.keys(map),
                    endOnTick: false,
                    minRange: states.length + 4,
                    minPadding: 10,
                    labels: {
                        formatter: function() {
                            return getItemByURI(this.value).label;
                        }
                    }
                },
                options: {
                    useUTC: false,
                    navigator: {
                        enabled: true
                    },
                    rangeSelector: {
                        enabled: false
                    }
                },
                series: [
                    {
                        pointStart: (new Date()).getTime(),
                        name: type,
                        step: "center",
                        tooltip: {
                            pointFormatter: function () {
                                return states[this.y].description;
                            }
                        },
                        data: values
                    }
                ],
                title: {
                    text: type
                }
            };
        },
        normalizeTSDBData: function(type, result) {
            let data = [];
            if (result.data[0]) {
                let dps = result.data[0].dps;
                let localTime = (new Date()).getTime() + CONFIG.TIMEZONE_OFFSET;
                for (let timestamp in dps) {
                    if (timestamp * 1000 < localTime) {
                        data.push([timestamp * 1000 + CONFIG.TIMEZONE_OFFSET, dps[timestamp]]);
                    }
                }
            }
            return data;
        },
        parseTopicFromEndpoint: function(endpoint) {
            let prefix = "ws://wamprouter/ws?topic=";
            return endpoint.substr(prefix.length);
        },
        getMockHeatObservation: function() {
            return  [
                "@prefix hmtr: <http://purl.org/NET/ssnext/heatmeters#> .",
                "@prefix meter: <http://purl.org/NET/ssnext/meters/core#> .",
                "@prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> .",
                "@prefix xsd: <http://www.example.org/> .",
                "@prefix : <coap://fdsfs:6500/meter/heat#> .",

                ":54543543534 a hmtr:HeatObservation ;",
                "    ssn:observationResultTime \"3433\"^^xsd:dateTime ;",
                "    ssn:observedBy <coap://fdsfs:6500/meter> ;",
                "    ssn:observationResult :54543543534-result .",

                ":54543543534-result a hmtr:HeatSensorOutput ;",
                "    ssn:isProducedBy <coap://fdsfs:6500/meter> ;",
                "    ssn:hasValue :54543543534-resultvalue .",

                ":54543543534-resultvalue a hmtr:HeatValue ;",
                "    meter:hasQuantityValue \"{0}\"^^xsd:float .".format(Math.ceil(Math.random() * 50))
            ].join('\n');
        },
        getMockNewSystem: function() {
            return [
                "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .",
                "@prefix ssn: <http://purl.oclc.org/NET/ssnx/ssn#> .",
                "@prefix hmtr: <http://purl.org/NET/ssnext/heatmeters#> .",
                "@prefix ssncom: <http://purl.org/NET/ssnext/communication#> .",

                "<coap://10.1.1.2:{0}/meter> a hmtr:HeatMeter ;".format(counter++),
                "    rdfs:label \"Heat Meter #{0}\" ;".format(counter),
                "    ssn:hasSubsystem <coap://10.1.1.2:{0}/meter/temperature> ;".format(counter),
                "    ssn:hasSubsystem <coap://10.1.1.2:{0}/meter/heat> .".format(counter),

                "<coap://10.1.1.2:545/meter/temperature> a ssn:Sensor ;",
                "    ssn:observes hmtr:Temperature ;",
                "    ssncom:hasCommunicationEndpoint <coap://10.1.1.2:545/meter/temperature/obs> .",

                "<coap://10.1.1.2:545/meter/heat> a ssn:Sensor ;",
                "    ssn:observes hmtr:Heat ;",
                "    ssncom:hasCommunicationEndpoint <coap://10.1.1.2:545/meter/heat/obs> .",

                "<coap://10.1.1.2:545/meter/temperature/obs> a ssncom:CommunicationEndpoint ;",
                "    ssncom:protocol \"COAP\" .",
                "<coap://10.1.1.2:545/meter/heat/obs> a ssncom:CommunicationEndpoint ;",
                "    ssncom:protocol \"COAP\" ."
            ].join('\n');
        }
    };

    return instance;
}
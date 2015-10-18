"use strict";

export default function() {

    const baseChartConfig = {
        useHighStocks: true,
        xAxis: {
            type: 'datetime'
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
            {}
        ]
    };

    // store pair `uri`: { ... }
    let STATE_MAP = {};

    const instance = {
        getObservationChartConfig: function(title) {
            return $.extend({}, baseChartConfig, {
                options: {
                    chart: {
                        type: "spline"
                    },
                    timezoneOffset: 3 * 60
                },
                title: {
                    text: title
                }
            });
        },
        getStateChartConfig: function(title, states) {
            STATE_MAP = {};

            states.forEach((s, index) => {
                let uri = s.uri.replace(':', '_').replace('#', '-');
                STATE_MAP[uri] = {
                    index: index + 1,
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

            return $.extend({}, baseChartConfig, {
                yAxis: {
                    categories: Object.keys(STATE_MAP),
                    endOnTick: true,
                    minRange: states.length + 5, // 5 is magic empirical number. for somehow, states.length is not enough to show all labels
                    labels: {
                        formatter: function() {
                            return getItemByURI(this.value).label;
                        }
                    }
                },
                title: {
                    text: title
                },
                series: [
                    {
                        step: 'center',
                        tooltip: {
                            pointFormatter: function() {
                                return states[this.y].description;
                            }
                        }
                    }
                ]
            });
        },

        parseObservationChartData(data) {
            // TODO
        },
        parseStateChartData(data) {
            let values = [];

            data.map((item) => {
                for (let timestamp in item.dps) {
                    values.push([timestamp * 1000, STATE_MAP[item.tags.enum_value] ? STATE_MAP[item.tags.enum_value].index : 0 ]);
                }
            });

            return values.sort((a, b) => {
                return a[0] > b[0] ? 1 : -1;
            });
        }
    };

    return instance;
}
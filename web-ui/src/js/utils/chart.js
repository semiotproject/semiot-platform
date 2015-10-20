"use strict";

export default function(CONFIG) {

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
                let uri = this.convertValueToTSDBNotation(s.uri);
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

            return $.extend({}, baseChartConfig, {
                yAxis: {
                    categories: Object.keys(STATE_MAP),
                    endOnTick: true,
                    minRange: states.length,
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
            let values = [];

            if (data.length > 0) {
                let dps = data[0].dps;
                let localTime = (new Date()).getTime() + CONFIG.TIMEZONE_OFFSET;
                for (let timestamp in dps) {
                    if (timestamp * 1000 < localTime) {
                        values.push([timestamp * 1000 + CONFIG.TIMEZONE_OFFSET, dps[timestamp]]);
                    }
                }
            }

            return values;
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
        convertValueToTSDBNotation(value) {
            return value.replace(':', '_').replace('#', '-');
        },
        parseStateChartValue(value) {
            value = this.convertValueToTSDBNotation(value);
            return STATE_MAP[value] ? STATE_MAP[value].index : 0;
        }
    };

    return instance;
}
const DEFAULT_HOSTNAME = "winghouse.semiot.ru";

const HTTP_PROTOCOL = 'https:';
const WS_PROTOCOL = HTTP_PROTOCOL === 'https:' ? 'wss:' : 'ws:';

// change it to target BASE_HOSTNAME when develop
const BASE_HOSTNAME = location.hostname;

const BASE_URL = `${HTTP_PROTOCOL}//${BASE_HOSTNAME}`;

export default {
    VERSION: `@VERSION`,
    URLS: {
        base: BASE_URL,
        currentUser: `${BASE_URL}/user`,
        messageBus: `${WS_PROTOCOL}//${BASE_HOSTNAME}/wamp`,
        tripleStore: `${BASE_URL}:3030/ds/query`,
        tsdb: {
            // archiveQuantity: `${BASE_URL}?start={0}&end={1}&m=sum:{2}`,
            archiveQuantity: `${BASE_URL}/systems/{0}/observations?sensor_id={1}&start={2}`,
            archiveEnum: `${BASE_URL}/?start={0}&end={1}&m=sum:{2}{enum_value=*}`,
            last: `${BASE_URL}/last/{0}`
        },
        systems: {
            list: `${BASE_URL}/systems`
        }
    },
    TOPICS: {
        "device_registered": 'ru.semiot.devices.newandobserving',
        "new_observation": 'fixme',
        "device_turned_off": 'ru.semiot.devices.turnoff',
        "device_remove": 'ru.semiot.devices.remove'
    },
    TIMEZONE_OFFSET: -1 * new Date().getTimezoneOffset() * 60 * 1000 // in ms
};

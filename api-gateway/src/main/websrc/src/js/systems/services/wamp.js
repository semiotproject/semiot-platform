import autobahn from 'autobahn';
import CONFIG from '../config';

import currentUser from '../api/user';

let wampConnection = null;
let wampSession = null;
const wampSubscriptions = {};
const onWampConnectionOpenCallbacks = [];

const connect = () => {
    console.info(`creating WAMP connection; first, loading current user..`);
    currentUser.getCurrentUser().then((res) => {
        const user = res.data;
        console.info(`current user loaded: `, user);
        debugger;
        wampConnection = new autobahn.Connection({
            url: CONFIG.URLS.messageBus,
            realm: 'realm1',
            authmethods: ["ticket"],
            authid: user.username,
            onchallenge: (session, method, extra) => {
                if (method === "ticket") {
                    console.info(`authorising on WAMP wit ticket method`);
                    return user.password;
                }
                console.error(`unknown WAMP authentication method '${method}'`);
            }
        });
        wampConnection.onopen = function(session) {
            console.info(`WAMP connection opened; session instantiated`);
            wampSession = session;
            onWampConnectionOpenCallbacks.map((c) => {
                c(session);
            })
        };
        wampConnection.onclose = function() {
            console.warn(`WAMP connection closed; reconnecting..`, arguments);
        };
        wampConnection.open();
    });
};

const ensureSession = (callback) => {
    if (wampSession) {
        callback(wampSession);
    } else {
        console.info(`WAMP connection is not ready yet; waiting..`);
        onWampConnectionOpenCallbacks.push(callback);
    }
};

connect();

export default {
    subscribe({ topic, callback }) {
        ensureSession((s) => {
            console.info(`subscribing to topic ${topic}..`);
            s.subscribe(topic, callback).then((subscr) => {
                console.info(`subscription to ${topic} created`);
                wampSubscriptions[topic] = subscr;
            });
        });
    },
    unsubscribe(topic) {
        ensureSession((s) => {
            if (!wampSubscriptions[topic]) {
                console.warn(`not found subscriptions for topic ${topic}`);
                return;
            }
            console.log(`unsubscribing from ${topic}..`);
            s.unsubscribe(wampSubscriptions[topic]);
            wampSubscriptions[topic] = undefined;
        });
    }
};
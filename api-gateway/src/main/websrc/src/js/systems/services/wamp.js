"use strict";

import autobahn from 'autobahn';

export default function(CONFIG, currentUser) {

    // `endpoint` : { `topic`:`autobahn.Subscribtion` }
    let _sessions = {};

    // hash `topic`:`autobahn.Subscribtion`
    // let _subscriptions = {};

    // lasy initialisation
    const checkSession = (endpoint, callback) => {
        console.info(`checking WAMP session for endpoint ${endpoint}`);
        if (_sessions[endpoint]) {
            console.info(`session for endpoint ${endpoint} already exists; reusing`);
            callback(_sessions[endpoint]);
            return;
        }
        console.info(`not found opened session for endpoint ${endpoint}; connecting..`);
        currentUser.getCurrentUser().then((res) => {
            const user = res.data;
            // remove this in production
            // user = {
            //     username: "root",
            //     password: "root"
            // };
            console.log('initialising WAMP session...');
            let connection = new autobahn.Connection({
                url: endpoint,
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
            connection.onopen = function(session) {
                if (!_sessions[endpoint]) {
                    _sessions[endpoint] = session;
                } else {
                    console.warn(`session opened, but another session for endpoint ${endpoint} already registered; possible race run?`);
                }
                callback(_sessions[endpoint]);
            };
            connection.open();
        });
    };

    const instance = {
        subscribe({ topic, endpoint = CONFIG.URLS.messageBus, callback }) {
            checkSession(endpoint, (s) => {
                if (!topic) {
                    throw new Error('WAMP topic is required');
                }
                console.info(`subscribing to ${topic}..`);
                s.subscribe(topic, callback).then((subscr) => {
                    console.info(`subscription to ${topic} created`);
                });
            });
        },
        unsubscribe(topic) {
            /*
            checkSession((s) => {
                if (!_subscriptions[topic]) {
                    console.warn(`not found subscriptions for topic ${topic}`);
                    return;
                }
                console.log(`unsubscribing from ${topic}..`);
                s.unsubscribe(_subscriptions[topic]);
            });
            */
        }
    };

    return instance;
}
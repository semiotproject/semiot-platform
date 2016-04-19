"use strict";

import autobahn from 'autobahn';

export default function(CONFIG) {

    let _session;

    // hash `topic`:`autobahn.Subscribtion`
    let _subscriptions = {};

    // lasy initialisation
    const checkSession = (callback) => {
        if (_session) {
            callback(_session);
            return;
        }
        $.get(CONFIG.URLS.currentUser).done((user) => {
            user = JSON.parse(user);
            console.log('initialising WAMP session...');
            let connection = new autobahn.Connection({
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
            connection.onopen = function(session) {
                if (!_session) {
                     _session = session;
                }
                callback(_session);
            };
            connection.open();
        }).fail(() => {
            console.error('unable to get current user; do nothing with WAMP');
        });
    };

    const instance = {
        subscribe(listeners) {
            checkSession((s) => {
                listeners.forEach(function(listener) {
                    if (!listener.topic) {
                        throw new Error('WAMP topic is required');
                    }
                    console.log(`subscribing to ${listener.topic}..`);
                    _session.subscribe(listener.topic, listener.callback).then((subscr) => {
                        _subscriptions[listener.topic] = subscr;
                    });
                });
            });
        },
        unsubscribe(topic) {
            checkSession((s) => {
                if (!_subscriptions[topic]) {
                    console.warn(`not found subscriptions for topic ${topic}`);
                    return;
                }
                console.log(`unsubscribing from ${topic}..`);
                s.unsubscribe(_subscriptions[topic]);
            });
        }
    };

    return instance;
}
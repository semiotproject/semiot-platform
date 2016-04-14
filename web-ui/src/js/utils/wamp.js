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
        console.log('initialising WAMP session...');
        let connection = new autobahn.Connection({
            url: CONFIG.URLS.messageBus,
            realm: 'realm1'
        });
        connection.onopen = function(session) {
            if (!_session) {
                 _session = session;
            }
            callback(_session);
        };
        connection.open();
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
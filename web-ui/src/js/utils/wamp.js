"use strict";

import autobahn from 'autobahn';

export default function(CONFIG) {

    let _session;

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
            _session = session;
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
                    _session.subscribe(listener.topic, listener.callback);
                });
            });
        },
        unsubscribe(topic) {
            checkSession((s) => {
                console.log(`unsubscribing from ${topic}..`);
                s.unsubscribe(topic);
            });
        }
    };

    return instance;
}
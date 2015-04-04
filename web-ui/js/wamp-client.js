(function() {
	"use strict";

	var Logger = function() {
		this.$el = $("#message-log ul");
	};
	Logger.prototype = {
		constructor: Logger,
		append: function(message) {
			var time = new Date();
			var text =  time.getHours() + ":" + time.getMinutes() + ":" + time.getSeconds() + " " + message;
			var template = $(".message.template").clone(true).removeClass('template');
			template.find('p').text(text);
			this.$el.prepend(template);
		}
	}

	var logger = new Logger();

	var connection;
	$(document).on("ready", function() {
		$("#wamp-subscribe").on('click', function() {
			var url = $('.wamp-url input').val();
			var topic = $('.wamp-topic input').val();
			if(url && topic) {
				connection = new autobahn.Connection({
					url: url,
					realm: 'realm1'
				});
				logger.append("opening connection, url - " + url + ", topic - " + topic);				
				$('.on-listen').show().siblings('.on-config').hide();
				$("#summary .url").text(url);
				$("#summary .topic").text(topic);
				connection.onopen = function (session) {
					session.subscribe(topic, function(args) {
						logger.append("message received: " + args[0]);
					});
				};
				connection.onclose = function (reason, details) {
					logger.append("connection closed: reason: " + reason + ", " + "details: " + details.message);
				};
				connection.open();
			}
		});
	});

})();
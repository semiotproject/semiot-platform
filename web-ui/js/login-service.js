angular.module('loginService', ['commonUtils'])
.factory('loginService', function($q, $http, $interval, commonUtils) {	
	
	var set_cookie = function(name, value, expired_days) {
		var expires = '';
		if (expired_days) {
			var expired = new Date();
			expired.setDate(expired.getDate() + expired_days);
			expires = '; expires=' + expired.toUTCString();
		}
		var cookie_value = escape(value) + expires;
		document.cookie = name + '=' + cookie_value;
	};

	var get_cookie = function(name) {
		var cookies = document.cookie;
		var start = cookies.indexOf(' ' + name + '=');
		if (start == -1) {
			start = cookies.indexOf(name + '=');
		}
		if (start != -1) {
			start = cookies.indexOf('=', start) + 1;
			var end = cookies.indexOf(';', start);
			if (end == -1) {
				end = cookies.length;
			}
			return unescape(cookies.substring(start, end));
		}
		return null;
	};

	var USERS = [
		"b4b8daf4b8ea9d39568719e1e320076f",
		"5cc32e366c87c4cb49e4309b75f57d64"
	]

	return {
		authenticate: function(user, password) {
			var hash = commonUtils.md5(user + password);
			if (USERS.indexOf(hash) > -1) {
				set_cookie(CONFIG.COOKIE_NAME, hash);
				return true;
			} else {
				console.error("failed to authenticate with hash = " + hash);
			}
			return false;
		},
		isLogged: function() {
			if (get_cookie(CONFIG.COOKIE_NAME)) {
				return true;
			}
			return false;
		}
	};

});

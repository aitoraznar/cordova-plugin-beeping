var exec = require("cordova/exec");
var PLUGIN_NAME = "FirebaseAnalytics";

module.exports = {
    logEvent: function(name, params, success, error) {
        exec(success, error, PLUGIN_NAME, "logEvent", [name, params || {}]);
    },
    startBeepingListen: function(userId, success, error) {
        exec(success, error, PLUGIN_NAME, "startBeepingListen", []);
    }
};

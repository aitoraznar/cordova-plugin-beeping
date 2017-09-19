var exec = require("cordova/exec");
var PLUGIN_NAME = "Beeping";

module.exports = {
    startBeepingListen: function(success, error) {
        exec(success, error, PLUGIN_NAME, "startBeepingListen", []);
    },
    stopBeepingListen: function(success, error) {
        exec(success, error, PLUGIN_NAME, "stopBeepingListen", []);
    }
};

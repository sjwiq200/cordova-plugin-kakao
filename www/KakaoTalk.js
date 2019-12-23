var exec = require('cordova/exec');

var KakaoTalk = {
    share : function(options, successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'KakaoTalk', 'share', [options]);
    }
};

module.exports = KakaoTalk;

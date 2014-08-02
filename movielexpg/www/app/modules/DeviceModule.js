angular.module('DeviceModule', [])
.factory('DeviceModule', function() {
    return {
        uuid: function(){
            return device.uuid;
        },
        platform: function(){
            return device.platform;
        }
    };
});

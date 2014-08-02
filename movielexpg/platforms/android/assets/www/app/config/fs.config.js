(function() {
    var
    config_data = {
        FS_CONFIG: {
            MEDIA_DIR: "Translations/"
        },
        GENERAL_CONFIG: {
            APP_NAME: "MoovieFish"
        }
    },
    /* set settings */
    config_module = angular.module('fs.config', []);
    angular.forEach(config_data, function(key, value) {
        config_module.constant(value, key);
    });
}());
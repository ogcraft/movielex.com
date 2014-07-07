(function() {
    var
    config_data = {
        API_CONFIG: {
            REQUEST_URL: "http://mooviefish.com/api/",
            REQUEST_PRMS: {
                dataType: 'json',
                headers: {'Content-type': 'application/json'}
            }
        },
        API_ROUTES: {
            getFilms: {
                url: 'movies/:lang',
                method: "GET"
            },
            getOneFilm : {
                url: 'movie/:lang/:id',
                method: "GET"
            },
            play : {
                url : 'aquire/:uuid/:id',
                method: "POST"
            }
        }
    },
    /* set settings */
    config_module = angular.module('api.config', []);
    angular.forEach(config_data, function(key, value) {
        config_module.constant(value, key);
    });
}());
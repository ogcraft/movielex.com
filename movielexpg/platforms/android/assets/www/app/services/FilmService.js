angular.module('MoovieFish')
.factory('FilmService', function(API, LanguageService) {
    return {
        getFilms: function(callback) {
            /* add lang param to passed params */
            API("getFilms", {
                lang: LanguageService.getCurrentLanguage()
            }, callback);
        },
        getOneFilm: function(id, callback){
            /* add lang param to passed params */
            API("getOneFilm", {
                id: id,
                lang: LanguageService.getCurrentLanguage()
            }, callback);
        }
    };
});

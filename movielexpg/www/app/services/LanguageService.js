angular.module('MoovieFish')
.factory('LanguageService', function(LocalStorageAdapter, CURRENT_LANGUAGE, LOCALIZATION) {

    if (!LocalStorageAdapter.get("CURRENT_LANGUAGE"))
        LocalStorageAdapter.set("CURRENT_LANGUAGE", CURRENT_LANGUAGE);
    
    CURRENT_LANGUAGE = LocalStorageAdapter.get("CURRENT_LANGUAGE");
        
    return {
        getCurrentLanguage: function() {
            return CURRENT_LANGUAGE;
        },
        getLanguageList: function() {
            return LOCALIZATION.languages;
        },
        changeLanguage: function(lang) {
            CURRENT_LANGUAGE = lang;
            LocalStorageAdapter.set("CURRENT_LANGUAGE", lang);
            return this.getTranslations();
        },
        getTranslations: function(){
            return LOCALIZATION.translations[CURRENT_LANGUAGE];
        },
        translate: function(tr){
            try{
                return LOCALIZATION.translations[CURRENT_LANGUAGE][tr]
                    ? LOCALIZATION.translations[CURRENT_LANGUAGE][tr]
                    : tr;
            }catch(e){
               return tr
               ? tr
               : "";
            }
        }
    };

});
(function() {
    var
    config_data = {
        default_language : "en",
        LOCALIZATION: {
            languages: ["en", "ar", "he", "ru"],
            translations: {
                ar: {
                    "Language": "لغة",
                    "Play": "اللعب",
                    "Download": "تحميل",
                    "Settings": "إعدادات",
                    "Start" : "بداية",
                    "Pause" : "وقفة",
                    "Stop" : "توقف",
                    "Menu" : "قائمة الطعام",
                    "Are you sure?" : "Are you sure?",
                    "Refresh" : "Refresh",
                    "Films" : "Films",
                    "Downloading" : "Идёт загрузка",
                    "Data location" : "Data location",
                    "Exit" : "Exit"
                },
                he: {
                    "Language": "لغة",
                    "Play": "اللعب",
                    "Download": "تحميل",
                    "Settings": "إعدادات",
                    "Start" : "بداية",
                    "Pause" : "وقفة",
                    "Stop" : "توقف",
                    "Menu" : "قائمة الطعام",
                    "Are you sure?" : "Clean the cache. Are you sure?",
                    "Refresh" : "Refresh",
                    "Films" : "Films",
                    "Downloading" : "Идёт загрузка",
                    "Data location" : "Data location",
                    "Exit" : "Exit"
                },
                en: {
                    "Language": "Language",
                    "Play": "Play",
                    "Download": "Download",
                    "Settings": "Settings",
                    "Start" : "Start",
                    "Pause" : "Pause",
                    "Stop" : "Stop",
                    "Menu" : "Menu",
                    "Are you sure?" : "Are you sure?",
                    "Refresh" : "Refresh",
                    "Films" : "Films",
                    "Downloading" : "Downloading",
                    "Data location" : "Data location",
                    "Exit" : "Exit"
                },
                ru: {
                    "Language": "Язык",
                    "Play": "Слушать",
                    "Download": "Загрузка",
                    "Settings": "Настройки",
                    "Start" : "Старт",
                    "Pause" : "Пауза",
                    "Stop" : "Стоп",
                    "Menu" : "Меню",
                    "Are you sure?" : "Вы уверены, что хотите очистить кэш?",
                    "Refresh" : "Очистить кэш",
                    "Films" : "Фильмы",
                    "Downloading" : "Идёт загрузка",
                    "Data location" : "Путь сохранения",
                    "Exit" : "Выход"
                }
            }
        }
    };

    angular.module('localization', [])
        .constant("LOCALIZATION", config_data.LOCALIZATION)
        .value("CURRENT_LANGUAGE", config_data.default_language);
}());

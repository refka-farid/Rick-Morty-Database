package com.shevaalex.android.rickmortydatabase.utils.networking;


public abstract class ApiConstants {
    public static final String INFO = "info";
    public static final String PAGES = "pages";
    public static final String RESULTS_ARRAY = "results";
    public static final String BASE_URL = "https://rickandmortyapi.com/api/";
    public static final String KEY_QUERY_PAGE = "page";
    public static final String KEY_PAGE_PREV = "prev";
    public static final String KEY_PAGE_NEXT = "next";

    public static class ApiCallCharacterKeys {
        public static final String SUB_URL_CHARACTER = "character/";
        public static final String BASE_URL_CHARACTER_PAGES = "https://rickandmortyapi.com/api/character/?page=";

        public static final String CHARACTER_ID = "id";
        public static final String CHARACTER_NAME = "name";
        public static final String CHARACTER_STATUS = "status";
        public static final String CHARACTER_SPECIES = "species";
        public static final String CHARACTER_TYPE = "type";
        public static final String CHARACTER_GENDER = "gender";
        public static final String CHARACTER_ORIGIN_LOCATION = "origin";
        public static final String CHARACTER_LAST_LOCATION = "location";
        public static final String CHARACTER_LOCATIONS_URL = "url";
        public static final String CHARACTER_LOCATIONS_SUBSTRING = "location/";
        public static final String CHARACTER_IMAGE_URL = "image";
        public static final String CHARACTER_EPISODE_LIST = "episode";
    }

    public static class ApiCallLocationKeys {
        public static final String SUB_URL_LOCATION = "location/";
        public static final String BASE_URL_LOCATION_PAGES = "https://rickandmortyapi.com/api/location/?page=";
        public static final String BASE_URL_LOCATION = "https://rickandmortyapi.com/api/location/";
        public static final String LOCATION_ID = "id";
        public static final String LOCATION_NAME = "name";
        public static final String LOCATION_TYPE = "type";
        public static final String LOCATION_DIMENSION = "dimension";
        public static final String LOCATION_RESIDENTS = "residents";
    }

    public static class ApiCallEpisodeKeys {
        public static final String SUB_URL_EPISODE = "episode/";
        public static final String BASE_URL_EPISODE_PAGES = "https://rickandmortyapi.com/api/episode/?page=";

        public static final String EPISODE_ID = "id";
        public static final String EPISODE_NAME = "name";
        public static final String EPISODE_AIR_DATE = "air_date";
        public static final String EPISODE_CODE = "episode";
        public static final String EPISODE_CHARACTERS = "characters";
    }
}
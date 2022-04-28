package com.example.roomie.core

object Constants {

    // Retrofit base url
    private const val LOCAL_SERVER = "http://10.0.2.2:8080/"
    private const val PROD_SERVER = "https://msp-WS2122-2.mobile.ifi.lmu.de/"
    const val BASE_URL: String = PROD_SERVER

    // HTTP Codes
    val HTTP_SUCCESS = 200..299
    val HTTP_ERROR = 400..599

    // TMP ID
    const val TMP_ID = -1

    const val FORCE_FETCH = true


    // Default caching threshold
    const val CACHING_THRESHOLD: Long = 60000 * 10

    // FetchDate format
    const val SQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
    const val BACKEND_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"
    const val FINANCE_TRANSACTION_DATE_FORMAT = "d. MMM"
    const val FINANCE_TRANSACTION_TIME_FORMAT = "HH:mm"

    // SavedStateHandle
    const val PARAM_SHOPPING_LIST_ID: String = "shoppingListId"

    // Tab indices
    // Finance
    const val FINANCE_OVERVIEW_INDEX      = 0
    const val FINANCE_STATISTICS_INDEX    = 1
    const val FINANCE_GRAPH_INDEX         = 2
    const val FINANCE_ARCHIVE_INDEX       = 3

    // Home
    const val HOME_OVERVIEW_INDEX = 0
    const val HOME_NEWS_INDEX     = 1

    // Flats
    const val FLAT_JOIN_INDEX   = 0
    const val FLAT_CREATE_INDEX = 1

}
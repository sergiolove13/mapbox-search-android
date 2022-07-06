package com.mapbox.search.autofill

import android.content.Context
import com.mapbox.geojson.Point
import com.mapbox.search.MapboxSearchSdk
import com.mapbox.search.SearchEngineSettings

/**
 * Main entrypoint to the Mapbox Autofill API. Performs forward or reverse geocoding requests to fetch addresses.
 */
public interface AddressAutofill {

    /**
     * Performs reverse geocoding request.
     *
     * @param point Coordinate to resolve.
     * @param options Request options.
     * @return Result of the search request represented by [AddressAutofillResponse].
     */
    public suspend fun suggestions(
        point: Point,
        options: AddressAutofillOptions
    ): AddressAutofillResponse

    /**
     * Performs forward geocoding request.
     *
     * @param query Search query.
     * @param options Request options.
     * @return Result of the search request represented by [AddressAutofillResponse].
     */
    public suspend fun suggestions(
        query: String,
        options: AddressAutofillOptions
    ): AddressAutofillResponse

    /**
     * @suppress
     */
    public companion object {

        /**
         * Creates a new instance of the [AddressAutofill].
         *
         * @param context The Context of the Android Application.
         * @param accessToken [Mapbox Access Token](https://docs.mapbox.com/help/glossary/access-token/).
         *
         * @return a new instance instance of [AddressAutofill].
         */
        public fun create(context: Context, accessToken: String): AddressAutofill {
            val settings = SearchEngineSettings(context.applicationContext, accessToken)
            return AddressAutofillImpl(
                MapboxSearchSdk.internalCreateAutofillSearchEngine(settings)
            )
        }
    }
}

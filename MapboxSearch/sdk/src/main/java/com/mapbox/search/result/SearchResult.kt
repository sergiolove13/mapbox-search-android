package com.mapbox.search.result

import android.os.Parcelable
import com.mapbox.geojson.Point
import com.mapbox.search.RequestOptions
import com.mapbox.search.SearchResultMetadata
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.utils.extension.mapToPlatform
import com.mapbox.search.common.RoutablePoint
import com.mapbox.search.mapToPlatform
import com.mapbox.search.record.IndexableRecord
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Resolved search object with populated fields.
 */
@Parcelize
public class SearchResult internal constructor(
    internal val base: BaseSearchResult
) : Parcelable {

    /**
     * Search request options.
     */
    @IgnoredOnParcel
    public val requestOptions: RequestOptions = base.requestOptions.mapToPlatform()

    /**
     * Result unique identifier.
     */
    @IgnoredOnParcel
    public val id: String = base.id

    /**
     * Result name.
     */
    @IgnoredOnParcel
    public val name: String = base.name

    /**
     * The feature name, as matched by the search algorithm.
     */
    @IgnoredOnParcel
    public val matchingName: String? = base.matchingName

    /**
     * Additional description for the search result.
     */
    @IgnoredOnParcel
    public val descriptionText: String? = base.descriptionText

    /**
     * Result address.
     */
    @IgnoredOnParcel
    public val address: SearchAddress? = base.address?.mapToPlatform()

    /**
     * Full formatted address.
     */
    @IgnoredOnParcel
    public val fullAddress: String? = base.fullAddress

    /**
     * List of points near [coordinate], that represents entries to associated building.
     */
    @IgnoredOnParcel
    public val routablePoints: List<RoutablePoint>? = base.routablePoints?.map { it.mapToPlatform() }

    /**
     * Poi categories. Always empty for non-POI search results.
     * @see types
     */
    @IgnoredOnParcel
    public val categories: List<String>? = base.categories

    /**
     * [Maki](https://github.com/mapbox/maki/) icon name for search result.
     */
    @IgnoredOnParcel
    public val makiIcon: String? = base.makiIcon

    /**
     * Result coordinates.
     */
    @IgnoredOnParcel
    public val coordinate: Point = base.coordinate

    /**
     * A point accuracy metric for the returned address.
     */
    @IgnoredOnParcel
    public val accuracy: ResultAccuracy? = base.accuracy?.mapToPlatform()

    /**
     * Non-empty list of resolved [SearchResult] types.
     */
    @IgnoredOnParcel
    public val types: List<SearchResultType> = base.types.map { it.mapToPlatform() }

    /**
     * Estimated time of arrival (in minutes) based on the specified in the [com.mapbox.search.SearchOptions] origin point and navigation profile.
     * For [com.mapbox.search.result.SearchResult] this property is not null
     * only if it was present in the corresponding [com.mapbox.search.result.SearchSuggestion].
     *
     * You can always calculate ETA on your own using user's location and result's [coordinate].
     */
    @IgnoredOnParcel
    public val etaMinutes: Double? = base.etaMinutes

    /**
     * Search result metadata containing geo place's detailed information if available.
     */
    @IgnoredOnParcel
    public val metadata: SearchResultMetadata? = base.metadata?.let { SearchResultMetadata(it) }

    /**
     * Experimental API, can be changed or removed in the next SDK releases.
     * Map of external ids. Returned Map instance is unmodifiable.
     */
    @IgnoredOnParcel
    public val externalIDs: Map<String, String> = base.externalIDs

    /**
     * Distance in meters from result to requested origin (for forward geocoding and category search) or provided point (for reverse geocoding).
     * For provided point always returns non-null distance.
     */
    @IgnoredOnParcel
    public val distanceMeters: Double? = base.distanceMeters

    /**
     * Index in response from server.
     */
    @IgnoredOnParcel
    public val serverIndex: Int? = base.serverIndex

    /**
     * Returns [IndexableRecord] if this [SearchResult] is based on
     * item from [com.mapbox.search.record.IndexableDataProvider], null otherwise.
     */
    @IgnoredOnParcel
    public val indexableRecord: IndexableRecord? =
        base.indexableRecord?.sdkResolvedRecord as? IndexableRecord

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SearchResult

        if (base != other.base) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        return base.hashCode()
    }

    /**
     * @suppress
     */
    override fun toString(): String {
        return "SearchResult(" +
                "requestOptions=$requestOptions, " +
                "id='$id', " +
                "name='$name', " +
                "matchingName=$matchingName, " +
                "descriptionText=$descriptionText, " +
                "address=$address, " +
                "fullAddress=$fullAddress, " +
                "routablePoints=$routablePoints, " +
                "categories=$categories, " +
                "makiIcon=$makiIcon, " +
                "coordinate=$coordinate, " +
                "accuracy=$accuracy, " +
                "types=$types, " +
                "etaMinutes=$etaMinutes, " +
                "metadata=$metadata, " +
                "externalIDs=$externalIDs, " +
                "distanceMeters=$distanceMeters, " +
                "serverIndex=$serverIndex, " +
                "indexableRecord=$indexableRecord" +
                ")"
    }
}

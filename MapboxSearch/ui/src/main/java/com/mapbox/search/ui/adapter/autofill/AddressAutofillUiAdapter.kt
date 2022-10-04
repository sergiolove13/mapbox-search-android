package com.mapbox.search.ui.adapter.autofill

import android.Manifest
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.search.autofill.AddressAutofill
import com.mapbox.search.autofill.AddressAutofillOptions
import com.mapbox.search.autofill.AddressAutofillResponse
import com.mapbox.search.autofill.AddressAutofillSuggestion
import com.mapbox.search.autofill.Query
import com.mapbox.search.base.failDebug
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.view.SearchResultAdapterItem
import com.mapbox.search.ui.view.SearchResultsView
import com.mapbox.search.ui.view.UiError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Helper class that implements search-specific logic over [AddressAutofill]
 * and shows search results on the [SearchResultsView].
 */
public class AddressAutofillUiAdapter(

    /**
     * [SearchResultsView] for displaying search results.
     */
    private val view: SearchResultsView,

    /**
     * Address autofill engine.
     */
    private val addressAutofill: AddressAutofill,

    /**
     * The mechanism responsible for providing location approximations to the SDK.
     * By default [LocationEngine] is retrieved from [LocationEngineProvider.getBestLocationEngine].
     * Note that this class requires [Manifest.permission.ACCESS_COARSE_LOCATION] or
     * [Manifest.permission.ACCESS_FINE_LOCATION] to work properly.
     */
    locationEngine: LocationEngine = LocationEngineProvider.getBestLocationEngine(view.context.applicationContext),
) {

    private val itemsCreator = AutofillItemsCreator(view.context, locationEngine)
    private val searchListeners = CopyOnWriteArrayList<SearchListener>()

    @Volatile
    private var latestQueryOptions: Pair<Query, AddressAutofillOptions>? = null

    @Volatile
    private var currentRequestJob: Job? = null

    private var searchResultsShown: Boolean = false

    init {
        view.addActionListener(object : SearchResultsView.ActionListener {

            override fun onResultItemClick(item: SearchResultAdapterItem.Result) {
                when (val payload = item.payload) {
                    is AddressAutofillSuggestion -> searchListeners.forEach { it.onSuggestionSelected(payload) }
                    else -> failDebug {
                        "Unknown adapter item payload: $payload"
                    }
                }
            }

            override fun onErrorItemClick(item: SearchResultAdapterItem.Error) {
                view.findViewTreeLifecycleOwner()?.lifecycle?.coroutineScope?.launchWhenStarted {
                    latestQueryOptions?.let { (query, options) ->
                        search(query, options)
                    }
                }
            }

            override fun onHistoryItemClick(item: SearchResultAdapterItem.History) {
                // Should not be called
            }

            override fun onPopulateQueryClick(item: SearchResultAdapterItem.Result) {
                // Should not be called
            }

            override fun onMissingResultFeedbackClick(item: SearchResultAdapterItem.MissingResultFeedback) {
                // Should not be called
            }
        })
    }

    /**
     * Performs suggestions request.
     * @param query The search query.
     * @param options The autofill options.
     */
    @JvmOverloads
    public suspend fun search(query: Query, options: AddressAutofillOptions = AddressAutofillOptions()) {
        currentRequestJob?.let {
            if (it.isActive) {
                it.cancel()
            }
        }

        latestQueryOptions = query to options

        coroutineScope {
            currentRequestJob = launch {
                withContext(Dispatchers.Main) {
                    if (!searchResultsShown) {
                        view.setAdapterItems(itemsCreator.createForLoading())
                    }
                }

                when (val response = addressAutofill.suggestions(query, options)) {
                    is AddressAutofillResponse.Suggestions -> {
                        withContext(Dispatchers.Main) {
                            searchResultsShown = true
                            searchListeners.forEach { it.onSuggestionsShown(response.suggestions) }
                            view.setAdapterItems(itemsCreator.createForSuggestions(response.suggestions, query.query))
                        }
                    }
                    is AddressAutofillResponse.Error -> {
                        withContext(Dispatchers.Main) {
                            searchResultsShown = false
                            searchListeners.forEach { it.onError(response.error) }
                            view.setAdapterItems(itemsCreator.createForError(UiError.createFromException(response.error)))
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds a listener to be notified of search events.
     *
     * @param listener The listener to notify when a search event happened.
     */
    public fun addSearchListener(listener: SearchListener) {
        searchListeners.add(listener)
    }

    /**
     * Removes a previously added listener.
     *
     * @param listener The listener to remove.
     */
    public fun removeSearchListener(listener: SearchListener) {
        searchListeners.remove(listener)
    }

    /**
     * Search results view listener.
     */
    public interface SearchListener {

        /**
         * Called when the [AddressAutofillSuggestion]s are received and displayed on the [view].
         *
         * @param suggestions List of [SearchSuggestion] as result of the first step of forward geocoding.
         * @see AddressAutofill.suggestions
         */
        public fun onSuggestionsShown(suggestions: List<AddressAutofillSuggestion>)

        /**
         * Called when a suggestion is selected by a user.
         *
         * @param suggestion The clicked [AddressAutofillSuggestion].
         */
        public fun onSuggestionSelected(suggestion: AddressAutofillSuggestion)

        /**
         * Called when error occurs during the suggestions request.
         * When this happens, error information is displayed on the [view].
         *
         * @param e Exception, occurred during the request.
         * @see AddressAutofill.suggestions
         */
        public fun onError(e: Exception)
    }
}

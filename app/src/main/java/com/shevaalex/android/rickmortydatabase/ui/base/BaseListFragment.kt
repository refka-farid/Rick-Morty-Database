package com.shevaalex.android.rickmortydatabase.ui.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.Transition
import androidx.viewbinding.ViewBinding
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialFadeThrough
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shevaalex.android.rickmortydatabase.R
import com.shevaalex.android.rickmortydatabase.ui.viewmodel.BaseListViewModel
import com.shevaalex.android.rickmortydatabase.utils.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

abstract class BaseListFragment<T: ViewBinding>: BaseFragment() {

    protected abstract val viewModel: BaseListViewModel

    protected abstract val keyListFilterMap: String

    protected abstract val keyListQuery: String

    protected abstract val keyListPosition: String

    private var _binding: T? = null
    protected val binding get() = _binding!!

    protected var searchSuggestionsAdapter: ArrayAdapter<String>? = null

    protected var recentQueriesAdapter: ArrayAdapter<String>? = null


    override fun onAttach(context: Context) {
        //inject fragment
        injectFragment()
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = setBinding(inflater,container)
        registerObservers()
        inflateToolbar()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //restore the view state
        restoreViewState(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.doOnPreDraw { startPostponedEnterTransition() }
        getToolbar()?.let {
            setupToolbarWithNavController(it)
            setupEdgeToEdgePadding(it)
        }
    }

    override fun onResume() {
        restoreSearchViewState()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        getToolbar()?.let {
            clearUi(it)
        }
        saveRvListPosition()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveViewState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        searchSuggestionsAdapter = null
        recentQueriesAdapter = null
    }

    private fun registerObservers() {
        //set searchView new query suggestions adapter
        viewModel.suggestions.observe(viewLifecycleOwner, { suggestionList ->
            suggestionList?.let {
                searchSuggestionsAdapter = getSearchSuggectionsAdapter(it)
            }
        })
        //set searchView recent suggestions adapter
        viewModel.recentQueries.observe(viewLifecycleOwner, { recentQueries ->
            recentQueries?.let {
                recentQueriesAdapter = getRecentQueriesAdapter(it)
            }
        })
    }

    private fun inflateToolbar() {
        val toolbar: Toolbar? = getToolbar()
        toolbar?.inflateMenu(R.menu.menu_filter)
        setupSearchView()
        setupMenuButtons()
    }

    @SuppressLint("RestrictedApi")
    private fun setupSearchView() {
        val toolbar: Toolbar? = getToolbar()
        toolbar?.let {
            val searchView: SearchView? = it.findViewById(R.id.search_view)
            val searchPlate: SearchView.SearchAutoComplete? =
                    searchView?.findViewById(androidx.appcompat.R.id.search_src_text)
            searchPlate?.setTextAppearance(R.style.TextAppearance_RM_SearchView_Hint)
            searchPlate?.threshold = 0
            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let {
                        val queryText = query.trim().toLowerCase(Locale.getDefault())
                        if (queryText != viewModel.searchQuery) {
                            // if true - this query has never been logged or saved to db table
                            if (viewModel.addLogQuery(queryText)) {
                                //log query to Firebase
                                firebaseLogQuery(queryText)
                                //save query for recent suggestions list
                                lifecycleScope.launch {
                                    viewModel.saveSearchQuery(queryText)
                                }
                            }
                            viewModel.setNameQuery(queryText)
                        }
                    }
                    clearUi(it)
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let { query ->
                        //if query is empty -> show all results in RV and set the suggestions adapter
                        // to show the list of recent queries
                        if (query.isBlank()) {
                            if (query != viewModel.searchQuery) {
                                viewModel.setNameQuery("")
                            }
                            recentQueriesAdapter?.let { adapter ->
                                if (searchPlate?.adapter != adapter) {
                                    searchPlate?.setAdapter(adapter)
                                }
                            }
                        }
                        // else set the suggestions adapter with Character names
                        else {
                            searchSuggestionsAdapter?.let { adapter ->
                                if (searchPlate?.adapter != adapter) {
                                    searchPlate?.setAdapter(adapter)
                                }
                            }
                        }
                    }
                    return false
                }
            })
            searchView?.setOnSuggestionListener(object : SearchView.OnSuggestionListener {
                override fun onSuggestionSelect(position: Int): Boolean {
                    // do nothing
                    return true
                }

                override fun onSuggestionClick(position: Int): Boolean {
                    val newQuery: String? = searchPlate?.adapter?.getItem(position)?.toString()
                    newQuery?.let {
                        searchView.setQuery(newQuery, true)
                    }
                    return true
                }
            })
            //after searchPlate gained focus -> set the adapter to recentQueriesAdapter (if hasn't been set)
            searchPlate?.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus && searchPlate.text.isNullOrBlank()) {
                    recentQueriesAdapter?.let { adapter ->
                        if (searchPlate.adapter != adapter) {
                            searchPlate.setAdapter(adapter)
                        }
                    }
                }
            }
        }
    }

    private fun setupMenuButtons() {
        val toolbar: Toolbar? = getToolbar()
        toolbar?.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.settings_button -> {
                    toolbar.menu?.findItem(R.id.search_view)?.collapseActionView()
                    navigateToSettings()
                    true
                }
                R.id.filter_button -> {
                    showFilterDialog()
                    true
                }
                else -> false
            }
        }
    }

    /**
     * restores view state of SearchView and hides softInput keyboard
     */
    private fun restoreSearchViewState() {
        viewModel.searchQuery?.let {
            val searchView: SearchView? = getToolbar()?.findViewById(R.id.search_view)
            if (it.isNotBlank()) {
                searchView?.setQuery(it, false)
                searchView?.isIconified = false
                searchView?.clearFocus()
            } else {
                searchView?.isIconified = true
            }
        }
    }

    private fun firebaseLogQuery(query: String) {
        Timber.v("logging query to firebase: %s", query)
        val className = "_".plus(this.javaClass.simpleName).toLowerCase(Locale.ROOT)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH.plus(className)) {
            param(FirebaseAnalytics.Param.SEARCH_TERM, query)
        }
    }

    /**
     * called in onActivityCreated() to restore fragment's view state
     */
    private fun restoreViewState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            (it[keyListFilterMap] as String?)?.let {string ->
                val type = object: TypeToken<Map<String, Pair<Boolean, String?>>>() {}.type
                val map = Gson().fromJson<Map<String, Pair<Boolean, String?>>>(string, type)
                Timber.v("savedInstance restoring map: %s", map)
                viewModel.setFilterFlags(map)
            }
            (it[keyListQuery] as String?)?.let {string ->
                Timber.v("savedInstance restoring query: %s", string)
                viewModel.setNameQuery(string)
            }
            (it[keyListPosition] as Parcelable?)?.let { parcelable ->
                viewModel.setLayoutManagerState(parcelable)
            }
        }
    }

    /**
     * called in onSaveInstanceState to save fragment's view state
     */
    private fun saveViewState(outState: Bundle) {
        outState.run {
            viewModel.getFilterMap()?.let {
                val jsonMap = Gson().toJson(it)
                putString(keyListFilterMap, jsonMap)
            }
            viewModel.searchQuery?.let {
                putString(keyListQuery, it)
            }
            viewModel.rvListPosition.value?.let {
                putParcelable(keyListPosition, it)
            }
        }
    }

    private fun navigateToSettings() {
        findNavController().navigate(R.id.action_global_settingsFragment)
    }

    /**
     * sets the recyclerview animation on exit and reenter during fragment transition
     */
    protected fun setExitAndReenterAnimation(listener: Transition.TransitionListener? = null){
        postponeEnterTransition()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            exitTransition = MaterialElevationScale(false).apply {
                duration = resources.getInteger(R.integer.rm_motion_default_large).toLong()
            }
            reenterTransition = MaterialElevationScale(true).apply {
                duration = resources.getInteger(R.integer.rm_motion_default_large).toLong()
                listener?.let {
                    addListener(it)
                }
            }
        } else {
            exitTransition = MaterialFadeThrough().apply {
                duration = resources.getInteger(R.integer.rm_motion_default_large).toLong()
            }
        }
    }

    private fun setupEdgeToEdgePadding(toolbar: Toolbar) {
        toolbar.setOnApplyWindowInsetsListener{ view, insets ->
            val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets)
            val systemWindow = insetsCompat.getInsets(
                    WindowInsetsCompat.Type.statusBars()
            )
            view.updatePadding(top = systemWindow.top)
            insets
        }
    }

    /**
     * called in onPause to save RV list position in ViewModel
     */
    protected abstract fun saveRvListPosition()

    protected abstract fun setupFiltration(mdialog: MaterialDialog)

    /**
     * called to set the check box status depending on filter's map values
     */
    protected abstract fun restoreCheckBoxState(dialogView: View)

    /**
     * gets the string resources for filter values and maps them to appropriate key constants
     */
    protected abstract fun getStringMap(): Map<String, String>

    protected abstract fun injectFragment()

    protected abstract fun setBinding(inflater: LayoutInflater, container: ViewGroup?): T

    protected abstract fun getToolbar(): Toolbar?

    protected abstract fun showFilterDialog()

}
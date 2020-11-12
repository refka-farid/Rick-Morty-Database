package com.shevaalex.android.rickmortydatabase.ui.location

import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shevaalex.android.rickmortydatabase.R
import com.shevaalex.android.rickmortydatabase.RmApplication
import com.shevaalex.android.rickmortydatabase.databinding.FragmentLocationsListBinding
import com.shevaalex.android.rickmortydatabase.ui.BaseListFragment
import com.shevaalex.android.rickmortydatabase.ui.location.LocationAdapter.OnLocationClickListener
import com.shevaalex.android.rickmortydatabase.utils.Constants
import com.shevaalex.android.rickmortydatabase.utils.CustomItemDecoration
import com.shevaalex.android.rickmortydatabase.utils.MyViewModelFactory
import com.shevaalex.android.rickmortydatabase.utils.displayErrorDialog
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import timber.log.Timber
import javax.inject.Inject

class LocationsListFragment : BaseListFragment<FragmentLocationsListBinding>(), OnLocationClickListener {

    @Inject
    lateinit var viewModelFactory: MyViewModelFactory<LocationListViewModel>

    override val viewModel: LocationListViewModel by activityViewModels {
        viewModelFactory
    }

    private var locationAdapter: LocationAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        registerObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationAdapter = null
    }

    private fun setRecyclerView() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity?.let {
                val spanCount = it.applicationContext.resources.getInteger(R.integer.grid_span_count)
                val gridLayoutManager =
                        GridLayoutManager(
                                it.applicationContext,
                                spanCount,
                                RecyclerView.HORIZONTAL,
                                false)
                binding.recyclerviewLocation.layoutManager = gridLayoutManager
                // apply spacing to gridlayout
                val itemDecoration = CustomItemDecoration(it, false)
                binding.recyclerviewLocation.addItemDecoration(itemDecoration)
            }
        } else {
            val linearLayoutManager = LinearLayoutManager(this.activity)
            binding.recyclerviewLocation.layoutManager = linearLayoutManager
            activity?.let {
                ResourcesCompat
                        .getDrawable(it.resources, R.drawable.track_drawable, it.theme)
                        ?.let { drawable ->
                            FastScrollerBuilder(binding.recyclerviewLocation)
                            .setTrackDrawable(drawable)
                            .build()
                        }
            }
        }
        binding.recyclerviewLocation.setHasFixedSize(true)
        //instantiate an adapter and set this fragment as a listener for onClick
        locationAdapter = LocationAdapter(this)
        locationAdapter?.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT
    }

    private fun registerObservers() {
        viewModel.locationList.observe(viewLifecycleOwner, {
            if (it.isEmpty()) {
                viewModel.searchQuery?.let {
                    binding.tvNoResults?.visibility = View.VISIBLE
                }
            } else {
                binding.tvNoResults?.visibility = View.GONE
            }
            //set data to the adapter
            locationAdapter?.submitList(it)
            //set adapter to the recyclerview
            binding.recyclerviewLocation.adapter = locationAdapter
            //restore list position
            viewModel.rvListPosition.value?.let {state ->
                binding.recyclerviewLocation.layoutManager?.onRestoreInstanceState(state)
            }
        })
    }

    override fun injectFragment() {
        activity?.run {
            (application as RmApplication).appComponent
        }?.inject(this)
    }

    override fun setBinding(
            inflater: LayoutInflater,
            container: ViewGroup?
    ): FragmentLocationsListBinding =
            FragmentLocationsListBinding.inflate(inflater, container, false)

    override fun getToolbar(): Toolbar? {
        return binding.toolbarFragmentLocationList
    }

    override fun restoreViewState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            (savedInstanceState[Constants.KEY_FRAGMENT_LOCATION_LIST_FILTER_MAP] as String?)?.let {
                val type = object: TypeToken<Map<String, Pair<Boolean, String?>>>() {}.type
                val map = Gson().fromJson<Map<String, Pair<Boolean, String?>>>(it, type)
                Timber.v("savedInstance restoring map: %s", map)
                viewModel.setFilterFlags(map)
            }
            (savedInstanceState[Constants.KEY_FRAGMENT_LOCATION_LIST_QUERY] as String?)?.let {
                Timber.v("savedInstance restoring query: %s", it)
                viewModel.setNameQuery(it)
            }
            (savedInstanceState[Constants.KEY_FRAGMENT_LOCATION_LIST_LIST_POSITION] as Parcelable?)?.let {
                viewModel.setLayoutManagerState(it)
            }
        }
    }

    override fun saveViewState(outState: Bundle) {
        outState.run {
            viewModel.getFilterMap.value?.let {
                val jsonMap = Gson().toJson(it)
                putString(Constants.KEY_FRAGMENT_LOCATION_LIST_FILTER_MAP, jsonMap)
            }
            viewModel.searchQuery?.let {
                putString(Constants.KEY_FRAGMENT_LOCATION_LIST_QUERY, it)
            }
            viewModel.rvListPosition.value?.let {
                putParcelable(Constants.KEY_FRAGMENT_LOCATION_LIST_LIST_POSITION, it)
            }
        }
    }

    override fun saveRvListPosition() {
        binding.recyclerviewLocation.layoutManager?.onSaveInstanceState()?.let { lmState ->
            viewModel.setLayoutManagerState(lmState)
        }
    }

    override fun navigateToSettings() {
        findNavController().navigate(
                LocationsListFragmentDirections.actionLocationsListFragmentToSettingsFragment()
        )
    }

    override fun showFilterDialog() {
        activity?.let { activity ->
            val dialog = MaterialDialog(activity)
                    .title(R.string.dialog_title)
                    .negativeButton(text = getString(R.string.dialog_negative_button)) {
                        it.dismiss()
                    }
                    .noAutoDismiss()
                    .customView(
                            viewRes = R.layout.dialog_filter_location,
                            scrollable = true)
            val dialogView = dialog.getCustomView()

            val typeShowAll = dialogView.findViewById<MaterialCheckBox>(R.id.type_all)
            val typeCustom = listOf(
                    dialogView.findViewById(R.id.type_planet),
                    dialogView.findViewById(R.id.type_space_station),
                    dialogView.findViewById<MaterialCheckBox>(R.id.type_microverse)
            )
            val dimensionShowAll = dialogView.findViewById<MaterialCheckBox>(R.id.dimension_all)
            val dimensionCustom = listOf(
                    dialogView.findViewById(R.id.dimension_replacement),
                    dialogView.findViewById(R.id.dimension_c_137),
                    dialogView.findViewById(R.id.dimension_cronenberg),
                    dialogView.findViewById<MaterialCheckBox>(R.id.dimension_unknown)
            )

            restoreCheckBoxState(dialogView)

            typeShowAll.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    typeCustom.forEach {
                        it.isChecked = false
                    }
                }
            }
            dimensionShowAll.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    dimensionCustom.forEach {
                        it.isChecked = false
                    }
                }
            }
            typeCustom.forEach {
                it.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && typeShowAll.isChecked) {
                        typeShowAll.isChecked = false
                    }
                }
            }
            dimensionCustom.forEach {
                it.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked && dimensionShowAll.isChecked) {
                        dimensionShowAll.isChecked = false
                    }
                }
            }

            dialog.positiveButton(text = getString(R.string.dialog_positive_button)) { mdialog ->
                if ((typeCustom.any { it.isChecked } || typeShowAll.isChecked)
                        && (dimensionCustom.any { it.isChecked } || dimensionShowAll.isChecked)) {
                    setupFiltration(mdialog)
                } else {
                    val errors: MutableList<String> = mutableListOf()
                    if (typeCustom.all { !it.isChecked } && !typeShowAll.isChecked) {
                        errors.add(getString(R.string.dialog_error_type))
                    }
                    if (dimensionCustom.any { !it.isChecked } && !dimensionShowAll.isChecked) {
                        errors.add(getString(R.string.dialog_error_dimension))
                    }
                    activity.displayErrorDialog(
                            getString(R.string.dialog_error_message)
                                    .plus(errors.joinToString())
                    )
                }
            }

            dialog.show {
                lifecycleOwner(viewLifecycleOwner)
            }
        }
    }

    override fun setupFiltration(mdialog: MaterialDialog) {
        val dialogView = mdialog.getCustomView()

        val stringMap = getStringMap()
        val filterMap = mutableMapOf<String, Pair<Boolean, String?>>()

        //according to the state of a checkbox map the appropriate booleans and string values
        filterMap[Constants.KEY_MAP_FILTER_LOC_TYPE_ALL] =
                if (dialogView.findViewById<MaterialCheckBox>(R.id.type_all).isChecked)
                    Pair(true, null)
                else Pair(false, null)
        filterMap[Constants.KEY_MAP_FILTER_LOC_TYPE_PLANET] =
                if (dialogView.findViewById<MaterialCheckBox>(R.id.type_planet).isChecked)
                    Pair(true, stringMap[Constants.KEY_MAP_FILTER_LOC_TYPE_PLANET])
                else Pair(false, null)
        filterMap[Constants.KEY_MAP_FILTER_LOC_TYPE_SPACE_ST] =
                if (dialogView.findViewById<MaterialCheckBox>(R.id.type_space_station).isChecked)
                    Pair(true, stringMap[Constants.KEY_MAP_FILTER_LOC_TYPE_SPACE_ST])
                else Pair(false, null)
        filterMap[Constants.KEY_MAP_FILTER_LOC_TYPE_MICRO] =
                if (dialogView.findViewById<MaterialCheckBox>(R.id.type_microverse).isChecked)
                    Pair(true, stringMap[Constants.KEY_MAP_FILTER_LOC_TYPE_MICRO])
                else Pair(false, null)
        filterMap[Constants.KEY_MAP_FILTER_LOC_DIMENS_ALL] =
                if (dialogView.findViewById<MaterialCheckBox>(R.id.dimension_all).isChecked)
                    Pair(true, null)
                else Pair(false, null)
        filterMap[Constants.KEY_MAP_FILTER_LOC_DIMENS_REPLACE] =
                if (dialogView.findViewById<MaterialCheckBox>(R.id.dimension_replacement).isChecked)
                    Pair(true, stringMap[Constants.KEY_MAP_FILTER_LOC_DIMENS_REPLACE])
                else Pair(false, null)
        filterMap[Constants.KEY_MAP_FILTER_LOC_DIMENS_C_137] =
                if (dialogView.findViewById<MaterialCheckBox>(R.id.dimension_c_137).isChecked)
                    Pair(true, stringMap[Constants.KEY_MAP_FILTER_LOC_DIMENS_C_137])
                else Pair(false, null)
        filterMap[Constants.KEY_MAP_FILTER_LOC_DIMENS_CRONENBERG] =
                if (dialogView.findViewById<MaterialCheckBox>(R.id.dimension_cronenberg).isChecked)
                    Pair(true, stringMap[Constants.KEY_MAP_FILTER_LOC_DIMENS_CRONENBERG])
                else Pair(false, null)
        filterMap[Constants.KEY_MAP_FILTER_LOC_DIMENS_UNKNOWN] =
                if (dialogView.findViewById<MaterialCheckBox>(R.id.dimension_unknown).isChecked)
                    Pair(true, stringMap[Constants.KEY_MAP_FILTER_LOC_DIMENS_UNKNOWN])
                else Pair(false, null)

        viewModel.setFilterFlags(filterMap.toMap())

        mdialog.dismiss()
    }

    override fun restoreCheckBoxState(dialogView: View) {
        viewModel.getFilterMap.value?.let {
            dialogView.findViewById<MaterialCheckBox>(R.id.type_all).isChecked =
                    it[Constants.KEY_MAP_FILTER_LOC_TYPE_ALL]?.first?: false
            dialogView.findViewById<MaterialCheckBox>(R.id.type_planet).isChecked =
                    it[Constants.KEY_MAP_FILTER_LOC_TYPE_PLANET]?.first?: false
            dialogView.findViewById<MaterialCheckBox>(R.id.type_space_station).isChecked =
                    it[Constants.KEY_MAP_FILTER_LOC_TYPE_SPACE_ST]?.first?: false
            dialogView.findViewById<MaterialCheckBox>(R.id.type_microverse).isChecked =
                    it[Constants.KEY_MAP_FILTER_LOC_TYPE_MICRO]?.first?: false
            dialogView.findViewById<MaterialCheckBox>(R.id.dimension_all).isChecked =
                    it[Constants.KEY_MAP_FILTER_LOC_DIMENS_ALL]?.first?: false
            dialogView.findViewById<MaterialCheckBox>(R.id.dimension_replacement).isChecked =
                    it[Constants.KEY_MAP_FILTER_LOC_DIMENS_REPLACE]?.first?: false
            dialogView.findViewById<MaterialCheckBox>(R.id.dimension_c_137).isChecked =
                    it[Constants.KEY_MAP_FILTER_LOC_DIMENS_C_137]?.first?: false
            dialogView.findViewById<MaterialCheckBox>(R.id.dimension_cronenberg).isChecked =
                    it[Constants.KEY_MAP_FILTER_LOC_DIMENS_CRONENBERG]?.first?: false
            dialogView.findViewById<MaterialCheckBox>(R.id.dimension_unknown).isChecked =
                    it[Constants.KEY_MAP_FILTER_LOC_DIMENS_UNKNOWN]?.first?: false
        }
    }

    override fun getStringMap() = mapOf(
            Constants.KEY_MAP_FILTER_LOC_TYPE_PLANET to getString(R.string.location_Planet),
            Constants.KEY_MAP_FILTER_LOC_TYPE_SPACE_ST to getString(R.string.location_Space_station),
            Constants.KEY_MAP_FILTER_LOC_TYPE_MICRO to getString(R.string.location_Microverse),
            Constants.KEY_MAP_FILTER_LOC_DIMENS_REPLACE to getString(R.string.location_Replacement_Dimension),
            Constants.KEY_MAP_FILTER_LOC_DIMENS_C_137 to getString(R.string.location_Dimension_C_137),
            Constants.KEY_MAP_FILTER_LOC_DIMENS_CRONENBERG to getString(R.string.location_Cronenberg_Dimension),
            Constants.KEY_MAP_FILTER_LOC_DIMENS_UNKNOWN to getString(R.string.character_gender_unknown)
    )

    override fun onLocationClick(position: Int, v: View) {
        val locationList = locationAdapter!!.currentList
        if (locationList != null && !locationList.isEmpty()) {
            val clickedLocation = locationList[position]
            val action = LocationsListFragmentDirections.toLocationDetailFragmentAction()
            clickedLocation?.let {
                action.setLocationName(it.name)
                        .setLocationDimension(it.dimension)
                        .setLocationType(it.type)
                        //.setLocationResidents(it.characters)
                        .locationId = it.id
                v.findNavController().navigate(action)
            }
        }
    }
}
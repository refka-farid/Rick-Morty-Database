package com.shevaalex.android.rickmortydatabase.ui.episode.detail

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shevaalex.android.rickmortydatabase.RmApplication
import com.shevaalex.android.rickmortydatabase.databinding.FragmentEpisodeDetailBinding
import com.shevaalex.android.rickmortydatabase.models.character.CharacterModel
import com.shevaalex.android.rickmortydatabase.models.episode.EpisodeModel
import com.shevaalex.android.rickmortydatabase.ui.BaseDetailFragment
import com.shevaalex.android.rickmortydatabase.ui.CharacterSmallAdapter
import com.shevaalex.android.rickmortydatabase.ui.CharacterSmallAdapter.CharacterClickListener
import com.shevaalex.android.rickmortydatabase.utils.Constants
import com.shevaalex.android.rickmortydatabase.utils.MyViewModelFactory
import javax.inject.Inject

class EpisodeDetailFragment: BaseDetailFragment<FragmentEpisodeDetailBinding, EpisodeModel>() {

    @Inject
    lateinit var viewModelFactory: MyViewModelFactory<EpisodeDetailViewModel>

    private var adapter: CharacterSmallAdapter? = null

    override val keyDetailObject: String = Constants.KEY_FRAGMENT_EPISODE_DETAIL_OBJECT

    override val viewModel: EpisodeDetailViewModel by viewModels {
        viewModelFactory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArgs()?.let {
            setupViews(it)
        }
        setRecyclerView()
        registerObservers()
        binding.buttonBack?.let {
            setBackButton(it)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //restore the view state
        savedInstanceState?.let {
            restoreViewState(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
    }

    /**
     * gets passed data from list fragment
     */
    private fun getArgs(): EpisodeModel? {
        val args: EpisodeDetailFragmentArgs by navArgs()
        return args.episodeObject
    }

    private fun setupViews(episode: EpisodeModel) {
        setShareButton(episode)
        viewModel.setDetailObject(episode)
        binding.imageEpisode?.let {
            setMainImage(episode.imageUrl, it)
        }
        binding.episodeName.text = episode.name
        binding.episodeAirDateValue.text = episode.airDate
        binding.episodeCodeValue?.text = episode.code
        episode.description?.let {
            binding.episodeDescription?.text = it
            binding.episodeDescription?.visibility = View.VISIBLE
        }
    }

    private fun setRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutManager.orientation = RecyclerView.HORIZONTAL
        }
        binding.recyclerviewEpisodeDetail.layoutManager = layoutManager
        binding.recyclerviewEpisodeDetail.setHasFixedSize(true)
        //get recyclerview Adapter and set data to it using ViewModel
        adapter = CharacterSmallAdapter(object : CharacterClickListener {
            override fun onCharacterClick(character: CharacterModel) {
                navigateCharacterDetail(character)
            }
        })
        adapter?.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.recyclerviewEpisodeDetail.adapter = adapter
    }

    private fun registerObservers() {
        viewModel.characters.observe(viewLifecycleOwner, {
            it?.let {
                adapter?.setCharacterList(it)
                //explicilty reset the RV adapter, otherwise RV is invisible at the start of transition
                binding.recyclerviewEpisodeDetail.adapter = adapter
            }
        })
    }

    private fun setShareButton(episode: EpisodeModel) {
        binding.buttonShare?.setOnClickListener {
            shareImageWithGlide(episode.name, episode.imageUrl)
        }
    }

    private fun navigateCharacterDetail(character: CharacterModel) {
        Toast.makeText(requireContext(), character.name, Toast.LENGTH_SHORT).show()
        // do nothing for now
    }

    private fun restoreViewState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            (it[keyDetailObject] as EpisodeModel?)?.let { episode ->
                viewModel.setDetailObject(episode)
            }
        }
    }

    override fun injectFragment() {
        activity?.run {
            (application as RmApplication).appComponent
        }?.inject(this)
    }

    override fun setBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentEpisodeDetailBinding.inflate(inflater, container, false)

}
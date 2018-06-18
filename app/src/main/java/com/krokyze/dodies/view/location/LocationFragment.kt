package com.krokyze.dodies.view.location

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.ComponentTree
import com.krokyze.dodies.R
import com.krokyze.dodies.repository.api.LocationExtra
import com.krokyze.dodies.repository.api.NetworkRequest
import com.krokyze.dodies.repository.data.Location
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_location.*
import org.koin.android.architecture.ext.viewModel

/**
 * Created by krokyze on 05/02/2018.
 */
class LocationFragment : BottomSheetDialogFragment() {

    private val locationUrl by lazy { arguments!!.getString(LOCATION_URL) }
    private val viewModel by viewModel<LocationViewModel> { mapOf("url" to locationUrl) }

    private var disposable: Disposable? = null

    private val componentContext by lazy { ComponentContext(requireContext()) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener { dialogInterface ->
                val dialog = dialogInterface as BottomSheetDialog
                val bottomSheet = dialog.findViewById<View>(android.support.design.R.id.design_bottom_sheet)
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        disposable = viewModel.getLocation()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { (location, locationExtra) ->
                    if (locationExtra is NetworkRequest.Failure) {
                        Toast.makeText(requireContext(), R.string.network_error, Toast.LENGTH_SHORT).show()
                    }

                    onLocation(location, locationExtra)
                }
    }

    private fun onLocation(location: Location, locationExtra: NetworkRequest<LocationExtra>) {
        // fixes disappearing layout on rotation
        if (litho_view.componentTree == null) {
            litho_view.componentTree = ComponentTree.create(componentContext, createLocationComponent(location, locationExtra))
                    .apply { incrementalMount(false) }
                    .build()
        } else {
            litho_view.setComponentAsync(createLocationComponent(location, locationExtra))
        }
    }

    private fun createLocationComponent(location: Location, locationExtra: NetworkRequest<LocationExtra>): Component {
        return LocationComponent.create(componentContext)
                .location(location)
                .locationExtra(locationExtra)
                .onFavoriteClickListener(object : LocationComponentSpec.OnFavoriteClickListener {
                    override fun onFavorite() {
                        viewModel.onFavorite(location)
                    }
                })
                .onSeeMoreClickListener(object : LocationComponentSpec.OnSeeMoreClickListener {
                    override fun onSeeMore() {
                        viewModel.onSeeMore()
                    }
                })
                .build()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable?.dispose()
    }

    companion object {
        private const val LOCATION_URL = "location_url"

        fun newInstance(location: Location): LocationFragment {
            val bundle = Bundle()
            bundle.putString(LOCATION_URL, location.url)
            return LocationFragment().apply { arguments = bundle }
        }
    }
}

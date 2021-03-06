package com.krokyze.dodies.view.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.collection.LruCache
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.krokyze.dodies.R
import com.krokyze.dodies.repository.data.Location
import kotlinx.android.synthetic.main.cluster_item_view.view.*

/**
 * Created by krokyze on 05/02/2018.
 */
class ClusterRenderer(
        context: Context,
        googleMap: GoogleMap,
        clusterManager: ClusterManager<Location>
) : DefaultClusterRenderer<Location>(context, googleMap, clusterManager) {

    private val cache: LruCache<String, BitmapDescriptor> by lazy { LruCache<String, BitmapDescriptor>((Runtime.getRuntime().maxMemory() / 1024 / 8).toInt()) }

    private val itemView: View by lazy { LayoutInflater.from(context).inflate(R.layout.cluster_item_view, null) }
    private val itemPinView: ImageView by lazy { itemView.pin_image_view }
    private val itemTypeView: ImageView by lazy { itemView.type_image_view }
    private val itemIconGenerator by lazy {
        IconGenerator(context).apply {
            setBackground(null)
            setContentView(itemView)
        }
    }

    private val color: Int by lazy { ResourcesCompat.getColor(context.resources, R.color.colorPrimaryDark, null) }

    override fun onBeforeClusterItemRendered(location: Location, markerOptions: MarkerOptions) {
        val cacheKey = location.status.value + location.type.value

        var bitmapDescriptor = cache.get(cacheKey)
        if (bitmapDescriptor == null) {
            val statusResource = when (location.status) {
                Location.Status.VERIFIED -> R.drawable.ic_location_verified
                Location.Status.UNKNOWN -> R.drawable.ic_location
            }
            val typeResource = when (location.type) {
                Location.Type.PICNIC -> R.drawable.ic_type_picnic
                Location.Type.TRAIL -> R.drawable.ic_type_trail
                Location.Type.TOWER -> R.drawable.ic_type_tower
                Location.Type.PARK -> R.drawable.ic_type_park
            }

            itemPinView.setImageResource(statusResource)
            itemTypeView.setImageResource(typeResource)

            val icon = itemIconGenerator.makeIcon()

            bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(icon)
            cache.put(cacheKey, bitmapDescriptor!!)
        }

        markerOptions.icon(bitmapDescriptor)
    }

    override fun getColor(clusterSize: Int): Int {
        return color
    }
}

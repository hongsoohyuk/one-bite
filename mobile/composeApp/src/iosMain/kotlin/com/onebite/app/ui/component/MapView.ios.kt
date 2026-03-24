package com.onebite.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeMapView(
    modifier: Modifier,
    markers: List<MapMarker>,
    onMarkerClick: (Long) -> Unit,
    centerLatitude: Double,
    centerLongitude: Double,
    zoom: Float
) {
    val annotations = remember(markers) {
        markers.map { marker ->
            MKPointAnnotation().apply {
                setCoordinate(CLLocationCoordinate2DMake(marker.latitude, marker.longitude))
                setTitle(marker.title)
                setSubtitle(marker.snippet)
            }
        }
    }

    UIKitView(
        factory = {
            MKMapView().apply {
                val span = MKCoordinateSpanMake(
                    0.01 * (20 - zoom),
                    0.01 * (20 - zoom)
                )
                val region = MKCoordinateRegionMake(
                    CLLocationCoordinate2DMake(centerLatitude, centerLongitude),
                    span
                )
                setRegion(region, animated = false)
                showsUserLocation = true
            }
        },
        update = { mapView ->
            mapView.removeAnnotations(mapView.annotations)
            annotations.forEach { mapView.addAnnotation(it) }
        },
        modifier = modifier
    )
}

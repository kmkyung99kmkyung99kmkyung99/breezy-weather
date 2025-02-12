package org.breezyweather.sources.android

import android.content.Context
import android.location.Geocoder
import android.os.Build
import breezyweather.domain.location.model.Location
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.rx3.rxObservable
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.source.ReverseGeocodingSource
import javax.inject.Inject

class AndroidGeocoderService @Inject constructor() : ReverseGeocodingSource {

    override val id = "nativegeocoder"
    override val name = "Android"
    override val reverseGeocodingAttribution = name

    override fun isReverseGeocodingSupportedForLocation(location: Location): Boolean {
        return Geocoder.isPresent()
    }

    override fun requestReverseGeocodingLocation(
        context: Context,
        location: Location,
    ): Observable<List<Location>> {
        val geocoder = Geocoder(context, context.currentLocale)
        return rxObservable {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            val locationList = mutableListOf<Location>()
            addresses?.getOrNull(0)?.let {
                locationList.add(
                    location.copy(
                        city = it.locality,
                        district = it.subLocality,
                        admin1 = it.adminArea,
                        admin2 = it.subAdminArea,
                        country = it.countryName,
                        countryCode = it.countryCode,
                        // Make sure to update TimeZone in case the user moved
                        timeZone = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            android.icu.util.TimeZone.getDefault().id
                        } else {
                            java.util.TimeZone.getDefault().id
                        }
                    )
                )
            }
            send(locationList)
        }
    }
}

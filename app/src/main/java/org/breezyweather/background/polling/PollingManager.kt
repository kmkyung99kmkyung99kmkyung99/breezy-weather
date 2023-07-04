package org.breezyweather.background.polling

import android.content.Context
import android.os.Build
import org.breezyweather.background.polling.services.permanent.PermanentServiceHelper.startPollingService
import org.breezyweather.background.polling.services.permanent.PermanentServiceHelper.stopPollingService
import org.breezyweather.background.polling.work.WorkerHelper
import org.breezyweather.common.basic.models.options.BackgroundUpdateMethod
import org.breezyweather.common.utils.helpers.AsyncHelper
import org.breezyweather.common.utils.helpers.IntentHelper
import org.breezyweather.db.repositories.LocationEntityRepository.readLocationList
import org.breezyweather.db.repositories.WeatherEntityRepository.readWeather
import org.breezyweather.remoteviews.Notifications
import org.breezyweather.remoteviews.Widgets
import org.breezyweather.settings.SettingsManager

object PollingManager {

    fun resetAllBackgroundTask(context: Context, forceRefresh: Boolean) {
        val settings = SettingsManager.getInstance(context)
        if (forceRefresh) {
            forceRefresh(context)
            return
        }
        if (settings.backgroundUpdateMethod === BackgroundUpdateMethod.NOTIFICATION) {
            WorkerHelper.cancelNormalPollingWork(context)
            WorkerHelper.cancelTodayForecastUpdateWork(context)
            WorkerHelper.cancelTomorrowForecastUpdateWork(context)

            // Polling service is started only if there is a need for one of the background services
            if (settings.updateInterval.intervalInHour != null
                || settings.isTodayForecastEnabled
                || settings.isTomorrowForecastEnabled
            ) {
                startPollingService(context)
            } else {
                stopPollingService(context)
            }
        } else {
            stopPollingService(context)
            settings.updateInterval.intervalInHour?.let {
                WorkerHelper.setNormalPollingWork(context, it)
            } ?: WorkerHelper.cancelNormalPollingWork(context)
            if (settings.isTodayForecastEnabled) {
                WorkerHelper.setTodayForecastUpdateWork(context, settings.todayForecastTime, false)
            } else {
                WorkerHelper.cancelTodayForecastUpdateWork(context)
            }
            if (settings.isTomorrowForecastEnabled) {
                WorkerHelper.setTomorrowForecastUpdateWork(context, settings.tomorrowForecastTime, false)
            } else {
                WorkerHelper.cancelTomorrowForecastUpdateWork(context)
            }
        }
    }

    @JvmStatic
    fun resetNormalBackgroundTask(context: Context, forceRefresh: Boolean) {
        val settings = SettingsManager.getInstance(context)
        if (forceRefresh) {
            forceRefresh(context)
            return
        }
        if (settings.backgroundUpdateMethod === BackgroundUpdateMethod.NOTIFICATION) {
            WorkerHelper.cancelNormalPollingWork(context)
            WorkerHelper.cancelTodayForecastUpdateWork(context)
            WorkerHelper.cancelTomorrowForecastUpdateWork(context)

            // Polling service is started only if there is a need for one of the background services
            if (settings.updateInterval.intervalInHour != null || settings.isTodayForecastEnabled
                || settings.isTomorrowForecastEnabled
            ) {
                startPollingService(context)
            } else {
                stopPollingService(context)
            }
        } else {
            stopPollingService(context)
            settings.updateInterval.intervalInHour?.let {
                WorkerHelper.setNormalPollingWork(context, it)
            } ?: WorkerHelper.cancelNormalPollingWork(context)
        }
    }

    fun resetTodayForecastBackgroundTask(context: Context, forceRefresh: Boolean, nextDay: Boolean) {
        val settings = SettingsManager.getInstance(context)
        if (forceRefresh) {
            forceRefresh(context)
            return
        }
        if (settings.backgroundUpdateMethod === BackgroundUpdateMethod.NOTIFICATION) {
            WorkerHelper.cancelNormalPollingWork(context)
            WorkerHelper.cancelTodayForecastUpdateWork(context)
            WorkerHelper.cancelTomorrowForecastUpdateWork(context)

            // Polling service is started only if there is a need for one of the background services
            if (settings.updateInterval.intervalInHour != null
                || settings.isTodayForecastEnabled
                || settings.isTomorrowForecastEnabled
            ) {
                startPollingService(context)
            } else {
                stopPollingService(context)
            }
        } else {
            stopPollingService(context)
            if (settings.isTodayForecastEnabled) {
                WorkerHelper.setTodayForecastUpdateWork(context, settings.todayForecastTime, nextDay)
            } else {
                WorkerHelper.cancelTodayForecastUpdateWork(context)
            }
        }
    }

    fun resetTomorrowForecastBackgroundTask(context: Context, forceRefresh: Boolean, nextDay: Boolean) {
        val settings = SettingsManager.getInstance(context)
        if (forceRefresh) {
            forceRefresh(context)
            return
        }
        if (settings.backgroundUpdateMethod === BackgroundUpdateMethod.NOTIFICATION) {
            WorkerHelper.cancelNormalPollingWork(context)
            WorkerHelper.cancelTodayForecastUpdateWork(context)
            WorkerHelper.cancelTomorrowForecastUpdateWork(context)

            // Polling service is started only if there is a need for one of the background services
            if (settings.updateInterval.intervalInHour != null
                || settings.isTodayForecastEnabled
                || settings.isTomorrowForecastEnabled
            ) {
                startPollingService(context)
            } else {
                stopPollingService(context)
            }
        } else {
            stopPollingService(context)
            if (settings.isTomorrowForecastEnabled) {
                WorkerHelper.setTomorrowForecastUpdateWork(context, settings.tomorrowForecastTime, nextDay)
            } else {
                WorkerHelper.cancelTomorrowForecastUpdateWork(context)
            }
        }
    }

    private fun forceRefresh(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AsyncHelper.runOnIO {
                val locationList = readLocationList(context)
                for (i in locationList.indices) {
                    locationList[i] = locationList[i].copy(weather = readWeather(locationList[i]))
                }
                Widgets.updateWidgetIfNecessary(context, locationList[0])
                Widgets.updateWidgetIfNecessary(context, locationList)
                Notifications.updateNotificationIfNecessary(context, locationList)
            }
            WorkerHelper.setExpeditedPollingWork(context)
        } else {
            IntentHelper.startAwakeForegroundUpdateService(context)
        }
    }
}

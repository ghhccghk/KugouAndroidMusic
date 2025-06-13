package org.akanework.gramophone.logic.ui

import android.app.Notification
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.ghhccghk.musicplay.R
import com.google.common.collect.ImmutableList

private const val FLAG_ALWAYS_SHOW_TICKER = 0x01000000
private const val FLAG_ONLY_UPDATE_TICKER = 0x02000000

@OptIn(UnstableApi::class)
class MeiZuLyricsMediaNotificationProvider(
    context: MediaSessionService,
    private val tickerProvider: () -> CharSequence?
) : DefaultMediaNotificationProvider(context) {
    private var ticker: CharSequence? = null
    init {
        setSmallIcon(R.drawable.ic_gramophone_monochrome)
    }

    override fun addNotificationActions(
        mediaSession: MediaSession,
        mediaButtons: ImmutableList<CommandButton>,
        builder: NotificationCompat.Builder,
        actionFactory: MediaNotification.ActionFactory
    ): IntArray {
        ticker = tickerProvider()
        builder.setTicker(ticker)
        if (ticker != null) {
            builder.addExtras(Bundle().apply {
                putInt("ticker_icon", R.drawable.ic_gramophone_mono16)
                // set to true if icon changed and SysUI has to dispose of cached one
                putBoolean("ticker_icon_switch", false)
            })
        }
        return super.addNotificationActions(mediaSession, mediaButtons, builder, actionFactory)
    }

    override fun createMediaNotification(
        notificationId: Int,
        notification: Notification,
        reason: @MediaSessionService.NotificationUpdate Int,
        isRebuild: Boolean
    ): MediaNotification {
        val notification = super.createMediaNotification(notificationId, notification, reason, isRebuild)
        val isManual = !isRebuild && reason == MediaSessionService.NOTIFICATION_UPDATE_MANUAL
        if (ticker != null || isManual)
            notification.notification.apply {
                // Keep the status bar lyrics scrolling
                if (ticker != null)
                    flags = flags.or(FLAG_ALWAYS_SHOW_TICKER)
                // Only update the ticker (lyrics), and do not update other properties
                if (isManual)
                    flags = flags.or(FLAG_ONLY_UPDATE_TICKER)
            }
        return notification
    }

}
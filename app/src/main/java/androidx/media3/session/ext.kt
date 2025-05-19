package androidx.media3.session

import android.os.Looper

// see MeiZuLyricsMediaNotificationProvider

var isManualNotificationUpdate = false
    private set

// onUpdateNotificationInternal is package-private
fun MediaSessionService.doUpdateNotification(session: MediaSession) {
    if (Looper.myLooper() != session.player.applicationLooper)
        throw UnsupportedOperationException("wrong looper for doUpdateNotification")
    isManualNotificationUpdate = true
    onUpdateNotificationInternal(session, false)
    isManualNotificationUpdate = false
}
package com.qiscus.pocagora.ui.presentation.page.video

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.TextureView
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ramcosta.composedestinations.annotation.Destination
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas

private const val TAG = "TAGED"

@Destination
@Composable
fun VideoPage(
    channelName: String,
    userRole: String
) {
    val context = LocalContext.current

    val localSurfaceView: TextureView by remember {
        mutableStateOf(TextureView(context))
    }

    var remoteUserMap by remember {
        mutableStateOf(mapOf<Int, TextureView?>())
    }

    val mEngine = remember {
        initEngine(context, object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                Log.d(TAG, "channel:$channel,uid:$uid,elapsed:$elapsed")
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                Log.d(TAG, "onUserJoined:$uid")
                val desiredUserList = remoteUserMap.toMutableMap()
                desiredUserList[uid] = null
                remoteUserMap = desiredUserList.toMap()
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                Log.d(TAG, "onUserOffline:$uid")
                val desiredUserList = remoteUserMap.toMutableMap()
                desiredUserList.remove(uid)
                remoteUserMap = desiredUserList.toMap()
            }

            override fun onError(err: Int) {
                super.onError(err)
                Log.d(TAG, "Error :$err")
            }

            override fun onLocalVideoStat(sentBitrate: Int, sentFrameRate: Int) {
                Log.d(TAG, "Local video  :$sentBitrate --> $sentFrameRate")
                super.onLocalVideoStat(sentBitrate, sentFrameRate)
            }
        }, channelName, userRole)
    }
    if (userRole == "Broadcaster") {
        mEngine.setupLocalVideo(VideoCanvas(localSurfaceView, Constants.RENDER_MODE_FIT, 0))
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(factory = { localSurfaceView }, Modifier.fillMaxSize())
        RemoteView(remoteListInfo = remoteUserMap, mEngine = mEngine)
        UserControls(mEngine = mEngine)
    }
}

@Composable
private fun RemoteView(remoteListInfo: Map<Int, TextureView?>, mEngine: RtcEngine) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(fraction = 0.2f)
            .horizontalScroll(state = rememberScrollState())
    ) {
        remoteListInfo.forEach { entry ->
            val remoteTextureView =
                TextureView(context).takeIf { entry.value == null } ?: entry.value
            AndroidView(
                factory = { remoteTextureView!! },
                modifier = Modifier.size(Dp(180f), Dp(240f))
            )
            mEngine.setupRemoteVideo(
                VideoCanvas(
                    remoteTextureView,
                    Constants.RENDER_MODE_HIDDEN,
                    entry.key
                )
            )
        }
    }
}

fun initEngine(
    current: Context,
    eventHandler: IRtcEngineEventHandler,
    channelName: String,
    userRole: String
): RtcEngine =
    RtcEngine.create(current, "47cf2c42ea8245bfa4f689f35f3f1179", eventHandler).apply {
        enableVideo()
        setChannelProfile(1)
        if (userRole == "Broadcaster") {
            setClientRole(1)
        } else {
            setClientRole(0)
        }
        joinChannel("poc", channelName, "", 0)
    }

@Composable
private fun UserControls(mEngine: RtcEngine) {
    var muted by remember { mutableStateOf(false) }
    var videoDisabled by remember { mutableStateOf(false) }
    val activity = (LocalContext.current as? Activity)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 50.dp),
        Arrangement.SpaceEvenly,
        Alignment.Bottom
    ) {
        OutlinedButton(
            onClick = {
                muted = !muted
                mEngine.muteLocalAudioStream(muted)
            },
            shape = CircleShape,
            modifier = Modifier.size(50.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (muted) Color.Blue else Color.White)
        ) {
            if (muted) {
                Icon(
                    Icons.Rounded.MicOff,
                    contentDescription = "Tap to unmute mic",
                    tint = Color.White
                )
            } else {
                Icon(Icons.Rounded.Mic, contentDescription = "Tap to mute mic", tint = Color.Blue)
            }
        }
        OutlinedButton(
            onClick = {
                mEngine.leaveChannel()
                activity?.finish()
            },
            shape = CircleShape,
            modifier = Modifier.size(70.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Red)
        ) {
            Icon(
                Icons.Rounded.CallEnd,
                contentDescription = "Tap to disconnect Call",
                tint = Color.White
            )

        }
        OutlinedButton(
            onClick = {
                videoDisabled = !videoDisabled
                mEngine.muteLocalVideoStream(videoDisabled)
                mEngine.muteAllRemoteVideoStreams(videoDisabled)
            },
            shape = CircleShape,
            modifier = Modifier.size(50.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = if (videoDisabled) Color.Blue else Color.White)
        ) {
            if (videoDisabled) {
                Icon(
                    Icons.Rounded.VideocamOff,
                    contentDescription = "Tap to enable Video",
                    tint = Color.White
                )
            } else {
                Icon(
                    Icons.Rounded.Videocam,
                    contentDescription = "Tap to disable Video",
                    tint = Color.Blue
                )
            }
        }
    }
}
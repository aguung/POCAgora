package com.qiscus.pocagora.ui.presentation.page.login

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.qiscus.pocagora.ui.presentation.page.destinations.VideoPageDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


@RootNavGraph(start = true)
@Destination
@Composable
fun LoginPage(
    navigator: DestinationsNavigator
) {
    val context = LocalContext.current
    val channelNameState = remember { mutableStateOf(TextFieldValue()) }
    val userRoleOptions = listOf("Broadcaster", "Audience")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(userRoleOptions[0]) }

    val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
        if (areGranted) {
            navigator.navigate(
                VideoPageDestination(
                    channelName = channelNameState.value.text,
                    userRole = selectedOption
                )
            )
        } else {
            Toast.makeText(context, "Need granted all permission", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Qiscus Agora POC", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Jetpack Compose", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(50.dp))

        Column(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            TextField(
                value = channelNameState.value,
                onValueChange = { channelNameState.value = it },
                label = { Text("Channel Name ") },
                placeholder = { Text("test") },
                modifier = Modifier
                    .align(alignment = Alignment.CenterHorizontally)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            userRoleOptions.forEach { text ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .selectable(
                            selected = (text == selectedOption),
                            onClick = { onOptionSelected(text) }
                        )
                ) {
                    RadioButton(
                        selected = (text == selectedOption),
                        modifier = Modifier.padding(
                            horizontal = 25.dp,
                            vertical = 10.dp
                        ),
                        onClick = {
                            onOptionSelected(text)
                        }
                    )

                    Text(
                        text = text,
                        modifier = Modifier.padding(start = 10.dp, top = 10.dp),
                        fontSize = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

        Button(
            onClick = {
                if (permissions.all {
                        ContextCompat.checkSelfPermission(
                            context,
                            it
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                ) {
                    navigator.navigate(
                        VideoPageDestination(
                            channelName = channelNameState.value.text,
                            userRole = selectedOption
                        )
                    )
                } else {
                    launcher.launch(permissions)
                }
            },
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 10.dp
            )
        ) {
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "Join",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = "Join", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}
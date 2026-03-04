package com.sakethh.limae.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.limae.platform.Platform
import com.sakethh.limae.ui.Icons
import com.sakethh.limae.ui.common.showHandOnHover
import limae.app.generated.resources.Res
import limae.app.generated.resources.secretary_bird_png
import org.jetbrains.compose.resources.painterResource

@Composable
fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    val localUriHandler = LocalUriHandler.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier = Modifier.padding(start = 15.dp, end = 15.dp).fillMaxWidth(),
        ) {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer.copy(
                                0.15f,
                            ),
                    ),
                shape = RoundedCornerShape(25.dp),
                modifier =
                    Modifier
                        .fillMaxWidth(),
            ) {
                Column(Modifier.padding(15.dp)) {
                    Box {
                        Image(
                            modifier = Modifier.size(150.dp).clip(RoundedCornerShape(25.dp)),
                            painter = painterResource(Res.drawable.secretary_bird_png),
                            contentDescription = "Secretary bird, the mascot of Limae Project.",
                        )
                        FilledTonalIconButton(
                            shape = RoundedCornerShape(15.dp),
                            modifier =
                                Modifier
                                    .align(Alignment.BottomStart)
                                    .alpha(0.6f)
                                    .scale(0.7f)
                                    .showHandOnHover(),
                            onClick = {
                                localUriHandler.openUri("https://www.artstation.com/artwork/eR8mk3")
                            },
                        ) {
                            Icon(imageVector = Icons.OpenInNew, contentDescription = null)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "limae",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(2.5.dp))
                    Text(
                        text = "writing assistant with no side hustle",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(7.5.dp))
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer.copy(
                                0.15f,
                            ),
                    ),
                shape = RoundedCornerShape(25.dp),
                modifier =
                    Modifier
                        .fillMaxWidth(),
            ) {
                Column(Modifier.padding(15.dp)) {
                    Text(
                        text = "Open. Private. Yours.",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "Limae and its components are open-source under their respective licenses. Runs on your device by default, your server when you want more. Nothing reaches infrastructure you don't control. Your terms, always.",
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Spacer(modifier = Modifier.height(7.5.dp))
            FilledTonalButton(
                modifier =
                    Modifier.fillMaxWidth().showHandOnHover().then(
                        if (Platform.onAndroid) {
                            Modifier.navigationBarsPadding()
                        } else {
                            Modifier.padding(
                                bottom = 15.dp,
                            )
                        },
                    ),
                onClick = onOnboardingComplete,
            ) {
                Text(text = "Get in", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

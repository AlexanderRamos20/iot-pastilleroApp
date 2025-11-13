package com.example.app1.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelControlScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {

                    Text(
                        text = "Panel de control",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Pastillero de ",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Compartimentos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PillSlot(status = PillStatus.OK)
                        PillSlot(status = PillStatus.OK)
                        PillSlot(status = PillStatus.OK)
                        PillSlot(status = PillStatus.MISS)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Historial
                    Text(
                        "Historial de eventos",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                    )
                    HistoryItem("Toma atrasada", HistoryColor.Muted)
                    HistoryItem("Dosis tomada", HistoryColor.Success)
                    HistoryItem("Recarga completada", HistoryColor.Muted)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Estado ambiental
                    Text(
                        "Estado ambiental",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    )  {
                        SensorChip(emoji = "🌡️", value = "21 °C", modifier = Modifier.weight(1f))
                        SensorChip(emoji = "💧", value = "60 %", modifier = Modifier.weight(1f))
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Alertas (chip visual compatible)
                    Text(
                        "Alertas",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 0.dp,
                        border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 40.dp)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Toma atrasada",
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------- Componentes UI --------------------

private enum class PillStatus { OK, MISS }

@Composable
private fun PillSlot(status: PillStatus) {
    Box(
        modifier = Modifier
            .size(62.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(18.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "💊", style = MaterialTheme.typography.headlineSmall)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(6.dp)
                .size(14.dp)
                .background(
                    color = when (status) {
                        PillStatus.OK -> Color(0xFF2E7D32)
                        PillStatus.MISS -> Color(0xFFC62828)
                    },
                    shape = CircleShape
                )
        )
    }
}

private enum class HistoryColor { Success, Muted }

@Composable
private fun HistoryItem(text: String, color: HistoryColor) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = when (color) {
                        HistoryColor.Success -> Color(0xFF2E7D32)
                        HistoryColor.Muted -> MaterialTheme.colorScheme.outline
                    },
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SensorChip(emoji: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
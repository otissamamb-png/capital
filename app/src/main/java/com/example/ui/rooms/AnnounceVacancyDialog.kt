package com.example.ui.rooms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.data.model.VacancyItem
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900

@Composable
fun AnnounceVacancyDialog(
    profile: UserProfile?,
    onDismiss: () -> Unit,
    onSubmit: (VacancyItem) -> Unit
) {
    var roomNumber by remember { mutableStateOf(profile?.room_id?.ifEmpty { "204" } ?: "204") }
    var roomType by remember { mutableStateOf("New Room") }
    var moveOutDate by remember { mutableStateOf("End of this month") }
    var description by remember { mutableStateOf("Moving out for graduation internship. Room is clean, quiet, and ready for a new occupant.") }
    var contactPreference by remember { mutableStateOf("In-App Chat") }
    var isSubmitting by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Announce Vacancy",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Notify the residence community that your room will be vacant.",
                    fontSize = 12.sp,
                    color = Slate500,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Room Number
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room Number") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vacancy_room_number_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Room Type Selection
                Text("Room Classification", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = roomType == "Old Room",
                        onClick = { roomType = "Old Room" },
                        label = { Text("Old (KSh 15k)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = roomType == "New Room",
                        onClick = { roomType = "New Room" },
                        label = { Text("New (KSh 25k)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Move Out Date
                OutlinedTextField(
                    value = moveOutDate,
                    onValueChange = { moveOutDate = it },
                    label = { Text("Expected Move-Out Date") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vacancy_move_out_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Details") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vacancy_description_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        isSubmitting = true
                        val price = if (roomType == "New Room") 25000L else 15000L
                        val item = VacancyItem(
                            room_number = roomNumber,
                            room_type = roomType,
                            price_per_semester = price,
                            expected_move_out_date = moveOutDate,
                            description = description,
                            poster_name = profile?.full_name ?: "Resident",
                            poster_phone = profile?.phone ?: "",
                            contact_preference = contactPreference,
                            title = "Room $roomNumber Move-out Announcement",
                            status = "Approved"
                        )
                        onSubmit(item)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting && roomNumber.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_vacancy_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Post Vacancy Announcement", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

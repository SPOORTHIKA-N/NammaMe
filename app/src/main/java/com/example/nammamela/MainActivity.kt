package com.example.nammamela

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()

        // ✅ Replace with your actual Firebase database URL from Firebase Console
        val database = FirebaseDatabase.getInstance("https://nammamela-d4e59-default-rtdb.asia-southeast1.firebasedatabase.app/")

        setContent {
            App(auth, database)
        }
    }
}

data class Mela(
    val id: String,
    val name: String,
    val date: String,
    val price: Int,
    val image: Int
)

@Composable
fun App(auth: FirebaseAuth, database: FirebaseDatabase) {
    var screen by remember { mutableStateOf("register") }
    var selectedMela by remember { mutableStateOf<Mela?>(null) }

    when (screen) {
        "register" -> RegisterScreen(
            auth = auth,
            onSuccess = { screen = "login" },
            onLogin = { screen = "login" }
        )
        "login" -> LoginScreen(
            auth = auth,
            onSuccess = { screen = "dashboard" },
            onRegister = { screen = "register" }
        )
        "dashboard" -> DashboardScreen(
            onSelect = {
                selectedMela = it
                screen = "seats"
            },
            onProfile = { screen = "profile" },
            onLogout = {
                auth.signOut()
                screen = "login"
            }
        )
        "seats" -> {
            val mela = selectedMela
            if (mela != null) {
                SeatScreen(
                    auth = auth,
                    database = database,
                    mela = mela,
                    onBack = { screen = "dashboard" }
                )
            } else {
                screen = "dashboard"
            }
        }
        "profile" -> ProfileScreen(
            auth = auth,
            database = database,
            onBack = { screen = "dashboard" }
        )
    }
}

@Composable
fun RegisterScreen(
    auth: FirebaseAuth,
    onSuccess: () -> Unit,
    onLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                auth.createUserWithEmailAndPassword(email.trim(), pass.trim())
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { error = it.message ?: "Error" }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
        TextButton(onClick = onLogin) {
            Text("Already have an account? Login")
        }
        if (error.isNotEmpty()) {
            Text(error, color = Color.Red)
        }
    }
}

@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    onSuccess: () -> Unit,
    onRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email.trim(), pass.trim())
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { error = it.message ?: "Error" }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        TextButton(onClick = onRegister) {
            Text("Create account")
        }
        if (error.isNotEmpty()) {
            Text(error, color = Color.Red)
        }
    }
}

@Composable
fun DashboardScreen(
    onSelect: (Mela) -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val shows = listOf(
        Mela("1", "Mahabharata Drama", "20 May", 200, android.R.drawable.ic_menu_camera),
        Mela("2", "Village Circus", "21 May", 150, android.R.drawable.ic_menu_gallery),
        Mela("3", "Comedy Nataka", "22 May", 100, android.R.drawable.ic_menu_compass),
        Mela("4", "Royal Drama", "23 May", 250, android.R.drawable.ic_menu_report_image),
        Mela("5", "Magic Show", "24 May", 180, android.R.drawable.ic_menu_slideshow),
        Mela("6", "Folk Dance", "25 May", 130, android.R.drawable.ic_menu_camera),
        Mela("7", "Devotional Play", "26 May", 160, android.R.drawable.ic_menu_gallery),
        Mela("8", "Street Drama", "27 May", 140, android.R.drawable.ic_menu_compass),
        Mela("9", "History Show", "28 May", 220, android.R.drawable.ic_menu_report_image),
        Mela("10", "Grand Finale", "29 May", 300, android.R.drawable.ic_menu_slideshow)
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row {
            Button(onClick = onProfile) { Text("Profile") }
            Spacer(Modifier.width(10.dp))
            Button(onClick = onLogout) { Text("Logout") }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            items(shows) { m ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(m.name, fontWeight = FontWeight.Bold)
                            Text("Date: ${m.date}")
                            Text("₹${m.price}")
                        }
                        Button(onClick = { onSelect(m) }) {
                            Text("Book")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(14.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp)
    }
}

@Composable
fun SeatScreen(
    auth: FirebaseAuth,
    database: FirebaseDatabase,
    mela: Mela,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allSeats = remember {
        mutableStateListOf<String>().apply {
            for (r in 'A'..'J') for (i in 1..10) add("$r$i")
        }
    }
    val selected = remember { mutableStateListOf<String>() }
    val bookedSeats = remember { mutableStateListOf<String>() }
    var isBooking by remember { mutableStateOf(false) }
    val uid = auth.currentUser?.uid

    DisposableEffect(mela.id) {
        val ref = database.getReference("melaSeats").child(mela.id)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookedSeats.clear()
                for (child in snapshot.children) {
                    val key = child.key
                    if (key != null) bookedSeats.add(key)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("BOOKING", "melaSeats load cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Booking: ${mela.name}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Date: ${mela.date}  |  ₹${mela.price}/seat", color = Color.DarkGray)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendDot(Color(0xFFE0E0E0), "Available")
            LegendDot(Color(0xFF4CAF50), "Selected")
            LegendDot(Color(0xFFEF5350), "Booked")
        }

        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allSeats.size) { index ->
                val seat = allSeats[index]
                val isBooked = bookedSeats.contains(seat)
                val isSelected = selected.contains(seat)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            color = when {
                                isBooked -> Color(0xFFEF5350)
                                isSelected -> Color(0xFF4CAF50)
                                else -> Color(0xFFE0E0E0)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = !isBooked) {
                            if (isSelected) selected.remove(seat) else selected.add(seat)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = seat,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBooked || isSelected) Color.White else Color.Black
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (selected.isNotEmpty()) {
            Text(
                "Selected: ${selected.joinToString(", ")}",
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium
            )
            Text(
                "Total: ₹${mela.price * selected.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (uid == null) {
                    android.util.Log.e("BOOKING", "uid is null")
                    Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (selected.isEmpty()) {
                    Toast.makeText(context, "Please select at least one seat", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isBooking = true

                val userRef = database.getReference("users").child(uid).child("bookings")
                val melaSeatsRef = database.getReference("melaSeats").child(mela.id)
                val bookingId = userRef.push().key

                android.util.Log.d("BOOKING", "uid = $uid")
                android.util.Log.d("BOOKING", "bookingId = $bookingId")
                android.util.Log.d("BOOKING", "selected = $selected")
                android.util.Log.d("BOOKING", "writing to path: users/$uid/bookings/$bookingId")

                if (bookingId == null) {
                    isBooking = false
                    Toast.makeText(context, "Could not create booking ID", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val bookingData = hashMapOf(
                    "melaId" to mela.id,
                    "melaName" to mela.name,
                    "melaDate" to mela.date,
                    "seats" to selected.joinToString(","),
                    "price" to (mela.price * selected.size),
                    "status" to "Booked"
                )

                userRef.child(bookingId).setValue(bookingData)
                    .addOnSuccessListener {
                        android.util.Log.d("BOOKING", "userRef write SUCCESS")
                        val seatUpdates = mutableMapOf<String, Any>()
                        for (seat in selected) {
                            seatUpdates[seat] = uid
                        }
                        melaSeatsRef.updateChildren(seatUpdates)
                            .addOnSuccessListener {
                                android.util.Log.d("BOOKING", "melaSeats write SUCCESS")
                                isBooking = false
                                Toast.makeText(context, "Booked Successfully 🎉", Toast.LENGTH_SHORT).show()
                                selected.clear()
                            }
                            .addOnFailureListener { e ->
                                android.util.Log.e("BOOKING", "melaSeats write FAILED: ${e.message}")
                                isBooking = false
                                Toast.makeText(context, "Seat lock failed: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("BOOKING", "userRef write FAILED: ${e.message}")
                        isBooking = false
                        Toast.makeText(context, "Booking failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            },
            enabled = selected.isNotEmpty() && !isBooking,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isBooking) "Booking..." else "BOOK NOW  ₹${mela.price * selected.size}")
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@Composable
fun ProfileScreen(
    auth: FirebaseAuth,
    database: FirebaseDatabase,
    onBack: () -> Unit
) {
    val uid = auth.currentUser?.uid

    if (uid == null) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("No user logged in")
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Back") }
        }
        return
    }

    val ref = database.getReference("users").child(uid).child("bookings")
    val bookings = remember { mutableStateListOf<Map<String, Any?>>() }

    DisposableEffect(uid) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookings.clear()
                for (bookingSnap in snapshot.children) {
                    val map = bookingSnap.value as? Map<*, *> ?: continue
                    bookings.add(map.entries.associate { it.key.toString() to it.value })
                }
            }
            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("BOOKING", "profile load cancelled: ${error.message}")
            }
        }
        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("My Bookings", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Button(onClick = onBack) { Text("Back") }
        }

        Spacer(Modifier.height(4.dp))
        Text("${bookings.size} booking(s)", color = Color.Gray)
        Spacer(Modifier.height(12.dp))

        if (bookings.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No bookings yet!", color = Color.Gray)
            }
        } else {
            LazyColumn {
                items(bookings) { b ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(
                                "${b["melaName"] ?: "Unknown Show"}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("📅 Date: ${b["melaDate"] ?: "-"}")
                            Text("💺 Seats: ${b["seats"] ?: "-"}")
                            Text("💰 Total: ₹${b["price"] ?: 0}")
                            Spacer(Modifier.height(4.dp))
                            val status = b["status"]?.toString() ?: "Unknown"
                            Text(
                                "● $status",
                                color = if (status == "Booked") Color(0xFF4CAF50) else Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
package com.absdev.saferoad.core.navigation.HomeScreen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import com.absdev.saferoad.core.navigation.HomeView.HomeViewModel
import com.absdev.saferoad.core.navigation.model.Artist
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun HomeScreen(viewModel: HomeViewModel = HomeViewModel()) {

    val artist: State<List<Artist>> = viewModel.artist.collectAsState()

    Column(Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        Spacer(modifier = Modifier.weight(1f))
        Text("Popular artist",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            modifier = Modifier.padding(16.dp)
        )

        LazyRow {
            items(artist.value){
                ArtistItem(it)
            }

        }
        Spacer(modifier = Modifier.weight(1f))
    }

}

@Composable
fun ArtistItem(artist: Artist){
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            modifier = Modifier.size(60.dp).clip(CircleShape),
            model = artist.image,
            contentDescription = "Artist image"
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = artist.name.orEmpty(), color = Color.White)

    }
}

@Preview
@Composable
fun ArtistItemPreview(){
    val artist = Artist(
        name = "pepe",
        description = "El mejor",
        image = "https://scontent.fmvd2-2.fna.fbcdn.net/v/t39.30808-6/462487760_8557440024351416_8629422464122185847_n.jpg?_nc_cat=111&ccb=1-7&_nc_sid=cc71e4&_nc_ohc=kFNtAKw1_QMQ7kNvgEqZKOS&_nc_oc=AdkdR1rpgWEOw11DqfB6V0ik4qjsOVJPA2zvlrtLZ5g6xZkxgXOr8zMxBlhZuNvbH8ayUzGWU0AhUIQUcBfRqclv&_nc_zt=23&_nc_ht=scontent.fmvd2-2.fna&_nc_gid=lXATarma8AQTeT7_Cr_GIg&oh=00_AYHJ20NRdOjbXCaeglbA7q_Cjy2abXMUuOeGdUfZGx9kog&oe=67F474FE",
        //emptyList()
    )
    ArtistItem(artist)
}







//fun createArtist(db: FirebaseFirestore) {
//    val random = (1..10000).random()
//    val artist = Artist(name = "Random $random", numberOfSongs = random)
//    db.collection("artists")
//        .add(artist)
//        .addOnSuccessListener {
//            //si tod sale bien se llama aca
//            Log.i("Aris", "SUCCESS")
//        }
//        .addOnFailureListener {
//            //si algo sale mal se llama aca
//            Log.i("Aris", "FAILURE")
//        }
//        .addOnCompleteListener {
//            //cuando termine se llama aca
//            Log.i("Aris", "COMPLETE")
//        }
//}
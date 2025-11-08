package com.griffith.perfectclock

import android.R.attr.text
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.griffith.perfectclock.ui.theme.PerfectClockTheme
import kotlin.jvm.java

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PerfectClockTheme {
                var count by remember{
                    mutableStateOf(0)
                }

                    Column (modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Text(text = count.toString(),
                            fontSize= 20.sp,
                            color = Color.White
                            
                        )
                        Button(onClick = {count++}) {Text(text="Click me") }
                    }

            }

//            val context = LocalContext.current;
//            Button(onClick = {
//                intent = Intent(context, Menu::class.java)
//                startActivity(intent)
//            }) { }
        }
    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier, ) {
//    Image(painter = painterResource(R.drawable.ic_launcher_foreground), contentDescription = null,
//    modifier = Modifier.background(Color.Red)
//    )

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    PerfectClockTheme {
//        Greeting("Vla")
//    }
//}

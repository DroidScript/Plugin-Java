/*
	Test activity for testing the plugin directly from Android Studio
	(This activity is not used when the plugin is running inside a DroidScript app)
*/

package com.mycompany.plugins.user;

import androidx.appcompat.app.*;
import androidx.constraintlayout.widget.*;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity 
{

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get main layout.
        ConstraintLayout layMain = (ConstraintLayout)findViewById( R.id.main );
        
        //Create and display our custom button to test it out without needing to install plugin.
        String txt = "My Custom Button";
        MyButton btn = new MyButton( getApplicationContext(), txt, 300, 100 );
        layMain.addView( btn );
    }
}

/*
	DroidScript Plugin class.
	(This is where you put your main plugin code)
	
	Notes:

	- You can make and install the plugin to DroidScript by running the 'make-plugin.sh' script in
	  the AndroidStudio terminal window (after building an APK)

	- You can't use resources in the project 'res' folder in plugins, so put required images files
	  etc in the top level of the 'assets' folder instead.  They can then be loaded from the
	  directory path stored in the m_plugDir variable at runtime after the plugin is installed.

	- Only top level asset files and those found in the sub-folders 'assets/lib', 'assets/arm64-v8a'
	  and 'assets/armeabi-v7a' are included in APKs built with the APKBuilder.

	 - It is important to have the multiDexEnabled option set to false in your build.gradle file.

	 - The package name for free plugins must end with the string '.user' or a license error will
	   be triggered.  Premium only plugins can have any package name.
*/

package com.mycompany.plugins.user;

import android.os.*;
import android.content.*;
import android.util.Log;
import android.widget.*;
import android.graphics.*;
import android.graphics.drawable.*;
import java.io.*;
import java.lang.reflect.*;

public class MyPlugin
{
	public static String TAG = "MyPlugin";	
	public static float VERSION = 1.33f;
	private Method m_callscript;
	private Method m_execscript;
	private Method m_getObject;
	private Object m_parent;
	private Context m_ctx;
	private String m_plugDir;
	private String m_filesDir;
	private boolean m_libsLoaded;

	//Script callbacks.
	private String m_OnMyReply;

	//Construct plugin.
	public MyPlugin()
	{
		Log.d( TAG, "Creating plugin object");
	}

	//Initialise plugin.
	public void Init( Context ctx, Object parent ) throws Exception
	{
		Log.d( TAG, "Initialising plugin object");

		//Save context and reference to parent (AndroidScript).
		m_ctx = ctx;
		m_parent = parent;

		//Use reflection to get parent's method
		Log.d( TAG, "Getting CallScript method");
		m_callscript = parent.getClass().getMethod( "CallScript", Bundle.class );
		m_execscript = parent.getClass().getMethod( "ExecScript", String.class );
		m_getObject = parent.getClass().getMethod( "GetObject", String.class );

		//Get the plugin directory path (where your plugin will live when running in an app)
		m_plugDir = m_ctx.getDir( "Plugins", 0 ).getAbsolutePath() + "/MyPlugin";

		//Get the public storage folder for the current app.
		m_filesDir = m_ctx.getExternalFilesDir(null).getPath();

		//This is how to dynamically load 3rd party .so library files if you should need them.
		//(Put them in the project libs folder)
		/*
		if( !m_libsLoaded )
		{
			Log.d( TAG, "arch: " + System.getProperty("os.arch")  );
			String arch = System.getProperty("os.arch").equals("aarch64") ? "arm64-v8a" : "armeabi-v7a";
			System.load( m_plugDir+"/"+arch+"/libuseful.so" );
			m_libsLoaded = true;
		}*/
	}

	//Release plugin resources.
	//(Called when plugin is destroyed)
	public void Release()
	{
	}

	//Handle Activity events.
	//(These are forwarded from main DroidScript activity)
	public void OnResume() { }
	public void OnPause() { }
	public void OnConfig() { }
	public void OnNewIntent( Intent intent ) { }
	public void OnActivityResult(int requestCode, int resultCode, Intent data) { }

	//Call a function in the parent script.
	private void CallScript( Bundle b ) throws Exception
	{
		m_callscript.invoke( m_parent, b );
	}

	//Send code directly to parent script.
	public void ExecScript( final String code )  throws Exception
	{
		m_execscript.invoke( m_parent, code );
	}

	//Get an existing DroidScript object given its id.
	public Object GetObject( final String id )  throws Exception
	{
		return m_getObject.invoke( m_parent, id );
	}

	//Handle commands from DroidScript.
	public String CallPlugin( Bundle b, Object obj ) throws Exception
	{
		//Extract command.
		String cmd = b.getString("cmd");
	
		//Process commands.
		String ret = null;
		if( cmd.equals("GetVersion") ) return GetVersion( b );
		else if( cmd.equals("MyFunc") ) MyFunc( b );
		else if( cmd.equals("SetOnMyReply") ) SetOnMyReply( b );
		else if( cmd.equals("SaveMyImage") ) return SaveMyImage( b );
		else if( cmd.equals("ModifyMyImage") ) return ModifyMyImage( b, obj );
		return ret;
	}

	//Handle commands from DroidScript.
	public String CallPlugin( Bundle b ) throws Exception
	{
		return CallPlugin( b, null );
	}

	//Handle creating object from DroidScript.
	public Object CreateObject( Bundle b )
	{
		//Extract object type.
		String type = b.getString("type");
	
		//Process commands.
		Object ret = null;
		if( type.equals("MyButton") )
			return CreateMyButton( b );
		return ret;
	}

	//Handle the GetVersion command.
	private String GetVersion( Bundle b )
	{
		Log.d( TAG, "Got GetVersion" );
		return Float.toString( VERSION );
	}
	
	//Handle the 'SetOnMyReply' command.
	private void SetOnMyReply( Bundle b )
	{
		Log.d( TAG, "Got SetOnMyReply" );
		m_OnMyReply = b.getString("p1");
	}

	//Handle the 'MyFunc' command.
	private void MyFunc( Bundle b ) throws Exception
	{
		Log.d( TAG, "Got MyFunc" );

		//Extract params from 'MyFunc' command.
		String txt = b.getString("p1");
		float num = b.getFloat("p2");
		boolean boo = b.getBoolean("p3");
	
		//If 'OnReply' callback is set.
		if( m_OnMyReply!=null )
		{
			//Fire callback in script.
			b = new Bundle();
			b.putString( "cmd", m_OnMyReply );
			b.putString( "p1", txt + " world" );
			b.putFloat( "p2", num+20 );
			b.putBoolean( "p3", !boo );
			CallScript( b );
		}
	}

	//Handle 'SaveImage' command.
	private String SaveMyImage( Bundle b ) throws Exception
	{
		//Get byte array from bundle.
		byte[] byteArray = b.getByteArray("img");

		//Convert image to bitmap.
		Bitmap bmp = BitmapFactory.decodeByteArray( byteArray, 0, byteArray.length );

		//Save image to sdcard.
		String file = m_filesDir + "/MyPlugin.jpg";
		Log.d( TAG, "Saving jpeg " + file );
	
		FileOutputStream outStream = new FileOutputStream( file );
		bmp.compress( Bitmap.CompressFormat.JPEG, 95, outStream );
		outStream.close();

		return file;
	}

	//Handle 'ModifyMyImage' command.
	private String ModifyMyImage( Bundle b, Object obj )
	{
		//Extract params.
		String txt = b.getString("p1");
		ImageView v = (ImageView)obj;

		//Create canvas wrapper for image.
		BitmapDrawable drw = (BitmapDrawable)v.getDrawable();
		Bitmap bmp = drw.getBitmap();
		Canvas canv = new Canvas( bmp );

		//Set paint style.
		Paint paint = new Paint();
		paint.setStyle( Paint.Style.FILL );
		paint.setAntiAlias( true );
		paint.setColor( 0xffff00ff );
		paint.setTextSize( 42 );

		//Draw text on image.
        canv.drawText( txt, bmp.getWidth()/3, bmp.getHeight()/3, paint );
		v.invalidate();

		return null;
	}

	//Handle 'CreateMyButton' command.
	private Object CreateMyButton( Bundle b )
	{
		//Extract params.
		String txt = b.getString("p1");
		float width = b.getFloat("p2");
		float height = b.getFloat("p3");

		//Create custom button.
		MyButton btn = new MyButton( m_ctx, txt, width, height );
		return btn;
	}

} 



//Add CreateMyPlugin method to global app object.
app.CreateMyPlugin = function( options ) 
{ 
    return new MyPlugin( options );
}

//Plugin wrapper class.
function MyPlugin( options )
{
	var dw = app.GetDisplayWidth();
	var dh = app.GetDisplayHeight();
    this.plg = _CreatePlugin( "com.mycompany.plugins.user.MyPlugin", options );  
    
	this.GetVersion = function( num, txt ) { return parseFloat( this.plg.Send( "GetVersion" ) ); }
    this.MyFunc = function( txt, num, bool ) { this.plg.Send( "MyFunc", txt, num, bool ); }
    this.SetOnMyReply = function( cb ) { this.plg.Send( "SetOnMyReply", cb.name ); }
    this.SaveMyImage = function( img ) { return this.plg.SendImg( "SaveMyImage", img ); }
    this.ModifyMyImage = function( img,txt ) { return this.plg.SendObj( "ModifyMyImage", img,txt ); }
    this.CreateMyButton = function( txt,width,height ) { 
        var ret = this.plg.CreateObj( "MyButton", txt, width*dw, height*dh );  
        if( ret ) return new Obj(ret); else return null; 
    }
}

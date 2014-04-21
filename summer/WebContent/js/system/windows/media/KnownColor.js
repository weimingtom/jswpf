/**
 * KnownColor
 */

define(["dojo/_base/declare", "system/Type"], function(declare, Type){
	var KnownColor = declare("KnownColor", null,{
	});
	
	 // We've reserved the value "1" as unknown.  If for some odd reason "1" is added to the 
    // list, redefined UnknownColor 

	KnownColor.AliceBlue = 0xFFF0F8FF; 
	KnownColor.AntiqueWhite = 0xFFFAEBD7;
	KnownColor.Aqua = 0xFF00FFFF;
	KnownColor.Aquamarine = 0xFF7FFFD4;
	KnownColor.Azure = 0xFFF0FFFF; 
	KnownColor.Beige = 0xFFF5F5DC;
	KnownColor.Bisque = 0xFFFFE4C4; 
	KnownColor.Black = 0xFF000000; 
	KnownColor.BlanchedAlmond = 0xFFFFEBCD;
	KnownColor.Blue = 0xFF0000FF; 
	KnownColor.BlueViolet = 0xFF8A2BE2;
	KnownColor.Brown = 0xFFA52A2A;
	KnownColor.BurlyWood = 0xFFDEB887;
	KnownColor.CadetBlue = 0xFF5F9EA0; 
	KnownColor.Chartreuse = 0xFF7FFF00;
	KnownColor.Chocolate = 0xFFD2691E; 
	KnownColor.Coral = 0xFFFF7F50; 
	KnownColor.CornflowerBlue = 0xFF6495ED;
	KnownColor.Cornsilk = 0xFFFFF8DC; 
	KnownColor.Crimson = 0xFFDC143C;
	KnownColor.Cyan = 0xFF00FFFF;
	KnownColor.DarkBlue = 0xFF00008B;
	KnownColor.DarkCyan = 0xFF008B8B; 
	KnownColor.DarkGoldenrod = 0xFFB8860B;
	KnownColor.DarkGray = 0xFFA9A9A9; 
	KnownColor.DarkGreen = 0xFF006400; 
	KnownColor.DarkKhaki = 0xFFBDB76B;
	KnownColor.DarkMagenta = 0xFF8B008B; 
	KnownColor.DarkOliveGreen = 0xFF556B2F;
	KnownColor.DarkOrange = 0xFFFF8C00;
	KnownColor.DarkOrchid = 0xFF9932CC;
	KnownColor.DarkRed = 0xFF8B0000; 
	KnownColor.DarkSalmon = 0xFFE9967A;
	KnownColor.DarkSeaGreen = 0xFF8FBC8F; 
	KnownColor.DarkSlateBlue = 0xFF483D8B; 
	KnownColor.DarkSlateGray = 0xFF2F4F4F;
	KnownColor.DarkTurquoise = 0xFF00CED1; 
	KnownColor.DarkViolet = 0xFF9400D3;
	KnownColor.DeepPink = 0xFFFF1493;
	KnownColor.DeepSkyBlue = 0xFF00BFFF;
	KnownColor.DimGray = 0xFF696969; 
	KnownColor.DodgerBlue = 0xFF1E90FF;
	KnownColor.Firebrick = 0xFFB22222; 
	KnownColor.FloralWhite = 0xFFFFFAF0; 
	KnownColor.ForestGreen = 0xFF228B22;
	KnownColor.Fuchsia = 0xFFFF00FF; 
	KnownColor.Gainsboro = 0xFFDCDCDC;
	KnownColor.GhostWhite = 0xFFF8F8FF;
	KnownColor.Gold = 0xFFFFD700;
	KnownColor.Goldenrod = 0xFFDAA520; 
	KnownColor.Gray = 0xFF808080;
	KnownColor.Green = 0xFF008000; 
	KnownColor.GreenYellow = 0xFFADFF2F; 
	KnownColor.Honeydew = 0xFFF0FFF0;
	KnownColor.HotPink = 0xFFFF69B4; 
	KnownColor.IndianRed = 0xFFCD5C5C;
	KnownColor.Indigo = 0xFF4B0082;
	KnownColor.Ivory = 0xFFFFFFF0;
	KnownColor.Khaki = 0xFFF0E68C; 
	KnownColor.Lavender = 0xFFE6E6FA;
	KnownColor.LavenderBlush = 0xFFFFF0F5; 
	KnownColor.LawnGreen = 0xFF7CFC00; 
	KnownColor.LemonChiffon = 0xFFFFFACD;
	KnownColor.LightBlue = 0xFFADD8E6; 
	KnownColor.LightCoral = 0xFFF08080;
	KnownColor.LightCyan = 0xFFE0FFFF;
	KnownColor.LightGoldenrodYellow = 0xFFFAFAD2;
	KnownColor.LightGreen = 0xFF90EE90; 
	KnownColor.LightGray = 0xFFD3D3D3;
	KnownColor.LightPink = 0xFFFFB6C1; 
	KnownColor.LightSalmon = 0xFFFFA07A; 
	KnownColor.LightSeaGreen = 0xFF20B2AA;
	KnownColor.LightSkyBlue = 0xFF87CEFA; 
	KnownColor.LightSlateGray = 0xFF778899;
	KnownColor.LightSteelBlue = 0xFFB0C4DE;
	KnownColor.LightYellow = 0xFFFFFFE0;
	KnownColor.Lime = 0xFF00FF00; 
	KnownColor.LimeGreen = 0xFF32CD32;
	KnownColor.Linen = 0xFFFAF0E6; 
	KnownColor.Magenta = 0xFFFF00FF; 
	KnownColor.Maroon = 0xFF800000;
	KnownColor.MediumAquamarine = 0xFF66CDAA; 
	KnownColor.MediumBlue = 0xFF0000CD;
	KnownColor.MediumOrchid = 0xFFBA55D3;
	KnownColor.MediumPurple = 0xFF9370DB;
	KnownColor.MediumSeaGreen = 0xFF3CB371; 
	KnownColor.MediumSlateBlue = 0xFF7B68EE;
	KnownColor.MediumSpringGreen = 0xFF00FA9A; 
	KnownColor.MediumTurquoise = 0xFF48D1CC; 
	KnownColor.MediumVioletRed = 0xFFC71585;
	KnownColor.MidnightBlue = 0xFF191970; 
	KnownColor.MintCream = 0xFFF5FFFA;
	KnownColor.MistyRose = 0xFFFFE4E1;
	KnownColor.Moccasin = 0xFFFFE4B5;
	KnownColor.NavajoWhite = 0xFFFFDEAD; 
	KnownColor.Navy = 0xFF000080;
	KnownColor.OldLace = 0xFFFDF5E6; 
	KnownColor.Olive = 0xFF808000; 
	KnownColor.OliveDrab = 0xFF6B8E23;
	KnownColor.Orange = 0xFFFFA500; 
	KnownColor.OrangeRed = 0xFFFF4500;
	KnownColor.Orchid = 0xFFDA70D6;
	KnownColor.PaleGoldenrod = 0xFFEEE8AA;
	KnownColor.PaleGreen = 0xFF98FB98; 
	KnownColor.PaleTurquoise = 0xFFAFEEEE;
	KnownColor.PaleVioletRed = 0xFFDB7093; 
	KnownColor.PapayaWhip = 0xFFFFEFD5; 
	KnownColor.PeachPuff = 0xFFFFDAB9;
	KnownColor.Peru = 0xFFCD853F; 
	KnownColor.Pink = 0xFFFFC0CB;
	KnownColor.Plum = 0xFFDDA0DD;
	KnownColor.PowderBlue = 0xFFB0E0E6;
	KnownColor.Purple = 0xFF800080; 
	KnownColor.Red = 0xFFFF0000;
	KnownColor.RosyBrown = 0xFFBC8F8F; 
	KnownColor.RoyalBlue = 0xFF4169E1; 
	KnownColor.SaddleBrown = 0xFF8B4513;
	KnownColor.Salmon = 0xFFFA8072; 
	KnownColor.SandyBrown = 0xFFF4A460;
	KnownColor.SeaGreen = 0xFF2E8B57;
	KnownColor.SeaShell = 0xFFFFF5EE;
	KnownColor.Sienna = 0xFFA0522D; 
	KnownColor.Silver = 0xFFC0C0C0;
	KnownColor.SkyBlue = 0xFF87CEEB; 
	KnownColor.SlateBlue = 0xFF6A5ACD; 
	KnownColor.SlateGray = 0xFF708090;
	KnownColor.Snow = 0xFFFFFAFA; 
	KnownColor.SpringGreen = 0xFF00FF7F;
	KnownColor.SteelBlue = 0xFF4682B4;
	KnownColor.Tan = 0xFFD2B48C;
	KnownColor.Teal = 0xFF008080; 
	KnownColor.Thistle = 0xFFD8BFD8;
	KnownColor.Tomato = 0xFFFF6347; 
	KnownColor.Transparent = 0x00FFFFFF; 
	KnownColor.Turquoise = 0xFF40E0D0;
	KnownColor.Violet = 0xFFEE82EE; 
	KnownColor.Wheat = 0xFFF5DEB3;
	KnownColor.White = 0xFFFFFFFF;
	KnownColor.WhiteSmoke = 0xFFF5F5F5;
	KnownColor.Yellow = 0xFFFFFF00; 
	KnownColor.YellowGreen = 0xFF9ACD32;
	KnownColor.UnknownColor = 0x00000001; 
	
	KnownColor.Type = new Type("KnownColor", KnownColor, [Object.Type]);
	return KnownColor;
});
  

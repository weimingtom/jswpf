/**
 * KnownColors
 */

define(["dojo/_base/declare", "system/Type", "media/KnownColor", "generic/Dictionary", "media/SolidColorBrush"], 
		function(declare, Type, KnownColor, Dictionary, SolidColorBrush){
	
//	private static Dictionary<uint, SolidColorBrush> 
	var s_solidColorBrushCache = new Dictionary/*<uint, SolidColorBrush>*/();
	var KnownColors = declare("KnownColors", null,{

	});
	
//	Object.defineProperties(KnownColors,{
////		private static Dictionary<uint, SolidColorBrush> 
//		s_solidColorBrushCache:
//		{
//			get:function(){
//				if(KnownColors._s_solidColorBrushCache){
//					KnownColors._s_solidColorBrushCache = new Dictionary/*<uint, SolidColorBrush>*/();
//				}
//				
//				return KnownColors._s_solidColorBrushCache;
//			}
//		}
//	});
	
	/// Return the solid color brush from a color string.  If there's no match, null
//    public static SolidColorBrush 
    KnownColors.ColorStringToKnownBrush = function(/*string*/ s) 
    {
        if (null != s) 
        { 
            /*KnownColor*/var result = ColorStringToKnownColor(s);

            // If the result is UnknownColor, that means this string wasn't found
            if (result != KnownColor.UnknownColor)
            {
                // Otherwise, return the appropriate SolidColorBrush 
                return SolidColorBrushFromUint(result);
            } 
        } 
        return null;
    };

//    public static SolidColorBrush 
    KnownColors.SolidColorBrushFromUint = function(/*uint*/ argb)
    {
        /*SolidColorBrush*/var scp = null; 
        var scpOut ={
        	"scp" : scp
        };
        // Attempt to retrieve the color.  If it fails create it.
        var r = s_solidColorBrushCache.TryGetValue(argb, /*out scp*/scpOut);
    	scp = scpOut.scp;
        if (!r) 
        {
        	scpOut.scp = new SolidColorBrush(Color.FromUInt32(argb));
        	scpOut.scp.Freeze();
        	s_solidColorBrushCache.Set(argb, scpOut.scp); 
        }

        return scp;
    };

//    static internal string 
    KnownColors.MatchColor = function(/*string*/ colorString, 
    		/*out bool isKnownColor*/isKnownColorOut, /*out bool isNumericColor*/isNumericColorOut, 
    		/*out bool isContextColor*/isContextColorOut, /*out bool isScRgbColor*/isScRgbColorOut)
    { 

        var trimmedString = colorString.Trim();

        if (((trimmedString.Length == 4) || 
            (trimmedString.Length == 5) ||
            (trimmedString.Length == 7) || 
            (trimmedString.Length == 9)) && 
            (trimmedString[0] == '#'))
        { 
            isNumericColor = true;
            isScRgbColor = false;
            isKnownColor = false;
            isContextColor = false; 
            return trimmedString;
        } 
        else 
            isNumericColor = false;

        if ((trimmedString.StartsWith("sc#", StringComparison.Ordinal) == true))
        {
            isNumericColor = false;
            isScRgbColor = true; 
            isKnownColor = false;
            isContextColor = false; 
        } 
        else
        { 
            isScRgbColor = false;
        }

        if ((trimmedString.StartsWith(Parsers.s_ContextColor, StringComparison.OrdinalIgnoreCase) == true)) 
        {
            isContextColor = true; 
            isScRgbColor = false; 
            isKnownColor = false;
            return trimmedString; 
        }
        else
        {
            isContextColor = false; 
            isKnownColor = true;
        } 

        return trimmedString;
    }; 

    /// Return the KnownColor from a color string.  If there's no match, KnownColor.UnknownColor
//    internal static KnownColor 
    KnownColors.ColorStringToKnownColor = function(/*string*/ colorString) 
    {
        if (null != colorString) 
        { 
            // We use invariant culture because we don't globalize our color names
            var colorUpper = colorString.ToUpper(System.Globalization.CultureInfo.InvariantCulture); 

            // Use String.Equals because it does explicit equality
            // StartsWith/EndsWith are culture sensitive and are 4-7 times slower than Equals

            switch (colorUpper.Length)
            { 
                case 3: 
                    if (colorUpper.Equals("RED")) return KnownColor.Red;
                    if (colorUpper.Equals("TAN")) return KnownColor.Tan; 
                    break;
                case 4:
                switch(colorUpper[0])
                { 
                    case 'A':
                        if (colorUpper.Equals("AQUA")) return KnownColor.Aqua; 
                        break; 
                    case 'B':
                        if (colorUpper.Equals("BLUE")) return KnownColor.Blue; 
                        break;
                    case 'C':
                        if (colorUpper.Equals("CYAN")) return KnownColor.Cyan;
                        break; 
                    case 'G':
                        if (colorUpper.Equals("GOLD")) return KnownColor.Gold; 
                        if (colorUpper.Equals("GRAY")) return KnownColor.Gray; 
                        break;
                    case 'L': 
                        if (colorUpper.Equals("LIME")) return KnownColor.Lime;
                        break;
                    case 'N':
                        if (colorUpper.Equals("NAVY")) return KnownColor.Navy; 
                        break;
                    case 'P': 
                        if (colorUpper.Equals("PERU")) return KnownColor.Peru; 
                        if (colorUpper.Equals("PINK")) return KnownColor.Pink;
                        if (colorUpper.Equals("PLUM")) return KnownColor.Plum; 
                        break;
                    case 'S':
                        if (colorUpper.Equals("SNOW")) return KnownColor.Snow;
                        break; 
                    case 'T':
                        if (colorUpper.Equals("TEAL")) return KnownColor.Teal; 
                        break; 
                }
                    break; 
                case 5:
                switch(colorUpper[0])
                {
                    case 'A': 
                        if (colorUpper.Equals("AZURE")) return KnownColor.Azure;
                        break; 
                    case 'B': 
                        if (colorUpper.Equals("BEIGE")) return KnownColor.Beige;
                        if (colorUpper.Equals("BLACK")) return KnownColor.Black; 
                        if (colorUpper.Equals("BROWN")) return KnownColor.Brown;
                        break;
                    case 'C':
                        if (colorUpper.Equals("CORAL")) return KnownColor.Coral; 
                        break;
                    case 'G': 
                        if (colorUpper.Equals("GREEN")) return KnownColor.Green; 
                        break;
                    case 'I': 
                        if (colorUpper.Equals("IVORY")) return KnownColor.Ivory;
                        break;
                    case 'K':
                        if (colorUpper.Equals("KHAKI")) return KnownColor.Khaki; 
                        break;
                    case 'L': 
                        if (colorUpper.Equals("LINEN")) return KnownColor.Linen; 
                        break;
                    case 'O': 
                        if (colorUpper.Equals("OLIVE")) return KnownColor.Olive;
                        break;
                    case 'W':
                        if (colorUpper.Equals("WHEAT")) return KnownColor.Wheat; 
                        if (colorUpper.Equals("WHITE")) return KnownColor.White;
                        break; 
                } 
                    break;
                case 6: 
                switch(colorUpper[0])
                {
                    case 'B':
                        if (colorUpper.Equals("BISQUE")) return KnownColor.Bisque; 
                        break;
                    case 'I': 
                        if (colorUpper.Equals("INDIGO")) return KnownColor.Indigo; 
                        break;
                    case 'M': 
                        if (colorUpper.Equals("MAROON")) return KnownColor.Maroon;
                        break;
                    case 'O':
                        if (colorUpper.Equals("ORANGE")) return KnownColor.Orange; 
                        if (colorUpper.Equals("ORCHID")) return KnownColor.Orchid;
                        break; 
                    case 'P': 
                        if (colorUpper.Equals("PURPLE")) return KnownColor.Purple;
                        break; 
                    case 'S':
                        if (colorUpper.Equals("SALMON")) return KnownColor.Salmon;
                        if (colorUpper.Equals("SIENNA")) return KnownColor.Sienna;
                        if (colorUpper.Equals("SILVER")) return KnownColor.Silver; 
                        break;
                    case 'T': 
                        if (colorUpper.Equals("TOMATO")) return KnownColor.Tomato; 
                        break;
                    case 'V': 
                        if (colorUpper.Equals("VIOLET")) return KnownColor.Violet;
                        break;
                    case 'Y':
                        if (colorUpper.Equals("YELLOW")) return KnownColor.Yellow; 
                        break;
                } 
                    break; 
                case 7:
                switch(colorUpper[0]) 
                {
                    case 'C':
                        if (colorUpper.Equals("CRIMSON")) return KnownColor.Crimson;
                        break; 
                    case 'D':
                        if (colorUpper.Equals("DARKRED")) return KnownColor.DarkRed; 
                        if (colorUpper.Equals("DIMGRAY")) return KnownColor.DimGray; 
                        break;
                    case 'F': 
                        if (colorUpper.Equals("FUCHSIA")) return KnownColor.Fuchsia;
                        break;
                    case 'H':
                        if (colorUpper.Equals("HOTPINK")) return KnownColor.HotPink; 
                        break;
                    case 'M': 
                        if (colorUpper.Equals("MAGENTA")) return KnownColor.Magenta; 
                        break;
                    case 'O': 
                        if (colorUpper.Equals("OLDLACE")) return KnownColor.OldLace;
                        break;
                    case 'S':
                        if (colorUpper.Equals("SKYBLUE")) return KnownColor.SkyBlue; 
                        break;
                    case 'T': 
                        if (colorUpper.Equals("THISTLE")) return KnownColor.Thistle; 
                        break;
                } 
                    break;
                case 8:
                switch(colorUpper[0])
                { 
                    case 'C':
                        if (colorUpper.Equals("CORNSILK")) return KnownColor.Cornsilk; 
                        break; 
                    case 'D':
                        if (colorUpper.Equals("DARKBLUE")) return KnownColor.DarkBlue; 
                        if (colorUpper.Equals("DARKCYAN")) return KnownColor.DarkCyan;
                        if (colorUpper.Equals("DARKGRAY")) return KnownColor.DarkGray;
                        if (colorUpper.Equals("DEEPPINK")) return KnownColor.DeepPink;
                        break; 
                    case 'H':
                        if (colorUpper.Equals("HONEYDEW")) return KnownColor.Honeydew; 
                        break; 
                    case 'L':
                        if (colorUpper.Equals("LAVENDER")) return KnownColor.Lavender; 
                        break;
                    case 'M':
                        if (colorUpper.Equals("MOCCASIN")) return KnownColor.Moccasin;
                        break; 
                    case 'S':
                        if (colorUpper.Equals("SEAGREEN")) return KnownColor.SeaGreen; 
                        if (colorUpper.Equals("SEASHELL")) return KnownColor.SeaShell; 
                        break;
                } 
                    break;
                case 9:
                switch(colorUpper[0])
                { 
                    case 'A':
                        if (colorUpper.Equals("ALICEBLUE")) return KnownColor.AliceBlue; 
                        break; 
                    case 'B':
                        if (colorUpper.Equals("BURLYWOOD")) return KnownColor.BurlyWood; 
                        break;
                    case 'C':
                        if (colorUpper.Equals("CADETBLUE")) return KnownColor.CadetBlue;
                        if (colorUpper.Equals("CHOCOLATE")) return KnownColor.Chocolate; 
                        break;
                    case 'D': 
                        if (colorUpper.Equals("DARKGREEN")) return KnownColor.DarkGreen; 
                        if (colorUpper.Equals("DARKKHAKI")) return KnownColor.DarkKhaki;
                        break; 
                    case 'F':
                        if (colorUpper.Equals("FIREBRICK")) return KnownColor.Firebrick;
                        break;
                    case 'G': 
                        if (colorUpper.Equals("GAINSBORO")) return KnownColor.Gainsboro;
                        if (colorUpper.Equals("GOLDENROD")) return KnownColor.Goldenrod; 
                        break; 
                    case 'I':
                        if (colorUpper.Equals("INDIANRED")) return KnownColor.IndianRed; 
                        break;
                    case 'L':
                        if (colorUpper.Equals("LAWNGREEN")) return KnownColor.LawnGreen;
                        if (colorUpper.Equals("LIGHTBLUE")) return KnownColor.LightBlue; 
                        if (colorUpper.Equals("LIGHTCYAN")) return KnownColor.LightCyan;
                        if (colorUpper.Equals("LIGHTGRAY")) return KnownColor.LightGray; 
                        if (colorUpper.Equals("LIGHTPINK")) return KnownColor.LightPink; 
                        if (colorUpper.Equals("LIMEGREEN")) return KnownColor.LimeGreen;
                        break; 
                    case 'M':
                        if (colorUpper.Equals("MINTCREAM")) return KnownColor.MintCream;
                        if (colorUpper.Equals("MISTYROSE")) return KnownColor.MistyRose;
                        break; 
                    case 'O':
                        if (colorUpper.Equals("OLIVEDRAB")) return KnownColor.OliveDrab; 
                        if (colorUpper.Equals("ORANGERED")) return KnownColor.OrangeRed; 
                        break;
                    case 'P': 
                        if (colorUpper.Equals("PALEGREEN")) return KnownColor.PaleGreen;
                        if (colorUpper.Equals("PEACHPUFF")) return KnownColor.PeachPuff;
                        break;
                    case 'R': 
                        if (colorUpper.Equals("ROSYBROWN")) return KnownColor.RosyBrown;
                        if (colorUpper.Equals("ROYALBLUE")) return KnownColor.RoyalBlue; 
                        break; 
                    case 'S':
                        if (colorUpper.Equals("SLATEBLUE")) return KnownColor.SlateBlue; 
                        if (colorUpper.Equals("SLATEGRAY")) return KnownColor.SlateGray;
                        if (colorUpper.Equals("STEELBLUE")) return KnownColor.SteelBlue;
                        break;
                    case 'T': 
                        if (colorUpper.Equals("TURQUOISE")) return KnownColor.Turquoise;
                        break; 
                } 
                    break;
                case 10: 
                switch(colorUpper[0])
                {
                    case 'A':
                        if (colorUpper.Equals("AQUAMARINE")) return KnownColor.Aquamarine; 
                        break;
                    case 'B': 
                        if (colorUpper.Equals("BLUEVIOLET")) return KnownColor.BlueViolet; 
                        break;
                    case 'C': 
                        if (colorUpper.Equals("CHARTREUSE")) return KnownColor.Chartreuse;
                        break;
                    case 'D':
                        if (colorUpper.Equals("DARKORANGE")) return KnownColor.DarkOrange; 
                        if (colorUpper.Equals("DARKORCHID")) return KnownColor.DarkOrchid;
                        if (colorUpper.Equals("DARKSALMON")) return KnownColor.DarkSalmon; 
                        if (colorUpper.Equals("DARKVIOLET")) return KnownColor.DarkViolet; 
                        if (colorUpper.Equals("DODGERBLUE")) return KnownColor.DodgerBlue;
                        break; 
                    case 'G':
                        if (colorUpper.Equals("GHOSTWHITE")) return KnownColor.GhostWhite;
                        break;
                    case 'L': 
                        if (colorUpper.Equals("LIGHTCORAL")) return KnownColor.LightCoral;
                        if (colorUpper.Equals("LIGHTGREEN")) return KnownColor.LightGreen; 
                        break; 
                    case 'M':
                        if (colorUpper.Equals("MEDIUMBLUE")) return KnownColor.MediumBlue; 
                        break;
                    case 'P':
                        if (colorUpper.Equals("PAPAYAWHIP")) return KnownColor.PapayaWhip;
                        if (colorUpper.Equals("POWDERBLUE")) return KnownColor.PowderBlue; 
                        break;
                    case 'S': 
                        if (colorUpper.Equals("SANDYBROWN")) return KnownColor.SandyBrown; 
                        break;
                    case 'W': 
                        if (colorUpper.Equals("WHITESMOKE")) return KnownColor.WhiteSmoke;
                        break;
                }
                    break; 
                case 11:
                switch(colorUpper[0]) 
                { 
                    case 'D':
                        if (colorUpper.Equals("DARKMAGENTA")) return KnownColor.DarkMagenta; 
                        if (colorUpper.Equals("DEEPSKYBLUE")) return KnownColor.DeepSkyBlue;
                        break;
                    case 'F':
                        if (colorUpper.Equals("FLORALWHITE")) return KnownColor.FloralWhite; 
                        if (colorUpper.Equals("FORESTGREEN")) return KnownColor.ForestGreen;
                        break; 
                    case 'G': 
                        if (colorUpper.Equals("GREENYELLOW")) return KnownColor.GreenYellow;
                        break; 
                    case 'L':
                        if (colorUpper.Equals("LIGHTSALMON")) return KnownColor.LightSalmon;
                        if (colorUpper.Equals("LIGHTYELLOW")) return KnownColor.LightYellow;
                        break; 
                    case 'N':
                        if (colorUpper.Equals("NAVAJOWHITE")) return KnownColor.NavajoWhite; 
                        break; 
                    case 'S':
                        if (colorUpper.Equals("SADDLEBROWN")) return KnownColor.SaddleBrown; 
                        if (colorUpper.Equals("SPRINGGREEN")) return KnownColor.SpringGreen;
                        break;
                    case 'T':
                        if (colorUpper.Equals("TRANSPARENT")) return KnownColor.Transparent; 
                        break;
                    case 'Y': 
                        if (colorUpper.Equals("YELLOWGREEN")) return KnownColor.YellowGreen; 
                        break;
                } 
                    break;
                case 12:
                switch(colorUpper[0])
                { 
                    case 'A':
                        if (colorUpper.Equals("ANTIQUEWHITE")) return KnownColor.AntiqueWhite; 
                        break; 
                    case 'D':
                        if (colorUpper.Equals("DARKSEAGREEN")) return KnownColor.DarkSeaGreen; 
                        break;
                    case 'L':
                        if (colorUpper.Equals("LIGHTSKYBLUE")) return KnownColor.LightSkyBlue;
                        if (colorUpper.Equals("LEMONCHIFFON")) return KnownColor.LemonChiffon; 
                        break;
                    case 'M': 
                        if (colorUpper.Equals("MEDIUMORCHID")) return KnownColor.MediumOrchid; 
                        if (colorUpper.Equals("MEDIUMPURPLE")) return KnownColor.MediumPurple;
                        if (colorUpper.Equals("MIDNIGHTBLUE")) return KnownColor.MidnightBlue; 
                        break;
                }
                    break;
                case 13: 
                switch(colorUpper[0])
                { 
                    case 'D': 
                        if (colorUpper.Equals("DARKSLATEBLUE")) return KnownColor.DarkSlateBlue;
                        if (colorUpper.Equals("DARKSLATEGRAY")) return KnownColor.DarkSlateGray; 
                        if (colorUpper.Equals("DARKGOLDENROD")) return KnownColor.DarkGoldenrod;
                        if (colorUpper.Equals("DARKTURQUOISE")) return KnownColor.DarkTurquoise;
                        break;
                    case 'L': 
                        if (colorUpper.Equals("LIGHTSEAGREEN")) return KnownColor.LightSeaGreen;
                        if (colorUpper.Equals("LAVENDERBLUSH")) return KnownColor.LavenderBlush; 
                        break; 
                    case 'P':
                        if (colorUpper.Equals("PALEGOLDENROD")) return KnownColor.PaleGoldenrod; 
                        if (colorUpper.Equals("PALETURQUOISE")) return KnownColor.PaleTurquoise;
                        if (colorUpper.Equals("PALEVIOLETRED")) return KnownColor.PaleVioletRed;
                        break;
                } 
                    break;
                case 14: 
                switch(colorUpper[0]) 
                {
                    case 'B': 
                        if (colorUpper.Equals("BLANCHEDALMOND")) return KnownColor.BlanchedAlmond;
                        break;
                    case 'C':
                        if (colorUpper.Equals("CORNFLOWERBLUE")) return KnownColor.CornflowerBlue; 
                        break;
                    case 'D': 
                        if (colorUpper.Equals("DARKOLIVEGREEN")) return KnownColor.DarkOliveGreen; 
                        break;
                    case 'L': 
                        if (colorUpper.Equals("LIGHTSLATEGRAY")) return KnownColor.LightSlateGray;
                        if (colorUpper.Equals("LIGHTSTEELBLUE")) return KnownColor.LightSteelBlue;
                        break;
                    case 'M': 
                        if (colorUpper.Equals("MEDIUMSEAGREEN")) return KnownColor.MediumSeaGreen;
                        break; 
                } 
                    break;
                case 15: 
                    if (colorUpper.Equals("MEDIUMSLATEBLUE")) return KnownColor.MediumSlateBlue;
                    if (colorUpper.Equals("MEDIUMTURQUOISE")) return KnownColor.MediumTurquoise;
                    if (colorUpper.Equals("MEDIUMVIOLETRED")) return KnownColor.MediumVioletRed;
                    break; 
                case 16:
                    if (colorUpper.Equals("MEDIUMAQUAMARINE")) return KnownColor.MediumAquamarine; 
                    break; 
                case 17:
                    if (colorUpper.Equals("MEDIUMSPRINGGREEN")) return KnownColor.MediumSpringGreen; 
                    break;
                case 20:
                    if (colorUpper.Equals("LIGHTGOLDENRODYELLOW")) return KnownColor.LightGoldenrodYellow;
                    break; 
            }
        } 
        // colorString was null or not found 
        return KnownColor.UnknownColor;
    };
	
	KnownColors.Type = new Type("KnownColors", KnownColors, [Object.Type]);
	return KnownColors;
});
        

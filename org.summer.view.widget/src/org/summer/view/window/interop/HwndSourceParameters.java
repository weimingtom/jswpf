package org.summer.view.window.interop;

/// <summary> 
///     Base class for HwndSource Creation Parameters.
///     This allows flexibility and control of parameters to HwndSource's 
///     Constructor without many different overloaded constructors. 
/// </summary>
public class HwndSourceParameters 
{
    /// <summary>
    ///     Simple Ctor w/ just a WindowName
    /// </summary> 
    public HwndSourceParameters(String name)
    { 
        // Initialize some fields to useful default values 
        _styleBits  = NativeMethods.WS_VISIBLE;
        _styleBits |= NativeMethods.WS_CAPTION; 
        _styleBits |= NativeMethods.WS_SYSMENU;
        _styleBits |= NativeMethods.WS_THICKFRAME;
        _styleBits |= NativeMethods.WS_MINIMIZEBOX;
        _styleBits |= NativeMethods.WS_MAXIMIZEBOX; 
        _styleBits |= NativeMethods.WS_CLIPCHILDREN;

        // The Visual Manager has a hard time creating 
        // a surface with zero pixels.
        _width  = 1; 
        _height = 1;

        _x = NativeMethods.CW_USEDEFAULT;
        _y = NativeMethods.CW_USEDEFAULT; 

        WindowName = name; 
    } 

    /// <summary> 
    ///     Ctor.  w/ WindowName and Size.
    /// </summary>
    /// <param name="name">  Name of the window </param>
    /// <param name="width">  Width of the window </param> 
    /// <param name="height">  Height of the window </param>
    public HwndSourceParameters(String name, int width, int height): this(name) 
    { 
        Width = width;
        Height = height; 
    }

    /// <summary>
    ///     Returns the hashcode for this struct. 
    /// </summary>
    /// <returns>hashcode</returns> 
    public override int GetHashCode( ) 
    {
        return base.GetHashCode(); 
    }

    /// <summary>
    ///  The Window Class Style Property 
    /// </summary>
    public int WindowClassStyle 
    { 
        get{ return _classStyleBits; }
        set{ _classStyleBits = value; } 
    }

    /// <summary>
    /// Allow the app to set the Style bits. 
    /// The Clip Children bit must always be set on a Standard Window.
    /// </summary> 
    public int WindowStyle 
    {
        get{ 
            return _styleBits;
        }

        set{ 
            _styleBits = value | NativeMethods.WS_CLIPCHILDREN;
        } 
    } 

    /// <summary> 
    /// The Extended Style bits.
    /// </summary>
    public int ExtendedWindowStyle
    { 
        get{ return _extendedStyleBits; }
        set{ _extendedStyleBits = value; } 
    } 

    /// <summary> 
    ///     Set the X,Y Position of HwndSource Creation Parameters.
    /// </summary>
    public void SetPosition(int x, int y)
    { 
        _x = x;
        _y = y; 
    } 

    /// <summary> 
    ///     The X position of the HwndSource Property.
    /// </summary>
    public int PositionX
    { 
        get{ return _x; }
        set{ _x = value; } 
    } 

    /// <summary> 
    ///     The Y position of the HwndSource Property.
    /// </summary>
    public int PositionY
    { 
        get{ return _y; }
        set{ _y = value; } 
    } 

    /// <summary> 
    ///     Set the Width and Height of HwndSource Creation Parameters.
    /// </summary>
    public void SetSize(int width, int height)
    { 
        _width = width;
        _height = height; 
        _hasAssignedSize = true; 
    }

    /// <summary>
    ///     The Width Property of the HwndSource.
    /// </summary>
    public int Width 
    {
        get{ return _width; } 
        set{ 
            _width = value;
            _hasAssignedSize = true; 
        }
    }

    /// <summary> 
    ///     The Height Property of the HwndSource.
    /// </summary> 
    public int Height 
    {
        get{ return _height; } 
        set{
            _height = value;
            _hasAssignedSize = true;
        } 
    }

    /// <summary> 
    ///     Was the Size assigned or did we just default.
    /// </summary> 
    public boolean HasAssignedSize
    {
        get { return _hasAssignedSize; }
    } 

    /// <summary> 
    ///     The Window Name Property. 
    /// </summary>
    public String WindowName 
    {
        get{ return _name; }
        set{ _name = value; }
    } 

    /// <summary> 
    ///     The ParentWindow Property. 
    /// </summary>
    public IntPtr ParentWindow 
    {
        get{ return _parent; }
        set{ _parent = value; }
    } 

    /// <summary> 
    ///     The HwndSourceHook Property.  This allows a message hook to 
    ///     process window messages to the window.  A Hook provided in the
    ///     HwndSourceParameters will be installed before the call to 
    ///     CreateWindow and this hook will see the window creation msgs.
    /// </summary>
    public HwndSourceHook HwndSourceHook
    { 
        get{ return _hwndSourceHook; }
        set{ this._hwndSourceHook = value; } 
    } 

    /// <summary> 
    ///     The size that an HwndSource uses for layout purposes is
    ///     normally the size of the client area.  However, top-level
    ///     windows may want to use the size of the entire window,
    ///     including the non-client area, for layout purposes.  This 
    ///     allows properties such as Width and Height to reflect the
    ///     size of the entire window. 
    /// </summary> 
    public boolean AdjustSizingForNonClientArea
    { 
        get { return _adjustSizingForNonClientArea; }
        set { _adjustSizingForNonClientArea = value; }
    }

    /// <summary>
    ///     Whether or not the ancestors of an HwndSource should 
    ///     be considered as non-client area. 
    /// </summary>
    /// <remarks> 
    ///     Used in conjunction with the AdjustSizingForNonClientArea
    ///     setting.  XAML Browser Applications use this to enable
    ///     passing the size of the browser application itself to
    ///     layout. 
    /// </remarks>
    public boolean TreatAncestorsAsNonClientArea 
    { 
        get { return _treatAncestorsAsNonClientArea; }
        set { _treatAncestorsAsNonClientArea = value; } 
    }

    /// <summary>
    ///     Specifies whether or not the per-pixel opacity of the window content 
    ///     is respected.
    /// </summary> 
    /// <remarks> 
    ///     By enabling per-pixel opacity, the system will no longer draw the non-client area.
    /// </remarks> 
    public boolean UsesPerPixelOpacity
    {
        get {return _usesPerPixelOpacity;}
        set {_usesPerPixelOpacity = value;} 
    }

    /// <summary> 
    ///     The RestoreFocusMode for the window.
    /// </summary> 
    public RestoreFocusMode RestoreFocusMode
    {
        get { return _restoreFocusMode ?? Keyboard.DefaultRestoreFocusMode; }
        set { _restoreFocusMode = value; } 
    }

    /// <summary> 
    ///     The AcquireHwndFocusInMenuMode setting for the window.
    /// </summary> 
    public boolean AcquireHwndFocusInMenuMode
    {
        get { return _acquireHwndFocusInMenuMode ?? HwndSource.DefaultAcquireHwndFocusInMenuMode; }
        set { _acquireHwndFocusInMenuMode = value; } 
    }

    /// <summary> 
    /// Whether an HwndSource should be given messages straight off the
    /// message loop to preprocess, like top-level ones do normally. 
    /// </summary>
    /// <remarks> Used for RootBrowserWindow. </remarks>
    public boolean TreatAsInputRoot
    { 
        get { return _treatAsInputRoot ?? ((uint)_styleBits & NativeMethods.WS_CHILD) == 0; }
        set { _treatAsInputRoot = value; } 
    } 

    /// <summary> 
    /// == operator
    /// </summary>
    /// <param name="a"></param>
    /// <param name="b"></param> 
    /// <returns></returns>
    public static boolean operator==(HwndSourceParameters a, HwndSourceParameters b) 
    { 
        return a.Equals(b);
    } 

    /// <summary>
    /// != operator
    /// </summary> 
    /// <param name="a"></param>
    /// <param name="b"></param> 
    /// <returns></returns> 
    public static boolean operator!=(HwndSourceParameters a, HwndSourceParameters b)
    { 
        return !a.Equals(b);
    }

    /// <summary> 
    /// Compare two HwndSourceParameters blocks.
    /// </summary> 
    /// <param name="obj"></param> 
    /// <returns></returns>
    public override boolean Equals(object obj) 
    {
        if (obj == null)
        {
            return false; 
        }

        return Equals( (HwndSourceParameters)obj ); 
    }

    /// <summary>
    /// Compare two HwndSourceParameters blocks.
    /// </summary>
    /// <param name="obj"></param> 
    /// <returns></returns>
    public boolean Equals(HwndSourceParameters obj) 
    { 
        return ((this._classStyleBits    == obj._classStyleBits)
             && (this._styleBits         == obj._styleBits) 
             && (this._extendedStyleBits == obj._extendedStyleBits)
             && (this._x == obj._x)
             && (this._y == obj._y)
             && (this._width  == obj._width) 
             && (this._height == obj._height)
             && (this._name   == obj._name) 
             && (this._parent == obj._parent) 
             && (this._hwndSourceHook  == obj._hwndSourceHook)
             && (this._adjustSizingForNonClientArea == obj._adjustSizingForNonClientArea) 
             && (this._hasAssignedSize == obj._hasAssignedSize)
             // && (this._colorKey == obj._colorKey)
             // && (this._opacity == obj._opacity)
             // && (this._opacitySpecified == obj._opacitySpecified) 
             && (this._usesPerPixelOpacity == obj._usesPerPixelOpacity)
              ); 
    } 

    private int _classStyleBits; 
    private int _styleBits;
    private int _extendedStyleBits;
    private int _x;
    private int _y; 
    private int _width;
    private int _height; 
    private String _name; 
    private IntPtr _parent;
    private HwndSourceHook _hwndSourceHook; 

    private boolean _adjustSizingForNonClientArea;
    private boolean _hasAssignedSize;
    // private Nullable<Color> _colorKey; 
    // private double _opacity;
    // private boolean _opacitySpecified; // default value for opacity needs to be 1.0 
    private boolean _usesPerPixelOpacity; 
    private boolean/*?*/ _treatAsInputRoot;
    private boolean _treatAncestorsAsNonClientArea; 
    private RestoreFocusMode/*?*/ _restoreFocusMode;
    private boolean/*?*/ _acquireHwndFocusInMenuMode;

} 


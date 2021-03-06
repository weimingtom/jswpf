/**
 */
package org.summer.view.widget.controls;

import static org.summer.view.widget.controls.primitives.Selector.IsSelectionActivePropertyKey;

import java.awt.Rectangle;

import org.eclipse.osgi.framework.debug.Debug;
import org.summer.view.widget.DataTemplate;
import org.summer.view.widget.DependencyObject;
import org.summer.view.widget.DependencyObjectType;
import org.summer.view.widget.DependencyProperty;
import org.summer.view.widget.DependencyPropertyChangedEventArgs;
import org.summer.view.widget.DependencyPropertyKey;
import org.summer.view.widget.EventArgs;
import org.summer.view.widget.EventManager;
import org.summer.view.widget.EventPrivateKey;
import org.summer.view.widget.FrameworkElement;
import org.summer.view.widget.FrameworkPropertyMetadata;
import org.summer.view.widget.FrameworkPropertyMetadataOptions;
import org.summer.view.widget.FrameworkTemplate;
import org.summer.view.widget.PresentationSource;
import org.summer.view.widget.PropertyChangedCallback;
import org.summer.view.widget.Rect;
import org.summer.view.widget.RoutedEventArgs;
import org.summer.view.widget.StringComparison;
import org.summer.view.widget.UIElement;
import org.summer.view.widget.UIPropertyMetadata;
import org.summer.view.widget.WeakReference;
import org.summer.view.widget.controls.primitives.MenuBase;
import org.summer.view.widget.controls.primitives.Popup;
import org.summer.view.widget.controls.primitives.Selector;
import org.summer.view.widget.documents.TextElement;
import org.summer.view.widget.input.CaptureMode;
import org.summer.view.widget.input.FocusNavigationDirection;
import org.summer.view.widget.input.Key;
import org.summer.view.widget.input.KeyEventArgs;
import org.summer.view.widget.input.Keyboard;
import org.summer.view.widget.input.KeyboardNavigationMode;
import org.summer.view.widget.input.ModifierKeys;
import org.summer.view.widget.input.Mouse;
import org.summer.view.widget.input.MouseButtonEventArgs;
import org.summer.view.widget.input.MouseEventArgs;
import org.summer.view.widget.internal.EventHandler;
import org.summer.view.widget.media.Stretch;
import org.summer.view.widget.media.Visual;
import org.summer.view.widget.media.VisualTreeHelper;
import org.summer.view.widget.media.animation.VisualBrush;
import org.summer.view.widget.threading.DispatcherTimer;
import org.summer.view.window.FlowDirection;
import org.summer.view.window.VisualStateManager;
import org.summer.view.window.automation.peer.AutomationEvents;
import org.summer.view.window.automation.peer.AutomationPeer;

/// <summary> 
/// ComboBox control
/// </summary> 
//[Localizability(LocalizationCategory.ComboBox)] 
//[TemplatePart(Name = "PART_EditableTextBox", Type = typeof(TextBox))]
//[TemplatePart(Name = "PART_Popup", Type = typeof(Popup))] 
//[StyleTypedProperty(Property = "ItemContainerStyle", StyleTargetType = typeof(ComboBoxItem))]
public class ComboBox extends Selector
{
//    #region Constructors 

    static //ComboBox() 
    { 
        KeyboardNavigation.TabNavigationProperty.OverrideMetadata(typeof(ComboBox), new FrameworkPropertyMetadata(KeyboardNavigationMode.Local));
        KeyboardNavigation.ControlTabNavigationProperty.OverrideMetadata(typeof(ComboBox), new FrameworkPropertyMetadata(KeyboardNavigationMode.None)); 
        KeyboardNavigation.DirectionalNavigationProperty.OverrideMetadata(typeof(ComboBox), new FrameworkPropertyMetadata(KeyboardNavigationMode.None));

        // Disable tooltips on combo box when it is open
        ToolTipService.IsEnabledProperty.OverrideMetadata(typeof(ComboBox), new FrameworkPropertyMetadata(null, new CoerceValueCallback(CoerceToolTipIsEnabled))); 

        DefaultStyleKeyProperty.OverrideMetadata(typeof(ComboBox), new FrameworkPropertyMetadata(typeof(ComboBox))); 
        _dType = DependencyObjectType.FromSystemTypeInternal(typeof(ComboBox)); 

        IsTextSearchEnabledProperty.OverrideMetadata(typeof(ComboBox), new FrameworkPropertyMetadata(BooleanBoxes.TrueBox)); 

        EventManager.RegisterClassHandler(typeof(ComboBox), Mouse.LostMouseCaptureEvent, new MouseEventHandler(OnLostMouseCapture));
        EventManager.RegisterClassHandler(typeof(ComboBox), Mouse.MouseDownEvent, new MouseButtonEventHandler(OnMouseButtonDown), true); // call us even if the transparent button in the style gets the click.
        EventManager.RegisterClassHandler(typeof(ComboBox), Mouse.MouseMoveEvent, new MouseEventHandler(OnMouseMove)); 
        EventManager.RegisterClassHandler(typeof(ComboBox), Mouse.PreviewMouseDownEvent, new MouseButtonEventHandler(OnPreviewMouseButtonDown));
        EventManager.RegisterClassHandler(typeof(ComboBox), Mouse.MouseWheelEvent, new MouseWheelEventHandler(OnMouseWheel), true); // call us even if textbox in the style gets the click. 
        EventManager.RegisterClassHandler(typeof(ComboBox), UIElement.GotFocusEvent, new RoutedEventHandler(OnGotFocus)); // call us even if textbox in the style get focus 

        // Listen for ContextMenu openings/closings 
        EventManager.RegisterClassHandler(typeof(ComboBox), ContextMenuService.ContextMenuOpeningEvent, new ContextMenuEventHandler(OnContextMenuOpen), true);
        EventManager.RegisterClassHandler(typeof(ComboBox), ContextMenuService.ContextMenuClosingEvent, new ContextMenuEventHandler(OnContextMenuClose), true);

        IsEnabledProperty.OverrideMetadata(typeof(ComboBox), new UIPropertyMetadata(new PropertyChangedCallback(OnVisualStatePropertyChanged))); 
        IsMouseOverPropertyKey.OverrideMetadata(typeof(ComboBox), new UIPropertyMetadata(new PropertyChangedCallback(OnVisualStatePropertyChanged)));
        IsSelectionActivePropertyKey.OverrideMetadata(typeof(ComboBox), new FrameworkPropertyMetadata(new PropertyChangedCallback(OnVisualStatePropertyChanged))); 
    } 

    /// <summary> 
    /// Default DependencyObject constructor
    /// </summary>
    public ComboBox() 
    { 
    	super();
        Initialize();
    } 

//    #endregion

//    #region Properties

    /// <summary>
    ///     DependencyProperty for MaxDropDownHeight 
    /// </summary>
    // 
    public static final DependencyProperty MaxDropDownHeightProperty 
        = DependencyProperty.Register("MaxDropDownHeight", typeof(Double), typeof(ComboBox),
                                      new FrameworkPropertyMetadata(SystemParameters.PrimaryScreenHeight / 3, OnVisualStatePropertyChanged)); 

    /// <summary>
    ///     The maximum height of the popup
    /// </summary> 
//    [Bindable(true), Category("Layout")]
//    [TypeConverter(typeof(LengthConverter))] 
    public double MaxDropDownHeight 
    {
        get 
        {
            return (double)GetValue(MaxDropDownHeightProperty);
        }
        set 
        {
            SetValue(MaxDropDownHeightProperty, value); 
        } 
    }

    /// <summary>
    /// DependencyProperty for IsDropDownOpen
    /// </summary>
    public static final DependencyProperty IsDropDownOpenProperty = 
            DependencyProperty.Register(
                    "IsDropDownOpen", 
                    typeof(Boolean), 
                    typeof(ComboBox),
                    new FrameworkPropertyMetadata( 
                            BooleanBoxes.FalseBox,
                            FrameworkPropertyMetadataOptions.BindsTwoWayByDefault,
                            new PropertyChangedCallback(OnIsDropDownOpenChanged),
                            new CoerceValueCallback(CoerceIsDropDownOpen))); 

    /// <summary> 
    /// Whether or not the "popup" for this control is currently open 
    /// </summary>
//    [Bindable(true), Browsable(false), Category("Appearance")] 
    public boolean IsDropDownOpen
    {
        get { return (boolean) GetValue(IsDropDownOpenProperty); }
        set { SetValue(IsDropDownOpenProperty, BooleanBoxes.Box(value)); } 
    }

    private static Object CoerceIsDropDownOpen(DependencyObject d, Object value) 
    {
        if ((boolean) value) 
        {
            ComboBox cb = (ComboBox) d;
            if (!cb.IsLoaded)
            { 
                cb.RegisterToOpenOnLoad();
                return BooleanBoxes.FalseBox; 
            } 
        }

        return value;
    }

    private static Object CoerceToolTipIsEnabled(DependencyObject d, Object value) 
    {
        ComboBox cb = (ComboBox) d; 
        return cb.IsDropDownOpen ? BooleanBoxes.FalseBox : value; 
    }

    private void RegisterToOpenOnLoad()
    {
        Loaded += new RoutedEventHandler(OpenOnLoad);
    } 

    private void OpenOnLoad(Object sender, RoutedEventArgs e) 
    { 
        // Open combobox after it has rendered (Loaded is fired before 1st render)
        Dispatcher.BeginInvoke(DispatcherPriority.Input, new DispatcherOperationCallback(delegate(Object param) 
        {
            CoerceValue(IsDropDownOpenProperty);

            return null; 
        }), null);
    } 

    /// <summary>
    /// 
    /// </summary>
    /// <param name="e"></param>
    protected /*virtual*/ void OnDropDownOpened(EventArgs e)
    { 
        RaiseClrEvent(DropDownOpenedKey, e);
    } 

    /// <summary>
    /// 
    /// </summary>
    /// <param name="e"></param>
    protected /*virtual*/ void OnDropDownClosed(EventArgs e)
    { 
        RaiseClrEvent(DropDownClosedKey, e);
    } 

    private static void OnIsDropDownOpenChanged(DependencyObject d, DependencyPropertyChangedEventArgs e)
    { 
        ComboBox comboBox = (ComboBox)d;

        comboBox.HasMouseEnteredItemsHost = false;

        boolean newValue = (boolean) e.NewValue;
        boolean oldValue = !newValue; 

        // Fire accessibility event
        ComboBoxAutomationPeer peer = UIElementAutomationPeer.FromElement(comboBox) as ComboBoxAutomationPeer; 
        if(peer != null)
        {
            peer.RaiseExpandCollapseAutomationEvent(oldValue, newValue);
        } 

        if (newValue) 
        { 
            // When the drop down opens, take capture
            Mouse.Capture(comboBox, CaptureMode.SubTree); 

            // Select text if editable
            if (comboBox.IsEditable && comboBox.EditableTextBoxSite != null)
                comboBox.EditableTextBoxSite.SelectAll(); 


            // Popup.IsOpen is databound to IsDropDownOpen.  We can't know 
            // if IsDropDownOpen will be invalidated before Popup.IsOpen.
            // If we are invalidated first and we try to focus the item, we 
            // might succeed (b/c there's a logical link from the item to
            // a PresentationSource).  When the popup finally opens, Focus
            // will be sent to null because Core doesn't know what else to do.
            // So, we must focus the element only after we are sure the popup 
            // has opened.  We will queue an operation (at Send priority) to
            // do this work -- this is the soonest we can make this happen. 
            comboBox.Dispatcher.BeginInvoke( 
                DispatcherPriority.Send,
//                (DispatcherOperationCallback) delegate(Object arg) 
//                {
//                    ComboBox cb = (ComboBox)arg;
//                    if (cb.IsItemsHostVisible)
//                    { 
//                        cb.NavigateToItem(cb.InternalSelectedItem, ItemNavigateArgs.Empty, true /* alwaysAtTopOfViewport */);
//                    } 
//                    return null; 
//                },
                comboBox); 

            comboBox.OnDropDownOpened(EventArgs.Empty);
        }
        else 
        {
            // If focus is within the subtree, make sure we have the focus so that focus isn't in the disposed hwnd 
            if (comboBox.IsKeyboardFocusWithin) 
            {
                if (comboBox.IsEditable) 
                {
                    if (comboBox.EditableTextBoxSite != null && !comboBox.EditableTextBoxSite.IsKeyboardFocusWithin)
                    {
                        comboBox.Focus(); 
                    }
                } 
                else 
                {
                    // It's not editable, make sure the combobox has focus 
                    comboBox.Focus();
                }
            }

            // Make sure to clear the highlight when the dropdown closes
            comboBox.HighlightedElement = null; 

            if (comboBox.HasCapture)
            { 
                Mouse.Capture(null);
            }

            // No Popup in the style so fire closed now 
            if (comboBox._dropDownPopup == null)
            { 
                comboBox.OnDropDownClosed(EventArgs.Empty); 
            }
        } 

        comboBox.CoerceValue(IsSelectionBoxHighlightedProperty);
        comboBox.CoerceValue(ToolTipService.IsEnabledProperty);

        comboBox.UpdateVisualState();
    } 

    private void OnPopupClosed(Object source, EventArgs e)
    { 
        OnDropDownClosed(EventArgs.Empty);
    }

    /// <summary> 
    /// DependencyProperty for IsEditable
    /// </summary> 
    public static final DependencyProperty IsEditableProperty = 
            DependencyProperty.Register(
                    "IsEditable", 
                    typeof(Boolean),
                    typeof(ComboBox),
                    new FrameworkPropertyMetadata(
                            BooleanBoxes.FalseBox, 
                            new PropertyChangedCallback(OnIsEditableChanged)));


    /// <summary>
    ///     True if this ComboBox is editable. 
    /// </summary>
    /// <value></value>
    public boolean IsEditable
    { 
        get { return (boolean) GetValue(IsEditableProperty); }
        set { SetValue(IsEditableProperty, BooleanBoxes.Box(value)); } 
    } 

    private static void OnIsEditableChanged(DependencyObject d, DependencyPropertyChangedEventArgs e) 
    {
        ComboBox cb = d as ComboBox;

        cb.Update(); 
        cb.UpdateVisualState();
    } 

    /// <summary>
    ///     DependencyProperty for Text 
    /// </summary>
    public static final DependencyProperty TextProperty =
            DependencyProperty.Register(
                    "Text", 
                    typeof(String),
                    typeof(ComboBox), 
                    new FrameworkPropertyMetadata( 
                            String.Empty,
                            FrameworkPropertyMetadataOptions.BindsTwoWayByDefault | FrameworkPropertyMetadataOptions.Journal, 
                            new PropertyChangedCallback(OnTextChanged)));

    /// <summary>
    ///     The text of the currently selected item.  When there is no SelectedItem and IsEditable is true 
    ///     this is the text entered in the text box.  When IsEditable is false, this value represent the String version of the selected item.
    /// </summary> 
    /// <value></value> 
    public String Text
    { 
        get { return (String) GetValue(TextProperty); }
        set { SetValue(TextProperty, value); }
    }

    /// <summary>
    ///     DependencyProperty for the IsReadOnlyProperty 
    /// </summary> 
    public static final DependencyProperty IsReadOnlyProperty =
            TextBox.IsReadOnlyProperty.AddOwner(typeof(ComboBox)); 

    /// <summary>
    ///     When the ComboBox is Editable, if the TextBox within it is read only.
    /// </summary> 
    public boolean IsReadOnly
    { 
        get { return (boolean) GetValue(IsReadOnlyProperty); } 
        set { SetValue(IsReadOnlyProperty, BooleanBoxes.Box(value)); }
    } 

    private static final DependencyPropertyKey SelectionBoxItemPropertyKey =
        DependencyProperty.RegisterReadOnly("SelectionBoxItem", typeof(Object), typeof(ComboBox),
                                            new FrameworkPropertyMetadata(String.Empty)); 

    // This property is used as a Style Helper. 
    // When the SelectedItem is a UIElement a VisualBrush is created and set to the Fill property 
    // of a Rectangle. Then we set SelectionBoxItem to that rectangle.
    // For data items, SelectionBoxItem is set to a String. 
    /// <summary>
    /// The DependencyProperty for the SelectionBoxItemProperty
    /// </summary>
    public static final DependencyProperty SelectionBoxItemProperty = SelectionBoxItemPropertyKey.DependencyProperty; 

    /// <summary> 
    /// Used to display the selected item 
    /// </summary>
    public Object SelectionBoxItem 
    {
        get { return GetValue(SelectionBoxItemProperty); }
        private set { SetValue(SelectionBoxItemPropertyKey, value); }
    } 

    private static final DependencyPropertyKey SelectionBoxItemTemplatePropertyKey = 
        DependencyProperty.RegisterReadOnly("SelectionBoxItemTemplate", typeof(DataTemplate), typeof(ComboBox), 
                                            new FrameworkPropertyMetadata((DataTemplate)null));

    /// <summary>
    /// The DependencyProperty for the SelectionBoxItemProperty
    /// </summary>
    public static final DependencyProperty SelectionBoxItemTemplateProperty = SelectionBoxItemTemplatePropertyKey.DependencyProperty; 

    /// <summary> 
    /// Used to set the item DataTemplate 
    /// </summary>
    public DataTemplate SelectionBoxItemTemplate 
    {
        get { return (DataTemplate) GetValue(SelectionBoxItemTemplateProperty); }
        private set { SetValue(SelectionBoxItemTemplatePropertyKey, value); }
    } 

    private static final DependencyPropertyKey SelectionBoxItemStringFormatPropertyKey = 
        DependencyProperty.RegisterReadOnly("SelectionBoxItemStringFormat", typeof(String), typeof(ComboBox), 
                                            new FrameworkPropertyMetadata((String)null));

    /// <summary>
    /// The DependencyProperty for the SelectionBoxItemProperty
    /// </summary>
    public static final DependencyProperty SelectionBoxItemStringFormatProperty = SelectionBoxItemStringFormatPropertyKey.DependencyProperty; 

    /// <summary> 
    /// Used to set the item DataStringFormat 
    /// </summary>
    public String SelectionBoxItemStringFormat 
    {
        get { return (String) GetValue(SelectionBoxItemStringFormatProperty); }
        private set { SetValue(SelectionBoxItemStringFormatPropertyKey, value); }
    } 

    /// <summary> 
    ///     DependencyProperty for StaysOpenOnEdit 
    /// </summary>
    public static final DependencyProperty StaysOpenOnEditProperty 
        = DependencyProperty.Register("StaysOpenOnEdit", typeof(Boolean), typeof(ComboBox),
                                      new FrameworkPropertyMetadata(BooleanBoxes.FalseBox));

    /// <summary> 
    ///     Determines whether the ComboBox will remain open when clicking on
    ///     the text box when the drop down is open 
    /// </summary> 
    /// <value></value>
    public boolean StaysOpenOnEdit 
    {
        get
        {
            return (boolean)GetValue(StaysOpenOnEditProperty); 
        }
        set 
        { 
            SetValue(StaysOpenOnEditProperty, BooleanBoxes.Box(value));
        } 
    }


    private static final DependencyPropertyKey IsSelectionBoxHighlightedPropertyKey = 
                DependencyProperty.RegisterReadOnly("IsSelectionBoxHighlighted", typeof(boolean), typeof(ComboBox),
                                    new FrameworkPropertyMetadata(BooleanBoxes.FalseBox, 
                                                                  null, 
                                                                  new CoerceValueCallback(CoerceIsSelectionBoxHighlighted)));

    /// <summary>
    /// The DependencyProperty for the IsSelectionBoxHighlighted Property
    /// </summary>
    private static final DependencyProperty IsSelectionBoxHighlightedProperty = IsSelectionBoxHighlightedPropertyKey.DependencyProperty; 

    /// <summary> 
    /// Indicates the SelectionBox area should be highlighted 
    /// </summary>
    public boolean IsSelectionBoxHighlighted 
    {
        get { return (boolean)GetValue(IsSelectionBoxHighlightedProperty); }
    }

    private static Object CoerceIsSelectionBoxHighlighted(Object o, Object value)
    { 
        ComboBox comboBox = (ComboBox)o; 
        return (!comboBox.IsDropDownOpen && comboBox.IsKeyboardFocusWithin) ||
               (comboBox.HighlightedElement != null && comboBox.HighlightedElement.Content == comboBox._clonedElement); 
    }

//    #endregion

//    #region Events

    /// <summary> 
    ///     DropDown Open event
    /// </summary> 
    public /*event*/ EventHandler DropDownOpened
    {
        add { EventHandlersStoreAdd(DropDownOpenedKey, value); }
        remove { EventHandlersStoreRemove(DropDownOpenedKey, value); } 
    }
    private static final EventPrivateKey DropDownOpenedKey = new EventPrivateKey(); 

    /// <summary>
    ///     DropDown Close event 
    /// </summary>
    public /*event*/ EventHandler DropDownClosed
    {
        add { EventHandlersStoreAdd(DropDownClosedKey, value); } 
        remove { EventHandlersStoreRemove(DropDownClosedKey, value); }
    } 
    private static final EventPrivateKey DropDownClosedKey = new EventPrivateKey(); 

//    #endregion 

//    #region Selection Changed

    // Combo Box has several methods of input for selecting items 
    //   * Selector.OnSelectionChanged
    //   * ComboBox.Text Changed 
    //   * Editable Text Box changed 
    // When any one of these inputs change, the other two must be updated to reflect
    // the third. 
    //
    // When Text changes, TextUpdated() tries searching (if TextSearch is enabled) for an
    //   item that exactly matches the new value of Text.  If it finds one, it sets
    //   selected index to that item.  This will cause OnSelectionChanged to update 
    //   the SelectionBoxItem.  Finally TextUpdated() updates the TextBox.
    // 
    // When the TextBox text changes, TextUpdated() tries searching (if TextSearch is enabled) 
    //   for an item that partially matches the new value of Text.  If it finds one, it updates
    //   The textbox and selects the remaining portion of text.  Then it sets the selected index 
    //   which will cause OnSelectionChanged to update the SelectionBoxItem.  Finally
    //   TextUpdated() updates ComboBox.Text property.
    //
    // When Selection changes, SelectedItemUpdated() will update the ComboBox.Text property 
    //   and then update the SelectionBoxItem or EditableTextBox.Text depending on edit mode
    // 

    /// <summary>
    /// A /*virtual*/ function that is called when the selection is changed. Default behavior 
    /// is to raise a SelectionChangedEvent
    /// </summary>
    /// <param name="e">The inputs for this event. Can be raised (default behavior) or processed
    ///   in some other way.</param> 
    protected /*override*/ void OnSelectionChanged(SelectionChangedEventArgs e)
    { 
        super.OnSelectionChanged(e); 

        SelectedItemUpdated(); 

        if (IsDropDownOpen)
        {
            Object selectedItem = InternalSelectedItem; 
            if (selectedItem != null)
            { 
                NavigateToItem(selectedItem, ItemNavigateArgs.Empty); 
            }
        } 

        if (    AutomationPeer.ListenerExists(AutomationEvents.SelectionPatternOnInvalidated)
            ||  AutomationPeer.ListenerExists(AutomationEvents.SelectionItemPatternOnElementSelected)
            ||  AutomationPeer.ListenerExists(AutomationEvents.SelectionItemPatternOnElementAddedToSelection) 
            ||  AutomationPeer.ListenerExists(AutomationEvents.SelectionItemPatternOnElementRemovedFromSelection)   )
        { 
            ComboBoxAutomationPeer peer = UIElementAutomationPeer.CreatePeerForElement(this) as ComboBoxAutomationPeer; 
            if (peer != null)
                peer.RaiseSelectionEvents(e); 
        }
    }

    // When the selected item (or its content) changes, update 
    // The SelectedItem property and the Text properties
    // ComboBoxItem also calls this method when its content changes 
    /*internal*/ public void SelectedItemUpdated() 
    {
        try 
        {
            UpdatingSelectedItem = true;

            // If the selection changed as a result of Text or the TextBox 
            // changing, don't update the Text property - TextUpdated() will
            if (!UpdatingText) 
            { 
                // Don't change the text in the TextBox unless there is an item selected.
                String text = TextSearch.GetPrimaryTextFromItem(this, InternalSelectedItem); 

                if (Text != text)
                {
                    SetCurrentValueInternal(TextProperty, text); 
                }
            } 

            // Update SelectionItem/TextBox
            Update(); 
        }
        finally
        {
            UpdatingSelectedItem = false; 
        }
    } 

    // When the Text Property changes, search for an item exactly
    // matching the new text and set the selected index to that item 
    private static void OnTextChanged(DependencyObject d, DependencyPropertyChangedEventArgs e)
    {
        ComboBox cb = (ComboBox)d;

        ComboBoxAutomationPeer peer = UIElementAutomationPeer.FromElement(cb) as ComboBoxAutomationPeer;
	    // Raise the propetyChangeEvent for Value if Automation Peer exist, the new Value must 
        // be the one in SelctionBoxItem(selected value is the one user will care about) 
        if (peer != null)
            peer.RaiseValuePropertyChangedEvent((String)e.OldValue, (String)e.NewValue); 

        cb.TextUpdated((String)e.NewValue, false);

    } 

    // When the user types in the TextBox, search for an item that partially 
    // matches the new text and set the selected index to that item 
    private void OnEditableTextBoxTextChanged(Object sender, TextChangedEventArgs e)
    { 
        Debug.Assert(_editableTextBoxSite == sender);

        if (!IsEditable)
        { 
            // Don't do any work if we're not editable.
            return; 
        } 

        TextUpdated(EditableTextBoxSite.Text, true); 
    }

    // When selection changes, save the location of the selection start
    private void OnEditableTextBoxSelectionChanged(Object sender, RoutedEventArgs e) 
    {
        _textBoxSelectionStart = EditableTextBoxSite.SelectionStart; 
    } 

    // If TextSearch is enabled search for an item matching the new text 
    // (partial search if user is typing, exact search if setting Text)
    private void TextUpdated(String newText, boolean textBoxUpdated)
    {
        // Only process this event if it is coming from someone outside setting Text directly 
        if (!UpdatingText && !UpdatingSelectedItem)
        { 
            try 
            {
                // Set the updating flags so we don't reenter this function 
                UpdatingText = true;

                // Try searching for an item matching the new text
                if (IsTextSearchEnabled) 
                {
                    int matchedIndex = TextSearch.FindMatchingPrefix(this, newText); 

                    if (matchedIndex >= 0)
                    { 
                        // Allow partial matches when updating textbox
                        if (textBoxUpdated)
                        {
                            int selectionStart = EditableTextBoxSite.SelectionStart; 
                            // Perform type search when the selection is at the end
                            // of the textbox and the selection start increased 
                            if (selectionStart == newText.Length && 
                                selectionStart > _textBoxSelectionStart)
                            { 
                                // Replace the currently typed text with the text
                                // from the matched item
                                String matchedText = TextSearch.GetPrimaryTextFromItem(this, Items[matchedIndex]);

                                // Replace the TextBox's text with the matched text and
                                // select the text beyond what the user typed 
                                EditableTextBoxSite.Text = matchedText; 
                                EditableTextBoxSite.SelectionStart = newText.Length;
                                EditableTextBoxSite.SelectionLength = matchedText.Length - newText.Length; 

                                // ComboBox's text property should be updated with the matched text
                                newText = matchedText;
                            } 
                        }
                        else //Text Property Set 
                        { 
                            // Require exact matches when setting TextProperty
                            String matchedText = TextSearch.GetPrimaryTextFromItem(this, Items[matchedIndex]); 
                            if (!String.Equals(newText, matchedText, StringComparison.CurrentCulture))
                            {
                                // Strings not identical, no match
                                matchedIndex = -1; 
                            }
                        } 
                    } 

                    // Update SelectedIndex if it changed 
                    if (matchedIndex != SelectedIndex)
                    {
                        // OnSelectionChanged will update the SelectedItem
                        SetCurrentValueInternal(SelectedIndexProperty, matchedIndex); 
                    }
                } 

                // Update TextProperty when TextBox changes and TextBox when TextProperty changes
                if (textBoxUpdated) 
                {
                    SetCurrentValueInternal(TextProperty, newText);
                }
                else if (EditableTextBoxSite != null) 
                {
                    EditableTextBoxSite.Text = newText; 
                } 
            }
            finally 
            {
                // Clear the updating flag
                UpdatingText = false;
            } 
        }
    } 

    // Updates:
    //    SelectionBox if not editable 
    //    EditableTextBox.Text if editable
    private void Update()
    {
        if (IsEditable) 
        {
            UpdateEditableTextBox(); 
        } 
        else
        { 
            UpdateSelectionBoxItem();
        }
    }

    // Update the editable TextBox to match combobox text
    private void UpdateEditableTextBox() 
    { 
        if (!UpdatingText)
        { 
            try
            {
                UpdatingText = true;

                String text = Text;

                // Copy ComboBox.Text to the editable TextBox 
                if (EditableTextBoxSite != null && EditableTextBoxSite.Text != text)
                { 
                    EditableTextBoxSite.Text = text;
                    EditableTextBoxSite.SelectAll();
                }
            } 
            finally
            { 
                UpdatingText = false; 
            }
        } 
    }

    /// <summary>
    /// This function updates the selected item in the "selection box". 
    /// This is called when selection changes or when the combobox
    /// switches from editable to non-editable or vice versa. 
    /// This will also get called in ApplyTemplate in case selection 
    /// is set prior to the control being measured.
    /// </summary> 
    private void UpdateSelectionBoxItem()
    {
        // propagate the new selected item to the SelectionBoxItem property;
        // this displays it in the selection box 
        Object item = InternalSelectedItem;
        DataTemplate itemTemplate = ItemTemplate; 
        String stringFormat = ItemStringFormat; 

        // if Items contains an explicit ContentControl, use its content instead 
        // (this handles the case of ComboBoxItem)
        ContentControl contentControl = item as ContentControl;

        if (contentControl != null) 
        {
            item = contentControl.Content; 
            itemTemplate = contentControl.ContentTemplate; 
            stringFormat = contentControl.ContentStringFormat;
        } 

        if (_clonedElement != null)
        {
            _clonedElement.LayoutUpdated -= CloneLayoutUpdated; 
            _clonedElement = null;
        } 

        if (itemTemplate == null && ItemTemplateSelector == null && stringFormat == null)
        { 
            // if the item is a logical element it cannot be displayed directly in
            // the selection box because it already belongs to the tree (in the dropdown box).
            // Instead, try to extract some useful text from the visual.
            DependencyObject logicalElement = item as DependencyObject; 

            if (logicalElement != null) 
            { 
                // If the item is a UIElement, create a copy using a visual brush
                _clonedElement = logicalElement as UIElement; 

                if (_clonedElement != null)
                {
                    // Create visual copy of selected element 
                    VisualBrush visualBrush = new VisualBrush(_clonedElement);
                    visualBrush.Stretch = Stretch.None; 

                    //Set position and dimension of content
                    visualBrush.ViewboxUnits = BrushMappingMode.Absolute; 
                    visualBrush.Viewbox = new Rect(_clonedElement.RenderSize);

                    //Set position and dimension of tile
                    visualBrush.ViewportUnits = BrushMappingMode.Absolute; 
                    visualBrush.Viewport = new Rect(_clonedElement.RenderSize);

                    // If the FlowDirection on cloned element doesn't match the combobox's apply a mirror 
                    // If the FlowDirection on cloned element doesn't match its parent's apply a mirror
                    // If both are true, they cancel out so no mirror should be applied 
                    FlowDirection elementFD = (FlowDirection)_clonedElement.GetValue(FlowDirectionProperty);
                    DependencyObject parent = VisualTreeHelper.GetParent(_clonedElement);
                    FlowDirection parentFD = parent == null ? FlowDirection : (FlowDirection)parent.GetValue(FlowDirectionProperty);
                    if ((elementFD != this.FlowDirection) != (elementFD != parentFD)) 
                    {
                        visualBrush.Transform = new MatrixTransform(new Matrix(-1.0, 0.0, 0.0, 1.0, _clonedElement.RenderSize.Width, 0.0)); 
                    } 

                    // Apply visual brush to a rectangle 
                    Rectangle rect = new Rectangle();
                    rect.Fill = visualBrush;
                    rect.Width = _clonedElement.RenderSize.Width;
                    rect.Height = _clonedElement.RenderSize.Height; 

                    _clonedElement.LayoutUpdated += CloneLayoutUpdated; 

                    item = rect;
                    itemTemplate = null; 
                }
                else
                {
                    item = ExtractString(logicalElement); 
                    itemTemplate = ContentPresenter.StringContentTemplate;
                } 
            } 
        }

        // display a null item by an empty String
        if (item == null)
        {
            item = String.Empty; 
            itemTemplate = ContentPresenter.StringContentTemplate;
        } 

        SelectionBoxItem = item;
        SelectionBoxItemTemplate = itemTemplate; 
        SelectionBoxItemStringFormat = stringFormat;
    }

    // Update our clone's size to match the actual Object's size 
    private void CloneLayoutUpdated(Object sender, EventArgs e)
    { 
        Rectangle rect = (Rectangle)SelectionBoxItem; 
        rect.Width = _clonedElement.RenderSize.Width;
        rect.Height = _clonedElement.RenderSize.Height; 

        VisualBrush visualBrush = (VisualBrush)rect.Fill;
        visualBrush.Viewbox = new Rect(_clonedElement.RenderSize);
        visualBrush.Viewport = new Rect(_clonedElement.RenderSize); 
    }

//    #endregion 

//    #region Protected Methods 

    /// <summary>
    /// Change to the correct visual state.
    /// </summary> 
    /// <param name="useTransitions">
    /// true to use transitions when updating the visual state, false to snap directly to the new visual state. 
    /// </param> 
    /*internal*/ public /*override*/ void ChangeVisualState(boolean useTransitions)
    { 
        // Common States Group
        if (!IsEnabled)
        {
            VisualStateManager.GoToState(this, VisualStates.StateDisabled, useTransitions); 
        }
        else if (IsMouseOver) 
        { 
            VisualStateManager.GoToState(this, VisualStates.StateMouseOver, useTransitions);
        } 
        else
        {
            VisualStateManager.GoToState(this, VisualStates.StateNormal, useTransitions);
        } 

        // Focus States Group 
        if (!GetIsSelectionActive(this)) 
        {
            VisualStateManager.GoToState(this, VisualStates.StateUnfocused, useTransitions); 
        }
        else if (IsDropDownOpen)
        {
            VisualStateManager.GoToState(this, VisualStates.StateFocusedDropDown, useTransitions); 
        }
        else 
        { 
            VisualStateManager.GoToState(this, VisualStates.StateFocused, useTransitions);
        } 

        // Edit States Group
        if (IsEditable)
        { 
            VisualStateManager.GoToState(this, VisualStates.StateEditable, useTransitions);
        } 
        else 
        {
            VisualStateManager.GoToState(this, VisualStates.StateUneditable, useTransitions); 
        }

        super.ChangeVisualState(useTransitions);
    } 

    /// <summary> 
    ///     If control has a scrollviewer in its style and has a custom keyboard scrolling behavior when HandlesScrolling should return true. 
    /// Then ScrollViewer will not handle keyboard input and leave it up to the control.
    /// </summary> 
    protected /*internal*//*override*/ boolean HandlesScrolling
    {
        get { return true; }
    } 

    /// <summary> 
    /// Prepare the element to display the item.  This may involve 
    /// applying styles, setting bindings, etc.
    /// </summary> 
    protected /*override*/ void PrepareContainerForItemOverride(DependencyObject element, Object item)
    {
        super.PrepareContainerForItemOverride(element, item);

        if (item instanceof Separator)
            Separator.PrepareContainer(element as Control); 
    } 

    /// <summary> 
    ///     Called when this element or any below gets focus.
    /// </summary>
    private static void OnGotFocus(Object sender, RoutedEventArgs e)
    { 
        // When ComboBox gets logical focus, select the text inside us.
        ComboBox comboBox = (ComboBox)sender; 

        // If we're an editable combobox, forward focus to the TextBox element
        if (!e.Handled) 
        {
            if (comboBox.IsEditable && comboBox.EditableTextBoxSite != null)
            {
                if (e.OriginalSource == comboBox) 
                {
                    comboBox.EditableTextBoxSite.Focus(); 
                    e.Handled = true; 
                }
                else if (e.OriginalSource == comboBox.EditableTextBoxSite) 
                {
                    comboBox.EditableTextBoxSite.SelectAll();
                }
            } 
        }
    } 

    /// <summary>
    ///     Called when an item is being focused 
    /// </summary>
    /*internal*/ public /*override*/ void FocusItem(Object item, ItemNavigateArgs itemNavigateArgs)
    {
        // The base implementation sets focus, and we don't want to do that 
        // if we're editable.
        if (!IsEditable) 
        { 
            super.FocusItem(item, itemNavigateArgs);
        } 

        ComboBoxItem highlightedElement = ItemContainerGenerator.ContainerFromItem(item) as ComboBoxItem;
        HighlightedElement = highlightedElement;

        // When IsEditable is 'true', we'll always want to commit the selection. e.g. when user press KeyUp/Down.
        // However, when IsEditable is 'false' and Dropdown is open, we could get here when user navigate to 
        // the item using ITS. In this case, we don't want to commit the selection. 
        if ((IsEditable || (!IsDropDownOpen)) && itemNavigateArgs.DeviceUsed is KeyboardDevice)
        { 
            if (highlightedElement != null)
            {
                SetCurrentValueInternal(SelectedIndexProperty, ItemContainerGenerator.IndexFromContainer(highlightedElement));
            } 
            else
            { 
                SetCurrentValueInternal(SelectedIndexProperty, Items.IndexOf(item)); 
            }
        } 
    }


    /// <summary> 
    ///     An event reporting that the IsKeyboardFocusWithin property changed.
    /// </summary> 
    protected /*override*/ void OnIsKeyboardFocusWithinChanged(DependencyPropertyChangedEventArgs e) 
    {
        super.OnIsKeyboardFocusWithinChanged(e); 

        // This is for the case when focus goes elsewhere and the popup is still open; make sure it is closed.
        if (IsDropDownOpen && !IsKeyboardFocusWithin)
        { 
            // IsKeyboardFocusWithin still flickers under certain conditions.  The case
            // we care about is focus going from the ComboBox to a ComboBoxItem. 
            // Here we can just check if something has focus and if it's a child 
            // of ours or is a context menu that opened below us.
            DependencyObject currentFocus = Keyboard.FocusedElement as DependencyObject; 
            if (currentFocus == null || (!IsContextMenuOpen && ItemsControlFromItemContainer(currentFocus) != this))
            {
                Close();
            } 
        }

        CoerceValue(IsSelectionBoxHighlightedProperty); 
    }

    /// <summary>
    ///     An event reporting a mouse wheel rotation.
    /// </summary>
    private static void OnMouseWheel(Object sender, MouseWheelEventArgs e) 
    {
        ComboBox comboBox = (ComboBox)sender; 

        // If we get a mouse wheel event we should scroll when the
        // drop down is closed and eat the mouse wheel when it's open. 
        // (If the drop down is open and has a scrollviewer, the scrollviewer
        // will handle it before we get here).
        // We should only do this when focus is within the combobox,
        // otherwise we could severely confuse the user. 
        if (comboBox.IsKeyboardFocusWithin)
        { 
            if (!comboBox.IsDropDownOpen) 
            {
                // Negative delta means "down", which means we should move next in that case. 
                if (e.Delta < 0)
                {
                    comboBox.SelectNext();
                } 
                else
                { 
                    comboBox.SelectPrev(); 
                }
            } 

            e.Handled = true;
        }
        else 
        {
            // If focus isn't within the combobox (say, we're not focusable) 
            // but we get a mouse wheel event, we should do nothing unless 
            // the drop down is open, in which case we should eat it.
            if (comboBox.IsDropDownOpen) 
            {
                e.Handled = true;
            }
        } 
    }

    private static void OnContextMenuOpen(Object sender, ContextMenuEventArgs e) 
    {
        ((ComboBox)sender).IsContextMenuOpen = true; 
    }

    private static void OnContextMenuClose(Object sender, ContextMenuEventArgs e)
    { 
        ((ComboBox)sender).IsContextMenuOpen = false;
    } 

    /// <summary>
    /// Called when IsMouseCaptured changes on this element. 
    /// </summary>
    /// <param name="e"></param>
    protected /*override*/ void OnIsMouseCapturedChanged(DependencyPropertyChangedEventArgs e)
    { 
        // When we take capture, we should start a timer to call
        // us back and do auto scrolling behavior. 
        if (IsMouseCaptured) 
        {
            Debug.Assert(_autoScrollTimer == null, "IsMouseCaptured went from true to true"); 
            if (_autoScrollTimer == null)
            {
                _autoScrollTimer = new DispatcherTimer(DispatcherPriority.SystemIdle);
                _autoScrollTimer.Interval = AutoScrollTimeout; 
                _autoScrollTimer.Tick += new EventHandler(OnAutoScrollTimeout);
                _autoScrollTimer.Start(); 
            } 
        }
        else 
        {
            if (_autoScrollTimer != null)
            {
                _autoScrollTimer.Stop(); 
                _autoScrollTimer = null;
            } 
        } 

        super.OnIsMouseCapturedChanged(e); 
    }

//    #endregion

//    #region Internal Methods

    // Helper function called by ComboBoxItem when it receives a MouseDown 
    /*internal*/ public void NotifyComboBoxItemMouseDown(ComboBoxItem comboBoxItem)
    { 
    }

    // Helper function called by ComboBoxItem when it receives a MouseUp
    /*internal*/ public void NotifyComboBoxItemMouseUp(ComboBoxItem comboBoxItem) 
    {
        Object item = ItemContainerGenerator.ItemFromContainer(comboBoxItem); 
        if (item != null) 
        {
            SelectionChange.SelectJustThisItem(item, true /* assumeInItemsCollection */); 
        }

        Close();
    } 

    // Called when Item is entered via mouse or keyboard focus 
    /*internal*/ public void NotifyComboBoxItemEnter(ComboBoxItem item) 
    {
        // When a ComboBoxItem is entered, it should be highlighted (and focused). 
        // Note: We may reach this before a nested combo box can grab capture
        //       if one of its items releases capture.  In this case, ignore the
        //       enter event
        if (IsDropDownOpen && Mouse.Captured == this && DidMouseMove()) 
        {
            HighlightedElement = item; 
            if (!IsEditable && !item.IsKeyboardFocusWithin) 
            {
                item.Focus(); 
            }
        }
    }

    /// <summary>
    /// Return true if the item is (or is eligible to be) its own ItemUI 
    /// </summary> 
    protected /*override*/ boolean IsItemItsOwnContainerOverride(Object item)
    { 
        return (item instanceof ComboBoxItem);
    }

    /// <summary> Create or identify the element used to display the given item. </summary> 
    protected /*override*/ DependencyObject GetContainerForItemOverride()
    { 
        return new ComboBoxItem(); 
    }

//    #endregion

//    #region Private Methods

    private void Initialize()
    { 
        CanSelectMultiple = false; 
    }

    /// <summary>
    ///     An event reporting a key was pressed
    /// </summary>
    protected /*override*/ void OnPreviewKeyDown(KeyEventArgs e) 
    {
        // Only process preview key events if they going to our editable text box 
        if (IsEditable && e.OriginalSource == EditableTextBoxSite) 
        {
            KeyDownHandler(e); 
        }
    }

    /// <summary> 
    ///     An event reporting a key was pressed
    /// </summary> 
    protected /*override*/ void OnKeyDown(KeyEventArgs e) 
    {
        KeyDownHandler(e); 
    }

    private void KeyDownHandler(KeyEventArgs e)
    { 
        boolean handled = false;
        Key key = e.Key; 

        // We want to handle Alt key. Get the real key if it is Key.System.
        if (key == Key.System) 
        {
            key = e.SystemKey;
        }

        // In Right to Left mode we switch Right and Left keys
        boolean isRTL = (FlowDirection == FlowDirection.RightToLeft); 

        switch (key)
        { 
            case Key.Up:
                handled = true;
                if ((e.KeyboardDevice.Modifiers & ModifierKeys.Alt) == ModifierKeys.Alt)
                { 
                    KeyboardToggleDropDown(true /* commitSelection */);
                } 
                else 
                {
                    // When the drop down isn't open then focus is on the ComboBox 
                    // and we can't use KeyboardNavigation.
                    if (IsItemsHostVisible)
                    {
                        NavigateByLine(HighlightedItem, FocusNavigationDirection.Up, new ItemNavigateArgs(e.Device, Keyboard.Modifiers)); 
                    }
                    else 
                    { 
                        SelectPrev();
                    } 
                }

                break;

            case Key.Down:
                handled = true; 
                if ((e.KeyboardDevice.Modifiers & ModifierKeys.Alt) == ModifierKeys.Alt) 
                {
                    KeyboardToggleDropDown(true /* commitSelection */); 
                }
                else
                {
                    if (IsItemsHostVisible) 
                    {
                        NavigateByLine(HighlightedItem, FocusNavigationDirection.Down, new ItemNavigateArgs(e.Device, Keyboard.Modifiers)); 
                    } 
                    else
                    { 
                        SelectNext();
                    }
                }

                break;

            case Key.F4: 
                if ((e.KeyboardDevice.Modifiers & ModifierKeys.Alt) == 0)
                { 
                    KeyboardToggleDropDown(true /* commitSelection */);
                    handled = true;
                }
                break; 

            case Key.Escape: 
                if (IsDropDownOpen) 
                {
                    KeyboardCloseDropDown(false /* commitSelection */); 
                    handled = true;
                }
                break;

            case Key.Enter:
                if (IsDropDownOpen) 
                { 
                    KeyboardCloseDropDown(true /* commitSelection */);
                    handled = true; 
                }
                break;

            case Key.Home: 
                if ((e.KeyboardDevice.Modifiers & ModifierKeys.Alt) != ModifierKeys.Alt && !IsEditable)
                { 
                    if (IsItemsHostVisible) 
                    {
                        NavigateToStart(new ItemNavigateArgs(e.Device, Keyboard.Modifiers)); 
                    }
                    else
                    {
                        SelectFirst(); 
                    }
                    handled = true; 
                } 
                break;

            case Key.End:
                if ((e.KeyboardDevice.Modifiers & ModifierKeys.Alt) != ModifierKeys.Alt && !IsEditable)
                {
                    if (IsItemsHostVisible) 
                    {
                        NavigateToEnd(new ItemNavigateArgs(e.Device, Keyboard.Modifiers)); 
                    } 
                    else
                    { 
                        SelectLast();
                    }
                    handled = true;
                } 
                break;

            case Key.Right: 
                if ((e.KeyboardDevice.Modifiers & ModifierKeys.Alt) != ModifierKeys.Alt && !IsEditable)
                { 
                    if (IsItemsHostVisible)
                    {
                        NavigateByLine(HighlightedItem, FocusNavigationDirection.Right, new ItemNavigateArgs(e.Device, Keyboard.Modifiers));
                    } 
                    else
                    { 
                        if (!isRTL) 
                        {
                            SelectNext(); 
                        }
                        else
                        {
                            // If it's RTL then Right should go backwards 
                            SelectPrev();
                        } 
                    } 
                    handled = true;
                } 
                break;

            case Key.Left:
                if ((e.KeyboardDevice.Modifiers & ModifierKeys.Alt) != ModifierKeys.Alt && !IsEditable) 
                {
                    if (IsItemsHostVisible) 
                    { 
                        NavigateByLine(HighlightedItem, FocusNavigationDirection.Left, new ItemNavigateArgs(e.Device, Keyboard.Modifiers));
                    } 
                    else
                    {
                        if (!isRTL)
                        { 
                            SelectPrev();
                        } 
                        else 
                        {
                            // If it's RTL then Left should go the other direction 
                            SelectNext();
                        }
                    }
                    handled = true; 
                }
                break; 

            case Key.PageUp:
                if (IsItemsHostVisible) 
                {
                    NavigateByPage(HighlightedItem, FocusNavigationDirection.Up, new ItemNavigateArgs(e.Device, Keyboard.Modifiers));
                    handled = true;
                } 
                break;

            case Key.PageDown: 
                if (IsItemsHostVisible)
                { 
                    NavigateByPage(HighlightedItem, FocusNavigationDirection.Down, new ItemNavigateArgs(e.Device, Keyboard.Modifiers));
                    handled = true;
                }
                break; 

            case Key.Oem5: 
                if (Keyboard.Modifiers == ModifierKeys.Control) 
                {
                    // If Control is pressed (without Alt, Shift or Windows being pressed) 
                    // Scroll into view the selected item -- we want to highlight the item
                    // that we scroll in, so we should navigate to it.
                    NavigateToItem(InternalSelectedItem, ItemNavigateArgs.Empty);
                    handled = true; 
                }
                break; 

            default:
                handled = false; 
                break;
        }
        if (handled)
        { 
            e.Handled = true;
        } 
    } 

    private void SelectPrev() 
    {
        if (!Items.IsEmpty)
        {
            int selectedIndex = InternalSelectedIndex; 

            // Search backwards from SelectedIndex - 1 but don't start before the beginning. 
            // If SelectedIndex is less than 0, there is nothing to select before this item. 
            if (selectedIndex > 0)
            { 
                SelectItemHelper(selectedIndex - 1, -1, -1);
            }
        }
    } 

    private void SelectNext() 
    { 
        int count = Items.Count;
        if (count > 0) 
        {
            int selectedIndex = InternalSelectedIndex;

            // Search forwards from SelectedIndex + 1 but don't start past the end. 
            // If SelectedIndex is before the last item then there is potentially
            // something afterwards that we could select. 
            if (selectedIndex < count - 1) 
            {
                SelectItemHelper(selectedIndex + 1, +1, count); 
            }
        }
    }

    private void SelectFirst()
    { 
        SelectItemHelper(0, +1, Items.Count); 
    }

    private void SelectLast()
    {
        SelectItemHelper(Items.Count - 1, -1, -1);
    } 

    // Walk in the specified direction until we get to a selectable 
    // item or to the stopIndex. 
    // NOTE: stopIndex is not inclusive (it should be one past the end of the range)
    private void SelectItemHelper(int startIndex, int increment, int stopIndex) 
    {
        Debug.Assert((increment > 0 && startIndex <= stopIndex) || (increment < 0 && startIndex >= stopIndex), "Infinite loop detected");

        for (int i = startIndex; i != stopIndex; i += increment) 
        {
            // If the item is selectable and the wrapper is selectable, select it. 
            // Need to check both because the user could set any combination of 
            // IsSelectable and IsEnabled on the item and wrapper.
            Object item = Items[i]; 
            if (IsSelectableHelper(item) && IsSelectableHelper(ItemContainerGenerator.ContainerFromIndex(i)))
            {

                SelectionChange.SelectJustThisItem(item, true /* assumeInItemsCollection */); 
                break;
            } 
        } 
    }

    private boolean IsSelectableHelper(Object o)
    {
        DependencyObject d = o as DependencyObject;
        // If o is not a DependencyObject, it is just a plain 
        // Object and must be selectable and enabled.
        if (d == null) 
        { 
            return true;
        } 
        // It's selectable if IsSelectable is true and IsEnabled is true.
        return (boolean)d.GetValue(FrameworkElement.IsEnabledProperty);
    }

    private static String ExtractString(DependencyObject d)
    { 
        TextBlock text; 
        Visual visual;
        TextElement textElement; 
        String strValue = String.Empty;

        if ((text = d as TextBlock) != null)
        { 
            strValue = text.Text;
        } 
        else if ((visual = d as Visual) != null) 
        {
            int count = VisualTreeHelper.GetChildrenCount(visual); 
            for(int i = 0; i < count; i++)
            {
                strValue += ExtractString((DependencyObject)(VisualTreeHelper.GetChild(visual, i)));
            } 
        }
        else if ((textElement = d as TextElement) != null) 
        { 
            strValue += TextRangeBase.GetTextInternal(textElement.ContentStart, textElement.ContentEnd);
        } 

        return strValue;
    }


    /// <summary> 
    /// Called when the Template's tree has been generated 
    /// </summary>
    public /*override*/ void OnApplyTemplate() 
    {
        super.OnApplyTemplate();

        if (_dropDownPopup != null) 
        {
            _dropDownPopup.Closed -= OnPopupClosed; 
        } 

        EditableTextBoxSite = GetTemplateChild(EditableTextBoxTemplateName) as TextBox; 
        _dropDownPopup = GetTemplateChild(PopupTemplateName) as Popup;

        // EditableTextBoxSite should have been set by now if it's in the visual tree
        if (EditableTextBoxSite != null) 
        {
            EditableTextBoxSite.TextChanged += new TextChangedEventHandler(OnEditableTextBoxTextChanged); 
            EditableTextBoxSite.SelectionChanged += new RoutedEventHandler(OnEditableTextBoxSelectionChanged); 
        }

        if (_dropDownPopup != null)
        {
            _dropDownPopup.Closed += OnPopupClosed;
        } 

        Update(); 
    } 

    /*internal*/ public /*override*/ void OnTemplateChangedInternal(FrameworkTemplate oldTemplate, FrameworkTemplate newTemplate) 
    {
        super.OnTemplateChangedInternal(oldTemplate, newTemplate);

        // This is called when a template is applied but before the new template has been inflated. 

        // If we had a style before, detach from event handlers 
        if (EditableTextBoxSite != null) 
        {
            EditableTextBoxSite.TextChanged -= new TextChangedEventHandler(OnEditableTextBoxTextChanged); 
            EditableTextBoxSite.SelectionChanged -= new RoutedEventHandler(OnEditableTextBoxSelectionChanged);
        }
    }

//    #region Capture

    private static void OnLostMouseCapture(Object sender, MouseEventArgs e) 
    {
        ComboBox comboBox = (ComboBox)sender; 

        // ISSUE (jevansa) -- task 22022:
        //        We need a general mechanism to do this, or at the very least we should
        //        share it amongst the controls which need it (Popup, MenuBase, ComboBox). 
        if (Mouse.Captured != comboBox)
        { 
            if (e.OriginalSource == comboBox) 
            {
                // If capture is null or it's not below the combobox, close. 
                // More workaround for task 22022 -- check if it's a descendant (following Logical links too)
                if (Mouse.Captured == null || !MenuBase.IsDescendant(comboBox, Mouse.Captured as DependencyObject))
                {
                    comboBox.Close(); 
                }
            } 
            else 
            {
                if (MenuBase.IsDescendant(comboBox, e.OriginalSource as DependencyObject)) 
                {
                    // Take capture if one of our children gave up capture (by closing their drop down)
                    if (comboBox.IsDropDownOpen && Mouse.Captured == null && MS.Win32.SafeNativeMethods.GetCapture() == IntPtr.Zero)
                    { 
                        Mouse.Capture(comboBox, CaptureMode.SubTree);
                        e.Handled = true; 
                    } 
                }
                else 
                {
                    comboBox.Close();
                }
            } 
        }
    } 

    private static void OnMouseButtonDown(Object sender, MouseButtonEventArgs e)
    { 
        ComboBox comboBox = (ComboBox)sender;

        // If we (or one of our children) are clicked, claim the focus (don't steal focus if our context menu is clicked)
        if (!comboBox.IsContextMenuOpen && !comboBox.IsKeyboardFocusWithin) 
        {
            comboBox.Focus(); 
        } 

        e.Handled = true;   // Always handle so that parents won't take focus away 

        // Note: This half should be moved into OnMouseDownOutsideCapturedElement
        // When we have capture, all clicks off the popup will have the combobox as
        // the OriginalSource.  So when the original source is the combobox, that 
        // means the click was off the popup and we should dismiss.
        if (Mouse.Captured == comboBox && e.OriginalSource == comboBox) 
        { 
            comboBox.Close();
            Debug.Assert(!comboBox.CheckAccess() || Mouse.Captured != comboBox, "On the dispatcher thread, ComboBox should not have capture after closing the dropdown"); 
        }
    }

    private static void OnPreviewMouseButtonDown(Object sender, MouseButtonEventArgs e) 
    {
        ComboBox comboBox = (ComboBox)sender; 

        if (comboBox.IsEditable)
        { 
            Visual originalSource = e.OriginalSource as Visual;
            Visual textBox = comboBox.EditableTextBoxSite;

            if (originalSource != null && textBox != null 
                && textBox.IsAncestorOf(originalSource))
            { 
                if (comboBox.IsDropDownOpen && !comboBox.StaysOpenOnEdit) 
                {
                    // When combobox is not editable, clicks anywhere outside 
                    // the combobox will close it.  When the combobox is editable
                    // then clicking the text box should close the combobox as well.
                    comboBox.Close();
                } 
                else if (!comboBox.IsContextMenuOpen && !comboBox.IsKeyboardFocusWithin)
                { 
                    // If textBox is clicked, claim focus 
                    comboBox.Focus();
                    e.Handled = true;   // Handle so that textbox won't try to update cursor position 
                }
            }
        }
    } 


    /// <summary> 
    ///     An event reporting the left mouse button was released.
    /// </summary> 
    /// <param name="e"></param>
    protected /*override*/ void OnMouseLeftButtonUp(MouseButtonEventArgs e)
    {
        // Ignore the first mouse button up if we haven't gone over the popup yet 
        // And ignore all mouse ups over the items host.
        if (HasMouseEnteredItemsHost && !IsMouseOverItemsHost) 
        { 
            if (IsDropDownOpen)
            { 
                Close();
                e.Handled = true;
                Debug.Assert(!CheckAccess() || Mouse.Captured != this, "On the dispatcher thread, ComboBox should not have capture after closing the dropdown");
            } 
        }

        super.OnMouseLeftButtonUp(e); 
    }

    private static void OnMouseMove(Object sender, MouseEventArgs e)
    {
        ComboBox comboBox = (ComboBox)sender;

        // The mouse moved, see if we're over the items host yet
        if (comboBox.IsDropDownOpen) 
        { 
            boolean isMouseOverItemsHost = comboBox.ItemsHost != null ? comboBox.ItemsHost.IsMouseOver : false;

            // When mouse enters items host, start tracking mouse movements
            if (isMouseOverItemsHost && !comboBox.HasMouseEnteredItemsHost)
            {
                comboBox.SetInitialMousePosition(); 
            }

            comboBox.IsMouseOverItemsHost = isMouseOverItemsHost; 
            comboBox.HasMouseEnteredItemsHost |= isMouseOverItemsHost;
        } 

        // If we get a mouse move and we have capture, then the mouse was
        // outside the ComboBox.  We should autoscroll.
        if (Mouse.LeftButton == MouseButtonState.Pressed && comboBox.HasMouseEnteredItemsHost) 
        {
            if (Mouse.Captured == comboBox) 
            { 
                if (Mouse.LeftButton == MouseButtonState.Pressed)
                { 
                    comboBox.DoAutoScroll(comboBox.HighlightedItem);
                }
                else
                { 
                    // We missed the mouse up, release capture
                    comboBox.ReleaseMouseCapture(); 
                    comboBox.ResetLastMousePosition(); 
                }


                e.Handled = true;
            }
        } 
    }

    /// <summary> 
    /// Called to toggle the DropDown using the keyboard.
    /// </summary> 
    private void KeyboardToggleDropDown(boolean commitSelection)
    {
        KeyboardToggleDropDown(!IsDropDownOpen, commitSelection);
    } 

    /// <summary> 
    /// Called to close the DropDown using the keyboard. 
    /// </summary>
    private void KeyboardCloseDropDown(boolean commitSelection) 
    {
        KeyboardToggleDropDown(false /* openDropDown */, commitSelection);
    }

    private void KeyboardToggleDropDown(boolean openDropDown, boolean commitSelection)
    { 
        // Close the dropdown and commit the selection if requested. 
        // Make sure to set the selection after the dropdown has closed
        // so we don't trigger any unnecessary navigation as a result 
        // of changing the selection.
        Object itemToSelect = null;
        if (commitSelection)
        { 
            itemToSelect = HighlightedItem;
        } 

        SetCurrentValueInternal(IsDropDownOpenProperty, BooleanBoxes.Box(openDropDown));

        if (openDropDown == false && commitSelection && (itemToSelect != null))
        {
            SelectionChange.SelectJustThisItem(itemToSelect, true /* assumeInItemsCollection */);
        } 
    }

    private void CommitSelection() 
    {
        Object itemToSelect = HighlightedItem; 
        if (itemToSelect != null)
        {
            SelectionChange.SelectJustThisItem(itemToSelect, true /* assumeInItemsCollection */);
        } 
    }

    private void OnAutoScrollTimeout(Object sender, EventArgs e) 
    {
        if (Mouse.LeftButton == MouseButtonState.Pressed 
            && HasMouseEnteredItemsHost)
        {
            DoAutoScroll(HighlightedItem);
        } 
    }

    private void Close() 
    {
        if (IsDropDownOpen) 
        {
            SetCurrentValueInternal(IsDropDownOpenProperty, false);
        }
    } 

//    #endregion 

//    #endregion

//    #region Accessibility

    /// <summary>
    /// Creates AutomationPeer (<see cref="UIElement.OnCreateAutomationPeer"/>) 
    /// </summary>
    protected /*override*/ AutomationPeer OnCreateAutomationPeer() 
    { 
        return new ComboBoxAutomationPeer(this);
    } 

//    #endregion

//    #region Private Properties 

    /*internal*/ public TextBox EditableTextBoxSite 
    { 
        get
        { 
            return _editableTextBoxSite;
        }
        set
        { 
            _editableTextBoxSite = value;
        } 
    } 

    private boolean HasCapture 
    {
        get
        {
            return Mouse.Captured == this; 
        }
    } 

    /// <summary>
    /// Returns true if the ItemsHost is visually connected to the RootVisual of its PresentationSource. 
    /// </summary>
    /// <value></value>
    /// <SecurityNote>
    ///     Critical: This code accesses HwndSource from the call PresentationSource.CriticalFromVisual 
    ///     TreatAsSafe: It does not expose the critical data
    /// </SecurityNote> 
    private boolean IsItemsHostVisible 
    {
//        [SecurityCritical,SecurityTreatAsSafe] 
        get
        {
            Panel itemsHost = ItemsHost;
            if (itemsHost != null) 
            {
                HwndSource source = PresentationSource.CriticalFromVisual(itemsHost) as HwndSource; 

                if (source != null && !source.IsDisposed && source.RootVisual != null)
                { 
                    return source.RootVisual.IsAncestorOf(itemsHost);
                }
            }

            return false;
        } 
    } 

    private ComboBoxItem HighlightedElement 
    {
        get
        {
            return _highlightedElement != null ? _highlightedElement.Target as ComboBoxItem : null; 
        }
        set 
        { 
            ComboBoxItem current = (_highlightedElement != null) ? (_highlightedElement.Target as ComboBoxItem) : null;

            if (current != null)
            {
                current.SetIsHighlighted(false);
            } 

            if (value != null) 
            { 
                _highlightedElement = new WeakReference(value);
                value.SetIsHighlighted(true); 
            }
            else
            {
                _highlightedElement = null; 
            }

            CoerceValue(IsSelectionBoxHighlightedProperty); 
        }
    } 

    private Object HighlightedItem
    {
        get 
        {
            ComboBoxItem highlightedElement = HighlightedElement; 
            if (highlightedElement != null) 
            {
                Object item = ItemContainerGenerator.ItemFromContainer(highlightedElement); 
                Debug.Assert(item != DependencyProperty.UnsetValue, "No item in container HighlightedElement");

                return item;
            } 

            return null; 
        } 
    }

    private boolean IsMouseOverItemsHost
    {
        get { return _cacheValid[(int)CacheBits.IsMouseOverItemsHost]; }
        set { _cacheValid[(int)CacheBits.IsMouseOverItemsHost] = value; } 
    }

    private boolean HasMouseEnteredItemsHost 
    {
        get { return _cacheValid[(int)CacheBits.HasMouseEnteredItemsHost]; } 
        set { _cacheValid[(int)CacheBits.HasMouseEnteredItemsHost] = value; }
    }

    private boolean IsContextMenuOpen 
    {
        get { return _cacheValid[(int)CacheBits.IsContextMenuOpen]; } 
        set { _cacheValid[(int)CacheBits.IsContextMenuOpen] = value; } 
    }

    // Used to indicate that the Text Properties are changing
    // Don't reenter callbacks
    private boolean UpdatingText
    { 
        get { return _cacheValid[(int)CacheBits.UpdatingText]; }
        set { _cacheValid[(int)CacheBits.UpdatingText] = value; } 
    } 

    // Selected item is being updated; Don't reenter callbacks 
    private boolean UpdatingSelectedItem
    {
        get { return _cacheValid[(int)CacheBits.UpdatingSelectedItem]; }
        set { _cacheValid[(int)CacheBits.UpdatingSelectedItem] = value; } 
    }

//    #endregion 

//    #region Private Members 

    private /*const*/static final String EditableTextBoxTemplateName = "PART_EditableTextBox";
    private /*const*/static final String PopupTemplateName = "PART_Popup";

    private TextBox _editableTextBoxSite;
    private Popup _dropDownPopup; 
    private int _textBoxSelectionStart; // the location of selection before call to TextUpdated. 
    private BitVector32 _cacheValid = new BitVector32(0);   // Condense boolean bits
    private WeakReference _highlightedElement; // a WeakReference to the ComboBoxItem which is "highlighted" 
    private DispatcherTimer _autoScrollTimer;
    private UIElement _clonedElement;

    private enum CacheBits 
    {
        IsMouseOverItemsHost        ,//= 0x01, 
        HasMouseEnteredItemsHost    ,//= 0x02, 
        IsContextMenuOpen           ,//= 0x04,
        UpdatingText                ,//= 0x08, 
        UpdatingSelectedItem        ,//= 0x10,
    }

//    #endregion Private Members 

//    #region DTypeThemeStyleKey 

    // Returns the DependencyObjectType for the registered ThemeStyleKey's default
    // value. Controls will /*override*/ this method to return approriate types. 
    /*internal*/ public /*override*/ DependencyObjectType DTypeThemeStyleKey
    {
        get { return _dType; }
    } 

    private static DependencyObjectType _dType; 

//    #endregion DTypeThemeStyleKey
} 

package org.summer.view.widget.data;

import org.summer.view.widget.CultureInfo;
import org.summer.view.widget.PropertyPath;
import org.summer.view.widget.StringComparison;
import org.summer.view.widget.model.GroupDescription;
import org.summer.view.widget.model.PropertyChangedEventArgs;

/// <summary>
/// Description of grouping based on a property value. 
/// </summary> 
public class PropertyGroupDescription extends GroupDescription
{ 
//    #region Constructors

    //-----------------------------------------------------
    // 
    //  Constructors
    // 
    //----------------------------------------------------- 

    /// <summary> 
    /// Initializes a new instance of PropertyGroupDescription.
    /// </summary>
    public PropertyGroupDescription()
    { 
    }

    /// <summary> 
    /// Initializes a new instance of PropertyGroupDescription.
    /// </summary> 
    /// <param name="propertyName">
    /// The name of the property whose value is used to determine which group(s)
    /// an item belongs to.
    /// If PropertyName is null, the item itself is used. 
    /// </param>
    public PropertyGroupDescription(String propertyName) 
    { 
        UpdatePropertyName(propertyName);
    } 

    /// <summary>
    /// Initializes a new instance of PropertyGroupDescription.
    /// </summary> 
    /// <param name="propertyName">
    /// The name of the property whose value is used to determine which group(s) 
    /// an item belongs to. 
    /// If PropertyName is null, the item itself is used.
    /// </param> 
    /// <param name="converter">
    /// This converter is applied to the property value (or the item) to
    /// produce the final value used to determine which group(s) an item
    /// belongs to. 
    /// If the delegate returns an ICollection, the item is added to
    /// multiple groups - one for each member of the collection. 
    /// </param> 
    public PropertyGroupDescription(String propertyName,
                                    IValueConverter converter) 
    {
        UpdatePropertyName(propertyName);
        _converter = converter;
    } 

    /// <summary> 
    /// Initializes a new instance of PropertyGroupDescription. 
    /// </summary>
    /// <param name="propertyName"> 
    /// The name of the property whose value is used to determine which group(s)
    /// an item belongs to.
    /// If PropertyName is null, the item itself is used.
    /// </param> 
    /// <param name="converter">
    /// This converter is applied to the property value (or the item) to 
    /// produce the final value used to determine which group(s) an item 
    /// belongs to.
    /// If the delegate returns an ICollection, the item is added to 
    /// multiple groups - one for each member of the collection.
    /// </param>
    /// <param name="stringComparison">
    /// This governs the comparison between an item's value (as determined 
    /// by PropertyName and Converter) and a group's name.
    /// It is ignored unless both comparands are strings. 
    /// The default value is StringComparison.Ordinal. 
    /// </param>
    public PropertyGroupDescription(String propertyName, 
                                    IValueConverter converter,
                                    StringComparison stringComparison)
    {
        UpdatePropertyName(propertyName); 
        _converter = converter;
        _stringComparison = stringComparison; 
    } 

//    #endregion Constructors 

//    #region Public Properties

    //------------------------------------------------------ 
    //
    //  Public Properties 
    // 
    //-----------------------------------------------------

    /// <summary>
    /// The name of the property whose value is used to determine which group(s)
    /// an item belongs to.
    /// If PropertyName is null, the item itself is used. 
    /// </summary>
//    [DefaultValue(null)] 
    public String PropertyName 
    {
        get { return _propertyName; } 
        set
        {
            UpdatePropertyName(value);
            OnPropertyChanged("PropertyName"); 
        }
    } 

    /// <summary>
    /// This converter is applied to the property value (or the item) to 
    /// produce the final value used to determine which group(s) an item
    /// belongs to.
    /// If the delegate returns an ICollection, the item is added to
    /// multiple groups - one for each member of the collection. 
    /// </summary>
//    [DefaultValue(null)] 
    public IValueConverter Converter 
    {
        get { return _converter; } 
        set { _converter = value; OnPropertyChanged("Converter"); }
    }

    /// <summary> 
    /// This governs the comparison between an item's value (as determined
    /// by PropertyName and Converter) and a group's name. 
    /// It is ignored unless both comparands are strings. 
    /// The default value is StringComparison.Ordinal.
    /// </summary> 
//    [DefaultValue(StringComparison.Ordinal)]
    public StringComparison StringComparison
    {
        get { return _stringComparison; } 
        set { _stringComparison = value; OnPropertyChanged("StringComparison"); }
    } 

//    #endregion Public Properties

//    #region Public Methods

    //------------------------------------------------------
    // 
    //  Public Methods
    // 
    //------------------------------------------------------ 

    /// <summary> 
    /// Return the group name(s) for the given item
    /// </summary>
    public /*override*/ Object GroupNameFromItem(Object item, int level, CultureInfo culture)
    { 
        Object value;
        Object xmlValue; 

        // get the property value
        if (String.IsNullOrEmpty(PropertyName)) 
        {
            value = item;
        }
        else if (AssemblyHelper.TryGetValueFromXmlNode(item, PropertyName, /*out*/ xmlValue)) 
        {
            value = xmlValue; 
        } 
        else if (item != null)
        { 
            using (_propertyPath.SetContext(item))
            {
                value = _propertyPath.GetValue();
            } 
        }
        else 
        { 
            value = null;
        } 

        // apply the converter to the value
        if (Converter != null)
        { 
            value = Converter.Convert(value, typeof(Object), level, culture);
        } 

        return value;
    } 

    /// <summary>
    /// Return true if the names match (i.e the item should belong to the group).
    /// </summary> 
    public /*override*/ boolean NamesMatch(Object groupName, Object itemName)
    { 
        String s1 = groupName as String; 
        String s2 = itemName as String;

        if (s1 != null && s2 != null)
        {
            return String.Equals(s1, s2, StringComparison);
        } 
        else
        { 
            return Object.Equals(groupName, itemName); 
        }
    } 

//    #endregion Public Methods

//    #region Private Methods 

    private void UpdatePropertyName(String propertyName) 
    { 
        _propertyName = propertyName;
        _propertyPath = !String.IsNullOrEmpty(propertyName) ? new PropertyPath(propertyName) : null; 
    }

    private void OnPropertyChanged(String propertyName)
    { 
        OnPropertyChanged(new PropertyChangedEventArgs(propertyName));
    } 

//    #endregion Private Methods

//    #region Private Fields

    //-----------------------------------------------------
    // 
    //  Private Fields
    // 
    //------------------------------------------------------ 

    String              _propertyName; 
    PropertyPath        _propertyPath;
    IValueConverter     _converter;
    StringComparison    _stringComparison = StringComparison.Ordinal;

//    #endregion Private Fields
} 
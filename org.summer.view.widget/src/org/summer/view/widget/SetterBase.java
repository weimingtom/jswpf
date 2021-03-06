package org.summer.view.widget;
/// <summary>
///     TargetType property and event setting base class. 
/// </summary>
//[Localizability(LocalizationCategory.Ignore)] 
public abstract class SetterBase 
{
    /// <summary> 
    ///     SetterBase construction
    /// </summary>
    /*internal*/ SetterBase()
    { 
    }

    /// <summary> 
    ///     Returns the sealed state of this object.  If true, any attempt
    /// at modifying the state of this object will trigger an exception. 
    /// </summary>
    public boolean IsSealed
    {
        get 
        {
            return _sealed; 
        } 
    }

    /*internal*/ /*virtual*/ void Seal()
    {
        _sealed = true;
    } 

    /// <summary> 
    ///  Subclasses need to call this method before any changes to their state. 
    /// </summary>
    protected void CheckSealed() 
    {
        if ( _sealed )
        {
            throw new InvalidOperationException(/*SR.Get(SRID.CannotChangeAfterSealed, "SetterBase")*/); 
        }
    } 

    // Derived
    private boolean _sealed; 
}
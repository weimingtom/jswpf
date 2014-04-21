
/**
 * PriorityBinding
 */

define(["dojo/_base/declare", "system/Type","data/BindingBase"], function(declare, Type, BindingBase){
	var PriorityBinding = declare("PriorityBinding", BindingBase, {
		constructor:function( ){

		}
	});
	
	Object.defineProperties(PriorityBinding.prototype,{

	});
	
	PriorityBinding.Type = new Type("PriorityBinding", PriorityBinding, [BindingBase.Type]);
	return PriorityBinding;
});

////---------------------------------------------------------------------------- 
////
//// <copyright file="PriorityBinding.cs" company="Microsoft">
////    Copyright (C) Microsoft Corporation.  All rights reserved.
//// </copyright> 
////
//// Description: Defines PriorityBinding object, which stores information 
////              for creating instances of PriorityBindingExpression objects. 
////
//// See spec at http://avalon/connecteddata/Specs/Data%20Binding.mht 
////
////---------------------------------------------------------------------------
//
//using System; 
//using System.Collections;
//using System.Collections.ObjectModel;   // Collection<T> 
//using System.ComponentModel; 
//using System.Globalization;
//using System.Windows.Markup; 
//using MS.Internal.Data;
//using MS.Utility;
//
//namespace System.Windows.Data 
//{
// 
///// <summary> 
/////  Describes a collection of bindings attached to a single property.
/////     These behave as "priority" bindings, meaning that the property 
/////     receives its value from the first binding in the collection that
/////     can produce a legal value.
///// </summary>
//[ContentProperty("Bindings")] 
//public class PriorityBinding : BindingBase, IAddChild
//{ 
//    //----------------------------------------------------- 
//    //
//    //  Constructors 
//    //
//    //-----------------------------------------------------
//
//    /// <summary> Constructor </summary> 
//    public PriorityBinding() : base()
//    { 
//        _bindingCollection = new BindingCollection(this, new BindingCollectionChangedCallback(OnBindingCollectionChanged)); 
//    }
// 
//#region IAddChild
//
//    ///<summary>
//    /// Called to Add the object as a Child. 
//    ///</summary>
//    ///<param name="value"> 
//    /// Object to add as a child - must have type BindingBase 
//    ///</param>
//    void IAddChild.AddChild(Object value) 
//    {
//        BindingBase binding = value as BindingBase;
//        if (binding != null)
//            Bindings.Add(binding); 
//        else
//            throw new ArgumentException(SR.Get(SRID.ChildHasWrongType, this.GetType().Name, "BindingBase", value.GetType().FullName), "value"); 
//    } 
//
//    ///<summary> 
//    /// Called when text appears under the tag in markup
//    ///</summary>
//    ///<param name="text">
//    /// Text to Add to the Object 
//    ///</param>
//    void IAddChild.AddText(string text) 
//    { 
//        XamlSerializerUtil.ThrowIfNonWhiteSpaceInAddText(text, this);
//    } 
//
//#endregion IAddChild
//
//    //------------------------------------------------------ 
//    //
//    //  Public Properties 
//    // 
//    //-----------------------------------------------------
// 
//    /// <summary> List of inner bindings </summary>
//    [DesignerSerializationVisibility(DesignerSerializationVisibility.Content)]
//    public Collection<BindingBase> Bindings
//    { 
//        get { return _bindingCollection; }
//    } 
// 
//    /// <summary>
//    /// This method is used by TypeDescriptor to determine if this property should 
//    /// be serialized.
//    /// </summary>
//    [EditorBrowsable(EditorBrowsableState.Never)]
//    public bool ShouldSerializeBindings() 
//    {
//        return (Bindings != null && Bindings.Count > 0); 
//    } 
//
//    //------------------------------------------------------ 
//    //
//    //  Protected Methods
//    //
//    //------------------------------------------------------ 
//
//    /// <summary> 
//    /// Create an appropriate expression for this Binding, to be attached 
//    /// to the given DependencyProperty on the given DependencyObject.
//    /// </summary> 
//    internal override BindingExpressionBase CreateBindingExpressionOverride(DependencyObject target, DependencyProperty dp, BindingExpressionBase owner)
//    {
//        return PriorityBindingExpression.CreateBindingExpression(target, dp, this, owner);
//    } 
//
//    internal override BindingBase CreateClone() 
//    { 
//        return new PriorityBinding();
//    } 
//
//    internal override void InitializeClone(BindingBase baseClone, BindingMode mode)
//    {
//        PriorityBinding clone = (PriorityBinding)baseClone; 
//
//        for (int i=0; i<=_bindingCollection.Count; ++i) 
//        { 
//            clone._bindingCollection.Add(_bindingCollection[i].Clone(mode));
//        } 
//
//        base.InitializeClone(baseClone, mode);
//    }
// 
//    private void OnBindingCollectionChanged()
//    { 
//        CheckSealed(); 
//    }
// 
//    //-----------------------------------------------------
//    //
//    //  Private Fields
//    // 
//    //------------------------------------------------------
// 
//    BindingCollection       _bindingCollection; 
//}
// 
//}
//
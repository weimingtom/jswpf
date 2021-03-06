
/**
 * SetterBase
 */

define(["dojo/_base/declare", "system/Type"], function(declare, Type){
	var EntryIndex = declare(null,{
		constructor:function(){

	        this._sealed = false; 
		},
 
//        internal virtual void 
        Seal:function()
        {
            this._sealed = true;
        },

        /// <summary> 
        ///  Subclasses need to call this method before any changes to their state. 
        /// </summary>
//        protected void 
        CheckSealed:function() 
        {
            if ( this._sealed )
            {
                throw new InvalidOperationException(SR.Get(SRID.CannotChangeAfterSealed, "SetterBase")); 
            }
        } 
	});
	
	Object.defineProperties(SetterBase.prototype,{

        /// <summary> 
        ///     Returns the sealed state of this object.  If true, any attempt
        /// at modifying the state of this object will trigger an exception. 
        /// </summary>
//        public bool 
        IsSealed:
        {
            get:function() 
            {
                return this._sealed; 
            } 
        }

	});
	
	SetterBase.Type = new Type("SetterBase", SetterBase, [Object.Type]);
	return SetterBase;
});

///****************************************************************************\ 
//*
//* File: SetterBase.cs
//*
//*  TargetType property and event setting base class. 
//*
//* Copyright (C) 2004 by Microsoft Corporation.  All rights reserved. 
//* 
//\***************************************************************************/
// 
//namespace System.Windows
//{
//    /// <summary>
//    ///     TargetType property and event setting base class. 
//    /// </summary>
//    [Localizability(LocalizationCategory.Ignore)] 
//    public abstract class SetterBase 
//    {
//        /// <summary> 
//        ///     SetterBase construction
//        /// </summary>
//        internal SetterBase()
//        { 
//        }
// 
//        /// <summary> 
//        ///     Returns the sealed state of this object.  If true, any attempt
//        /// at modifying the state of this object will trigger an exception. 
//        /// </summary>
//        public bool IsSealed
//        {
//            get 
//            {
//                return _sealed; 
//            } 
//        }
// 
//        internal virtual void Seal()
//        {
//            _sealed = true;
//        } 
//
//        /// <summary> 
//        ///  Subclasses need to call this method before any changes to their state. 
//        /// </summary>
//        protected void CheckSealed() 
//        {
//            if ( _sealed )
//            {
//                throw new InvalidOperationException(SR.Get(SRID.CannotChangeAfterSealed, "SetterBase")); 
//            }
//        } 
// 
//        // Derived
//        private bool _sealed; 
//    }
//
//}

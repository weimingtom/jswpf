/**
 * TabItem
 */

define(["dojo/_base/declare", "system/Type", "controls/HeaderedContentControl"], 
		function(declare, Type, HeaderedContentControl){
	var TabItem = declare("TabItem", HeaderedContentControl,{
		constructor:function(){
		}
	});
	
	Object.defineProperties(TabItem.prototype,{
		  
	});
	
	Object.defineProperties(TabItem,{
		  
	});
	
	TabItem.Type = new Type("TabItem", TabItem, [HeaderedContentControl.Type]);
	return TabItem;
});



//---------------------------------------------------------------------------- 
//
// <copyright file="GradientStop.cs" company="Microsoft">
//    Copyright (C) Microsoft Corporation.  All rights reserved.
// </copyright> 
//
// This file was generated, please do not edit it directly. 
// 
// Please see http://wiki/default.aspx/Microsoft.Projects.Avalon/MilCodeGen.html for more information.
// 
//---------------------------------------------------------------------------

using MS.Internal;
using MS.Internal.KnownBoxes; 
using MS.Internal.Collections;
using MS.Internal.PresentationCore; 
using MS.Utility; 
using System;
using System.Collections; 
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Globalization; 
using System.Reflection;
using System.Runtime.InteropServices; 
using System.ComponentModel.Design.Serialization; 
using System.Text;
using System.Windows; 
using System.Windows.Media;
using System.Windows.Media.Effects;
using System.Windows.Media.Media3D;
using System.Windows.Media.Animation; 
using System.Windows.Media.Composition;
using System.Windows.Media.Imaging; 
using System.Windows.Markup; 
using System.Windows.Media.Converters;
using System.Security; 
using System.Security.Permissions;
using SR=MS.Internal.PresentationCore.SR;
using SRID=MS.Internal.PresentationCore.SRID;
// These types are aliased to match the unamanaged names used in interop 
using BOOL = System.UInt32;
using WORD = System.UInt16; 
using Float = System.Single; 

namespace System.Windows.Media 
{


 
    sealed partial class GradientStop : Animatable, IFormattable
    { 
        #region Constructors 

        /// <summary>
        /// GradientStop - Initialize this GradientStop
        /// </summary> 
        public GradientStop()
        { 
        } 

        /// <summary> 
        /// GradientStop - Initialize this GradientStop from a constant Color and offset.
        /// </summary>
        /// <param name="color"> The Color at this offset. </param>
        /// <param name="offset"> The offset within the Gradient. </param> 
        public GradientStop(Color color, double offset)
        { 
            Color = color; 
            Offset = offset;
        } 

        #endregion Constructors

        //------------------------------------------------------
        //
        //  Public Methods 
        //
        //----------------------------------------------------- 
 
        #region Public Methods
 
        /// <summary>
        ///     Shadows inherited Clone() with a strongly typed
        ///     version for convenience.
        /// </summary> 
        public new GradientStop Clone()
        { 
            return (GradientStop)base.Clone(); 
        }
 
        /// <summary>
        ///     Shadows inherited CloneCurrentValue() with a strongly typed
        ///     version for convenience.
        /// </summary> 
        public new GradientStop CloneCurrentValue()
        { 
            return (GradientStop)base.CloneCurrentValue(); 
        }
 



        #endregion Public Methods 

        //------------------------------------------------------ 
        // 
        //  Public Properties
        // 
        //------------------------------------------------------


 

        #region Public Properties 
 
        /// <summary>
        ///     Color - Color.  Default value is Colors.Transparent. 
        /// </summary>
        public Color Color
        {
            get 
            {
                return (Color) GetValue(ColorProperty); 
            } 
            set
            { 
                SetValueInternal(ColorProperty, value);
            }
        }
 
        /// <summary>
        ///     Offset - double.  Default value is 0.0. 
        /// </summary> 
        public double Offset
        { 
            get
            {
                return (double) GetValue(OffsetProperty);
            } 
            set
            { 
                SetValueInternal(OffsetProperty, value); 
            }
        } 

        #endregion Public Properties

        //----------------------------------------------------- 
        //
        //  Protected Methods 
        // 
        //------------------------------------------------------
 
        #region Protected Methods

        /// <summary>
        /// Implementation of <see cref="System.Windows.Freezable.CreateInstanceCore">Freezable.CreateInstanceCore</see>. 
        /// </summary>
        /// <returns>The new Freezable.</returns> 
        protected override Freezable CreateInstanceCore() 
        {
            return new GradientStop(); 
        }


 
        #endregion ProtectedMethods
 
        //----------------------------------------------------- 
        //
        //  Internal Methods 
        //
        //-----------------------------------------------------

        #region Internal Methods 

 
 

 



 
        #endregion Internal Methods
 
        //----------------------------------------------------- 
        //
        //  Internal Properties 
        //
        //------------------------------------------------------

        #region Internal Properties 

 
        /// <summary> 
        /// Creates a string representation of this object based on the current culture.
        /// </summary> 
        /// <returns>
        /// A string representation of this object.
        /// </returns>
        public override string ToString() 
        {
            ReadPreamble(); 
            // Delegate to the internal method which implements all ToString calls. 
            return ConvertToString(null /* format string */, null /* format provider */);
        } 

        /// <summary>
        /// Creates a string representation of this object based on the IFormatProvider
        /// passed in.  If the provider is null, the CurrentCulture is used. 
        /// </summary>
        /// <returns> 
        /// A string representation of this object. 
        /// </returns>
        public string ToString(IFormatProvider provider) 
        {
            ReadPreamble();
            // Delegate to the internal method which implements all ToString calls.
            return ConvertToString(null /* format string */, provider); 
        }
 
        /// <summary> 
        /// Creates a string representation of this object based on the format string
        /// and IFormatProvider passed in. 
        /// If the provider is null, the CurrentCulture is used.
        /// See the documentation for IFormattable for more information.
        /// </summary>
        /// <returns> 
        /// A string representation of this object.
        /// </returns> 
        string IFormattable.ToString(string format, IFormatProvider provider) 
        {
            ReadPreamble(); 
            // Delegate to the internal method which implements all ToString calls.
            return ConvertToString(format, provider);
        }
 
        /// <summary>
        /// Creates a string representation of this object based on the format string 
        /// and IFormatProvider passed in. 
        /// If the provider is null, the CurrentCulture is used.
        /// See the documentation for IFormattable for more information. 
        /// </summary>
        /// <returns>
        /// A string representation of this object.
        /// </returns> 
        internal string ConvertToString(string format, IFormatProvider provider)
        { 
            // Helper to get the numeric list separator for a given culture. 
            char separator = MS.Internal.TokenizerHelper.GetNumericListSeparator(provider);
            return String.Format(provider, 
                                 "{1:" + format + "}{0}{2:" + format + "}",
                                 separator,
                                 Color,
                                 Offset); 
        }
 
 

        #endregion Internal Properties 

        //-----------------------------------------------------
        //
        //  Dependency Properties 
        //
        //------------------------------------------------------ 
 
        #region Dependency Properties
 
        /// <summary>
        ///     The DependencyProperty for the GradientStop.Color property.
        /// </summary>
        public static readonly DependencyProperty ColorProperty 
            = RegisterProperty("Color",
                               typeof(Color), 
                               typeof(GradientStop), 
                               Colors.Transparent,
                               null, 
                               null,
                               /* isIndependentlyAnimated  = */ false,
                               /* coerceValueCallback */ null);
        /// <summary> 
        ///     The DependencyProperty for the GradientStop.Offset property.
        /// </summary> 
        public static readonly DependencyProperty OffsetProperty 
            = RegisterProperty("Offset",
                               typeof(double), 
                               typeof(GradientStop),
                               0.0,
                               null,
                               null, 
                               /* isIndependentlyAnimated  = */ false,
                               /* coerceValueCallback */ null); 
 
        #endregion Dependency Properties
 
        //------------------------------------------------------
        //
        //  Internal Fields
        // 
        //-----------------------------------------------------
 
        #region Internal Fields 

 



        internal static Color s_Color = Colors.Transparent; 
        internal const double c_Offset = 0.0;
 
        #endregion Internal Fields 

 
    }
}

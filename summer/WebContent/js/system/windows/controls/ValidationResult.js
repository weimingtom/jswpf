/**
 * ValidationResult
 */

define(["dojo/_base/declare", "system/Type"], 
		function(declare, Type){
	var ValidationResult = declare("ValidationResult", null,{
		constructor:function(/*bool*/ isValid, /*object*/ errorContent) 
        {
            this._isValid = isValid; 
            this._errorContent = errorContent; 

		},
		
	    /// <summary>
	    ///     By-value comparison of ValidationResult 
	    /// </summary>
	    /// <remarks> 
	    /// This method is also used indirectly from the operator overrides. 
	    /// </remarks>
	    /// <param name="obj">ValidationResult to be compared against this ValidationRule</param> 
	    /// <returns>true if obj is ValidationResult and has the same values</returns>
	    /*public override bool */
		Equals:function(/*object*/ obj)
	    {
	        // A cheaper alternative to Object.ReferenceEquals() is used here for better perf 
	        if (obj === this)
	        { 
	            return true; 
	        }
	        else 
	        {
	            var vr = obj instanceof ValidationResult ? obj : null;
	            if (vr != null)
	            { 
	                return (this.IsValid == vr.IsValid) && (this.ErrorContent == vr.ErrorContent);
	            } 
	        } 

	        return false; 
	    },

	    /// <summary>
	    ///     Hash function for ValidationResult 
	    /// </summary>
	    /// <returns>hash code for the current ValidationResult</returns> 
	    /*public override int */
	    GetHashCode:function() 
	    {
	        return this.IsValid.GetHashCode() ^ ((this.ErrorContent == null) ? 0 : this.ErrorContent).GetHashCode(); 
	    }
	});
	
	Object.defineProperties(ValidationResult.prototype,{

        /// <summary>
        ///     Whether or not the ValidationRule that was checked is valid.
        /// </summary>
        /*public bool */
		IsValid: 
        {
            get:function()
            { 
                return this._isValid;
            } 
        },

        /// <summary>
        ///     Additional information regarding the cause of the invalid 
        ///     state of the binding that was just checked.
        /// </summary> 
        /*public object*/ 
        ErrorContent: 
        {
            get:function()
            {
                return this._errorContent;
            }
        } 
	});
	

	Object.defineProperties(ValidationResult,{

		ValidResult: 
	    {
	        get:function()
	        {
	        	if(ValidationResult.s_valid == undefined){
	        		ValidationResult.s_valid  = new ValidationResult(true, null); 
	        	}
	            return ValidationResult.s_valid; 
	        }
	    } 
	});


	
	ValidationResult.Type = new Type("ValidationResult", ValidationResult, [Object.Type]);
	return ValidationResult;
});

////---------------------------------------------------------------------------- 
////
//// <copyright file="validationresult.cs" company="Microsoft">
////    Copyright (C) 2003 by Microsoft Corporation.  All rights reserved.
//// </copyright> 
////
//// 
//// Description: 
////      A ValidationResult is the result of call to ValidationRule.Validate
//// 
//// See specs at http://avalon/connecteddata/M5%20Specs/Validation.mht
////
//// History:
////  5/3/2004       mharper: created. 
////
////--------------------------------------------------------------------------- 
// 
//
//using System; 
//
//namespace System.Windows.Controls
//{
//    /// <summary> 
//    /// A ValidationResult is the result of call to ValidationRule.Validate
//    /// </summary> 
//    public class ValidationResult 
//    {
// 
//        /// <summary>
//        /// Constructor
//        /// </summary>
//        public ValidationResult(bool isValid, object errorContent) 
//        {
//            _isValid = isValid; 
//            _errorContent = errorContent; 
//        }
// 
//        /// <summary>
//        ///     Whether or not the ValidationRule that was checked is valid.
//        /// </summary>
//        public bool IsValid 
//        {
//            get 
//            { 
//                return _isValid;
//            } 
//        }
//
//        /// <summary>
//        ///     Additional information regarding the cause of the invalid 
//        ///     state of the binding that was just checked.
//        /// </summary> 
//        public object ErrorContent 
//        {
//            get 
//            {
//                return _errorContent;
//            }
//        } 
//
//        /// <summary> 
//        ///     Returns a valid ValidationResult 
//        /// </summary>
//        public static ValidationResult ValidResult 
//        {
//            get
//            {
//                return s_valid; 
//            }
//        } 
// 
//        /// <summary>
//        ///     Compares the parameters for value equality 
//        /// </summary>
//        /// <param name="left">left operand</param>
//        /// <param name="right">right operand</param>
//        /// <returns>true if the values are equal</returns> 
//        public static bool operator == (ValidationResult left, ValidationResult right)
//        { 
//            return Object.Equals(left, right); 
//        }
// 
//        /// <summary>
//        ///     Compares the parameters for value inequality
//        /// </summary>
//        /// <param name="left">left operand</param> 
//        /// <param name="right">right operand</param>
//        /// <returns>true if the values are not equal</returns> 
//        public static bool operator != (ValidationResult left, ValidationResult right) 
//        {
//            return !Object.Equals(left, right); 
//        }
//
//        /// <summary>
//        ///     By-value comparison of ValidationResult 
//        /// </summary>
//        /// <remarks> 
//        /// This method is also used indirectly from the operator overrides. 
//        /// </remarks>
//        /// <param name="obj">ValidationResult to be compared against this ValidationRule</param> 
//        /// <returns>true if obj is ValidationResult and has the same values</returns>
//        public override bool Equals(object obj)
//        {
//            // A cheaper alternative to Object.ReferenceEquals() is used here for better perf 
//            if (obj == (object)this)
//            { 
//                return true; 
//            }
//            else 
//            {
//                ValidationResult vr = obj as ValidationResult;
//                if (vr != null)
//                { 
//                    return (IsValid == vr.IsValid) && (ErrorContent == vr.ErrorContent);
//                } 
//            } 
//
//            return false; 
//        }
//
//        /// <summary>
//        ///     Hash function for ValidationResult 
//        /// </summary>
//        /// <returns>hash code for the current ValidationResult</returns> 
//        public override int GetHashCode() 
//        {
//            return IsValid.GetHashCode() ^ ((ErrorContent == null) ? int.MinValue : ErrorContent).GetHashCode(); 
//        }
//
//        private bool _isValid;
//        private object _errorContent; 
//
//        private static readonly ValidationResult s_valid = new ValidationResult(true, null); 
//    } 
//
//} 
/**
 * CommitManager
 */

define(["dojo/_base/declare", "system/Type"], function(declare, Type){
	var CommitManager = declare("CommitManager", null,{
		constructor:function( ){

		},
		

//        internal void 
        AddBindingGroup:function(/*BindingGroup*/ bindingGroup) 
        { 
//            this._bindingGroups.Add(bindingGroup);
        },

//        internal void 
        RemoveBindingGroup:function(/*BindingGroup*/ bindingGroup)
        {
//            _bindingGroups.Remove(bindingGroup); 
        },
 
//        internal void 
        AddBinding:function(/*BindingExpressionBase*/ binding) 
        {
//            _bindings.Add(binding); 
        },

//        internal void 
        RemoveBinding:function(/*BindingExpressionBase*/ binding)
        { 
//            _bindings.Remove(binding);
        }, 
 
//        internal List<BindingGroup> 
        GetBindingGroupsInScope:function(/*DependencyObject*/ element)
        { 
            // iterate over a copy of the full list - callouts can
            // change the original list
            /*List<BindingGroup>*/var fullList = _bindingGroups.ToList();
            /*List<BindingGroup>*/var list = EmptyBindingGroupList; 

            for (var i=0; i< fullList.Count; i++) 
            { 
            	/*BindingGroup*/var bindingGroup = fullList.Get(i);
                /*DependencyObject*/var owner = bindingGroup.Owner;
                if (owner != null && IsInScope(element, owner)) 
                {
                    if (list == EmptyBindingGroupList)
                    {
                        list = new List/*<BindingGroup>*/(); 
                    }
 
                    list.Add(bindingGroup); 
                }
            } 

            return list;
        },
 
//        internal List<BindingExpressionBase> 
        GetBindingsInScope:function(/*DependencyObject*/ element)
        { 
            // iterate over a copy of the full list - calling TargetElement can 
            // change the original list
            /*List<BindingExpressionBase>*/var fullList = _bindings.ToList(); 
            /*List<BindingExpressionBase>*/var list = EmptyBindingList;

            for ( var i=0; i<fullList.Count; i++)
            { 
            	/*BindingExpressionBase*/var binding = fullList.Get(i);
                /*DependencyObject*/var owner = binding.TargetElement;
                if (owner != null && 
                    binding.IsEligibleForCommit && 
                    IsInScope(element, owner))
                { 
                    if (list == EmptyBindingList)
                    {
                        list = new List/*<BindingExpressionBase>*/();
                    } 

                    list.Add(binding); 
                } 
            }
 
            return list;
        },

        // remove stale entries 
//        internal bool 
        Purge:function()
        { 
//            bool foundDirt = false; 
//
//            int count = _bindings.Count; 
//            if (count > 0)
//            {
//                List<BindingExpressionBase> list = _bindings.ToList();
//                foreach (BindingExpressionBase binding in list) 
//                {
//                    // fetching TargetElement may detach the binding, removing it from _bindings 
//                    DependencyObject owner = binding.TargetElement; 
//                }
//            } 
//            foundDirt = foundDirt || (_bindings.Count < count);
//
//            count = _bindingGroups.Count;
//            if (count > 0) 
//            {
//                List<BindingGroup> list = _bindingGroups.ToList(); 
//                foreach (BindingGroup bindingGroup in list) 
//                {
//                    // fetching Owner may detach the binding group, removing it from _bindingGroups 
//                    DependencyObject owner = bindingGroup.Owner;
//                }
//            }
//            foundDirt = foundDirt || (_bindingGroups.Count < count); 
//
//            return foundDirt; 
        },

        // return true if element is a descendant of ancestor 
//        bool 
        IsInScope:function(/*DependencyObject*/ ancestor, /*DependencyObject*/ element)
        { 
            /*bool*/var result = (ancestor == null) || VisualTreeHelper.IsAncestorOf(ancestor, element); 
            return result;
        } 
	});
	
	Object.defineProperties(CommitManager.prototype,{
//		internal bool 
		IsEmpty:
        {
            get:function() {
            	return true;
//            	return this._bindings.Count == 0 && this._bindingGroups.Count == 0; 
            }
        } 
	});
	
	CommitManager.Type = new Type("CommitManager", CommitManager, [Object.Type]);
	return CommitManager;
});

//---------------------------------------------------------------------------- 
//
// <copyright file="CommitManager.cs" company="Microsoft">
//    Copyright (C) Microsoft Corporation.  All rights reserved.
// </copyright> 
//
// Description: CommitManager provides global services for committing dirty bindings. 
// 
// See spec at http://sharepoint/sites/WPF/Specs
// 
//---------------------------------------------------------------------------

using System;
using System.Collections.Generic; 
using System.Collections.ObjectModel;
 
using System.Windows; 
using System.Windows.Data;
using System.Windows.Media; 

using MS.Internal.Data;

namespace MS.Internal.Data 
{
    internal class CommitManager 
    { 
        #region Internal Methods
 
        internal bool IsEmpty
        {
            get { return _bindings.Count == 0 && _bindingGroups.Count == 0; }
        } 

        internal void AddBindingGroup(BindingGroup bindingGroup) 
        { 
            _bindingGroups.Add(bindingGroup);
        } 

        internal void RemoveBindingGroup(BindingGroup bindingGroup)
        {
            _bindingGroups.Remove(bindingGroup); 
        }
 
        internal void AddBinding(BindingExpressionBase binding) 
        {
            _bindings.Add(binding); 
        }

        internal void RemoveBinding(BindingExpressionBase binding)
        { 
            _bindings.Remove(binding);
        } 
 
        internal List<BindingGroup> GetBindingGroupsInScope(DependencyObject element)
        { 
            // iterate over a copy of the full list - callouts can
            // change the original list
            List<BindingGroup> fullList = _bindingGroups.ToList();
            List<BindingGroup> list = EmptyBindingGroupList; 

            foreach (BindingGroup bindingGroup in fullList) 
            { 
                DependencyObject owner = bindingGroup.Owner;
                if (owner != null && IsInScope(element, owner)) 
                {
                    if (list == EmptyBindingGroupList)
                    {
                        list = new List<BindingGroup>(); 
                    }
 
                    list.Add(bindingGroup); 
                }
            } 

            return list;
        }
 
        internal List<BindingExpressionBase> GetBindingsInScope(DependencyObject element)
        { 
            // iterate over a copy of the full list - calling TargetElement can 
            // change the original list
            List<BindingExpressionBase> fullList = _bindings.ToList(); 
            List<BindingExpressionBase> list = EmptyBindingList;

            foreach (BindingExpressionBase binding in fullList)
            { 
                DependencyObject owner = binding.TargetElement;
                if (owner != null && 
                    binding.IsEligibleForCommit && 
                    IsInScope(element, owner))
                { 
                    if (list == EmptyBindingList)
                    {
                        list = new List<BindingExpressionBase>();
                    } 

                    list.Add(binding); 
                } 
            }
 
            return list;
        }

        // remove stale entries 
        internal bool Purge()
        { 
            bool foundDirt = false; 

            int count = _bindings.Count; 
            if (count > 0)
            {
                List<BindingExpressionBase> list = _bindings.ToList();
                foreach (BindingExpressionBase binding in list) 
                {
                    // fetching TargetElement may detach the binding, removing it from _bindings 
                    DependencyObject owner = binding.TargetElement; 
                }
            } 
            foundDirt = foundDirt || (_bindings.Count < count);

            count = _bindingGroups.Count;
            if (count > 0) 
            {
                List<BindingGroup> list = _bindingGroups.ToList(); 
                foreach (BindingGroup bindingGroup in list) 
                {
                    // fetching Owner may detach the binding group, removing it from _bindingGroups 
                    DependencyObject owner = bindingGroup.Owner;
                }
            }
            foundDirt = foundDirt || (_bindingGroups.Count < count); 

            return foundDirt; 
        } 

        #endregion Internal Methods 

        #region Private Methods

        // return true if element is a descendant of ancestor 
        bool IsInScope(DependencyObject ancestor, DependencyObject element)
        { 
            bool result = (ancestor == null) || VisualTreeHelper.IsAncestorOf(ancestor, element); 
            return result;
        } 

        #endregion Private Methods

        #region Private Data 

        Set<BindingGroup> _bindingGroups = new Set<BindingGroup>(); 
        Set<BindingExpressionBase> _bindings = new Set<BindingExpressionBase>(); 

        static readonly List<BindingGroup> EmptyBindingGroupList = new List<BindingGroup>(); 
        static readonly List<BindingExpressionBase> EmptyBindingList = new List<BindingExpressionBase>();

        #endregion Private Data
 
        #region Private types
 
        class Set<T> : Dictionary<T, object>, IEnumerable<T> 
        {
            public Set() 
                : base()
            {
            }
 
            public Set(IDictionary<T, object> other)
                : base(other) 
            { 
            }
 
            public Set(IEqualityComparer<T> comparer)
                : base(comparer)
            {
            } 

            public void Add(T item) 
            { 
                if (!ContainsKey(item))
                { 
                    Add(item, null);
                }
            }
 
            IEnumerator<T> IEnumerable<T>.GetEnumerator()
            { 
                return Keys.GetEnumerator(); 
            }
 
            public List<T> ToList()
            {
                return new List<T>(Keys);
            } 
        }
 
        #endregion Private types 
    }
} 

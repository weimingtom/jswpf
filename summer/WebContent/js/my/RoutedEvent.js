/**
 * RoutedEvent
 */

define(["dojo/_base/declare", "system/Type"], function(declare, Type){
	var RoutedEvent = declare(null,{
		constructor:function(/*int*/ index, /*boolean*/ found){
			if(arguments.length==1 ){
				this._store = index | 0x80000000;
			}else if(arguments.length==2 ){
				this._store = index & 0x7FFFFFFF;
				if (found){
					this._store |= 0x80000000;
				}
			}else{
				throw new Error();
			}
		}
	});
	
	Object.defineProperties(RoutedEvent.prototype,{
		  
		/*public boolean */Found:
		{
			get:function() { return (this._store & 0x80000000) != 0; }
		},
		 
		/*public int */Index:
		{
			get:function() { return this._store & 0x7FFFFFFF; }
		}
	});
	
	RoutedEvent.Type = new Type("RoutedEvent", RoutedEvent, [Object.Type]);
	return RoutedEvent;
});


using System; 
using System.ComponentModel;
using System.Windows.Markup;
using System.Globalization;
using MS.Internal.PresentationCore; 

namespace System.Windows 
{ 
    /// <summary>
    ///     RoutedEvent is a unique identifier for 
    ///     any registered RoutedEvent
    /// </summary>
    /// <remarks>
    ///     RoutedEvent constitutes the <para/> 
    ///     <see cref="RoutedEvent.Name"/>, <para/>
    ///     <see cref="RoutedEvent.RoutingStrategy"/>, <para/> 
    ///     <see cref="RoutedEvent.HandlerType"/> and <para/> 
    ///     <see cref="RoutedEvent.OwnerType"/> <para/>
    ///     <para/> 
    ///
    ///     NOTE: None of the members can be null
    /// </remarks>
    /// <ExternalAPI/> 
    [TypeConverter("System.Windows.Markup.RoutedEventConverter, PresentationFramework, Version=" + BuildInfo.WCP_VERSION + ", Culture=neutral, PublicKeyToken=" + BuildInfo.WCP_PUBLIC_KEY_TOKEN + ", Custom=null")]
    [ValueSerializer("System.Windows.Markup.RoutedEventValueSerializer, PresentationFramework, Version=" + BuildInfo.WCP_VERSION + ", Culture=neutral, PublicKeyToken=" + BuildInfo.WCP_PUBLIC_KEY_TOKEN + ", Custom=null")] 
    public sealed class RoutedEvent 
    {
        #region External API 
        /// <summary>
        ///     Associate another owner type with this event.
        /// </summary>
        /// <remarks> 
        ///     The owner type is used when resolving an event by name.
        /// </remarks> 
        /// <param name="ownerType">Additional owner type</param> 
        /// <returns>This event.</returns>
        public RoutedEvent AddOwner(Type ownerType) 
        {
            GlobalEventManager.AddOwner(this, ownerType);
            return this;
        } 

        /// <summary> 
        ///     Returns the Name of the RoutedEvent 
        /// </summary>
        /// <remarks> 
        ///     RoutedEvent Name is unique within the
        ///     OwnerType (super class types not considered
        ///     when talking about uniqueness)
        /// </remarks> 
        /// <ExternalAPI/>
        public string Name 
        { 
            get {return _name;}
        } 

        /// <summary>
        ///     Returns the <see cref="RoutingStrategy"/>
        ///     of the RoutedEvent 
        /// </summary>
        /// <ExternalAPI/> 
        public RoutingStrategy RoutingStrategy 
        {
            get {return _routingStrategy;} 
        }

        /// <summary>
        ///     Returns Type of Handler for the RoutedEvent 
        /// </summary>
        /// <remarks> 
        ///     HandlerType is a type of delegate 
        /// </remarks>
        /// <ExternalAPI/> 
        public Type HandlerType
        {
            get {return _handlerType;}
        } 

        // Check to see if the given delegate is a legal handler for this type. 
        //  It either needs to be a type that the registering class knows how to 
        //  handle, or a RoutedEventHandler which we can handle without the help
        //  of the registering class. 
        internal bool IsLegalHandler( Delegate handler )
        {
            Type handlerType = handler.GetType();
 
            return ( (handlerType == HandlerType) ||
                     (handlerType == typeof(RoutedEventHandler) ) ); 
        } 

        /// <summary> 
        ///     Returns Type of Owner for the RoutedEvent
        /// </summary>
        /// <remarks>
        ///     OwnerType is any object type 
        /// </remarks>
        /// <ExternalAPI/> 
        public Type OwnerType 
        {
            get {return _ownerType;} 
        }

        /// <summary>
        ///    String representation 
        /// </summary>
        public override string ToString() 
        { 
            return string.Format(CultureInfo.InvariantCulture, "{0}.{1}", _ownerType.Name, _name );
        } 

        #endregion External API

        #region Construction 

        // Constructor for a RoutedEvent (is internal 
        // to the EventManager and is onvoked when a new 
        // RoutedEvent is registered)
        internal RoutedEvent( 
            string name,
            RoutingStrategy routingStrategy,
            Type handlerType,
            Type ownerType) 
        {
            _name = name; 
            _routingStrategy = routingStrategy; 
            _handlerType = handlerType;
            _ownerType = ownerType; 

            _globalIndex = GlobalEventManager.GetNextAvailableGlobalIndex(this);
        }
 
        /// <summary>
        ///    Index in GlobalEventManager 
        /// </summary> 
        internal int GlobalIndex
        { 
            get { return _globalIndex; }
        }
        #endregion Construction
 
        #region Data
 
        private string _name; 
        private RoutingStrategy _routingStrategy;
        private Type _handlerType; 
        private Type _ownerType;

        private int _globalIndex;
 
        #endregion Data
    } 
} 

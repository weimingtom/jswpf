package org.summer.view.widget.threading;

import java.lang.reflect.Array;
import java.util.PriorityQueue;

import org.omg.CORBA.Environment;
import org.summer.view.internal.SecurityCriticalData;
import org.summer.view.internal.SecurityCriticalDataClass;
import org.summer.view.widget.Delegate;
import org.summer.view.widget.EventArgs;
import org.summer.view.widget.InvalidOperationException;
import org.summer.view.widget.TimeSpan;
import org.summer.view.widget.WeakReference;
import org.summer.view.widget.collection.List;
import org.summer.view.widget.internal.EventHandler;
import org.summer.view.window.SecurityHelper;
import org.summer.view.window.interop.IntPtr;
import org.summer.view.window.interop.MSG;

/// <summary>
    ///     Provides UI services for a thread.
    /// </summary>
    public final class Dispatcher
    {
        /// <securitynote>
        ///     Critical: This code calls into RegisterWindowMesssage which is critical
        ///     TreatAsSafe: This is safe to call as no external parameters are taken in
        /// </securitynote>
//        [SecurityCritical,SecurityTreatAsSafe]
        static
        {
            _msgProcessQueue = UnsafeNativeMethods.RegisterWindowMessage("DispatcherProcessQueue");
            _globalLock = new Object();
            _dispatchers = new List<weakreference>();
            _possibleDispatcher = new WeakReference(null);
            _exceptionWrapper = new ExceptionWrapper();
            _exceptionWrapper.Catch += new ExceptionWrapper.CatchHandler(CatchExceptionStatic);
            _exceptionWrapper.Filter += new ExceptionWrapper.FilterHandler(ExceptionFilterStatic);
        }
  
        /// <summary>
        ///     Returns the Dispatcher for the calling thread.
        /// </summary>
        /// <remarks>
        ///     If there is no dispatcher available for the current thread,
        ///     and the thread allows a dispatcher to be auto-created, a new
        ///     dispatcher will be created.
        ///     <p>
        ///     If there is no dispatcher for the current thread, and thread
        ///     does not allow one to be auto-created, an exception is thrown.
        /// 
        public static Dispatcher CurrentDispatcher
        {
            get
            {
                // Find the dispatcher for this thread.
                Dispatcher currentDispatcher = FromThread(Thread.CurrentThread);;
  
                // Auto-create the dispatcher if there is no dispatcher for
                // this thread (if we are allowed to).
                if(currentDispatcher == null)
                {
                    currentDispatcher = new Dispatcher();
                }
 
                return currentDispatcher;
            }
        }
  
 
        /// </p><summary>
        ///     Returns the Dispatcher for the specified thread.
        /// </summary>
        /// <remarks>
        ///     If there is no dispatcher available for the specified thread,
        ///     this method will return null.
        /// </remarks>
        public static Dispatcher FromThread(Thread thread)
        {
        	/*lock*/synchronized(_globalLock)
            {
                Dispatcher dispatcher = null;
 
                if(thread != null)
                {
                    // Shortcut: we track one static reference to the last current
                    // dispatcher we gave out.  For single-threaded apps, this will
                    // be set all the time.  For multi-threaded apps, this will be
                    // set for periods of time during which accessing CurrentDispatcher
                    // is cheap.  When a thread switch happens, the next call to
                    // CurrentDispatcher is expensive, but then the rest are fast
                    // again.
                    dispatcher = _possibleDispatcher.Target as Dispatcher;
                    if(dispatcher == null || dispatcher.Thread != thread)
                    {
                        // The "possible" dispatcher either was null or belongs to
                        // the a different thread.
                        dispatcher = null;
 
                        // Spin over the list of dispatchers looking for one that belongs
                        // to this thread.  We could use TLS here, but managed TLS is very
                        // expensive, so we think it is cheaper to search our own data
                        // structure.
                        //
                        // Note: Do not cache _dispatchers.Count because we rely on it
                        // being updated if we encounter a dead weak reference.
                        for(int i = 0; i < _dispatchers.Count; i++)
                        {
                            Dispatcher d = _dispatchers[i].Target as Dispatcher;
                            if(d != null)
                            {
                                // Note: we compare the thread objects themselves to protect
                                // against threads reusing old thread IDs.
                                Thread dispatcherThread = d.Thread;
                                if(dispatcherThread == thread)
                                {
                                    dispatcher = d;
  
                                    // Do not exit the loop early since we are also
                                    // looking for dead references.
                                }
                            }
                            else
                            {
                                // We found a dead reference, so remove it from
                                // the list, and adjust the index so we account
                                // for it.
                                _dispatchers.RemoveAt(i);
                                i--;
                            }
                        }
  
                        // Stash this dispatcher as a "possible" dispatcher for the
                        // next call to FromThread.
                        if(dispatcher != null)
                        {
                            _possibleDispatcher.Target = dispatcher;
                        }
  
                    }
                }
 
                return dispatcher;
            }
        }
 
        /// <summary>
        /// </summary>
        public Thread Thread
        {
            get
            {
                return _dispatcherThread;
            }
        }
  
        /// <summary>
        ///     Checks that the calling thread has access to this Object.
        /// </summary>
        /// <remarks>
        ///     Only the dispatcher thread may access DispatcherObjects.
        ///     <p>
        ///     This method is public so that any thread can probe to
        ///     see if it has access to the DispatcherObject.
        /// 
        /// <returns>
        ///     True if the calling thread has access to this Object.
        /// </returns>
//        [System.ComponentModel.EditorBrowsable(System.ComponentModel.EditorBrowsableState.Never)]
        public boolean CheckAccess()
        {
            return Thread == Thread.CurrentThread();
        }
  
        /// </p><summary>
        ///     Verifies that the calling thread has access to this Object.
        /// </summary>
        /// <remarks>
        ///     Only the dispatcher thread may access DispatcherObjects.
        ///     <p>
        ///     This method is public so that derived classes can probe to
        ///     see if the calling thread has access to itself.
        /// 
//        [System.ComponentModel.EditorBrowsable(System.ComponentModel.EditorBrowsableState.Never)]
        public void VerifyAccess()
        {
            if(!CheckAccess())
            {
                throw new InvalidOperationException(/*SR.Get(SRID.VerifyAccess)*/);
            }
        }
  
        /// </p><summary>
        ///     Begins the process of shutting down the dispatcher.
        /// </summary>
        /// <remarks>
        ///     This API demand unrestricted UI Permission
        /// </remarks>
        ///<securitynote>
        /// Critical - it calls critical methods (ShutdownCallback).
        /// PublicOK - it demands unrestricted UI permission.
        ///</securitynote>
//        [SecurityCritical]
        public void BeginInvokeShutdown(DispatcherPriority priority) // NOTE: should be Priority
        {
            // We didn't want to enable quitting in the SEE
            SecurityHelper.DemandUnrestrictedUIPermission();
  
            BeginInvoke(priority, new ShutdownCallback(ShutdownCallbackInternal));
        }
  
        /// <summary>
        ///     Begins the process of shutting down the dispatcher.
        /// </summary>
        /// <remarks>
        ///     Callers must have UIPermission(PermissionState.Unrestricted) to call this API.
        /// </remarks>
        ///<securitynote>
        /// Critical - it calls critical methods (ShutdownCallback).
        /// PublicOK - it demands unrestricted UI permission
        ///</securitynote>
//        [SecurityCritical]
        public void InvokeShutdown()
        {
            // We didn't want to enable quitting in the SEE
            SecurityHelper.DemandUnrestrictedUIPermission();
 
            CriticalInvokeShutdown();
        }
 
        ///<securitynote>
        /// Critical - it calls critical methods (ShutdownCallback).
        ///</securitynote>
//        [SecurityCritical]
//        [FriendAccessAllowed] //used by Application.ShutdownImpl() in PresentationFramework
        /*internal*/ public  void CriticalInvokeShutdown()
        {
            Invoke(DispatcherPriority.Send, new ShutdownCallback(ShutdownCallbackInternal)); // NOTE: should be Priority.Max
        }
  
        /// <summary>
        ///     Whether or not the dispatcher is shutting down.
        /// </summary>
        public boolean HasShutdownStarted
        {
            get
            {
                return _hasShutdownStarted; // Free-Thread access OK.
            }
        }
 
        /// <summary>
        ///     Whether or not the dispatcher has been shut down.
        /// </summary>
        public boolean HasShutdownFinished
        {
            get
            {
                return _hasShutdownFinished; // Free-Thread access OK.
            }
        }
  
        /// <summary>
        ///     Raised when the dispatcher is shutting down.
        /// </summary>
        public /*event*/ EventHandler ShutdownStarted;
  
        /// <summary>
        ///     Raised when the dispatcher is shut down.
        /// </summary>
        public /*event*/ EventHandler ShutdownFinished;
 
        /// <summary>
        ///     Push the main execution frame.
        /// </summary>
        /// <remarks>
        ///     This frame will continue until the dispatcher is shut down.
        ///     Callers must have UIPermission(PermissionState.Unrestricted) to call this API.
        /// </remarks>
        ///<securitynote>
        ///    Critical: This code is blocked off more as defense in depth
        ///    PublicOk: From a public perspective there is a link demand here
        ///</securitynote>
//        [UIPermissionAttribute(SecurityAction.LinkDemand,Unrestricted=true)]
//        [SecurityCritical]
        public static void Run()
        {
            PushFrame(new DispatcherFrame());
        }
 
        /// <summary>
        ///     Push an execution frame.
        /// </summary>
        /// <param name="frame">
        ///     The frame for the dispatcher to process.
        ///
        /// <remarks>
        ///     Callers must have UIPermission(PermissionState.Unrestricted) to call this API.
        /// </remarks>
        ///<securitynote>
        ///    Critical: This code is blocked off more as defense in depth
        ///    PublicOk: From a public perspective there is a link demand here
        ///</securitynote>
//        [UIPermissionAttribute(SecurityAction.LinkDemand,Unrestricted=true)]
//        [SecurityCritical]
        public static void PushFrame(DispatcherFrame frame)
        {
            if(frame == null)
            {
                throw new IllegalArgumentException("frame");
            }
  
            Dispatcher dispatcher = Dispatcher.CurrentDispatcher;
            if(dispatcher._hasShutdownFinished) // Dispatcher thread - no lock needed for read
            {
                throw new InvalidOperationException(SR.Get(SRID.DispatcherHasShutdown));
            }
  
            if(frame.Dispatcher != dispatcher)
            {
                throw new InvalidOperationException(SR.Get(SRID.MismatchedDispatchers));
            }
 
            if(dispatcher._disableProcessingCount > 0)
            {
                throw new InvalidOperationException(SR.Get(SRID.DispatcherProcessingDisabled));
            }
  
            dispatcher.PushFrameImpl(frame);
        }
 
        /// <summary>
        ///     Requests that all nested frames exit.
        /// </summary>
        /// <remarks>
        ///     Callers must have UIPermission(PermissionState.Unrestricted) to call this API.
        /// </remarks>
        /// <securitynote>
        ///     Critical - calls a critical method - postThreadMessage.
        ///     PublicOK - all we're doing is posting a current message to our thread.
        ///                net effect is the dispatcher "wakes up"
        ///                and uses the continue flag ( which may have just changed).
        /// </securitynote>
//        [SecurityCritical]
        public static void ExitAllFrames()
        {
            // We didn't want to enable exiting all frames in the SEE
            SecurityHelper.DemandUnrestrictedUIPermission();
 
            Dispatcher dispatcher = Dispatcher.CurrentDispatcher;
            if(dispatcher._frameDepth > 0)
            {
                dispatcher._exitAllFrames = true;
  
                // Post a message so that the message pump will wake up and
                // check our continue state.
                dispatcher.BeginInvoke(DispatcherPriority.Send, (DispatcherOperationCallback) delegate(Object unused) {return null;}, null);
            }
        }
 
        /// <summary>
        ///     Executes the specified delegate asynchronously on the thread that
        ///     the Dispatcher was created on.
        /// </summary>
        /// <param name="priority">
        ///     The priority that determines in what order the specified method
        ///     is invoked relative to the other pending methods in the Dispatcher.
        ///
        /// <param name="method">
        ///     A delegate to a method that takes no parameters.
        ///
        /// <returns>
        ///     An IAsyncResult Object that represents the result of the
        ///     BeginInvoke operation.
        /// </returns>
        public DispatcherOperation BeginInvoke(DispatcherPriority priority, Delegate method) // NOTE: should be Priority
        {
            return BeginInvokeImpl(priority, method, null, false /* isSingleParameter */);
        }
 
        /// <summary>
        ///     Executes the specified delegate asynchronously with the specified
        ///     arguments, on the thread that the Dispatcher was created on.
        /// </summary>
        /// <param name="priority">
        ///     The priority that determines in what order the specified method
        ///     is invoked relative to the other pending methods in the Dispatcher.
        /// 
        /// <param name="method">
        ///     A delegate to a method that takes parameters of the same number
        ///     and type that are contained in the args parameter.
        ///
        /// <param name="arg">
        ///     A Object to pass as an argument to the given method.
        ///     This can be null if no arguments are needed.
        ///
        /// <returns>
        ///     An IAsyncResult Object that represents the result of the
        ///     BeginInvoke operation.
        /// </returns>
        //[CodeAnalysis("AptcaMethodsShouldOnlyCallAptcaMethods")] //Tracking Bug: 29647
        public DispatcherOperation BeginInvoke(DispatcherPriority priority, Delegate method, Object arg) // NOTE: should be Priority
        {
            return BeginInvokeImpl(priority, method, arg, true /* isSingleParameter */);
        }
  
        /// <summary>
        ///     Executes the specified delegate asynchronously with the specified
        ///     arguments, on the thread that the Dispatcher was created on.
        /// </summary>
        /// <param name="priority">
        ///     The priority that determines in what order the specified method
        ///     is invoked relative to the other pending methods in the Dispatcher.
        ///
        /// <param name="method">
        ///     A delegate to a method that takes parameters of the same number
        ///     and type that are contained in the args parameter.
        /// 
        /// <param name="arg">
        /// enh
        ///
        /// <param name="args">
        ///     An array of objects to pass as arguments to the given method.
        ///     This can be null if no arguments are needed.
        ///
        /// <returns>
        ///     An IAsyncResult Object that represents the result of the
        ///     BeginInvoke operation.
        /// </returns>
        //[CodeAnalysis("AptcaMethodsShouldOnlyCallAptcaMethods")] //Tracking Bug: 29647
        public DispatcherOperation BeginInvoke(DispatcherPriority priority, Delegate method, Object arg, /*params*/ Object[] args) // NOTE: should be Priority
        {
            return BeginInvokeImpl(priority, method, CombineParameters(arg, args), false /* isSingleParameter */);
        }
  
        /// <securitynote>
        ///     Critical: accesses _hooks
        ///     TreatAsSafe: does not expose _hooks
        /// </securitynote>
//        [SecurityCritical, SecurityTreatAsSafe]
        private DispatcherOperation BeginInvokeImpl(DispatcherPriority priority, Delegate method, Object args, boolean isSingleParameter) // NOTE: should be Priority
        {
            ValidatePriority(priority, "priority");
            if(method == null)
            {
                throw new IllegalArgumentException("method");
            }
 
            DispatcherOperation operation = null;
            DispatcherHooks hooks = null;
            boolean succeeded = false;
 
            // Could be a non-dispatcher thread, lock to read
            /*lock*/synchronized(_instanceLock)
            {
                if (!_hasShutdownFinished && !Environment.HasShutdownStarted)
                {
                    operation = new DispatcherOperation(this, method, priority, args, isSingleParameter);
 
                    // Add the operation to the work queue
                    operation._item = _queue.Enqueue(priority, operation);
  
                    // Make sure we will wake up to process this operation.
                    succeeded = RequestProcessing();
  
                    if (succeeded)
                    {
                        hooks = _hooks;
                    }
                    else
                    {
                        // Dequeue and abort the item.  We can safely return this to the user.
                        _queue.RemoveItem(operation._item);
                        operation._status = DispatcherOperationStatus.Aborted;
                    }
                }
                else
                {
                    // Rather than returning null we'll create an aborted operation and return it to the user
                    operation = new DispatcherOperation(this, method, priority);
                }
            }
  
            if (operation != null && succeeded == true)
            {
                if(hooks != null)
                {
                    hooks.RaiseOperationPosted(this, operation);
                }
  
                if (EventTrace.IsEnabled(EventTrace.Flags.performance, EventTrace.Level.normal))
                {
                    EventTrace.EventProvider.TraceEvent(EventTrace.GuidFromId(EventTraceGuidId.DISPATCHERPOSTGUID), MS.Utility.EventType.Info, priority, operation.Name);
                }
            }
  
            return operation;
        }
  
        /// <summary>
        ///     Executes the specified delegate synchronously on the thread that
        ///     the Dispatcher was created on.
        /// </summary>
        /// <param name="priority">
        ///     The priority that determines in what order the specified method
        ///     is invoked relative to the other pending methods in the Dispatcher.
        /// 
        /// <param name="method">
        ///     A delegate to a method that takes no parameters.
        /// 
        /// <returns>
        ///     The return value from the delegate being invoked, or null if
        ///     the delegate has no return value.
        /// </returns>
        public Object Invoke(DispatcherPriority priority, Delegate method) // NOTE: should be Priority
        {
            return InvokeImpl(priority, TimeSpan.FromMilliseconds(-1), method, null, false /* isSingleParameter */);
        }
  
        /// <summary>
        ///     Executes the specified delegate synchronously with the specified
        ///     arguments, on the thread that the Dispatcher was created on.
        /// </summary>
        /// <param name="priority">
        ///     The priority that determines in what order the specified method
        ///     is invoked relative to the other pending methods in the Dispatcher.
        ///
        /// <param name="method">
        ///     A delegate to a method that takes parameters of the same number
        ///     and type that are contained in the args parameter.
        ///
        /// <param name="arg">
        ///     An Object to pass as an argument to the given method.
        ///     This can be null if no arguments are needed.
        /// 
        /// <returns>
        ///     The return value from the delegate being invoked, or null if
        ///     the delegate has no return value.
        /// </returns>
        public Object Invoke(DispatcherPriority priority, Delegate method, Object arg) // NOTE: should be Priority
        {
            return InvokeImpl(priority, TimeSpan.FromMilliseconds(-1), method, arg, true /* isSingleParameter */);
        }
  
        /// <summary>
        ///     Executes the specified delegate synchronously with the specified
        ///     arguments, on the thread that the Dispatcher was created on.
        /// </summary>
        /// <param name="priority">
        ///     The priority that determines in what order the specified method
        ///     is invoked relative to the other pending methods in the Dispatcher.
        /// 
        /// <param name="method">
        ///     A delegate to a method that takes parameters of the same number
        ///     and type that are contained in the args parameter.
        ///
        /// <param name="arg">
        ///
        /// <param name="args">
        ///     An array of objects to pass as arguments to the given method.
        ///     This can be null if no arguments are needed.
        /// 
        /// <returns>
        ///     The return value from the delegate being invoked, or null if
        ///     the delegate has no return value.
        /// </returns>
        public Object Invoke(DispatcherPriority priority, Delegate method, Object arg, params Object[] args) // NOTE: should be Priority
        {
            return InvokeImpl(priority, TimeSpan.FromMilliseconds(-1), method, CombineParameters(arg, args), false /* isSingleParameter */);
        }
  
        /// <summary>
        ///     Executes the specified delegate synchronously on the thread that
        ///     the Dispatcher was created on.
        /// </summary>
        /// <param name="priority">
        ///     The priority that determines in what order the specified method
        ///     is invoked relative to the other pending methods in the Dispatcher.
        /// 
        /// <param name="timeout">
        ///     The maximum amount of time to wait for the operation to complete.
        ///         /// <param name="method">
        ///     A delegate to a method that takes no parameters.
        ///
        /// <returns>
        ///     The return value from the delegate being invoked, or null if
        ///     the delegate has no return value.
        /// </returns>
        public Object Invoke(DispatcherPriority priority, TimeSpan timeout, Delegate method) // NOTE: should be Priority
        {
            return InvokeImpl(priority, timeout, method, null, false /* isSingleParameter */);
        }
 
        /// <summary>
        ///     Executes the specified delegate synchronously with the specified
        ///     arguments, on the thread that the Dispatcher was created on.
        /// </summary>
        /// <param name="priority">
        ///     The priority that determines in what order the specified method
        ///     is invoked relative to the other pending methods in the Dispatcher.
        ///
        /// <param name="timeout">
        ///     The maximum amount of time to wait for the operation to complete.
        /// 
        /// <param name="method">
        ///     A delegate to a method that takes parameters of the same number
        ///     and type that are contained in the args parameter.
        ///
        /// <param name="arg">
        ///     An Object to pass as an argument to the given method.
        ///     This can be null if no arguments are needed.
        ///
        /// <returns>
        ///     The return value from the delegate being invoked, or null if
        ///     the delegate has no return value.
        /// </returns>
        public Object Invoke(DispatcherPriority priority, TimeSpan timeout, Delegate method, Object arg) // NOTE: should be Priority
        {
            return InvokeImpl(priority, timeout, method, arg, true /* isSingleParameter */);
        }
 
        /// <summary>
        ///     Executes the specified delegate synchronously with the specified
        ///     arguments, on the thread that the Dispatcher was created on.
        /// </summary>
        /// <param name="priority">
        ///     The priority that determines in what order the specified method
        ///     is invoked relative to the other pending methods in the Dispatcher.
        ///
        /// <param name="timeout">
        ///     The maximum amount of time to wait for the operation to complete.
        ///
        /// <param name="method">
        ///     A delegate to a method that takes parameters of the same number
        ///     and type that are contained in the args parameter.
        /// 
        /// <param name="arg">
        ///
        /// <param name="args">
        ///     An array of objects to pass as arguments to the given method.
        ///     This can be null if no arguments are needed.
        /// 
        /// <returns>
        ///     The return value from the delegate being invoked, or null if
        ///     the delegate has no return value.
        /// </returns>
        public Object Invoke(DispatcherPriority priority, TimeSpan timeout, Delegate method, Object arg, /*params*/ Object[] args) // NOTE: should be Priority
        {
            return InvokeImpl(priority, timeout, method, CombineParameters(arg, args), false /* isSingleParameter */);
        }
  
        /// <securitynote>
        ///     Critical:This code causes arbitrary delegate to execute. Also link demand in SetSynchronizationContext
        ///     TreatAsSafe: Having a delegate execute is ok , it will break if it does anything that violate the trust boundaries
        /// </securitynote>
//        [SecurityCritical,SecurityTreatAsSafe]
        private Object InvokeImpl(DispatcherPriority priority, TimeSpan timeout, Delegate method, Object args, boolean isSingleParameter) // NOTE: should be Priority
        {
            ValidatePriority(priority, "priority");
            if(priority == DispatcherPriority.Inactive)
            {
                throw new IllegalArgumentException(SR.Get(SRID.InvalidPriority), "priority");
            }
 
            if(method == null)
            {
                throw new IllegalArgumentException("method");
            }
  
            Object result = null;
 
            if(priority == DispatcherPriority.Send && CheckAccess()) // NOTE: should be Priority.Max
            {
                // This is a special case for when the UI thread calls Invoke with
                // the maximum priority.  In such cases, we do not go through the
                // queue, but instead we execute the delegate directly.  This allows
                // the caller to know that nothing will execute before their callback.
  
                // Make sure we set this Dispatcher as the synchronization context.
                SynchronizationContext oldSynchronizationContext = SynchronizationContext.Current;
                try
                {
                    SynchronizationContext.SetSynchronizationContext(new DispatcherSynchronizationContext(this));
                    result = WrappedInvoke(method, args, isSingleParameter);
                }
                finally
                {
                    SynchronizationContext.SetSynchronizationContext(oldSynchronizationContext);
                }
            }
            else
            {
                DispatcherOperation op = BeginInvokeImpl(priority, method, args, isSingleParameter);
                if(op != null)
                {
                    op.Wait(timeout);
  
                    if(op.Status == DispatcherOperationStatus.Completed)
                    {
                        result = op.Result;
                    }
                    else if(op.Status == DispatcherOperationStatus.Aborted)
                    {
                        // Hm, someone aborted us.  Maybe the dispatcher got
                        // shut down on us?  Just return null.
                        result = null;
                    }
                    else
                    {
                        // We timed out, just abort the op so that it doesn't
                        // invoke later.
                        //
                        // Note the race condition: if this is a foreign thread,
                        // it is possible that the dispatcher thread could actually
                        // dispatch the operation between the time our Wait()
                        // call returns and we get here.  In the case the operation
                        // will actually be dispatched, but we will return failure.
                        //
                        // We recognize this but decide not to do anything about it,
                        // as this is a common problem is multi-threaded programming.
                        op.Abort();
                    }
                }
            }
 
            return result;
        }
 
        /// <summary>
        ///     Disable the event processing of the dispatcher.
        /// </summary>
        /// <remarks>
        ///     This is an advanced method intended to elliminate the chance of
        ///     unrelated reentrancy.  The effect of disabling processing is:
        ///     1) CLR locks will not pump messages internally.
        ///     2) No one is allowed to push a frame.
        ///     3) No message processing is permitted.
        /// </remarks>
        public DispatcherProcessingDisabled DisableProcessing()
        {
            VerifyAccess();
  
            // Turn off processing.
            _disableProcessingCount++;
 
            DispatcherProcessingDisabled dpd = new DispatcherProcessingDisabled();
            dpd._dispatcher = this;
            return dpd;
        }
  
/*
        /// <summary>
        ///     Reports the range of priorities that are considered
        ///     as foreground priorities.
        /// </summary>
        /// <remarks>
        ///     A foreground priority is processed before input.
        /// </remarks>
        public static PriorityRange ForegroundPriorityRange
        {
            get
            {
                return _foregroundPriorityRange;
            }
        }
  
        /// <summary>
        ///     Reports the range of priorities that are considered
        ///     as background priorities.
        /// </summary>
        /// <remarks>
        ///     A background priority is processed after input.
        /// </remarks>
        public static PriorityRange BackgroundPriorityRange
        {
            get
            {
                return _backgroundPriorityRange;
            }
        }
 
        /// <summary>
        ///     Reports the range of priorities that are considered
        ///     as idle priorities.
        /// </summary>
        /// <remarks>
        ///     An idle priority is processed periodically after background
        ///     priorities have been processed.
        /// </remarks>
        public static PriorityRange IdlePriorityRange
        {
            get
            {
                return _idlePriorityRange;
            }
        }
 
        /// <summary>
        ///     Represents a convenient foreground priority.
        /// </summary>
        /// <remarks>
        ///     A foreground priority is processed before input.  In general
        ///     you should define your own foreground priority to allow for
        ///     more fine-grained ordering of queued items.
        /// </remarks>
        public static Priority ForegroundPriority
        {
            get
            {
                return _foregroundPriority;
            }
        }
  
        /// <summary>
        ///     Represents a convenient background priority.
        /// </summary>
        /// <remarks>
        ///     A background priority is processed after input.  In general you
        ///     should define your own background priority to allow for more
        ///     fine-grained ordering of queued items.
        /// </remarks>
        public static Priority BackgroundPriority
        {
            get
            {
                return _backgroundPriority;
            }
        }
  
        /// <summary>
        ///     Represents a convenient idle priority.
        /// </summary>
        /// <remarks>
        ///     An idle priority is processed periodically after background
        ///     priorities have been processed.  In general you should define
        ///     your own idle priority to allow for more fine-grained ordering
        ///     of queued items.
        /// </remarks>
        public static Priority IdlePriority
        {
            get
            {
                return _idlePriority;
            }
        }
*/
  
        /// <summary>
        ///     Validates that a priority is suitable for use by the dispatcher.
        /// </summary>
        /// <param name="priority">
        ///     The priority to validate.
        ///
        /// <param name="parameterName">
        ///     The name if the argument to report in the IllegalArgumentException
        ///     that is raised if the priority is not suitable for use by
        ///     the dispatcher.
        /// 
        public static void ValidatePriority(DispatcherPriority priority, string parameterName) // NOTE: should be Priority
        {
            // First make sure the Priority is valid.
            // Priority.ValidatePriority(priority, paramName);
 
            // Second, make sure the priority is in a range recognized by
            // the dispatcher.
            if(!_foregroundPriorityRange.Contains(priority) &&
               !_backgroundPriorityRange.Contains(priority) &&
               !_idlePriorityRange.Contains(priority) &&
               DispatcherPriority.Inactive != priority)  // NOTE: should be Priority.Min
            {
                // If we move to a Priority class, this exception will have to change too.
                throw new /*System.ComponentModel.*/InvalidEnumArgumentException(parameterName, (int)priority, typeof(DispatcherPriority));
            }
        }
 
        /// <summary>
        ///     Checks that the calling thread has access to this Object.
        /// </summary>
        /// <remarks>
        ///     Only the dispatcher thread may access DispatcherObjects.
        ///     <p>
        ///     This method is public so that any thread can probe to
        ///     see if it has access to the DispatcherObject.
        ///     Callers must have UIPermission(PermissionState.Unrestricted) to call this API.
        /// 
        /// <returns>
        ///     True if the calling thread has access to this Object.
        /// </returns>
        /// <securitynote>
        ///     Critical: Accesses _hooks, which is critical.
        ///     TreatAsSafe: link-demands
        /// </securitynote>
//        [System.ComponentModel.EditorBrowsable(System.ComponentModel.EditorBrowsableState.Advanced)]
        public DispatcherHooks Hooks
        {
            [SecurityCritical]
//            [UIPermissionAttribute(SecurityAction.LinkDemand,Unrestricted=true)]
            get
            {
                DispatcherHooks hooks = null;
  
                lock(_instanceLock)
                {
                    if(_hooks == null)
                    {
                        _hooks = new DispatcherHooks();
                    }
 
                    hooks = _hooks;
                }
 
                return hooks;
            }
        }
  
        /// </p><summary>
        ///     Occurs when an untrapped thread exception is thrown.
        /// </summary>
        /// <remarks>
        ///     Raised during the filter stage for an exception raised during
        ///     execution of a delegate via Invoke or BeginInvoke.
        ///     <p>
        ///     The callstack is not unwound at this time (first-chance exception).
        ///     </p><p>
        ///     Listeners to this event must be written with care to avoid
        ///     creating secondary exceptions and to catch any that occur.
        ///     It is recommended to avoid allocating memory or doing any
        ///     heavylifting if possible.
        ///     Callers must have UIPermission(PermissionState.Unrestricted) to call this API.
        /// 
        /// <securitynote>
        ///     Critical: partially-trusted code is not allowed to access our exception filter.
        ///     TreatAsSafe: link-demands
        /// </securitynote>
        public event DispatcherUnhandledExceptionFilterEventHandler UnhandledExceptionFilter
        {
//            [SecurityCritical]
//            [UIPermissionAttribute(SecurityAction.LinkDemand,Unrestricted=true)]
            add
            {
                _unhandledExceptionFilter += value;
            }
//            [SecurityCritical]
//            [UIPermissionAttribute(SecurityAction.LinkDemand,Unrestricted=true)]
            remove
            {
                _unhandledExceptionFilter -= value;
            }
        }
 
        /// </p><summary>
        ///     Occurs when an untrapped thread exception is thrown.
        /// </summary>
        /// <remarks>
        ///     Raised when an exception was caught that was raised during
        ///     execution of a delegate via Invoke or BeginInvoke.
        ///     <p>
        ///     A handler can mark the exception as handled which will prevent
        ///     the /*internal*/ public  "final" exception handler from being called.
        ///     </p><p>
        ///     Listeners to this event must be written with care to avoid
        ///     creating secondary exceptions and to catch any that occur.
        ///     It is recommended to avoid allocating memory or doing any
        ///     heavylifting if possible.
        ///
        public /*event*/ DispatcherUnhandledExceptionEventHandler UnhandledException;
  
        /// </p><summary>
        ///     Reserved Dispatcher member
        /// </summary>
        /*internal*/ public  Object Reserved0
        {
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
            get { return _reserved0; }
  
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
            set { _reserved0 = value; }
        }
 
        /// <summary>
        ///     Reserved Dispatcher member
        /// </summary>
        /*internal*/ public  Object Reserved1
        {
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
            get { return _reserved1; }
  
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
            set { _reserved1 = value; }
        }
  
        /// <summary>
        ///     Reserved Dispatcher member
        /// </summary>
        /*internal*/ public  Object Reserved2
        {
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
            get { return _reserved2; }
 
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
            set { _reserved2 = value; }
        }
  
        /// <summary>
        ///     Reserved Dispatcher member
        /// </summary>
        /*internal*/ public  Object Reserved3
        {
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
            get { return _reserved3; }
  
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
            set { _reserved3 = value; }
        }
 
        /// <summary>
        ///     Reserved Dispatcher member for PtsCache
        /// </summary>
        /*internal*/ public  Object PtsCache
        {
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
            get { return _reservedPtsCache; }
  
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
            set { _reservedPtsCache = value; }
        }
  
        /*internal*/ public  Object InputMethod
        {
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
            get { return _reservedInputMethod; }
  
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
            set { _reservedInputMethod = value; }
        }
  
        /// <securitynote>
        ///     Critical: Since it hands out the InputManager
        /// </securitynote>
        /*internal*/ public  Object InputManager
        {
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
//            [SecurityCritical]
            get { return _reservedInputManager; }
  
//            [FriendAccessAllowed] // Built into Base, used by Core or Framework.
//            [SecurityCritical]
            set { _reservedInputManager = value; }
        }
  
        ///<securitynote>
        ///  Critical: Does an elevation via an unsafeNativeMethods call
        ///  TreatAsSafe: stores critical data in SecurityCritical wrapper
        ///</securitynote>
//        [SecurityCritical, SecurityTreatAsSafe]
        private Dispatcher()
        {
            _queue = new PriorityQueue<dispatcheroperation>();
  
            _tlsDispatcher = this; // use TLS for ownership only
            _dispatcherThread = Thread.CurrentThread;
 
            // Add ourselves to the map of dispatchers to threads.
            /*lock*/synchronized(_globalLock)
            {
                _dispatchers.Add(new WeakReference(this));
            }
  
            _unhandledExceptionEventArgs = new DispatcherUnhandledExceptionEventArgs(this);
            _exceptionFilterEventArgs = new DispatcherUnhandledExceptionFilterEventArgs(this);
 
            // Create the message-only window we use to receive messages
            // that tell us to process the queue.
            MessageOnlyHwndWrapper window = new MessageOnlyHwndWrapper();
            _window = new SecurityCriticalData<messageonlyhwndwrapper>( window );
 
            _hook = new HwndWrapperHook(WndProcHook);
            _window.Value.AddHook(_hook);
        }
 
        ///<securitynote>
        /// Critical - it calls critical methods (ShutdownImpl). it can initiate a shutdown process, disabled
        /// in partial trust.
        ///</securitynote>
//        [SecurityCritical]
        private void StartShutdownImpl()
        {
            if(!_startingShutdown)
            {
                // We only need this to prevent reentrancy if the ShutdownStarted event
                // tries to shut down again.
                _startingShutdown = true;
  
                // Call the ShutdownStarted event before we actually mark ourselves
                // as shutting down.  This is so the handlers can actaully do work
                // when they get this event without throwing exceptions.
                if(ShutdownStarted != null)
                {
                    ShutdownStarted(this, EventArgs.Empty);
                }
  
                _hasShutdownStarted = true;
 
                // Because we may have to defer the actual shutting-down until
                // later, we need to remember the execution context we started
                // the shutdown from.
                //
                // Note that we demanded permissions when BeginInvokeShutdown
                // or InvokeShutdown were called.  So if there were not enough
                // permissions, we would have thrown then.
                //
                ExecutionContext shutdownExecutionContext = ExecutionContext.Capture();
                _shutdownExecutionContext = new SecurityCriticalDataClass<executioncontext>(shutdownExecutionContext);
 
                // Tell Win32 to exit the message loop for this thread.
                // NOTE: I removed this code because of bug 1062099.
                //
                // UnsafeNativeMethods.PostQuitMessage(0);
  
                if(_frameDepth > 0)
                {
                    // If there are any frames running, we have to wait for them
                    // to unwind before we can safely destroy the dispatcher.
                }
                else
                {
                    // The current thread is not spinning inside of the Dispatcher,
                    // so we can go ahead and destroy it.
                    ShutdownImpl();
                }
            }
        }
 
        //<securitynote>
        //  Critical - Calls ShutdownImplInSecurityContext with the execution context that was
        //  active when the shutdown was initiated.
        //</securitynote>
//        [SecurityCritical]
        private void ShutdownImpl()
        {
            if(!_hasShutdownFinished) // Dispatcher thread - no lock needed for read
            {
                if(_shutdownExecutionContext != null && _shutdownExecutionContext.Value != null)
                {
                    // Continue using the execution context that was active when the shutdown
                    // was initiated.
                    ExecutionContext.Run(_shutdownExecutionContext.Value, new ContextCallback(ShutdownImplInSecurityContext), null);
                }
                else
                {
                    // It is possible to be called from WM_DESTROY, in which case no one has begun
                    // the shutdown process, so there is no execution context to use.
                    ShutdownImplInSecurityContext(null);
                }
  
                _shutdownExecutionContext = null;
            }
        }
 
        //<securitynote>
        //  Critical - as it accesses security critical data ( window handle)
        //</securitynote>
//        [SecurityCritical]
        private void ShutdownImplInSecurityContext(Object state)
        {
            // Call the ShutdownFinished event before we actually mark ourselves
            // as shut down.  This is so the handlers can actaully do work
            // when they get this event without throwing exceptions.
            if(ShutdownFinished != null)
            {
                ShutdownFinished(this, EventArgs.Empty);
            }
  
            // Destroy the message-only window we use to process Win32 messages
            //
            // Note: we need to do this BEFORE we actually mark the dispatcher
            // as shutdown.  This is because the window will need the dispatcher
            // to execute the window proc.
            MessageOnlyHwndWrapper window = null;
            /*lock*/synchronized(_instanceLock)
            {
                window = _window.Value;
                _window = new SecurityCriticalData<messageonlyhwndwrapper>(null);
            }
            window.Dispose();
 
            // Mark this dispatcher as shut down.  Attempts to BeginInvoke
            // or Invoke will result in an exception.
            /*lock*/synchronized(_instanceLock)
            {
                _hasShutdownFinished = true; // Dispatcher thread - lock to write
            }
 
            // Now that the queue is off-line, abort all pending operations,
            // including inactive ones.
            DispatcherOperation operation = null;
            do
            {
            	/*lock*/synchronized(_instanceLock)
                {
                    if(_queue.MaxPriority != DispatcherPriority.Invalid)
                    {
                        operation = _queue.Peek();
                    }
                    else
                    {
                        operation = null;
                    }
                }
  
                if(operation != null)
                {
                    operation.Abort();
                }
            } while(operation != null);
  
            // clear out the fields that could be holding onto large graphs of objects.
            /*lock*/synchronized(_instanceLock)
            {
                // We should not need the queue any more.
                _queue = null;
 
                // We should not need the timers any more.
                _timers = null;
  
                // Clear out the reserved fields.
                _reserved0 = null;
                _reserved1 = null;
                _reserved2 = null;
                _reserved3 = null;
                // _reservedPtsCache = null; // PTS needs this in a finalizer... the PTS code should not assume access to this in their finalizer.
                _reservedInputMethod = null;
                _reservedInputManager = null;
            }
 
            // Note: the Dispatcher is still held in TLS.  This maintains the 1-1 relationship
            // between the thread and the Dispatcher.  However the dispatcher is basically
            // dead - it has been marked as _hasShutdownFinished, and most operations are
            // now illegal.
        }
 
        // Returns whether or not the priority was set.
        //[CodeAnalysis("AptcaMethodsShouldOnlyCallAptcaMethods")] //Tracking Bug: 29647
        /// <securitynote>
        ///     Critical: accesses _hooks
        ///     TreatAsSafe: does not expose _hooks
        /// </securitynote>
//        [SecurityCritical, SecurityTreatAsSafe]
        /*internal*/ public  boolean SetPriority(DispatcherOperation operation, DispatcherPriority priority) // NOTE: should be Priority
        {
            boolean notify = false;
            DispatcherHooks hooks = null;
 
            /*lock*/synchronized(_instanceLock)
            {
                if(_queue != null && operation._item.IsQueued)
                {
                    _queue.ChangeItemPriority(operation._item, priority);
                    notify = true;
 
                    if(notify)
                    {
                        // Make sure we will wake up to process this operation.
                        RequestProcessing();
 
                        hooks = _hooks;
                    }
                }
            }
  
            if (notify)
            {
                if(hooks != null)
                {
                    hooks.RaiseOperationPriorityChanged(this, operation);
                }
 
                if (EventTrace.IsEnabled(org.summer.view.widget.documents.performance, EventTrace.Level.normal))
                {
                    EventTrace.EventProvider.TraceEvent(EventTrace.GuidFromId(EventTraceGuidId.DISPATCHERPROMOTEGUID), MS.Utility.EventType.Info, priority, operation.Name);
                }
            }
 
            return notify;
        }
 
        // Returns whether or not the operation was removed.
        //[CodeAnalysis("AptcaMethodsShouldOnlyCallAptcaMethods")] //Tracking Bug: 29647
        /// <securitynote>
        ///     Critical: accesses _hooks
        ///     TreatAsSafe: does not expose _hooks
        /// </securitynote>
//        [SecurityCritical, SecurityTreatAsSafe]
        /*internal*/ public  boolean Abort(DispatcherOperation operation)
        {
            boolean notify = false;
            DispatcherHooks hooks = null;
 
            /*lock*/synchronized(_instanceLock)
            {
                if(_queue != null && operation._item.IsQueued)
                {
                    _queue.RemoveItem(operation._item);
                    operation._status = DispatcherOperationStatus.Aborted;
                    notify = true;
  
                    hooks = _hooks;
                }
            }
 
            if (notify)
            {
                if(hooks != null)
                {
                    hooks.RaiseOperationAborted(this, operation);
                }
  
                if (EventTrace.IsEnabled(org.summer.view.widget.documents.performance, EventTrace.Level.normal))
                {
                    EventTrace.EventProvider.TraceEvent(EventTrace.GuidFromId(EventTraceGuidId.DISPATCHERABORTGUID), MS.Utility.EventType.Info, operation.Priority, operation.Name);
                }
            }
 
            return notify;
        }
  
        //[CodeAnalysis("AptcaMethodsShouldOnlyCallAptcaMethods")] //Tracking Bug: 29647
        /// <securitynote>
        ///    Critical: This code can be used to process input and calls into DispatcherOperation.Invoke which
        ///    is critical
        /// </securitynote>
//        [SecurityCritical]
        private void ProcessQueue()
        {
            DispatcherPriority maxPriority = DispatcherPriority.Invalid; // NOTE: should be Priority.Invalid
            DispatcherOperation op = null;
            DispatcherHooks hooks = null;
  
            //
            // Dequeue the next operation if appropriate.
            /*lock*/synchronized(_instanceLock)
            {
                _postedProcessingType = PROCESS_NONE;
  
                // We can only do background processing if there is
                // no input in the Win32 queue.
                boolean backgroundProcessingOK = !IsInputPending();
 
                maxPriority = _queue.MaxPriority;
 
                if(maxPriority != DispatcherPriority.Invalid &&  // Nothing. NOTE: should be Priority.Invalid
                   maxPriority != DispatcherPriority.Inactive)   // Not processed. // NOTE: should be Priority.Min
                {
                    if(_foregroundPriorityRange.Contains(maxPriority) || backgroundProcessingOK)
                    {
                         op = _queue.Dequeue();
                         hooks = _hooks;
                    }
                }
  
                // Hm... we are grabbing this here... but it could change while we are invoking
                // the operation...  maybe we should move this code to after the invoke?
                maxPriority = _queue.MaxPriority;
 
                // If there is more to do, request processing for it.
                RequestProcessing();
            }
 
            if(op != null)
            {
                boolean eventlogged = false;
  
                if (EventTrace.IsEnabled(org.summer.view.widget.documents.performance, EventTrace.Level.normal))
                {
                    EventTrace.EventProvider.TraceEvent(EventTrace.GuidFromId(EventTraceGuidId.DISPATCHERDISPATCHGUID), MS.Utility.EventType.StartEvent, op.Priority, op.Name);
                    eventlogged = true;
                }
  
                op.Invoke();
  
                if(hooks != null)
                {
                    hooks.RaiseOperationCompleted(this, op);
                }
 
                if (eventlogged)
                {
                    EventTrace.EventProvider.TraceEvent(EventTrace.GuidFromId(EventTraceGuidId.DISPATCHERDISPATCHGUID), MS.Utility.EventType.EndEvent);
  
                    if (_idlePriorityRange.Contains(maxPriority))
                    {
                        EventTrace.EventProvider.TraceEvent(EventTrace.GuidFromId(EventTraceGuidId.DISPATCHERIDLEGUID), MS.Utility.EventType.Info);
                    }
                }
 
            }
        }
  
        /*internal*/ public  delegate void ShutdownCallback();
 
        ///<securitynote>
        /// Critical - it calls critical methods (StartShutdownImpl). it can initiate a shutdown process, disabled
        /// in partial trust.
        ///</securitynote>
//        [SecurityCritical]
        private void ShutdownCallbackInternal()
        {
            StartShutdownImpl();
        }
  
        //<securitynote>
        // Critical - as this calls critical methods (GetMessage, TranslateMessage, DispatchMessage).
        // TreatAsSafe - as the critical method is not leaked out, and not controlled by external inputs.
        //</securitynote>
//        [SecurityCritical, SecurityTreatAsSafe ]
        private void PushFrameImpl(DispatcherFrame frame)
        {
            SynchronizationContext oldSyncContext = null;
            SynchronizationContext newSyncContext = null;
            MSG msg = new MSG();
 
            _frameDepth++;
            try
            {
                // Change the CLR SynchronizationContext to be compatable with our Dispatcher.
                oldSyncContext = SynchronizationContext.Current;
                newSyncContext = new DispatcherSynchronizationContext(this);
                SynchronizationContext.SetSynchronizationContext(newSyncContext);
 
                try
                {
                    while(frame.Continue)
                    {
                        if (!GetMessage(/*ref*/ msg, IntPtr.Zero, 0, 0))
                            break;
 
                        TranslateAndDispatchMessage(/*ref*/ msg);
                    }
 
                    // If this was the last frame to exit after a quit, we
                    // can now dispose the dispatcher.
                    if(_frameDepth == 1)
                    {
                        if(_hasShutdownStarted)
                        {
                            ShutdownImpl();
                        }
                    }
                }
                finally
                {
                    // Restore the old SynchronizationContext.
                    SynchronizationContext.SetSynchronizationContext(oldSyncContext);
                }
            }
            finally
            {
                _frameDepth--;
                if(_frameDepth == 0)
                {
                    // We have exited all frames.
                    _exitAllFrames = false;
                }
            }
        }
 
        //<securitynote>
        // SecurityCritical - as this does unsafe operations.
        //</securitynote>
//        [SecurityCritical]
        private boolean GetMessage(/*ref*/ MSG msg, IntPtr hwnd, int minMessage, int maxMessage)
        {
            // If Any TextServices for Cicero is not installed GetMessagePump() returns null.
            // If TextServices are there, we can get ITfMessagePump and have to use it instead of
            // Win32 GetMessage().
            boolean result;
            UnsafeNativeMethods.ITfMessagePump messagePump = GetMessagePump();
            try
            {
                if (messagePump == null)
                {
                    // We have foreground items to process.
                    // By posting a message, Win32 will service us fairly promptly.
                    result = UnsafeNativeMethods.GetMessageW(/*ref*/ msg,
                                                             new HandleRef(this, hwnd),
                                                             minMessage,
                                                             maxMessage);
                }
                else
                {
                    messagePump.GetMessageW(
                    	/*ref*/ msg,
                        (int)(IntPtr)hwnd,
                        (int)minMessage,
                        (int)maxMessage,
                        /*out*/ result);
                }
            }
            finally
            {
                if (messagePump != null) Marshal.ReleaseComObject(messagePump);
            }
 
            return result;
        }
  
        //  Get ITfMessagePump interface from Cicero.
        /// <securitynote>
        /// Critical - calls critical code, created objects deal with raw input
        /// </securitynote>
//        [SecurityCritical]
        private static UnsafeNativeMethods.ITfMessagePump GetMessagePump()
        {
            UnsafeNativeMethods.ITfMessagePump messagePump = null;
  
            // If the current thread is not STA, Cicero just does not work.
            // Probably this Dispatcher is running for worker thread.
            if (Thread.CurrentThread.GetApartmentState() == ApartmentState.STA)
            {
                // If there is no text services, we don't have to use ITfMessagePump.
                if (TextServicesLoader.ServicesInstalled)
                {
                    UnsafeNativeMethods.ITfThreadMgr threadManager;
                    threadManager = TextServicesLoader.Load();
  
                    // ThreadManager does not exist. No MessagePump yet.
                    if (threadManager != null)
                    {
                        // QI ITfMessagePump.
                        messagePump = threadManager as UnsafeNativeMethods.ITfMessagePump;
                    }
                }
            }
            return messagePump;
        }
  
        //<securitynote>
        // SecurityCritical - as this does unsafe operations.
        //</securitynote>
//        [SecurityCritical]
        private void TranslateAndDispatchMessage(/*ref*/ MSG msg)
        {
            boolean handled = false;
 
            handled = ComponentDispatcher.RaiseThreadMessage(/*ref*/ msg);
 
            if(!handled)
            {
                UnsafeNativeMethods.TranslateMessage(/*ref*/ msg);
                UnsafeNativeMethods.DispatchMessage(/*ref*/ msg);
            }
        }
 
        //<securitynote>
        //  Critical - as it accesses security critical data ( window handle)
        //</securitynote>
//        [SecurityCritical]
        private IntPtr WndProcHook(IntPtr hwnd, int msg, IntPtr wParam, IntPtr lParam, /*ref*/ boolean handled)
        {
            if(_disableProcessingCount > 0)
            {
                throw new InvalidOperationException(SR.Get(SRID.DispatcherProcessingDisabledButStillPumping));
            }
 
            if(msg == NativeMethods.WM_DESTROY)
            {
                if(!_hasShutdownStarted && !_hasShutdownFinished) // Dispatcher thread - no lock needed for read
                {
                    // Aack!  We are being torn down rudely!  Try to
                    // shut the dispatcher down as nicely as we can.
                    ShutdownImpl();
                }
            }
            else if(msg == _msgProcessQueue)
            {
                ProcessQueue();
            }
            else if(msg == NativeMethods.WM_TIMER && (int) wParam == 1)
            {
                // We want 1-shot only timers.  So stop the timer
                // that just fired.
                SafeNativeMethods.KillTimer(new HandleRef(this, hwnd), 1);
 
                ProcessQueue();
            }
            else if(msg == NativeMethods.WM_TIMER && (int) wParam == 2)
            {
                // We want 1-shot only timers.  So stop the timer
                // that just fired.
                KillWin32Timer();
 
                PromoteTimers(Environment.TickCount);
            }
  
            // We are about to return to the OS.  If there is nothing left
            // to do in the queue, then we will effectively go to sleep.
            // This is the condition that means Idle.
            DispatcherHooks hooks = null;
            boolean idle = false;
 
            /*lock*/synchronized(_instanceLock)
            {
                idle = (_postedProcessingType < PROCESS_BACKGROUND);
                if (idle)
                {
                    hooks = _hooks;
                }
            }
 
            if (idle)
            {
                if(hooks != null)
                {
                    hooks.RaiseDispatcherInactive(this);
                }
 
                ComponentDispatcher.RaiseIdle();
            }
 
            return IntPtr.Zero ;
        }
 
        ///<securitynote>
        ///     SecurityCritical - as this code performs an elevation.
        ///     TreatAsSafe - this method returns "I have input that can be processed".
        ///                          equivalent to saying a 'key has been hit'.
        ///                          Considered safe.
        ///</securitynote>
//        [SecurityCritical, SecurityTreatAsSafe ]
        private boolean IsInputPending()
        {
            int retVal = 0;
  
            // We need to know if there is any pending input in the Win32
            // queue because we want to only process Avalon "background"
            // items after Win32 input has been processed.
            //
            // Win32 provides the GetQueueStatus API -- but it has a major
            // drawback: it only counts "new" input.  This means that
            // sometimes it could return false, even if there really is input
            // that needs to be processed.  This results in very hard to
            // find bugs.
            //
            // Luckily, Win32 also provides the MsgWaitForMultipleObjectsEx
            // API.  While more awkward to use, this API can return queue
            // status information even if the input is "old".  The various
            // flags we use are:
            //
            // QS_INPUT
            // This represents any pending input - such as mouse moves, or
            // key presses.  It also includes the new GenericInput messages.
            //
            // QS_EVENT
            // This is actually a private flag that represents the various
            // events that can be queued in Win32.  Some of these events
            // can cause input, but Win32 doesn't include them in the
            // QS_INPUT flag.  An example is WM_MOUSELEAVE.
            //
            // QS_POSTMESSAGE
            // If there is already a message in the queue, we need to process
            // it before we can process input.
            //
            // MWMO_INPUTAVAILABLE
            // This flag indicates that any input (new or old) is to be
            // reported.
            //
            retVal = UnsafeNativeMethods.MsgWaitForMultipleObjectsEx(0, null, 0,
                                                                     NativeMethods.QS_INPUT |
                                                                     NativeMethods.QS_EVENT |
                                                                     NativeMethods.QS_POSTMESSAGE,
                                                                     NativeMethods.MWMO_INPUTAVAILABLE);
 
            return retVal == 0;
        }
  
        private boolean RequestProcessing() // NOTE: should be Priority
        {
            boolean succeeded = true;
 
            // This method is called from within the instance lock.  So we
            // can reliably check the _window field without worrying about
            // it being changed out from underneath us during shutdown.
            if (IsWindowNull())
                return false;
 
            DispatcherPriority priority = _queue.MaxPriority;
  
            if(priority != DispatcherPriority.Invalid &&  // Nothing. NOTE: should be Priority.Invalid
               priority != DispatcherPriority.Inactive)        // Not processed. NOTE: should be Priority.Min
            {
                if(_foregroundPriorityRange.Contains(priority))
                {
                    succeeded = RequestForegroundProcessing();
                }
                else
                {
                    succeeded = RequestBackgroundProcessing();
                }
            }
 
            return succeeded;
        }
 
        /// <securitynote>
        ///   Critical: This code accesses window
        ///   TreatAsSafe: This code is ok to call since it does not expose the resource
        /// </securitynote>
//        [SecurityCritical,SecurityTreatAsSafe]
        private boolean IsWindowNull()
        {
           if(_window.Value == null)
            {
                return true;
            }
            return false;
        }
 
        //<securitynote>
        // Critical as it access critical data - for the window handle.
        // TreatAsSafe - as this is a request to do queued work. Analogous to VB's DoEvents()
        //</securitynote>
//        [SecurityCritical, SecurityTreatAsSafe]
        private boolean RequestForegroundProcessing()
        {
            if(_postedProcessingType < PROCESS_FOREGROUND)
            {
                // If we have already set a timer to do background processing,
                // make sure we stop it before posting a message for foreground
                // processing.
                if(_postedProcessingType == PROCESS_BACKGROUND)
                {
                    SafeNativeMethods.KillTimer(new HandleRef(this, _window.Value.Handle), 1);
                }
  
                _postedProcessingType = PROCESS_FOREGROUND;
 
                // We have foreground items to process.
                // By posting a message, Win32 will service us fairly promptly.
                return UnsafeNativeMethods.TryPostMessage(new HandleRef(this, _window.Value.Handle), _msgProcessQueue, IntPtr.Zero, IntPtr.Zero);
            }
  
            return true;
        }
 
        //<securitynote>
        //  Critical - as it accesses critical data - to get the Window Handle.
        //  TreatAsSafe - as this method would be ok to expose publically, this is just a request for Timer processing.
        //</securitynote>
//        [SecurityCritical, SecurityTreatAsSafe]
        private boolean RequestBackgroundProcessing()
        {
            boolean succeeded = true;
 
            if(_postedProcessingType < PROCESS_BACKGROUND)
            {
                // If there is Win32 input pending, we can't do any background
                // processing until it is done.  We use a short timer to
                // get processing time after the input.
                if(IsInputPending())
                {
                    _postedProcessingType = PROCESS_BACKGROUND;
 
                    succeeded = SafeNativeMethods.TrySetTimer(new HandleRef(this, _window.Value.Handle), 1, 1);
                }
                else
                {
                    succeeded = RequestForegroundProcessing();
                }
            }
  
            return succeeded;
        }
 
        private void PromoteTimers(int currentTimeInTicks)
        {
            try
            {
                List<dispatchertimer> timers = null;
                long timersVersion = 0;
 
                /*lock*/synchronized(_instanceLock)
                {
                    if(!_hasShutdownFinished) // Could be a non-dispatcher thread, lock to read
                    {
                        if(_dueTimeFound && _dueTimeInTicks - currentTimeInTicks <= 0)
                        {
                            timers = _timers;
                            timersVersion = _timersVersion;
                        }
                    }
                }
  
                if(timers != null)
                {
                    DispatcherTimer timer = null;
                    int iTimer = 0;
  
                    do
                    {
                    	/*lock*/synchronized(_instanceLock)
                        {
                            timer = null;
  
                            // If the timers collection changed while we are in the middle of
                            // looking for timers, start over.
                            if(timersVersion != _timersVersion)
                            {
                                timersVersion = _timersVersion;
                                iTimer = 0;
                            }
 
                            while(iTimer < _timers.Count)
                            {
                                // WARNING: this is vulnerable to wrapping
                                if(timers[iTimer]._dueTimeInTicks - currentTimeInTicks <= 0)
                                {
                                    // Remove this timer from our list.
                                    // Do not increment the index.
                                    timer = timers[iTimer];
                                    timers.RemoveAt(iTimer);
                                    break;
                                }
                                else
                                {
                                    iTimer++;
                                }
                            }
                        }
 
                        // Now that we are outside of the lock, promote the timer.
                        if(timer != null)
                        {
                            timer.Promote();
                        }
                    } while(timer != null);
 
                }
            }
            finally
            {
                UpdateWin32Timer();
            }
        }
 
        /*internal*/ public  void AddTimer(DispatcherTimer timer)
        {
        	/*lock*/synchronized(_instanceLock)
            {
                if(!_hasShutdownFinished) // Could be a non-dispatcher thread, lock to read
                {
                    _timers.Add(timer);
                    _timersVersion++;
                }
            }
            UpdateWin32Timer();
        }
  
        /*internal*/ public  void RemoveTimer(DispatcherTimer timer)
        {
        	/*lock*/synchronized(_instanceLock)
            {
                if(!_hasShutdownFinished) // Could be a non-dispatcher thread, lock to read
                {
                    _timers.Remove(timer);
                    _timersVersion++;
                }
            }
            UpdateWin32Timer();
        }
 
        /*internal*/ public  void UpdateWin32Timer() // Called from DispatcherTimer
        {
            if(CheckAccess())
            {
                UpdateWin32TimerFromDispatcherThread(null);
            }
            else
            {
                BeginInvoke(DispatcherPriority.Send,
                            new DispatcherOperationCallback(UpdateWin32TimerFromDispatcherThread),
                            null);
            }
        }
  
        private Object UpdateWin32TimerFromDispatcherThread(Object unused)
        {
  
        	/*lock*/synchronized(_instanceLock)
            {
                if(!_hasShutdownFinished) // Dispatcher thread, does not technically need the lock to read
                {
                    boolean oldDueTimeFound = _dueTimeFound;
                    int oldDueTimeInTicks = _dueTimeInTicks;
                    _dueTimeFound = false;
                    _dueTimeInTicks = 0;
  
                    if(_timers.Count > 0)
                    {
                        // We could do better if we sorted the list of timers.
                        for(int i = 0; i < _timers.Count; i++)
                        {
                            DispatcherTimer timer = _timers[i];
  
                            if(!_dueTimeFound || timer._dueTimeInTicks - _dueTimeInTicks < 0)
                            {
                                _dueTimeFound = true;
                                _dueTimeInTicks = timer._dueTimeInTicks;
                            }
                        }
                    }
  
                    if(_dueTimeFound)
                    {
                        if(!_isWin32TimerSet || !oldDueTimeFound || (oldDueTimeInTicks != _dueTimeInTicks))
                        {
                            SetWin32Timer(_dueTimeInTicks);
                        }
                    }
                    else if(oldDueTimeFound)
                    {
                        KillWin32Timer();
                    }
                }
            }
 
            return null;
        }
 
        ///<securitynote>
        /// Critical - accesses critical data
        /// TreatAsSafe - we think it's ok to expose timers in the SEE.
        ///                      a denial-of-service attack may be possible - but these are low-pri and possible in many other ways.
        ///                      we can never bring down the iexplore process.
        ///</securitynote>
//        [SecurityCritical, SecurityTreatAsSafe]
        private void SetWin32Timer(int dueTimeInTicks)
        {
            if(!IsWindowNull())
            {
                int delta = dueTimeInTicks - Environment.TickCount;
                if(delta < 1)
                {
                    delta = 1;
                }
  
                // We are being called on the dispatcher thread so we can rely on
                // _window.Value being non-null without taking the instance lock.
  
                SafeNativeMethods.SetTimer(
                    new HandleRef(this, _window.Value.Handle),  // this HWND
                    2,                                          // win32 timer #2
                    delta);                                      // unused timer proc
 
                _isWin32TimerSet = true;
            }
        }
  
        ///<securitycritical>
        /// Critical - accesses critical data _window.Value.Handle
        /// TreatAsSafe - OK to stop a dispatcher timer.
        ///</securitycritical>
//        [SecurityCritical, SecurityTreatAsSafe]
        private void KillWin32Timer()
        {
            if(!IsWindowNull())
            {
                // We are being called on the dispatcher thread so we can rely on
                // _window.Value being non-null without taking the instance lock.
 
                SafeNativeMethods.KillTimer(
                    new HandleRef(this, _window.Value.Handle),  // this HWND
                    2);                                         // win32 timer #2
 
                _isWin32TimerSet = false;
            }
        }
  
        // Exception filter returns true if exception should be caught.
        /// <securitynote>
        ///     Critical: calls ExceptionFilter, which is critical
        /// </securitynote>
//        [SecurityCritical]
        private static boolean ExceptionFilterStatic(Object source, Exception e)
        {
            Dispatcher d = (Dispatcher)source;
            return d.ExceptionFilter(e);
        }
 
        /// <securitynote>
        ///     Critical: accesses _unhandledExceptionFilter
        /// </securitynote>
//        [SecurityCritical]
        private boolean ExceptionFilter(Exception e)
        {
            // see whether this dispatcher has already seen the exception.
            // This can happen when the dispatcher is re-entered via
            // PushFrame (or similar).
            if (!e.Data.Contains(ExceptionDataKey))
            {
                // first time we've seen this exception - add data to the exception
                e.Data.Add(ExceptionDataKey, null);
            }
            else
            {
                // we've seen this exception before - don't catch it
                return false;
            }
  
            // By default, Request catch if there's anyone signed up to catch it;
            boolean requestCatch = HasUnhandledExceptionHandler;
  
            // The app can hook up an ExceptionFilter to avoid catching it.
            // ExceptionFilter will run REGARDLESS of whether there are exception handlers.
            if (_unhandledExceptionFilter != null)
            {
                // The default requestCatch value that is passed in the args
                // should be returned unchanged if filters don't set them explicitly.
                _exceptionFilterEventArgs.Initialize(e, requestCatch);
                boolean bSuccess = false;
                try
                {
                    _unhandledExceptionFilter(this, _exceptionFilterEventArgs);
                    bSuccess = true;
                }
                finally
                {
                    if (bSuccess)
                    {
                        requestCatch = _exceptionFilterEventArgs.RequestCatch;
                    }
  
                    // For bSuccess is false,
                    // To be in line with default behavior of structured exception handling,
                    // we would want to set requestCatch to false, however, this means one
                    // poorly programmed filter will knock out all dispatcher exception handling.
                    // If an exception filter fails, we run with whatever value is set thus far.
                }
            }
 
            return requestCatch;
        }
 
        // This returns false when caller should rethrow the exception.
        // true means Exception is "handled" and things just continue on.
        private static boolean CatchExceptionStatic(Object source, Exception e)
        {
            Dispatcher dispatcher = (Dispatcher)source;
            return dispatcher.CatchException(e);
        }
 
        // The exception filter called for catching an unhandled exception.
        private boolean CatchException(Exception e)
        {
            boolean handled = false;
  
            if (UnhandledException != null)
            {
                _unhandledExceptionEventArgs.Initialize(e, false);
 
                boolean bSuccess = false;
                try
                {
                    UnhandledException(this, _unhandledExceptionEventArgs);
                    handled = _unhandledExceptionEventArgs.Handled;
                    bSuccess = true;
                }
                finally
                {
                    if (!bSuccess)
                        handled = false;
                }
 
            }
  
            return(handled);
        }
 
        // This is called by DRT (via reflection) to see if there is a UnhandledException handler.
        private boolean HasUnhandledExceptionHandler
        {
            get { return (UnhandledException != null); }
        }
  
        /*internal*/ public  Object WrappedInvoke(Delegate callback, Object args, boolean isSingleParameter)
        {
            return WrappedInvoke(callback, args, isSingleParameter, null);
        }
 
//        [FriendAccessAllowed] //used by ResourceReferenceExpression in PresentationFramework
        /*internal*/ public  Object WrappedInvoke(Delegate callback, Object args, boolean isSingleParameter, Delegate catchHandler)
        {
            // Win32 considers timers to be low priority.  Avalon does not, since different timers
            // are associated with different priorities.  So we promote the timers before we
            // dequeue any work items.
            PromoteTimers(Environment.TickCount);
            return _exceptionWrapper.TryCatchWhen(this, callback, args, isSingleParameter, catchHandler);
        }
  
        private Object[] CombineParameters(Object arg, Object[] args)
        {
            Object[] parameters = new Object[1 + (args == null ? 1 : args.Length)];
            parameters[0] = arg;
            if (args != null)
            {
                Array.Copy(args, 0, parameters, 1, args.Length);
            }
            else
            {
                parameters[1] = null;
            }
 
            return parameters;
        }
 
        private const int PROCESS_NONE = 0;
        private const int PROCESS_BACKGROUND = 1;
        private const int PROCESS_FOREGROUND = 2;
  
        private static List<weakreference> _dispatchers;
        private static WeakReference _possibleDispatcher;
        private static Object _globalLock;
 
//        [ThreadStatic]
        private static Dispatcher _tlsDispatcher;      // use TLS for ownership only
  
        private Thread _dispatcherThread;
  
        private int _frameDepth;
        /*internal*/ public  boolean _exitAllFrames;       // used from DispatcherFrame
        private boolean _startingShutdown;
        /*internal*/ public  boolean _hasShutdownStarted;  // used from DispatcherFrame
        private SecurityCriticalDataClass<executioncontext> _shutdownExecutionContext;
 
        /*internal*/ public  int _disableProcessingCount; // read by DispatcherSynchronizationContext, decremented by DispatcherProcessingDisabled
 
        //private static Priority _foregroundBackgroundBorderPriority = new Priority(Priority.Min, Priority.Max, "Dispatcher.ForegroundBackgroundBorder");
        //private static Priority _backgroundIdleBorderPriority = new Priority(Priority.Min, _foregroundBackgroundBorderPriority, "Dispatcher.BackgroundIdleBorder");
 
        //private static Priority _foregroundPriority = new Priority(_foregroundBackgroundBorderPriority, Priority.Max, "Dispatcher.Foreground");
        //private static Priority _backgroundPriority = new Priority(_backgroundIdleBorderPriority, _foregroundBackgroundBorderPriority, "Dispatcher.Background");
        //private static Priority _idlePriority = new Priority(Priority.Min, _backgroundIdleBorderPriority, "Dispatcher.Idle");
 
        //private static PriorityRange _foregroundPriorityRange = new PriorityRange(_foregroundBackgroundBorderPriority, false, Priority.Max, true);
        //private static PriorityRange _backgroundPriorityRange = new PriorityRange(_backgroundIdleBorderPriority, false, _foregroundBackgroundBorderPriority, false);
        //private static PriorityRange _idlePriorityRange = new PriorityRange(Priority.Min, false, _backgroundIdleBorderPriority, false);
  
        private static PriorityRange _foregroundPriorityRange = new PriorityRange(DispatcherPriority.Loaded, true, DispatcherPriority.Send, true);
        private static PriorityRange _backgroundPriorityRange = new PriorityRange(DispatcherPriority.Background, true, DispatcherPriority.Input, true);
        private static PriorityRange _idlePriorityRange = new PriorityRange(DispatcherPriority.SystemIdle, true, DispatcherPriority.ContextIdle, true);
 
        private SecurityCriticalData<messageonlyhwndwrapper> _window;
        private HwndWrapperHook _hook;
 
        private int _postedProcessingType;
        /// <securitynote>
        ///     Critical: This code gets set by RegisterWindowMessage which is under an elevation.
        /// </securitynote>
//        [SecurityCritical]
        private static int _msgProcessQueue;
 
        private static ExceptionWrapper _exceptionWrapper;
        private static readonly Object ExceptionDataKey = new Object();
  
        // Preallocated arguments for exception handling.
        // This helps avoid allocations in the handler code, a potential
        // source of secondary exceptions (i.e. in Out-Of-Memory cases).
        private DispatcherUnhandledExceptionEventArgs _unhandledExceptionEventArgs;
 
        /// <securitynote>
        ///     Do not expose to partially trusted code.
        /// </securitynote>
//        [SecurityCritical]
        private DispatcherUnhandledExceptionFilterEventHandler _unhandledExceptionFilter;
        private DispatcherUnhandledExceptionFilterEventArgs _exceptionFilterEventArgs;
  
        private Object _reserved0;
        private Object _reserved1;
        private Object _reserved2;
        private Object _reserved3;
        private Object _reservedPtsCache;
        private Object _reservedInputMethod;
        private Object _reservedInputManager;
 
        private Object _instanceLock = new Object();
        private PriorityQueue<dispatcheroperation> _queue;
        private List<dispatchertimer> _timers = new List<dispatchertimer>();
        private long _timersVersion;
        private boolean _dueTimeFound;
        private int _dueTimeInTicks;
        private boolean _isWin32TimerSet;
  
        // This can be read from any thread, but only written by the dispatcher thread.
        // Dispatcher Thread - lock _instanceLock only on write
        // Non-Dispatcher Threads - lock _instanceLock on read
        private boolean _hasShutdownFinished;
 
        /// <securitynote>
        ///     Do not expose hooks to partial trust.
        /// </securitynote>
//        [SecurityCritical]
        private DispatcherHooks _hooks;
    }
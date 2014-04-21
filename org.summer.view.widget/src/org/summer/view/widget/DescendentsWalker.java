package org.summer.view.widget;


import org.summer.view.widget.collection.IEnumerator;
import org.summer.view.widget.collection.List;
import org.summer.view.widget.controls.primitives.Popup;
import org.summer.view.widget.media.Visual;
import org.summer.view.widget.media.Visual3D;
import org.summer.view.widget.media.VisualTreeHelper;
import org.summer.view.window.ContextLayoutManager;

/// <summary>
///     This class iterates and callsback for 
///     each descendent in a given subtree
/// </summary> 
/*internal*/ public class DescendentsWalker<T> extends DescendentsWalkerBase 
{
//    #region Construction 

    public DescendentsWalker(TreeWalkPriority priority, VisitedCallback<T> callback) 
    { 
        this(priority, callback, default(T));
        // Forwarding
    } 

    public DescendentsWalker(TreeWalkPriority priority, VisitedCallback<T> callback, T data)
    {
    	super(priority);
        _callback = callback;
        _data = data;
    } 

//    #endregion Construction 

    /// <summary>
    ///     Start Iterating through the current subtree 
    /// </summary>
    public void StartWalk(DependencyObject startNode)
    {
        // Don't skip starting node 
        StartWalk(startNode, false);
    } 

    /// <summary>
    ///     Start Iterating through the current subtree 
    /// </summary>
    public /*virtual*/ void StartWalk(DependencyObject startNode, boolean skipStartNode)
    {
        _startNode = startNode; 
        boolean continueWalk = true;

        if (!skipStartNode) 
        {
            if (FrameworkElement.DType.IsInstanceOfType(_startNode) || 
                FrameworkContentElement.DType.IsInstanceOfType(_startNode))
            {
                // Callback for the root of the subtree
                continueWalk = _callback(_startNode, _data, _priority == TreeWalkPriority.VisualTree); 
            }
        } 

        if (continueWalk)
        { 
            // Iterate through the children of the root
            IterateChildren(_startNode);
        }
    } 

    /// <summary> 
    ///     Given a DependencyObject, see if it's any of the types we know 
    /// to have children.  If so, call VisitNode on each of its children.
    /// </summary> 
    private void IterateChildren(DependencyObject d)
    {
        _recursionDepth++;

        if (FrameworkElement.DType.IsInstanceOfType(d))
        { 
            FrameworkElement fe = (FrameworkElement) d; 
            boolean hasLogicalChildren = fe.HasLogicalChildren;

            // FrameworkElement have both a visual and a logical tree.
            //  Sometimes we want to hit Visual first, sometimes Logical.

            if (_priority == TreeWalkPriority.VisualTree) 
            {
                WalkFrameworkElementVisualThenLogicalChildren(fe, hasLogicalChildren); 
            } 
            else if (_priority == TreeWalkPriority.LogicalTree)
            { 
                WalkFrameworkElementLogicalThenVisualChildren(fe, hasLogicalChildren);
            }
            else
            { 
//                Debug.Assert( false, "Tree walk priority should be Visual first or Logical first - but this instance of DescendentsWalker has an invalid priority setting that's neither of the two." );
            } 
        } 
        else if (FrameworkContentElement.DType.IsInstanceOfType(d))
        { 
            // FrameworkContentElement only has a logical tree, so we
            // Walk logical children
            FrameworkContentElement fce = d as FrameworkContentElement;
            if (fce.HasLogicalChildren) 
            {
                WalkLogicalChildren( null, fce, fce.LogicalChildren ); 
            } 
        }
        else 
        {
            // Neither a FrameworkElement nor FrameworkContentElement.  See
            //  if it's a Visual and if so walk the Visual collection
            Visual v = d as Visual; 
            if (v != null)
            { 
                WalkVisualChildren(v); 
            }
            else 
            {
                Visual3D v3D = d as Visual3D;
                if (v3D != null)
                { 
                    WalkVisualChildren(v3D);
                } 
            } 
        }

        _recursionDepth--;
    }

    /// <summary> 
    ///     Given a Object of type Visual, call VisitNode on each of its
    /// Visual children. 
    /// </summary> 
    private void WalkVisualChildren( Visual v )
    { 
        v.IsVisualChildrenIterationInProgress = true;

        try
        { 
            int count = v.InternalVisual2DOr3DChildrenCount;
            for(int i = 0; i < count; i++) 
            { 
                DependencyObject childVisual = v.InternalGet2DOr3DVisualChild(i);
                if (childVisual != null) 
                {
                    boolean visitedViaVisualTree = true;
                    VisitNode(childVisual, visitedViaVisualTree);
                } 
            }
        } 
        finally 
        {
            v.IsVisualChildrenIterationInProgress = false; 
        }
    }

    /// <summary> 
    ///     Given a Object of type Visual3D, call VisitNode on each of its
    ///     children. 
    /// </summary> 
    private void WalkVisualChildren( Visual3D v )
    { 
        v.IsVisualChildrenIterationInProgress = true;

        try
        { 
            int count = v.InternalVisual2DOr3DChildrenCount;
            for(int i = 0; i < count; i++) 
            { 
                DependencyObject childVisual = v.InternalGet2DOr3DVisualChild(i);
                if (childVisual != null) 
                {
                    boolean visitedViaVisualTree = true;
                    VisitNode(childVisual, visitedViaVisualTree);
                } 
            }
        } 
        finally 
        {
            v.IsVisualChildrenIterationInProgress = false; 
        }
    }

    /// <summary> 
    ///     Given an enumerator for Logical children, call VisitNode on each
    /// of the nodes in the enumeration. 
    /// </summary> 
    private void WalkLogicalChildren(
        FrameworkElement        feParent, 
        FrameworkContentElement fceParent,
        IEnumerator             logicalChildren )
    {
        if (feParent != null) 
            feParent.IsLogicalChildrenIterationInProgress = true;
        else 
            fceParent.IsLogicalChildrenIterationInProgress = true; 

        try 
        {
            if (logicalChildren != null)
            {
                while (logicalChildren.MoveNext()) 
                {
                    DependencyObject child = logicalChildren.Current as DependencyObject; 
                    if (child != null) 
                    {
                        boolean visitedViaVisualTree = false; 
                        VisitNode(child, visitedViaVisualTree);
                    }
                }
            } 
        }
        finally 
        { 
            if (feParent != null)
                feParent.IsLogicalChildrenIterationInProgress = false; 
            else
                fceParent.IsLogicalChildrenIterationInProgress = false;
        }
    } 

    /// <summary> 
    ///     FrameworkElement objects have both a visual and a logical tree. 
    /// This variant walks the visual children first
    /// </summary> 
    /// <remarks>
    ///     It calls the generic WalkVisualChildren, but doesn't call the
    /// generic WalkLogicalChildren because there are shortcuts we can take
    /// to be smarter than the generic logical children walk. 
    /// </remarks>
    private void WalkFrameworkElementVisualThenLogicalChildren( 
        FrameworkElement feParent, boolean hasLogicalChildren ) 
    {
        WalkVisualChildren( feParent ); 

        //
        // If a popup is attached to the framework element visit each popup node.
        // 
        List<Popup> registeredPopups = Popup.RegisteredPopupsField.GetValue(feParent);

        if (registeredPopups != null) 
        {
            for/*each*/ (Popup p : registeredPopups) 
            {
                boolean visitedViaVisualTree = false;
                VisitNode(p, visitedViaVisualTree);
            } 
        }

        feParent.IsLogicalChildrenIterationInProgress = true; 


        try
        {
            // Optimized variant of WalkLogicalChildren
            if (hasLogicalChildren) 
            {
                IEnumerator logicalChildren = feParent.LogicalChildren; 
                if (logicalChildren != null) 
                {
                    while (logicalChildren.MoveNext()) 
                    {
                        Object current = logicalChildren.Current;
                        FrameworkElement fe = current as FrameworkElement;
                        if (fe != null) 
                        {
                            // For the case that both parents are identical, this node should 
                            // have already been visited when walking through visual 
                            // children, hence we short-circuit here
                            if (VisualTreeHelper.GetParent(fe) != fe.Parent) 
                            {
                                boolean visitedViaVisualTree = false;
                                VisitNode(fe, visitedViaVisualTree);
                            } 
                        }
                        else 
                        { 
                            FrameworkContentElement fce = current as FrameworkContentElement;
                            if (fce != null) 
                            {
                                boolean visitedViaVisualTree = false;
                                VisitNode(fce, visitedViaVisualTree);
                            } 
                        }
                    } 
                } 
            }
        } 
        finally
        {
            feParent.IsLogicalChildrenIterationInProgress = false;
        } 
    }

    /// <summary> 
    ///     FrameworkElement objects have both a visual and a logical tree.
    /// This variant walks the logical children first 
    /// </summary>
    /// <remarks>
    ///     It calls the generic WalkLogicalChildren, but doesn't call the
    /// generic WalkVisualChildren because there are shortcuts we can take 
    /// to be smarter than the generic visual children walk.
    /// </remarks> 
    private void WalkFrameworkElementLogicalThenVisualChildren( 
        FrameworkElement feParent, boolean hasLogicalChildren)
    { 
        if (hasLogicalChildren)
        {
            WalkLogicalChildren( feParent, null, feParent.LogicalChildren );
        } 

        feParent.IsVisualChildrenIterationInProgress = true; 

        try
        { 
            // Optimized variant of WalkVisualChildren
            int count = feParent.InternalVisualChildrenCount;

            for(int i = 0; i < count; i++) 
            {
                Visual child = feParent.InternalGetVisualChild(i); 
                if (child != null && FrameworkElement.DType.IsInstanceOfType(child)) 
                {
                    // For the case that both parents are identical, this node should 
                    // have already been visited when walking through logical
                    // children, hence we short-circuit here
                    if (VisualTreeHelper.GetParent(child) != ((FrameworkElement) child).Parent)
                    { 
                        boolean visitedViaVisualTree = true;
                        VisitNode(child, visitedViaVisualTree); 
                    } 
                }
            } 
        }
        finally
        {
            feParent.IsVisualChildrenIterationInProgress = false; 
        }

        // 
        // If a popup is attached to the framework element visit each popup node.
        // 
        List<Popup> registeredPopups = Popup.RegisteredPopupsField.GetValue(feParent);

        if (registeredPopups != null)
        { 
            for/*each*/ (Popup p : registeredPopups)
            { 
                boolean visitedViaVisualTree = false; 
                VisitNode(p, visitedViaVisualTree);
            } 
        }

    }

    private void VisitNode(FrameworkElement fe, boolean visitedViaVisualTree)
    { 
        if (_recursionDepth <= ContextLayoutManager.s_LayoutRecursionLimit) 
        {
            // For the case when the collection contains the node 
            // being visted, we do not need to visit it again. Also
            // this node will not be visited another time because
            // any node can be reached at most two times, once
            // via its visual parent and once via its logical parent 

            int index = _nodes.IndexOf(fe); 

            // If index is not -1, then fe was in the list, remove it
            if (index != -1) 
            {
                _nodes.RemoveAt(index);
            }
            else 
            {
                // A node will be visited a second time only if it has 
                // different non-null logical and visual parents. 
                // Hence that is the only case that we need to
                // remember this node, to avoid duplicate callback for it 

                DependencyObject dependencyObjectParent = VisualTreeHelper.GetParent(fe);
                DependencyObject logicalParent = fe.Parent;
                if (dependencyObjectParent != null && logicalParent != null && dependencyObjectParent != logicalParent) 
                {
                    _nodes.Add(fe); 
                } 

                _VisitNode(fe, visitedViaVisualTree); 
            }
        }
        else
        { 
            // We suspect a loop here because the recursion
            // depth has exceeded the MAX_TREE_DEPTH expected 
            throw new InvalidOperationException(/*SR.Get(SRID.LogicalTreeLoop)*/); 
        }
    } 

    private void VisitNode(DependencyObject d, boolean visitedViaVisualTree)
    {
        if (_recursionDepth <= ContextLayoutManager.s_LayoutRecursionLimit) 
        {
            if (FrameworkElement.DType.IsInstanceOfType(d)) 
            { 
                VisitNode((FrameworkElement)d , visitedViaVisualTree);
            } 
            else if (FrameworkContentElement.DType.IsInstanceOfType(d))
            {
                _VisitNode(d, visitedViaVisualTree);
            } 
            else
            { 
                // Iterate through the children of this node 
                IterateChildren(d);
            } 
        }
        else
        {
            // We suspect a loop here because the recursion 
            // depth has exceeded the MAX_TREE_DEPTH expected
            throw new InvalidOperationException(/*SR.Get(SRID.LogicalTreeLoop)*/); 
        } 

    } 

    protected /*virtual*/ void _VisitNode(DependencyObject d, boolean visitedViaVisualTree)
    {
        // Callback for this node of the subtree 
        boolean continueWalk = _callback(d, _data, visitedViaVisualTree);
        if (continueWalk) 
        { 
            // Iterate through the children of this node
            IterateChildren(d); 
        }
    }

    protected T Data 
    {
        get 
        { 
            return _data;
        } 
    }

    private VisitedCallback<T> _callback;
    private T _data; 
}

/// <summary> 
///     Callback for each visited node
/// </summary> 
///*internal*/ public delegate boolean VisitedCallback<T>(DependencyObject d, T data, boolean visitedViaVisualTree);
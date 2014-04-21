package org.summer.view.widget.collection;

// Implements a variable-size List that uses an array of objects to store the
// elements. A List has a capacity, which is the allocated length
// of the /*internal*/ public  array. As elements are added to a List, the capacity
// of the List is automatically increased as required by reallocating the
// /*internal*/ public  array.
//
//    [DebuggerTypeProxy(typeof(Mscorlib_CollectionDebugView<>))]
//    [DebuggerDisplay("Count = {Count}")]
//    [Serializable()]

public class List<t> implements IList
{
    private final int _defaultCapacity = 4;

    private T[] _items;
    private int _size;
    private int _version;
//    [NonSerialized]
    private Object _syncRoot;

    static T[]  _emptyArray = new T[0];

    // Constructs a List. The list is initially empty and has a capacity
    // of zero. Upon adding the first element to the list the capacity is
    // increased to 16, and then increased in multiples of two as required.
    public List() {
        _items = _emptyArray;
    }

    // Constructs a List with a given initial capacity. The list is
    // initially empty, but will have room for the given number of elements
    // before any reallocations are required.
    //
    public List(int capacity) {
        if (capacity < 0) ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.capacity, ExceptionResource.ArgumentOutOfRange_SmallCapacity);
        _items = new T[capacity];
    }

    // Constructs a List, copying the contents of the given collection. The
    // size and capacity of the new list will both be equal to the size of the
    // given collection.
    //
    public List(IEnumerable collection) {
        if (collection==null)
            ThrowHelper.ThrowArgumentNullException(ExceptionArgument.collection);

        ICollection c = collection as ICollection<t>;
        if( c != null) {
            int count = c.Count;
            _items = new T[count];
            c.CopyTo(_items, 0);
            _size = count;
        }
        else {
            _size = 0;
            _items = new T[_defaultCapacity];

            using(IEnumerator<t> en = collection.GetEnumerator()) {
                while(en.MoveNext()) {
                    Add(en.Current);
                }
            }
        }
    }

    // Gets and sets the capacity of this list.  The capacity is the size of
    // the /*internal*/ public  array used to hold items.  When set, the /*internal*/ public 
    // array of the list is reallocated to the given capacity.
    //
    public int Capacity {
        get { return _items.Length; }
        set {
            if (value != _items.Length) {
                if (value < _size) {
                    ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.value, ExceptionResource.ArgumentOutOfRange_SmallCapacity);
                }

                if (value > 0) {
                    T[] newItems = new T[value];
                    if (_size > 0) {
                        Array.Copy(_items, 0, newItems, 0, _size);
                    }
                    _items = newItems;
                }
                else {
                    _items = _emptyArray;
                }
            }
        }
    }

    // Read-only property describing how many elements are in the List.
    public int Count {
        get { return _size; }
    }

    boolean IList.IsFixedSize {
        get { return false; }
    }


    // Is this List read-only?
    boolean ICollection<t>.IsReadOnly {
        get { return false; }
    }

    boolean IList.IsReadOnly {
        get { return false; }
    }

    // Is this List synchronized (thread-safe)?
    boolean ICollection.IsSynchronized {
        get { return false; }
    }

    // Synchronization root for this object.
    Object ICollection.SyncRoot {
        get {
            if( _syncRoot == null) {
                System.Threading.Interlocked.CompareExchange(ref _syncRoot, new Object(), null);
            }
            return _syncRoot;
        }
    }
    // Sets or Gets the element at the given index.
    //
    public T this[int index] {
        get {
            // Fllowing trick can reduce the range check by one
            if ((uint) index >= (uint)_size) {
                ThrowHelper.ThrowArgumentOutOfRangeException();
            }
            return _items[index];
        }
        set {
            if ((uint) index >= (uint)_size) {
                ThrowHelper.ThrowArgumentOutOfRangeException();
            }
            _items[index] = value;
            _version++;
        }
    }

    private static boolean IsCompatibleObject(object value) {
        if( (value is T) || ( value == null && !typeof(T).IsValueType) ) {
            return true;
        }
        return false;
    }

    private static void VerifyValueType(object value) {
        if( !IsCompatibleObject(value)) {
            ThrowHelper.ThrowWrongValueTypeArgumentException(value, typeof(T));
        }
    }

    Object IList.this[int index] {
        get {
            return this[index];
        }
        set {
            VerifyValueType(value);
            this[index] = (T) value;
        }
    }

    // Adds the given object to the end of this list. The size of the list is
    // increased by one. If required, the capacity of the list is doubled
    // before adding the new element.
    //
    public void Add(T item) {
        if (_size == _items.Length) EnsureCapacity(_size + 1);
        _items[_size++] = item;
        _version++;
    }

    public int /*IList.*/Add(Object item)
    {
        VerifyValueType(item);
        Add((T) item);
        return Count - 1;
    }


    // Adds the elements of the given collection to the end of this list. If
    // required, the capacity of the list is increased to twice the previous
    // capacity or the new size, whichever is larger.
    //
    public void AddRange(IEnumerable collection) {
        InsertRange(_size, collection);
    }

    public ReadOnlyCollection<t> AsReadOnly() {
        return new ReadOnlyCollection<t>(this);
    }

    // Searches a section of the list for a given element using a binary search
    // algorithm. Elements of the list are compared to the search value using
    // the given IComparer interface. If comparer is null, elements of
    // the list are compared to the search value using the IComparable
    // interface, which in that case must be implemented by all elements of the
    // list and the given search value. This method assumes that the given
    // section of the list is already sorted; if this is not the case, the
    // result will be incorrect.
    //
    // The method returns the index of the given value in the list. If the
    // list does not contain the given value, the method returns a negative
    // integer. The bitwise complement operator (~) can be applied to a
    // negative result to produce the index of the first element (if any) that
    // is larger than the given search value. This is also the index at which
    // the search value should be inserted into the list in order for the list
    // to remain sorted.
    //
    // The method uses the Array.BinarySearch method to perform the
    // search.
    //
    public int BinarySearch(int index, int count, T item, IComparer<t> comparer) {
        if (index < 0 || count < 0)
            ThrowHelper.ThrowArgumentOutOfRangeException( (index<0 ? ExceptionArgument.index : ExceptionArgument.count), ExceptionResource.ArgumentOutOfRange_NeedNonNegNum);
        if (_size - index < count)
            ThrowHelper.ThrowArgumentException(ExceptionResource.Argument_InvalidOffLen);

        return Array.BinarySearch<t>(_items, index, count, item, comparer);
    }

    public int BinarySearch(T item)
    {
        return BinarySearch(0,Count,item, null);
    }

    public int BinarySearch(T item, IComparer<t> comparer)
    {
        return BinarySearch(0,Count,item,comparer);
    }


    // Clears the contents of List.
    public void Clear() {
        if (_size > 0)
        {
            Array.Clear(_items, 0, _size); // Don't need to doc this but we clear the elements so that the gc can reclaim the references.
            _size = 0;
        }
        _version++;
    }

    // Contains returns true if the specified element is in the List.
    // It does a linear, O(n) search.  Equality is determined by calling
    // item.Equals().
    //
    public boolean Contains(T item) {
        if ((Object) item == null) {
            for(int i=0; i<_size; i++)
                if ((Object) _items[i] == null)
                    return true;
            return false;
        }
        else {
            EqualityComparer<t> c = EqualityComparer<t>.Default;
            for(int i=0; i<_size; i++) {
                if (c.Equals(_items[i], item)) return true;
            }
            return false;
        }
    }

    boolean IList.Contains(Object item)
    {
        if(IsCompatibleObject(item)) {
            return Contains((T) item);
        }
        return false;
    }

    public List<toutput> ConvertAll<toutput>(Converter<t,toutput> converter) {
        if( converter == null) {
            ThrowHelper.ThrowArgumentNullException(ExceptionArgument.converter);
        }

        List<toutput> list = new List<toutput>(_size);
        for( int i = 0; i< _size; i++) {
            list._items[i] = converter(_items[i]);
        }
        list._size = _size;
        return list;
    }

    // Copies this List into array, which must be of a
    // compatible array type.
    //
    public void CopyTo(T[] array) {
        CopyTo(array, 0);
    }

    // Copies this List into array, which must be of a
    // compatible array type.
    //
    void System.Collections.ICollection.CopyTo(Array array, int arrayIndex) {
        if ((array != null) && (array.Rank != 1)) {
            ThrowHelper.ThrowArgumentException(ExceptionResource.Arg_RankMultiDimNotSupported);
        }

        try {
            // Array.Copy will check for NULL.
            Array.Copy(_items, 0, array, arrayIndex, _size);
        }
        catch(ArrayTypeMismatchException){
            ThrowHelper.ThrowArgumentException(ExceptionResource.Argument_InvalidArrayType);
        }
    }

    // Copies a section of this list to the given array at the given index.
    //
    // The method uses the Array.Copy method to copy the elements.
    //
    public void CopyTo(int index, T[] array, int arrayIndex, int count) {
        if (_size - index < count) {
            ThrowHelper.ThrowArgumentException(ExceptionResource.Argument_InvalidOffLen);
        }

        // Delegate rest of error checking to Array.Copy.
        Array.Copy(_items, index, array, arrayIndex, count);
    }

    public void CopyTo(T[] array, int arrayIndex) {
        // Delegate rest of error checking to Array.Copy.
        Array.Copy(_items, 0, array, arrayIndex, _size);
    }

    // Ensures that the capacity of this list is at least the given minimum
    // value. If the currect capacity of the list is less than min, the
    // capacity is increased to twice the current capacity or to min,
    // whichever is larger.
    private void EnsureCapacity(int min) {
        if (_items.Length < min) {
            int newCapacity = _items.Length == 0? _defaultCapacity : _items.Length * 2;
            if (newCapacity < min) newCapacity = min;
            Capacity = newCapacity;
        }
    }

    public boolean Exists(Predicate<t> match) {
        return FindIndex(match) != -1;
    }

    public T Find(Predicate<t> match) {
        if( match == null) {
            ThrowHelper.ThrowArgumentNullException(ExceptionArgument.match);
        }

        for(int i = 0 ; i < _size; i++) {
            if(match(_items[i])) {
                return _items[i];
            }
        }
        return default(T);
    }

    public List<t> FindAll(Predicate<t> match) {
        if( match == null) {
            ThrowHelper.ThrowArgumentNullException(ExceptionArgument.match);
        }

        List<t> list = new List<t>();
        for(int i = 0 ; i < _size; i++) {
            if(match(_items[i])) {
                list.Add(_items[i]);
            }
        }
        return list;
    }

    public int FindIndex(Predicate<t> match) {
        return FindIndex(0, _size, match);
    }

    public int FindIndex(int startIndex, Predicate<t> match) {
       return FindIndex( startIndex, _size - startIndex, match);
    }

    public int FindIndex(int startIndex, int count, Predicate<t> match) {
        if( (uint)startIndex > (uint)_size ) {
            ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.startIndex, ExceptionResource.ArgumentOutOfRange_Index);
        }

        if (count < 0 || startIndex > _size - count) {
            ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.count, ExceptionResource.ArgumentOutOfRange_Count);
        }

        if( match == null) {
            ThrowHelper.ThrowArgumentNullException(ExceptionArgument.match);
        }

        int endIndex = startIndex + count;
        for( int i = startIndex; i < endIndex; i++) {
            if( match(_items[i])) return i;
        }
        return -1;
    }

    public T FindLast(Predicate<t> match) {
        if( match == null) {
            ThrowHelper.ThrowArgumentNullException(ExceptionArgument.match);
        }

        for(int i = _size - 1 ; i >= 0; i--) {
            if(match(_items[i])) {
                return _items[i];
            }
        }
        return default(T);
    }

    public int FindLastIndex(Predicate<t> match) {
        return FindLastIndex( _size - 1, _size, match);
    }

    public int FindLastIndex(int startIndex, Predicate<t> match) {
       return FindLastIndex( startIndex, startIndex + 1, match);
    }

    public int FindLastIndex(int startIndex, int count, Predicate<t> match) {
        if( match == null) {
            ThrowHelper.ThrowArgumentNullException(ExceptionArgument.match);
        }

        if(_size == 0) {
            // Special case for 0 length List
            if( startIndex != -1) {
                ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.startIndex, ExceptionResource.ArgumentOutOfRange_Index);
            }
        }
        else {
            // Make sure we're not out of range
            if ( (uint)startIndex >= (uint)_size) {
                ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.startIndex, ExceptionResource.ArgumentOutOfRange_Index);
            }
        }

        // 2nd have of this also catches when startIndex == MAXINT, so MAXINT - 0 + 1 == -1, which is < 0.
        if (count < 0 || startIndex - count + 1 < 0) {
            ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.count, ExceptionResource.ArgumentOutOfRange_Count);
        }

        int endIndex = startIndex - count;
        for( int i = startIndex; i > endIndex; i--) {
            if( match(_items[i])) {
                return i;
            }
        }
        return -1;
    }


    public void ForEach(Action<t> action) {
        if( action == null) {
            ThrowHelper.ThrowArgumentNullException(ExceptionArgument.match);
        }

        for(int i = 0 ; i < _size; i++) {
            action(_items[i]);
        }
    }

    // Returns an enumerator for this list with the given
    // permission for removal of elements. If modifications made to the list
    // while an enumeration is in progress, the MoveNext and
    // GetObject methods of the enumerator will throw an exception.
    //
    public Enumerator GetEnumerator() {
        return new Enumerator(this);
    }

    /// <internalonly>
    IEnumerator<t> IEnumerable<t>.GetEnumerator() {
        return new Enumerator(this);
    }

    IEnumerator IEnumerable.GetEnumerator() {
        return new Enumerator(this);
    }

    public List<t> GetRange(int index, int count) {
        if (index < 0 || count < 0) {
            ThrowHelper.ThrowArgumentOutOfRangeException( (index<0 ? ExceptionArgument.index : ExceptionArgument.count), ExceptionResource.ArgumentOutOfRange_NeedNonNegNum);
        }

        if (_size - index < count) {
            ThrowHelper.ThrowArgumentException(ExceptionResource.Argument_InvalidOffLen);
        }

        List<t> list = new List<t>(count);
        Array.Copy(_items, index, list._items, 0, count);
        list._size = count;
        return list;
    }


    // Returns the index of the first occurrence of a given value in a range of
    // this list. The list is searched forwards from beginning to end.
    // The elements of the list are compared to the given value using the
    // Object.Equals method.
    //
    // This method uses the Array.IndexOf method to perform the
    // search.
    //
    public int IndexOf(T item) {
        return Array.IndexOf(_items, item, 0, _size);
    }

    int IList.IndexOf(Object item)
    {
        if(IsCompatibleObject(item)) {
            return IndexOf((T)item);
        }
        return -1;
    }

    // Returns the index of the first occurrence of a given value in a range of
    // this list. The list is searched forwards, starting at index
    // index and ending at count number of elements. The
    // elements of the list are compared to the given value using the
    // Object.Equals method.
    //
    // This method uses the Array.IndexOf method to perform the
    // search.
    //
    public int IndexOf(T item, int index) {
        if (index > _size)
            ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.index, ExceptionResource.ArgumentOutOfRange_Index);
        return Array.IndexOf(_items, item, index, _size - index);
    }

    // Returns the index of the first occurrence of a given value in a range of
    // this list. The list is searched forwards, starting at index
    // index and upto count number of elements. The
    // elements of the list are compared to the given value using the
    // Object.Equals method.
    //
    // This method uses the Array.IndexOf method to perform the
    // search.
    //
    public int IndexOf(T item, int index, int count) {
        if (index > _size)
            ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.index, ExceptionResource.ArgumentOutOfRange_Index);

        if (count <0 || index > _size - count) ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.count, ExceptionResource.ArgumentOutOfRange_Count);

        return Array.IndexOf(_items, item, index, count);
    }

    // Inserts an element into this list at a given index. The size of the list
    // is increased by one. If required, the capacity of the list is doubled
    // before inserting the new element.
    //
    public void Insert(int index, T item) {
        // Note that insertions at the end are legal.
        if ((uint) index > (uint)_size) {
            ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.index, ExceptionResource.ArgumentOutOfRange_ListInsert);
        }
        if (_size == _items.Length) EnsureCapacity(_size + 1);
        if (index < _size) {
            Array.Copy(_items, index, _items, index + 1, _size - index);
        }
        _items[index] = item;
        _size++;
        _version++;
    }

    public void /*System.Collections.IList.*/Insert(int index, Object item)
    {
        VerifyValueType(item);
        Insert(index, (T) item);
    }

    // Inserts the elements of the given collection at a given index. If
    // required, the capacity of the list is increased to twice the previous
    // capacity or the new size, whichever is larger.  Ranges may be added
    // to the end of the list by setting index to the List's size.
    //
    public void InsertRange(int index, IEnumerable<t> collection) {
        if (collection==null) {
            ThrowHelper.ThrowArgumentNullException(ExceptionArgument.collection);
        }

        if ((uint)index > (uint)_size) {
            ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.index, ExceptionResource.ArgumentOutOfRange_Index);
        }

        ICollection<t> c = collection as ICollection<t>;
        if( c != null ) {    // if collection is ICollection<t>
            int count = c.Count;
            if (count > 0) {
                EnsureCapacity(_size + count);
                if (index < _size) {
                    Array.Copy(_items, index, _items, index + count, _size - index);
                }

                // If we're inserting a List into itself, we want to be able to deal with that.
                if (this == c) {
                    // Copy first part of _items to insert location
                    Array.Copy(_items, 0, _items, index, index);
                    // Copy last part of _items back to inserted location
                    Array.Copy(_items, index+count, _items, index*2, _size-index);
                }
                else {
                    T[] itemsToInsert = new T[count];
                    c.CopyTo(itemsToInsert, 0);
                    itemsToInsert.CopyTo(_items, index);
                }
                _size += count;
            }
        }
        else {
            using(IEnumerator<t> en = collection.GetEnumerator()) {
                while(en.MoveNext()) {
                    Insert(index++, en.Current);
                }
            }
        }
        _version++;
    }

    // Returns the index of the last occurrence of a given value in a range of
    // this list. The list is searched backwards, starting at the end
    // and ending at the first element in the list. The elements of the list
    // are compared to the given value using the Object.Equals method.
    //
    // This method uses the Array.LastIndexOf method to perform the
    // search.
    //
    public int LastIndexOf(T item)
    {
        return LastIndexOf(item, _size - 1, _size);
    }

    // Returns the index of the last occurrence of a given value in a range of
    // this list. The list is searched backwards, starting at index
    // index and ending at the first element in the list. The
    // elements of the list are compared to the given value using the
    // Object.Equals method.
    //
    // This method uses the Array.LastIndexOf method to perform the
    // search.
    //
    public int LastIndexOf(T item, int index)
    {
        if (index >= _size)
            ThrowHelper.ThrowArgumentOutOfRangeException(ExceptionArgument.index, ExceptionResource.ArgumentOutOfRange_Index);
        return LastIndexOf(item, index, index + 1);
    }

    // Returns the index of the last occurrence of a given value in a range of
    // this list. The list is searched backwards, starting at index
    // index and upto count elements. The elements of
    // the list are compared to the given value using the Object.Equals
    // method.
    //
    // This method uses the Array.LastIndexOf method to perform the
    // search.
    //
    public int LastIndexOf(T item, int index, int count) {
        if (_size == 0) {
            return -1;
        }

        if (index < 0 || count < 0) {
            ThrowHelper.ThrowArgumentOutOfRangeException( (index<0 ? ExceptionArgument.index : ExceptionArgument.count), ExceptionResource.ArgumentOutOfRange_NeedNonNegNum);
        }

        if (index >= _size || count > index + 1) {
            ThrowHelper.ThrowArgumentOutOfRangeException((index >= _size ? ExceptionArgument.index : ExceptionArgument.count), ExceptionResource.ArgumentOutOfRange_BiggerThanCollection);
        }

        return Array.LastIndexOf(_items, item, index, count);
    }

    // Removes the element at the given index. The size of the list is
    // decreased by one.
    //
    public boolean Remove(T item) {
        int index = IndexOf(item);
        if (index >= 0) {
            RemoveAt(index);
            return true;
        }

        return false;
    }

    public void /*IList.*/Remove(Object item)
    {
        if(IsCompatibleObject(item)) {
            Remove((T) item);
        }
    }

    // This method removes all items which matches the predicate.
    // The complexity is O(n).
    public int RemoveAll(Predicate<t> match) {
        if( match == null) {
            ThrowHelper.ThrowArgumentNullException(ExceptionArgument.match);
        }

        int freeIndex = 0;   // the first free slot in items array

        // Find the first item which needs to be removed.
        while( freeIndex < _size && !match(_items[freeIndex])) freeIndex++;
        if( freeIndex >= _size) return 0;

        int current = freeIndex + 1;
        while( current < _size) {
            // Find the first item which needs to be kept.
            while( current < _size && match(_items[current])) current++;

            if( current < _size) {
                // copy item to the free slot.
                _items[freeIndex++] = _items[current++];
            }
        }

        Array.Clear(_items, freeIndex, _size - freeIndex);
        int result = _size - freeIndex;
        _size = freeIndex;
        _version++;
        return result;
    }

    // Removes the element at the given index. The size of the list is
    // decreased by one.
    //
    public void RemoveAt(int index) {
        if ((uint)index >= (uint)_size) {
            ThrowHelper.ThrowArgumentOutOfRangeException();
        }
        _size--;
        if (index < _size) {
            Array.Copy(_items, index + 1, _items, index, _size - index);
        }
        _items[_size] = default(T);
        _version++;
    }

    // Removes a range of elements from this list.
    //
    public void RemoveRange(int index, int count) {
        if (index < 0 || count < 0) {
            ThrowHelper.ThrowArgumentOutOfRangeException( (index<0 ? ExceptionArgument.index : ExceptionArgument.count), ExceptionResource.ArgumentOutOfRange_NeedNonNegNum);
        }

        if (_size - index < count)
            ThrowHelper.ThrowArgumentException(ExceptionResource.Argument_InvalidOffLen);

        if (count > 0) {
            int i = _size;
            _size -= count;
            if (index < _size) {
                Array.Copy(_items, index + count, _items, index, _size - index);
            }
            Array.Clear(_items, _size, count);
            _version++;
        }
    }

    // Reverses the elements in this list.
    public void Reverse() {
        Reverse(0, Count);
    }

    // Reverses the elements in a range of this list. Following a call to this
    // method, an element in the range given by index and count
    // which was previously located at index i will now be located at
    // index index + (index + count - i - 1).
    //
    // This method uses the Array.Reverse method to reverse the
    // elements.
    //
    public void Reverse(int index, int count) {
        if (index < 0 || count < 0) {
            ThrowHelper.ThrowArgumentOutOfRangeException( (index<0 ? ExceptionArgument.index : ExceptionArgument.count), ExceptionResource.ArgumentOutOfRange_NeedNonNegNum);
        }

        if (_size - index < count)
            ThrowHelper.ThrowArgumentException(ExceptionResource.Argument_InvalidOffLen);
        Array.Reverse(_items, index, count);
        _version++;
    }

    // Sorts the elements in this list.  Uses the default comparer and
    // Array.Sort.
    public void Sort()
    {
        Sort(0, Count, null);
    }

    // Sorts the elements in this list.  Uses Array.Sort with the
    // provided comparer.
    public void Sort(IComparer<t> comparer)
    {
        Sort(0, Count, comparer);
    }

    // Sorts the elements in a section of this list. The sort compares the
    // elements to each other using the given IComparer interface. If
    // comparer is null, the elements are compared to each other using
    // the IComparable interface, which in that case must be implemented by all
    // elements of the list.
    //
    // This method uses the Array.Sort method to sort the elements.
    //
    public void Sort(int index, int count, IComparer<t> comparer) {
        if (index < 0 || count < 0) {
            ThrowHelper.ThrowArgumentOutOfRangeException( (index<0 ? ExceptionArgument.index : ExceptionArgument.count), ExceptionResource.ArgumentOutOfRange_NeedNonNegNum);
        }

        if (_size - index < count)
            ThrowHelper.ThrowArgumentException(ExceptionResource.Argument_InvalidOffLen);

        Array.Sort<t>(_items, index, count, comparer);
        _version++;
    }

    public void Sort(Comparison<t> comparison) {
        if( comparison == null) {
            ThrowHelper.ThrowArgumentNullException(ExceptionArgument.match);
        }

        if( _size > 0) {
            IComparer<t> comparer = new Array.FunctorComparer<t>(comparison);
            Array.Sort(_items, 0, _size, comparer);
        }
    }

    // ToArray returns a new Object array containing the contents of the List.
    // This requires copying the List, which is an O(n) operation.
    public T[] ToArray() {
        T[] array = new T[_size];
        Array.Copy(_items, 0, array, 0, _size);
        return array;
    }

    // Sets the capacity of this list to the size of the list. This method can
    // be used to minimize a list's memory overhead once it is known that no
    // new elements will be added to the list. To completely clear a list and
    // release all memory referenced by the list, execute the following
    // statements:
    //
    // list.Clear();
    // list.TrimExcess();
    //
    public void TrimExcess() {
        int threshold = (int)(((double)_items.Length) * 0.9);
        if( _size < threshold ) {
            Capacity = _size;
        }
    }

    public boolean TrueForAll(Predicate<t> match) {
        if( match == null) {
            ThrowHelper.ThrowArgumentNullException(ExceptionArgument.match);
        }

        for(int i = 0 ; i < _size; i++) {
            if( !match(_items[i])) {
                return false;
            }
        }
        return true;
    }

//    [Serializable()]
    public class Enumerator implements IEnumerator
    {
        private List<t> list;
        private int index;
        private int version;
        private T current;

        /*internal*/ public  Enumerator(List<t> list) {
            this.list = list;
            index = 0;
            version = list._version;
            current = default(T);
        }

        public void Dispose() {
        }

        public boolean MoveNext() {

            List<t> localList = list;

            if (version == localList._version && ((uint)index < (uint)localList._size))
            {
                current = localList._items[index];
                index++;
                return true;
            }
            return MoveNextRare();
        }

        private boolean MoveNextRare()
        {
            if (version != list._version) {
                ThrowHelper.ThrowInvalidOperationException(ExceptionResource.InvalidOperation_EnumFailedVersion);
            }

            index = list._size + 1;
            current = default(T);
            return false;
        }

        public T Current {
            get {
                return current;
            }
        }

        Object IEnumerator.Current {
            get {
                if( index == 0 || index == list._size + 1) {
                     ThrowHelper.ThrowInvalidOperationException(ExceptionResource.InvalidOperation_EnumOpCantHappen);
                }
                return Current;
            }
        }

        void IEnumerator.Reset() {
            if (version != list._version) {
                ThrowHelper.ThrowInvalidOperationException(ExceptionResource.InvalidOperation_EnumFailedVersion);
            }

            index = 0;
            current = default(T);
        }

    }
}
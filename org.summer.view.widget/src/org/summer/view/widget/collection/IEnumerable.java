package org.summer.view.widget.collection;

// Implement this interface if you need to support VB's foreach semantics.
// Also, COM classes that support an enumerator will also implement this interface.
//    [ContractClass(typeof(IEnumerableContract))]
//    [Guid("496B0ABE-CDEE-11d3-88E8-00902754C43A")]
//    [System.Runtime.InteropServices.ComVisible(true)]
public interface IEnumerable<T> {
	// Interfaces are not serializable
	// Returns an IEnumerator for this enumerable Object. The enumerator
	// provides
	// a simple way to access all the contents of a collection.
	// [Pure]
	// [DispId(-4)]
	IEnumerator<T> GetEnumerator();
}
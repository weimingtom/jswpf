package org.summer.view.widget.media;
//[Flags]
public enum VisualFlags //: uint
	{
		None ,//= 0u,
		IsSubtreeDirtyForPrecompute ,//= 1u,
		ShouldPostRender ,//= 2u,
		IsUIElement ,//= 4u,
		IsLayoutSuspended ,//= 8u,
		IsVisualChildrenIterationInProgress ,//= 16u,
		Are3DContentBoundsValid ,//= 32u,
		FindCommonAncestor ,//= 64u,
		IsLayoutIslandRoot ,//= 128u,
		UseLayoutRounding ,//= 256u,
		VisibilityCache_Visible ,//= 512u,
		VisibilityCache_TakesSpace ,//= 1024u,
		RegisteredForAncestorChanged ,//= 2048u,
		SubTreeHoldsAncestorChanged ,//= 4096u,
		NodeIsCyclicBrushRoot ,//= 8192u,
		NodeHasEffect ,//= 16384u,
		IsViewport3DVisual ,//= 32768u,
		ReentrancyFlag ,//= 65536u,
		HasChildren ,//= 131072u,
		BitmapEffectEmulationDisabled ,//= 262144u,
		TreeLevelBit0 ,//= 2097152u,
		TreeLevelBit1 ,//= 4194304u,
		TreeLevelBit2 ,//= 8388608u,
		TreeLevelBit3 ,//= 16777216u,
		TreeLevelBit4 ,//= 33554432u,
		TreeLevelBit5 ,//= 67108864u,
		TreeLevelBit6 ,//= 134217728u,
		TreeLevelBit7 ,//= 268435456u,
		TreeLevelBit8 ,//= 536870912u,
		TreeLevelBit9 ,//= 1073741824u,
		TreeLevelBit10 ,//= 2147483648u
	}
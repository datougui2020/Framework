package com.library.adapter_recyclerview;

import java.util.List;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 有多种类型的recyclerView  adapter
 * 多item布局
 *     @author Yangjie
 *     className MultiUniversalAdapter
 *     created at  2016/12/26  17:52
 */
public abstract class MultiUniversalAdapter<D> extends UniversalAdapter<D> implements MultiItemTypeSupport<D>
{
	public MultiUniversalAdapter(@NonNull Context context, @Nullable List<D> list)
	{
		super(context, -1, list);
	}

	@Override
	public int initItemViewType(int position)
	{
		return getMultiItemViewType(position, null != getDataList() ? getDataList().get(position) : null);
	}

	@Override
	public int initLayoutId(int viewType)
	{
		return getLayoutId(viewType);
	}
}

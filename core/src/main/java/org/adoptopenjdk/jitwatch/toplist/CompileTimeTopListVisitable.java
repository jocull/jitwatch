/*
 * Copyright (c) 2016-2017 Chris Newland.
 * Licensed under https://github.com/AdoptOpenJDK/jitwatch/blob/master/LICENSE-BSD
 * Instructions: https://github.com/AdoptOpenJDK/jitwatch/wiki
 */
package org.adoptopenjdk.jitwatch.toplist;

import org.adoptopenjdk.jitwatch.model.*;
import org.adoptopenjdk.jitwatch.treevisitor.ITreeVisitable;

import java.util.List;

public class CompileTimeTopListVisitable extends AbstractTopListVisitable
{
	public CompileTimeTopListVisitable(IReadOnlyJITDataModel model, boolean sortHighToLow)
	{
		super(model, sortHighToLow);
	}

	@Override
	public void visit(IMetaMember mm)
	{
		// TODO: Testing duration sums only! Revert me!
		long durations = 0;
		long durationsStrict = 0;
		for (Compilation compilation : mm.getCompilations())
		{
			durations += compilation.getCompilationDuration();
			if (!compilation.isC2N())
			{
				topList.add(new MemberScore(mm, compilation.getCompilationDuration()));
				durationsStrict += compilation.getCompilationDuration();
			}
		}

		if (durations > 0 && durationsStrict <= 0) {
			System.out.println(":::::::DIFFERENCE:::::");
			System.out.println("Durations: " + durations);
			System.out.println("Durations strict: " + durationsStrict);
		}
	}

	// TODO: Duplicated code from `TreeVisitor`!
	private void walkTree(IReadOnlyJITDataModel model)
	{
		List<MetaPackage> roots = model.getPackageManager().getRootPackages();

		for (MetaPackage mp : roots)
		{
			walkPackage(mp);
		}
	}

	// TODO: Duplicated code from `TreeVisitor`!
	private void walkPackage(MetaPackage mp)
	{
		List<MetaPackage> childPackages = mp.getChildPackages();

		for (MetaPackage childPackage : childPackages)
		{
			walkPackage(childPackage);
		}

		List<MetaClass> packageClasses = mp.getPackageClasses();

		for (MetaClass mc : packageClasses)
		{
			for (IMetaMember mm : mc.getMetaMembers())
			{
				for (Compilation compilation : mm.getCompilations())
				{
					_durations += compilation.getCompilationDuration();
					if (!compilation.isC2N())
					{
						_durationsStrict += compilation.getCompilationDuration();
					}
				}
			}
		}
	}

	// TODO: Testing duration sums only!
	private long _durations = 0;
	private long _durationsStrict = 0;

	// TODO: Testing duration sums only!
	@Override
	public List<ITopListScore> buildTopList() {
		_durations = 0;
		_durationsStrict = 0;
		walkTree(model);
		System.out.println("TOTAL Durations: " + _durations);
		System.out.println("TOTAL Durations strict: " + _durationsStrict);

		return super.buildTopList();
	}
}
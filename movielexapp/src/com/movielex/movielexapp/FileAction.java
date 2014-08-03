/*
AmatchTestActivity.java:
Copyright (c) 2014, Oleg Galbert
All rights reserved.
*/

package com.movielex.movielexapp;

public enum FileAction
{
    LOAD(0),
    SAVE(1),
    KEY(2);

    public int value;

    private FileAction(final int value)
    {
        this.value = value;
    }

    public static FileAction fromValue(final int value)
    {
        for (final FileAction action : FileAction.values())
            if (action.value == value)
                return action;
        return null;
    }
}

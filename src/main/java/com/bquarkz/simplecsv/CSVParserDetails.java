package com.bquarkz.simplecsv;

public class CSVParserDetails
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Special Fields And Injections
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private final boolean shouldWriteHeader;
    private final boolean shouldSkipHeader;
    private final boolean shouldVerifyHeader;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    CSVParserDetails( CSVBean bean )
    {
        shouldWriteHeader = bean.shouldWriteHeader();
        shouldSkipHeader = bean.shouldSkipHeader();
        shouldVerifyHeader = bean.shouldVerifyHeader();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Factories
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters And Setters
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean shouldWriteHeader()
    {
        return shouldWriteHeader;
    }

    public boolean shouldSkipHeader()
    {
        return shouldSkipHeader;
    }

    public boolean shouldVerifyHeader()
    {
        return shouldVerifyHeader;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Inner Classes And Patterns
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

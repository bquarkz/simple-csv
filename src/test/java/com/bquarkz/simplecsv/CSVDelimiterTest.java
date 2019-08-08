package com.bquarkz.simplecsv;

import org.junit.Assert;
import org.junit.Test;

public class CSVDelimiterTest
{
    @Test
    public void test_DefaultShouldEuropean()
    {
        final CSVDelimiters delimiters = new CSVDelimiters();
        Assert.assertEquals( ";", delimiters.getColumn() );
        Assert.assertEquals( "\"", delimiters.getContent() );
        Assert.assertEquals( "\n", delimiters.getRow() );
        Assert.assertEquals( "#", delimiters.getComment() );
    }
}

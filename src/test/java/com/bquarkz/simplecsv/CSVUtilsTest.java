package com.bquarkz.simplecsv;

import org.junit.Assert;
import org.junit.Test;

import static com.bquarkz.simplecsv.CSVUtils.fromPosixStartsWithDelimiter;

public class CSVUtilsTest
{
    private static final String DELIMITER = "-DELIMITER-";
    private static final String CONTENT = "@CONTENT@";

    @Test
    public void test_GiveGoodContent_ThemEmbrace_ShouldBeOk()
    {
        final String s = CSVUtils.embrace( DELIMITER, CONTENT );
        Assert.assertNotNull( s );
        Assert.assertEquals( DELIMITER + CONTENT + DELIMITER, s );
    }

    @Test
    public void test_GiveGoodContent_ThemDesembrace_ShouldBeOk()
    {
        final String s = CSVUtils.desembrace( DELIMITER, DELIMITER + CONTENT + DELIMITER );
        Assert.assertNotNull( s );
        Assert.assertEquals( CONTENT, s );
    }

    @Test
    public void test_GiveContentNotEmbraced_ThemDesembrace_ShouldReturnFullContent()
    {
        {
            final String s = CSVUtils.desembrace( DELIMITER, DELIMITER + CONTENT );
            Assert.assertNotNull( s );
            Assert.assertEquals( DELIMITER + CONTENT, s );
        }

        {
            final String s = CSVUtils.desembrace( DELIMITER, CONTENT + DELIMITER );
            Assert.assertNotNull( s );
            Assert.assertEquals( CONTENT + DELIMITER, s );
        }

        {
            final String s = CSVUtils.desembrace( DELIMITER, CONTENT );
            Assert.assertNotNull( s );
            Assert.assertEquals( CONTENT, s );
        }
    }


    @Test
    public void test_GiveRandomContentWITHDelimiter_ThemCheckForDelimiterFromPosix_ShouldReturnTrue()
    {
        final String randomContent = "SOME RANDOM CONTENT";
        char[] withDelimiter = ( randomContent + DELIMITER ).toCharArray();
        Assert.assertTrue( fromPosixStartsWithDelimiter( withDelimiter, randomContent.length(), DELIMITER ) );
        char[] withoutDelimiter = ( randomContent + "-DELIMITER" ).toCharArray();
        Assert.assertFalse( fromPosixStartsWithDelimiter( withoutDelimiter, randomContent.length(), DELIMITER ) );
    }

    @Test
    public void test_GiveRandomContentWITHOUTDelimiter_ThemCheckForDelimiterFromPosix_ShouldReturnFalse()
    {
        final String randomContent = "SOME RANDOM CONTENT";
        char[] withoutDelimiter = ( randomContent + "-DELIMITER" ).toCharArray();
        Assert.assertFalse( fromPosixStartsWithDelimiter( withoutDelimiter, randomContent.length(), DELIMITER ) );
    }

    @Test
    public void test_GiveNullContent_ThemCheckForDelimiterFromPosix_ShouldReturnFalse()
    {
        Assert.assertFalse( fromPosixStartsWithDelimiter( null, 0, DELIMITER ) );
    }

    @Test
    public void test_GiveContent_ThemCheckForDelimiterFromPosix_WhenBufferIsSmallerThanDelimiter_ShouldReturnFalse()
    {
        final char[] buffer = "-DELIMITE".toCharArray();
        Assert.assertFalse( fromPosixStartsWithDelimiter( buffer, 0, DELIMITER ) );
    }

    @Test
    public void test_GiveContent_ThemCheckForDelimiterFromPosix_WhenPosixBreachBufferSize_ShouldReturnFalse()
    {
        final char[] buffer = ( CONTENT + "-DELIMITE" ).toCharArray();
        Assert.assertFalse( fromPosixStartsWithDelimiter( buffer, CONTENT.length(), DELIMITER ) );
    }

    @Test
    public void test_GiveSomeCSVWellDone_ThenSplitOnColumns_ShouldSplitOnColumnsAndKeepColumnsOrder()
    {
        final String csv = "'111';'222';'333';'444';'555'";
        final String[] contents = CSVUtils.splitOnColumns( csv, "'", ";" );
        Assert.assertNotNull( contents );
        Assert.assertEquals( 5, contents.length );
        Assert.assertEquals( "'111'", contents[ 0 ] );
        Assert.assertEquals( "'222'", contents[ 1 ] );
        Assert.assertEquals( "'333'", contents[ 2 ] );
        Assert.assertEquals( "'444'", contents[ 3 ] );
        Assert.assertEquals( "'555'", contents[ 4 ] );
    }

    @Test
    public void test_GiveSomeCSVWellDone_ThenSplitOnColumns_WhenAColumnDelimiterIsIncludedInsideAnyContent_ShouldSplitOnColumnsAndKeepColumnsOrder()
    {
        final String csv = "'111';'222';'333';'444';'5;55'";
        final String[] contents = CSVUtils.splitOnColumns( csv, "'", ";" );
        Assert.assertNotNull( contents );
        Assert.assertEquals( 5, contents.length );
        Assert.assertEquals( "'111'", contents[ 0 ] );
        Assert.assertEquals( "'222'", contents[ 1 ] );
        Assert.assertEquals( "'333'", contents[ 2 ] );
        Assert.assertEquals( "'444'", contents[ 3 ] );
        Assert.assertEquals( "'5;55'", contents[ 4 ] );
    }

    @Test
    public void test_GiveSomeCSVWellDone_ThenSplitOnColumns_WhenAContentDelimiterIsIncludedInsideAnyContent_ShouldSplitOnColumnsAndKeepColumnsOrder()
    {
        final String csv = "'1''11';'222';'333';'444';'555'";
        final String[] contents = CSVUtils.splitOnColumns( csv, "'", ";" );
        Assert.assertNotNull( contents );
        Assert.assertEquals( 5, contents.length );
        Assert.assertEquals( "'1''11'", contents[ 0 ] );
        Assert.assertEquals( "'222'", contents[ 1 ] );
        Assert.assertEquals( "'333'", contents[ 2 ] );
        Assert.assertEquals( "'444'", contents[ 3 ] );
        Assert.assertEquals( "'555'", contents[ 4 ] );
    }

    @Test
    public void test_GiveSomeCSVWellDone_ThenSplitOnColumns_WhenContentMixEmbracedOrNot_ShouldSplitOnColumnsAndKeepColumnsOrder()
    {
        final String csv = "111;222;'333';444;'555'";
        final String[] contents = CSVUtils.splitOnColumns( csv, "'", ";" );
        Assert.assertNotNull( contents );
        Assert.assertEquals( 5, contents.length );
        Assert.assertEquals( "111", contents[ 0 ] );
        Assert.assertEquals( "222", contents[ 1 ] );
        Assert.assertEquals( "'333'", contents[ 2 ] );
        Assert.assertEquals( "444", contents[ 3 ] );
        Assert.assertEquals( "'555'", contents[ 4 ] );
    }

    @Test
    public void test_GiveSomeCSVWellDone_ThenSplitOnColumns_WhenContentEndsWithOptionalColumnDelimiter_ShouldSplitOnColumnsAndKeepColumnsOrder()
    {
        final String csv = "'111';'222';'333';";
        final String[] contents = CSVUtils.splitOnColumns( csv, "'", ";" );
        Assert.assertNotNull( contents );
        Assert.assertEquals( 3, contents.length );
        Assert.assertEquals( "'111'", contents[ 0 ] );
        Assert.assertEquals( "'222'", contents[ 1 ] );
        Assert.assertEquals( "'333'", contents[ 2 ] );
    }

    @Test
    public void test_GiveSomeCSVWellDone_ThenSplitOnColumns_WhenContentEmptyColumnsWithDelimiters_ShouldSplitOnColumnsAndKeepColumnsOrder()
    {
        final String csv = "'111';'';'333';";
        final String[] contents = CSVUtils.splitOnColumns( csv, "'", ";" );
        Assert.assertNotNull( contents );
        Assert.assertEquals( 3, contents.length );
        Assert.assertEquals( "'111'", contents[ 0 ] );
        Assert.assertEquals( "''", contents[ 1 ] );
        Assert.assertEquals( "'333'", contents[ 2 ] );
    }

    @Test
    public void test_GiveSomeCSVWellDone_ThenSplitOnColumns_WhenContentEmptyColumnsWithoutDelimiters_ShouldSplitOnColumnsAndKeepColumnsOrder()
    {
        final String csv = "'111';;'333';";
        final String[] contents = CSVUtils.splitOnColumns( csv, "'", ";" );
        Assert.assertNotNull( contents );
        Assert.assertEquals( 3, contents.length );
        Assert.assertEquals( "'111'", contents[ 0 ] );
        Assert.assertEquals( "", contents[ 1 ] );
        Assert.assertEquals( "'333'", contents[ 2 ] );
    }

}

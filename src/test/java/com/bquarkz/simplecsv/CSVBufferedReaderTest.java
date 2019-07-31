package com.bquarkz.simplecsv;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CSVBufferedReaderTest
{
    private static final String CSV_HEADER = "SUPER-COLUMN-1;SUPER-COLUMN-2;SUPER-COLUMN-3;EMPTY-COLUMN-4;INNER-COLUMN-5";
    private static final String CSV_1 = "\"prefix-----c1__1\";\"c2__1-----suffix\";\"1\";\"1;2;3;4\";\"inner 3;3\"";
    private static final String CSV_2 = "\"prefix-----c1__2\";\"c2__2-----suffix\";\"2\";\"1;2;3;4\";\"inner 2;2\"";
    private static final String CSV_3 = "\"prefix-----c1__3\";\"c2__3-----suffix\";\"3\";\"1;2;3;4\";\"inner 3;3\"";
    private static final String CSV_4 = "\"prefix-----c1__4\";\"c2__4-----suffix\";\"4\";\"1;2;3;4\";\"inner 4;4\"";
    private static final String CSV_5 = "\"prefix-----c1__5\";\"c2__5-----suffix\";\"5\";\"1;2;3;4\";\"inner 3;3\"";
    private static final String[] CSVS = { CSV_1, CSV_2, CSV_3, CSV_4, CSV_5 };
    private static final String CSV = String.join( "\n", CSV_HEADER, CSV_1, CSV_2, CSV_3, CSV_4, CSV_5 );

    @Test
    public void test_GivenCSVBufferedReader_ThenReadACoupleOfCSVRows_ShouldBeOk() throws IOException
    {
        try( final ByteArrayInputStream bais = new ByteArrayInputStream( CSV.getBytes( StandardCharsets.UTF_8 ) );
             final InputStreamReader inputStreamReader = new InputStreamReader( bais, StandardCharsets.UTF_8 );
             final CSVBufferedReader reader = new CSVBufferedReader( inputStreamReader, 128, new CSVDelimiters(), true ) )
        {
            Assert.assertEquals( CSV_1, reader.readNextRow() );
            Assert.assertEquals( CSV_2, reader.readNextRow() );
            Assert.assertEquals( CSV_3, reader.readNextRow() );
            Assert.assertEquals( CSV_4, reader.readNextRow() );
            Assert.assertEquals( CSV_5, reader.readNextRow() );
        }
    }

    @Test
    public void test_GivenCSVBufferedReader_ThenReadAsAStream_ShouldBeOk() throws IOException
    {
        try( final ByteArrayInputStream bais = new ByteArrayInputStream( CSV.getBytes( StandardCharsets.UTF_8 ) );
             final InputStreamReader inputStreamReader = new InputStreamReader( bais, StandardCharsets.UTF_8 );
             final CSVBufferedReader reader = new CSVBufferedReader( inputStreamReader, 128, new CSVDelimiters(), true ) )
        {
            Assert.assertEquals( 3, reader.readAsStream().filter( c -> c.contains( "inner 3;3" ) ).count() );
        }
    }

    @Test
    public void test_GivenCSVBufferedReader_ThenReadAsFor_ShouldBeOk() throws IOException
    {
        try( final ByteArrayInputStream bais = new ByteArrayInputStream( CSV.getBytes( StandardCharsets.UTF_8 ) );
             final InputStreamReader inputStreamReader = new InputStreamReader( bais, StandardCharsets.UTF_8 );
             final CSVBufferedReader reader = new CSVBufferedReader( inputStreamReader, 128, new CSVDelimiters(), true ) )
        {
            int i = 0;
            for( String csv : reader )
            {
                Assert.assertEquals( CSVS[ i++ ], csv );
            }
        }
    }
}

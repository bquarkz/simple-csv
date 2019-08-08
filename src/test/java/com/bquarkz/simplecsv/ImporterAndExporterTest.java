package com.bquarkz.simplecsv;

import org.junit.Assert;
import org.junit.Test;

public class ImporterAndExporterTest
{
    @Test
    public void test_ExporterDefaultBehaviors()
    {
        final CSVExporterBuilder< Bean > exporter = CSVBuilder.newExporter( Bean.class );
        Assert.assertFalse( exporter.shouldWriteHeader() ); // true by default but changed to false via annotation
        exporter.writingHeaders( true ); // changed programmatically to true -- has precedence
        Assert.assertTrue( exporter.shouldWriteHeader() );
        Assert.assertFalse( exporter.shouldNotIgnoreErrors() ); // which means that we should ignore errors :)
        exporter.ignoringErrors( false );
        Assert.assertTrue( exporter.shouldNotIgnoreErrors() );
    }

    @Test
    public void test_ImporterDefaultBehaviors()
    {
        final CSVImporterBuilder< Bean > importer = CSVBuilder.newImporter( Bean.class );
        Assert.assertTrue( importer.shouldSkipHeader() ); // false by default but changed to false via annotation
        // verify header is strictly connected with skip header - if skip is enabled even verify is enabled it will be disable
        Assert.assertFalse( importer.shouldVerifyHeader() );
        importer.skippingHeader( false );
        Assert.assertFalse( importer.shouldSkipHeader() );
        // because skip was set false then verify is free to be true - as annotation configuration
        Assert.assertTrue( importer.shouldVerifyHeader() );
        importer.verifyingHeader( false );
        Assert.assertFalse( importer.shouldVerifyHeader() );

        //even programmatically skip has comes first than verify
        importer.skippingHeader( true );
        importer.verifyingHeader( true );
        Assert.assertTrue( importer.shouldSkipHeader() );
        Assert.assertFalse( importer.shouldVerifyHeader() ); //because skip is true

        Assert.assertFalse( importer.shouldNotIgnoreErrors() ); // which means that we should ignore errors :)
        importer.ignoringErrors( false );
        Assert.assertTrue( importer.shouldNotIgnoreErrors() );
    }

    @CSVBean( shouldSkipHeader = true, shouldVerifyHeader = true, shouldWriteHeader = false )
    private static class Bean
    {
        @CSVColumn( column = 1 )
        private String column1;
    }
}

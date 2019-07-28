package com.bquarkz.simplecsv;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVBuilderTest
{
    @Test
    public void test()
    {
        CSVExporter< Bean > exporter = CSVBuilder
                .newExporter( Bean.class )
                .build();

        try( CSVExporter< Bean >.CSVWriter writer = exporter.to( "test.csv" ) )
        {
            List< Bean > beans = new ArrayList<>();
            for( int i = 0; i < 10; i++ )
            {
                beans.add( new Bean( i ) );
            }
            writer.write( beans );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    @CSVBean
    private class Bean
    {
        @CSVColumn( name = "SUPER-NAME", column = 1 )
        private String name;

        @CSVColumn( name = "SUPER-NAME", column = 1 )
        private String name;

        public Bean( int i )
        {
            name = "name " + i;
        }
    }
}

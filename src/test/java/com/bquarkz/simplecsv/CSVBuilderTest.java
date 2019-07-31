package com.bquarkz.simplecsv;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSVBuilderTest
{
    private static final String PREFIX = "prefix-----";
    private static final String SUFFIX = "-----suffix";

    private static final String COLUMN_1 = "SUPER-COLUMN-1";
    private static final String COLUMN_2 = "SUPER-COLUMN-2";
    private static final String COLUMN_3 = "SUPER-COLUMN-3";
    private static final String COLUMN_4 = "EMPTY-COLUMN-4";
    private static final String COLUMN_5 = "INNER-COLUMN-5";

    @Test
    public void write() throws IOException
    {
        final CSVExporter< Bean > exporter = CSVBuilder
                .newExporter( Bean.class )
                .ignoringErrors( true )
                .withMappings(
                        MappingCSV.mapping( COLUMN_1, o -> PREFIX + o ),
                        MappingCSV.mapping( COLUMN_2, o -> o + SUFFIX ),
                        MappingCSV.mapping( COLUMN_4, o -> ( (List<Integer>)o )
                                .stream()
                                .map( Object::toString )
                                .reduce( "", ( i1, i2 ) -> i1 + ";" + i2 )
                                .substring( 1 )
                        ),
                        MappingCSV.mapping( COLUMN_5, o -> {
                            InnerBean ib = (InnerBean)o;
                            return ib.getStringField() + ";" + ib.getIntegerField();
                        } )
                )
                .build();

        try( CSVExporter< Bean >.CSVWriter writer = exporter.toFile( "test.csv" ) )
        {
            List< Bean > beans = new ArrayList<>();
            for( int i = 0; i < 1000; i++ )
            {
                beans.add( new Bean( i ) );
            }
            writer.write( beans );
        }
    }

    @Test
    public void testNextRow() throws IOException
    {
        final FileInputStream fileInputStream = new FileInputStream( "test.csv" );
        final InputStreamReader inputStreamReader = new InputStreamReader( fileInputStream, StandardCharsets.UTF_8 );
        final CSVBufferedReader reader = new CSVBufferedReader( inputStreamReader, 128, new CSVDelimiters(), true );

        System.out.println( "1: " + reader.readNextRow() );
        System.out.println( "2: " + reader.readNextRow() );
        System.out.println( "3: " + reader.readNextRow() );
        System.out.println( "4: " + reader.readNextRow() );
        System.out.println( "5: " + reader.readNextRow() );
    }

    @Test
    public void read() throws IOException
    {
        final CSVImporter< Bean > importer = CSVBuilder
                .newImporter( Bean.class )
                .withBeanFactory( Bean::new )
                .ignoringErrors( false )
                .verifyingHeader()
                .withBufferSize( 128 )
                .build(
                        MappingBean.mapping( COLUMN_4, content ->
                                Stream.of( content.split( ";" ) ).collect( Collectors.toList() )
                        ),
                        MappingBean.mapping( COLUMN_5, content -> {
                            final String[] pieces = content.split( ";" );
                            return new InnerBean( pieces[ 0 ], Integer.valueOf( pieces[ 1 ] ) );
                        } )
                );

        try( CSVImporter< Bean >.CSVReader reader = importer.fromFile( "test.csv" ) )
        {
            List< Bean > beans = reader.readNext( 5 );
            beans.forEach( bean -> System.out.println( bean.toString() ) );
        }
    }

    @Test
    public void testStream() throws IOException
    {
        final CSVImporter< Bean > importer = CSVBuilder
                .newImporter( Bean.class )
                .withBeanFactory( Bean::new )
                .ignoringErrors( false )
                .skippingHeader()
                .withBufferSize( 128 )
                .build(
                        MappingBean.mapping( COLUMN_4, content -> Stream.of( content.split( ";" ) ).collect( Collectors.toList() ) ),
                        MappingBean.mapping( COLUMN_5, content -> {
                            final String[] pieces = content.split( ";" );
                            return new InnerBean( pieces[ 0 ], Integer.valueOf( pieces[ 1 ] ) );
                        } )
                );

        try( CSVImporter< Bean >.CSVReader reader = importer.fromFile( "test.csv" ) )
        {
            final String value = reader
                    .stream()
                    .map( b -> "1:" + b.column1 + " -- 2:" + b.column2 + " -- 3:" + b.column3 + " -- 4:" + b.column4 + " -- 5:" + b.column5 )
                    .reduce( "", ( s1, s2 ) -> s1 + "\n" + s2 );

            System.out.println( value );
        }
    }

    @CSVBean
    private class Bean
    {
        @CSVColumn( name = COLUMN_1, column = 1 )
        private String column1;

        @CSVColumn( name = COLUMN_3, column = 3 )
        private Integer column3;

        @CSVColumn( name = COLUMN_4, column = 4 )
        private List< Integer > column4;

        @CSVColumn( name = COLUMN_2, column = 2 )
        private String column2;

        @CSVColumn( name = COLUMN_5, column = 5 )
        private InnerBean column5;

        public Bean( int i )
        {
            column1 = "c1__" + i;
            column2 = "c2__" + i;
            column3 = i;
            column4 = Stream.of( 1, 2, 3, 4 ).collect( Collectors.toList() );
            column5 = new InnerBean( "inner " + i, i );
        }

        public Bean()
        {
            this( new Random( System.currentTimeMillis() ).nextInt( 100 ) );
        }

        @Override
        public String toString()
        {
            return "Bean{" +
                    COLUMN_1 + "='" + column1 + '\'' +
                    ", " + COLUMN_2 + "='" + column2 + '\'' +
                    ", " + COLUMN_3 + "='" + column3 + '\'' +
                    ", " + COLUMN_4 + "='" + column4 + '\'' +
                    ", " + COLUMN_5 + "='" + column5 + '\'' +
                    '}';
        }
    }

    private static class InnerBean
    {
        private String stringField;
        private Integer integerField;

        public InnerBean(
                String stringField,
                Integer integerField )
        {
            this.stringField = stringField;
            this.integerField = integerField;
        }

        public String getStringField()
        {
            return stringField;
        }

        public void setStringField( String stringField )
        {
            this.stringField = stringField;
        }

        public Integer getIntegerField()
        {
            return integerField;
        }

        public void setIntegerField( Integer integerField )
        {
            this.integerField = integerField;
        }

        @Override
        public String toString()
        {
            return "InnerBean{" +
                    "stringField='" + stringField + '\'' +
                    ", integerField=" + integerField +
                    '}';
        }
    }
}

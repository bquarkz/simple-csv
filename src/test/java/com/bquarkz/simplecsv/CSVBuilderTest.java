package com.bquarkz.simplecsv;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

public class CSVBuilderTest
{
    private static final String PREFIX = "prefix-----";
    private static final String SUFFIX = "-----suffix";

    private static final String COLUMN_1 = "SUPER-COLUMN-1";
    private static final String COLUMN_2 = "SUPER-COLUMN-2";
    private static final String COLUMN_3 = "SUPER-COLUMN-3";
    private static final String COLUMN_4 = "EMPTY-COLUMN-4";
    private static final String COLUMN_5 = "INNER-COLUMN-5";

    private static final String CSV =
                    "SUPER-COLUMN-1;SUPER-COLUMN-2;SUPER-COLUMN-3;EMPTY-COLUMN-4;INNER-COLUMN-5\n" +
                    "\"prefix-----c1__0\";\"c2__0-----suffix\";\"0\";\"1;2;3;4\";\"inner 0;0\"\n" +
                    "\"prefix-----c1__1\";\"c2__1-----suffix\";\"1\";\"1;2;3;4\";\"inner 1;1\"\n" +
                    "\"prefix-----c1__2\";\"c2__2-----suffix\";\"2\";\"1;2;3;4\";\"inner 2;2\"\n" +
                    "\"prefix-----c1__3\";\"c2__3-----suffix\";\"3\";\"1;2;3;4\";\"inner 3;3\"\n" +
                    "\"prefix-----c1__4\";\"c2__4-----suffix\";\"4\";\"1;2;3;4\";\"inner 4;4\"\n" +
                    "\"prefix-----c1__5\";\"c2__5-----suffix\";\"5\";\"1;2;3;4\";\"inner 5;5\"\n" +
                    "\"prefix-----c1__6\";\"c2__6-----suffix\";\"6\";\"1;2;3;4\";\"inner 6;6\"\n" +
                    "\"prefix-----c1__7\";\"c2__7-----suffix\";\"7\";\"1;2;3;4\";\"inner 7;7\"\n" +
                    "\"prefix-----c1__8\";\"c2__8-----suffix\";\"8\";\"1;2;3;4\";\"inner 8;8\"\n" +
                    "\"prefix-----c1__9\";\"c2__9-----suffix\";\"9\";\"1;2;3;4\";\"inner 9;9\"\n";

    public static byte[] zip( final String string ) throws IOException
    {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try( final GZIPOutputStream gzip = new GZIPOutputStream( baos ) )
        {
            gzip.write( string.getBytes( StandardCharsets.UTF_8 ) );
        }
        return baos.toByteArray();
    }

    @Test
    public void test_GivenExporter_ThenWriteBean_WhenExportBeansToACSVGZipFile_ShouldBeOk() throws IOException
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

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try( final GZIPOutputStream gzip = new GZIPOutputStream( baos ); //used a gzip file already to test
             final CSVExporter< Bean >.CSVWriter writer = exporter.toOutputStream( gzip ) )
        {
            List< Bean > beans = new ArrayList<>();
            for( int i = 0; i < 10; i++ )
            {
                beans.add( new Bean( i ) );
            }
            writer.write( beans );
        }
        final byte[] csv = baos.toByteArray();
        final byte[] zip = zip( CSV );
        Assert.assertEquals( zip.length, csv.length );
        for( int i = 0; i < zip.length; i++ )
        {
            Assert.assertEquals( "error: " + i,zip[ i ], csv[ i ] );
        }
    }

    @Test
    public void test_GivenImporter_ThenReadNext5RowsFromCSV_ShouldBeOk() throws IOException
    {
        final CSVImporter< Bean > importer = CSVBuilder
                .newImporter( Bean.class )
                .withBeanFactory( Bean::new )
                .ignoringErrors( false )
                .withBufferSize( 128 )
                .build(
                        MappingBean.mapping( COLUMN_1, c -> c.substring( PREFIX.length() ) ),
                        MappingBean.mapping( COLUMN_2, c -> c.substring( 0, c.length() - SUFFIX.length() ) ),
                        MappingBean.mapping( COLUMN_4, content ->
                                Stream.of( content.split( ";" ) ).map( Integer::valueOf ).collect( Collectors.toList() ) ),
                        MappingBean.mapping( COLUMN_5, content -> {
                            final String[] pieces = content.split( ";" );
                            return new InnerBean( pieces[ 0 ], Integer.valueOf( pieces[ 1 ] ) );
                        } )
                );

        final ByteArrayInputStream bais = new ByteArrayInputStream( CSV.getBytes( StandardCharsets.UTF_8 ) );
        try( final CSVImporter< Bean >.CSVReader reader = importer.fromFile( bais ) )
        {
            final List< Bean > beans = reader.readNext( 5 );
            int i = 0;
            for( Bean bean : beans )
            {
                Assert.assertEquals( new Bean( i++ ), bean );
            }
        }
    }

    @Test
    public void test_BuilderWithCustomCSVAutoMapper()
    {
        Assert.assertNotNull( CSVBuilder.newExporter( Bean.class, new CSVBaseAutoMapper() ) );
        Assert.assertNotNull( CSVBuilder.newImporter( Bean.class, new CSVBaseAutoMapper() ) );
    }

    @CSVBean
    private static class Bean
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
        public boolean equals( Object o )
        {
            if( this == o )
            {
                return true;
            }
            if( o == null || getClass() != o.getClass() )
            {
                return false;
            }
            Bean bean = (Bean)o;
            final boolean column4Equals = column4.containsAll( bean.column4 );
            return Objects.equals( column1, bean.column1 ) &&
                    Objects.equals( column3, bean.column3 ) &&
                    column4Equals &&
                    Objects.equals( column2, bean.column2 ) &&
                    Objects.equals( column5, bean.column5 );
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
        public boolean equals( Object o )
        {
            if( this == o )
            {
                return true;
            }
            if( o == null || getClass() != o.getClass() )
            {
                return false;
            }
            InnerBean innerBean = (InnerBean)o;
            return Objects.equals( stringField, innerBean.stringField ) &&
                    Objects.equals( integerField, innerBean.integerField );
        }
    }
}

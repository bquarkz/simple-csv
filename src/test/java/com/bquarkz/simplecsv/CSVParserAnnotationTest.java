package com.bquarkz.simplecsv;

import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

public class CSVParserAnnotationTest
{
    @Test
    public void test_GiveParserFromBean_ThenGetHeaders_ShouldKeepColumnOrderAndHasColumnNames()
    {
        final CSVParserAnnotation< Bean > parser = new CSVParserAnnotation<>( Bean.class );
        final String[] headers = parser.getCSVHeaders();
        Assert.assertNotNull( headers );
        Assert.assertEquals( 2, headers.length ); // manually named
        Assert.assertEquals( "C1", headers[ 0 ] ); // manually named
        Assert.assertEquals( "column2", headers[ 1 ] ); // used the field name
    }

    @Test
    public void test_GiveParserFromBean_ThenGetParseDetailsWithDefaultConfig_ShouldBeOk()
    {
        final CSVParserAnnotation< Bean > parser = new CSVParserAnnotation<>( Bean.class );
        final CSVParserDetails details = parser.getParserDetails();
        Assert.assertNotNull( details );
        Assert.assertFalse( details.shouldSkipHeader() );
        Assert.assertFalse( details.shouldVerifyHeader() );
        Assert.assertTrue( details.shouldWriteHeader() );
    }

    @Test
    public void test_GiveParserFromBean_ThenGetParseDetailsWithCustomConfig_ShouldBeOk()
    {
        final CSVParserAnnotation< BeanCustomConfig > parser = new CSVParserAnnotation<>( BeanCustomConfig.class );
        final CSVParserDetails details = parser.getParserDetails();
        Assert.assertNotNull( details );
        Assert.assertTrue( details.shouldSkipHeader() );
        Assert.assertTrue( details.shouldVerifyHeader() );
        Assert.assertFalse( details.shouldWriteHeader() );
    }

    @Test( expected = ExceptionCSVBeanConfiguration.class )
    public void test_GiveParserFromBeanNoColumns_ShouldThrowAndException()
    {
        new CSVParserAnnotation<>( BeanNoColumns.class );
    }

    @Test( expected = ExceptionCSVBeanConfiguration.class )
    public void test_GiveParserFromBeanWithoutCSVBeanAnnotation_ShouldThrowAndException()
    {
        new CSVParserAnnotation<>( String.class );
    }

    @Test
    public void test_GivenParser_ThenParseBeanToCSV_ShouldBeOk()
    {
        final CSVParserAnnotation< Bean > parser = new CSVParserAnnotation<>( Bean.class );
        final String csv = parser.toCSV( new Bean( "aaa", "bbb" ), new CSVDelimiters() );
        Assert.assertNotNull( csv );
        Assert.assertEquals( "\"aaa\";\"bbb\"", csv );
    }

    @Test
    public void test_GivenParser_ThenParseCSVToBean_ShouldBeOk()
    {
        final CSVParserAnnotation< Bean > parser = new CSVParserAnnotation<>( Bean.class );
        final Bean bean = parser.toBean( "\"aaa\";\"bbb\"", new CSVDelimiters(), Bean::new );
        Assert.assertNotNull( bean );
        Assert.assertEquals( new Bean( "aaa", "bbb" ), bean );
    }

    @Test
    public void test_GivenParserWithCompositeBean_ThenParseBeanToCSV_ShouldBeOk()
    {
        final CSVParserAnnotation< CompositeBean > parser = new CSVParserAnnotation<>( CompositeBean.class );
        MappingCSV[] mappings = { MappingCSV.mapping( "C2", o -> ( (InnerBean)o ).getNumber().toString() ) };
        final String csv = parser.toCSV( new CompositeBean( "aaa", new InnerBean( 123 ) ), new CSVDelimiters(), mappings );
        Assert.assertNotNull( csv );
        Assert.assertEquals( "\"aaa\";\"123\"", csv );
    }

    @Test
    public void test_GivenParserWithCompositeBean_ThenParseCSVToBean_ShouldBeOk()
    {
        final CSVParserAnnotation< CompositeBean > parser = new CSVParserAnnotation<>( CompositeBean.class );
        MappingBean[] mappings = { MappingBean.mapping( "C2", s -> new InnerBean( Integer.valueOf( s ) ) ) };
        final CompositeBean bean = parser.toBean( "\"aaa\";\"123\"", new CSVDelimiters(), CompositeBean::new, mappings );
        Assert.assertNotNull( bean );
        Assert.assertEquals( new CompositeBean( "aaa", new InnerBean( 123 ) ), bean );
    }

    @CSVBean
    private static class Bean
    {
        @CSVColumn( name = "C1", column = 1 )
        private String column1;

        @CSVColumn( column = 2 )
        private String column2;

        private String fieldWithoutAnnotationThatShouldNotBeIncluded;

        public Bean()
        {
        }

        public Bean( String column1, String column2 )
        {
            this.column1 = column1;
            this.column2 = column2;
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
            return Objects.equals( column1, bean.column1 ) &&
                    Objects.equals( column2, bean.column2 );
        }
    }

    @CSVBean( shouldSkipHeader = true, shouldVerifyHeader = true, shouldWriteHeader = false )
    private class BeanCustomConfig
    {
        @CSVColumn( name = "C1", column = 1 )
        private String column1;
    }

    @CSVBean
    private class BeanNoColumns
    {
    }

    @CSVBean
    private static class CompositeBean
    {
        @CSVColumn( name = "C1", column = 1 )
        private String column1;

        @CSVColumn( name = "C2", column = 2 )
        private InnerBean column2;

        public CompositeBean()
        {
            this.column2 = new InnerBean( 0 );
        }

        public CompositeBean( String column1, InnerBean column2 )
        {
            this.column1 = column1;
            this.column2 = column2;
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
            CompositeBean that = (CompositeBean)o;
            return Objects.equals( column1, that.column1 ) &&
                    Objects.equals( column2, that.column2 );
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( column1, column2 );
        }
    }

    private static class InnerBean
    {
        private Integer number;

        public InnerBean()
        {
        }

        public InnerBean( Integer number )
        {
            this.number = number;
        }

        public Integer getNumber()
        {
            return number;
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
            return Objects.equals( number, innerBean.number );
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( number );
        }
    }

}

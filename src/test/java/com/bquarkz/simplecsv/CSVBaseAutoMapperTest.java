package com.bquarkz.simplecsv;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Objects;

import static com.bquarkz.simplecsv.CSVUtils.desembrace;

public class CSVBaseAutoMapperTest
{
    @Test
    public void test_GiveIntegerContent_ThenAutoMapAsAInteger_ShouldReturnAInteger()
    {
        final CSVAutoMapper autoMapper = new CSVBaseAutoMapper();
        final Object o = autoMapper.map( Integer.class, "'", "'123'" );
        Assert.assertNotNull( o );
        Assert.assertTrue( o instanceof Integer );
        Assert.assertEquals( 123, o );
    }

    @Test
    public void test_GiveLongContent_ThenAutoMapAsALong_ShouldReturnALong()
    {
        final CSVAutoMapper autoMapper = new CSVBaseAutoMapper();
        final Object o = autoMapper.map( Long.class, "'", "'123'" );
        Assert.assertNotNull( o );
        Assert.assertTrue( o instanceof Long );
        Assert.assertEquals( 123L, o );
    }

    @Test
    public void test_GiveDoubleContent_ThenAutoMapAsADouble_ShouldReturnADouble()
    {
        final CSVAutoMapper autoMapper = new CSVBaseAutoMapper();
        final Object o = autoMapper.map( Double.class, "'", "'123.321'" );
        Assert.assertNotNull( o );
        Assert.assertTrue( o instanceof Double );
        Assert.assertEquals( 123.321, o );
    }

    @Test
    public void test_GiveFloatContent_ThenAutoMapAsAFloat_ShouldReturnAFloat()
    {
        final CSVAutoMapper autoMapper = new CSVBaseAutoMapper();
        final Object o = autoMapper.map( Float.class, "'", "'123.321'" );
        Assert.assertNotNull( o );
        Assert.assertTrue( o instanceof Float );
        Assert.assertEquals( 123.321f, o );
    }

    @Test
    public void test_GiveStringContent_ThenAutoMapAsAString_ShouldReturnAString()
    {
        final CSVAutoMapper autoMapper = new CSVBaseAutoMapper();
        final Object o = autoMapper.map( String.class, "'", "'UPI UPI'" );
        Assert.assertNotNull( o );
        Assert.assertTrue( o instanceof String );
        Assert.assertEquals( "UPI UPI", o );
    }

    @Test
    public void test_GiveBigDecimalContent_ThenAutoMapAsABigDecimal_ShouldReturnABigDecimal()
    {
        final CSVAutoMapper autoMapper = new CSVBaseAutoMapper();
        final Object o = autoMapper.map( BigDecimal.class, "'", "'1.2345E+16'" );
        Assert.assertNotNull( o );
        Assert.assertTrue( o instanceof BigDecimal );
        Assert.assertEquals( new BigDecimal( "1.2345E+16" ), o );
    }

    @Test
    public void test_GiveSomeContent_ThenAutoMap_WhenItIsNotMapped_ShouldReturnAStringByDefault()
    {
        final CSVAutoMapper autoMapper = new CSVBaseAutoMapper();
        final Object o = autoMapper.map( Bean.class, "'", "'someStuff'" );
        Assert.assertNotNull( o );
        Assert.assertTrue( o instanceof String );
        Assert.assertEquals( "someStuff", o );
    }

    @Test
    public void test_GiveBooleanContent_ThenAutoMapAsABoolean_ShouldReturnABoolean()
    {
        final CSVAutoMapper autoMapper = new CSVBaseAutoMapper();
        final Object o = autoMapper.map( Boolean.class, "'", "'true'" );
        Assert.assertNotNull( o );
        Assert.assertTrue( o instanceof Boolean );
        Assert.assertTrue( (Boolean)o );
    }


    @Test
    public void test_ExtendsCSVBaseAutoMapper()
    {
        final CSVAutoMapper autoMapper = new TestMapper();
        final Object o = autoMapper.map( Bean.class, "'", "'someStuff'" );
        Assert.assertNotNull( o );
        Assert.assertTrue( o instanceof Bean );
        Assert.assertEquals( new Bean( "someStuff" ), o );
    }

    private static class Bean
    {
        private String someStuff;

        public Bean( String someStuff )
        {
            this.someStuff = someStuff;
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
            return Objects.equals( someStuff, bean.someStuff );
        }
    }

    private static class TestMapper extends CSVBaseAutoMapper
    {
        @Override
        public Object map( Class< ? > type, String delimitersContent, String content )
        {
            final String extract = desembrace( delimitersContent, content );
            if( type == Bean.class )
            {
                return new Bean( extract );
            }
            else
            {
                return super.map( type, delimitersContent, content );
            }
        }
    }
}

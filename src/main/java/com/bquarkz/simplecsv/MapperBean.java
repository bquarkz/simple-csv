package com.bquarkz.simplecsv;

import java.math.BigDecimal;

@FunctionalInterface
public interface MapperBean
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Static fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static String mapString( String content )
    {
        return content;
    }

    static Integer mapInteger( String content )
    {
        return Integer.valueOf( content );
    }

    static Double mapDouble( String content )
    {
        return Double.valueOf( content );
    }

    static Long mapLong( String content )
    {
        return Long.valueOf( content );
    }

    static Float mapFloat( String content )
    {
        return Float.valueOf( content );
    }

    static Boolean mapBoolean( String content )
    {
        return Boolean.valueOf( content );
    }

    static BigDecimal mapBigDecimal( String content )
    {
        return new BigDecimal( content );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Static Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Default Methods
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Contracts
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    Object map( String csv ) throws Exception;
}

package com.bquarkz.simplecsv;

import static com.bquarkz.simplecsv.CSVUtils.desembrace;

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

    static Object autoMapper(
            final Class< ? > type,
            final String delimitersContent,
            final String content )
    {
        final String extract = desembrace( delimitersContent, content );
        if( type == Integer.class )
        {
            return mapInteger( extract );
        }
        else if( type == Double.class )
        {
            return mapDouble( extract );
        }
        else if( type == Long.class )
        {
            return mapLong( extract );
        }
        else if( type == Float.class )
        {
            return mapFloat( extract );
        }
        else if( type == String.class )
        {
            return extract;
        }
        else
        {
            return extract; // by default return content as a String
        }
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
